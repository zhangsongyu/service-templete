package s.com.eoi.util.eoiAkka

import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, NotInfluenceReceiveTimeout}
import s.com.eoi.Service

/**
  * Created by jianghang on 18/01/03.
  */
trait WrapperActor extends Actor with ActorLogging {

  /**
    * 已经处理的消息数量(处理失败的消息数量)
    */
  private[this] var dealMsgNumber: BigDecimal = 0.0 //重启过后 清零
  /**
    * receive函数上次开始运行时刻与结束时刻
    */
  private[this] var lastRunTime: Long = -1L //重启过后 清零
  private[this] var lastFinishTime: Long = -1L //重启过后 清零
  /**
    * 每个消息的平均处理时间
    */
  private[this] var avgDealMsgDuration: BigDecimal = 0.0 //重启过后 清零
  /**
    * actor新建构造的时刻
    */
  private[this] var constructTime: Long = -1L //新建时间或者重启的时间

  private[this] var msgOnRestart: Option[Any] = None //哪里哪条消息引起的异常

  private[this] var exceptionOnRestart: Option[Throwable] = None //异常是什么

  //  def receive: Receive

  def wrapPreRestart(reason: Throwable, message: Option[Any]): Unit = {}

  @throws(classOf[Exception])
  def wrapPreStart(): Unit = ()

  @throws(classOf[Exception])
  def wrapPostStop(): Unit = ()

  /**
    * 请使用wrapReceive
    */
  override def aroundReceive(receive: Receive, msg: Any): Unit = {
    lastRunTime = System.currentTimeMillis()
    super.aroundReceive(receive, msg) //这里真正的执行receive函数 become与unbecome
    lastFinishTime = System.currentTimeMillis()
    dealMsgNumber += 1 //很重要
    avgDealMsgDuration = (avgDealMsgDuration * (dealMsgNumber - 1) + lastFinishTime - lastRunTime) / dealMsgNumber
    //    val stateRes = FetchAkkaMonitorStateResult(self, self.path, getClass.getName, dealMsgNumber, lastRunTime, lastFinishTime, avgDealMsgDuration, constructTime, msgOnRestart, exceptionOnRestart)
    //    ITOAService.akkaMonitorActor ! stateRes
  }


  /**
    * 请使用wrapPreStart
    */
  @Deprecated
  override def preStart(): Unit = {
    super.preStart() //这很重要

    constructTime = System.currentTimeMillis()
//    ITOAService.akkaMonitorActor ! FetchActorStarted(self)

//    val stateRes = FetchAkkaMonitorStateResult(self, self.path, getClass.getName, dealMsgNumber, lastRunTime, lastFinishTime, avgDealMsgDuration, constructTime, msgOnRestart, exceptionOnRestart)
//    ITOAService.akkaMonitorActor ! stateRes //actor启动时，就需要发送一次自身的状态
    wrapPreStart()
  }

  /**
    * 请使用wrapPostStop
    */
  @Deprecated
  override def postStop(): Unit = {
    super.postStop() //这很重要

    //某一个actor挂掉了，给akkaMonitor发送信号 监督actor的死亡
//    ITOAService.akkaMonitorActor ! FetchActorTerminated(self)
    wrapPostStop()
  }

  @Deprecated
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)

    wrapPreRestart(reason, message)
    constructTime = System.currentTimeMillis()
    msgOnRestart = message
    exceptionOnRestart = Option(reason)
//    val stateRes = FetchAkkaMonitorStateResult(self, self.path, getClass.getName, dealMsgNumber, lastRunTime, lastFinishTime, avgDealMsgDuration, constructTime, msgOnRestart, exceptionOnRestart)
//    ITOAService.akkaMonitorActor ! stateRes //重启时，立即更新汇报状态
  }
}

object WrapperActor {
  type ReceiveFetchState = PartialFunction[FetchSate, Unit]
}

trait FetchSate extends NotInfluenceReceiveTimeout //actor监控消息不会影响到setReceiveTimeOut方法

case object FetchAkkaMonitorState extends FetchSate

case object FetchChildren extends FetchSate

case object FetchActorRef extends FetchSate

trait FetchResult extends NotInfluenceReceiveTimeout //actor监控消息不会影响到setReceiveTimeOut方法

case class FetchAkkaMonitorStateResult(actor: ActorRef, path: ActorPath, className: String, dealMsgNumber: BigDecimal, lastRunTime: Long, lastFinishTime: Long, avgDealMsgDuration: BigDecimal, constructTime: Long, msgOnRestart: Option[Any] = None, exceptionOnRestart: Option[Throwable] = None) extends FetchResult

case class FetchActorTerminated(actor: ActorRef) extends FetchResult

case class FetchActorStarted(actor: ActorRef) extends FetchResult

case class FetchActorReStarted(actor: ActorRef) extends FetchResult

case class FetchChildrenListResult(actor: ActorRef, childrenList: List[ActorRef]) extends FetchResult

case class FetchActorRefResult(actor: ActorRef) extends FetchResult