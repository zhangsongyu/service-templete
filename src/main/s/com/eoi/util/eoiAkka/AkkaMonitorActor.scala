package s.com.eoi.util.eoiAkka

import akka.actor.{Actor, ActorPath}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import s.com.eoi.common.{ImperativeRequestContext, ServiceCommon}
import s.com.eoi.util.JsonUtil
import s.com.eoi.util.Utils._
import s.com.eoi.util.eoiCollection.{Branch, EmptyTree, Leaf, Tree}

/**
  * Created by jianghang on 2017/12/28.
  */
class AkkaMonitorActor extends Actor with ServiceCommon {
  //核心思想是每个actor向monitor发送信息，monitor不会去向actor发送任何信息
  val akkaAddressExtensionImpl = AkkaAddressExtension(context.system)
  val abbsolteAddress = akkaAddressExtensionImpl.address //形如akka.tcp://itoa@127.0.0.1:2551
  var tree: Tree[ActorPath] = Leaf(self.path.root)
  var actorState = Map.empty[ActorPath, FetchAkkaMonitorStateResult]
  implicit val ec = context.system.dispatcher

  override def preStart(): Unit = ()

  def genTreeList(): List[Map[String, Any]] = {
    val tempList = tree.toList.map { path =>
      actorState.get(path) match {
        case Some(stateRes) =>
          Map(
            "name" -> stateRes.path.toStringWithAddress(abbsolteAddress),
            "className" -> stateRes.className,
            "path" -> stateRes.path.toStringWithAddress(abbsolteAddress),
            "dealMsgNumber" -> stateRes.dealMsgNumber.toLongExact,
            "lastRunTime" -> stateRes.lastRunTime,
            "lastFinishTime" -> stateRes.lastFinishTime,
            "avgDealMsgDuration" -> stateRes.avgDealMsgDuration.doubleValue(),
            "constructTime" -> stateRes.constructTime,
            "msgOnRestart" -> stateRes.msgOnRestart.orNull,
            "exceptionOnRestart" -> stateRes.exceptionOnRestart.orNull,
            "parent" -> stateRes.path.parent.toStringWithAddress(abbsolteAddress)
          )
        case None => //找不到该actor节点的状态
          Map(
            "name" -> path.toStringWithAddress(abbsolteAddress),
            "parent" -> (if (path == tree.value) null else path.parent.toStringWithAddress(abbsolteAddress))
          )
      }
    }
    //tree.value是得到tree的root的值，如果一个actor的path与tree的根节点相等，那么该节点就没有父节点，它本身就是root
    tempList
  }

  def genTreeMap(): Map[String, Any] = {
    def loop(tree: Tree[ActorPath]): Map[String, Any] = {
      tree match {
        case EmptyTree => Map.empty
        case Leaf(v) =>
          actorState.get(v) match {
            case Some(targetState) =>
              Map(
                "name" -> targetState.path.toStringWithAddress(abbsolteAddress),
                "className" -> targetState.className,
                "path" -> targetState.path.toStringWithAddress(abbsolteAddress),
                "dealMsgNumber" -> targetState.dealMsgNumber.toLongExact,
                "lastRunTime" -> targetState.lastRunTime,
                "lastFinishTime" -> targetState.lastFinishTime,
                "avgDealMsgDuration" -> targetState.avgDealMsgDuration.doubleValue(),
                "constructTime" -> targetState.constructTime,
                "msgOnRestart" -> targetState.msgOnRestart.orNull,
                "exceptionOnRestart" -> targetState.exceptionOnRestart.orNull,
                "children" -> Nil)
            case None =>
              Map(
                "name" -> v.toStringWithAddress(abbsolteAddress),
                "children" -> Nil)
          }
        case Branch(v, bs) =>
          actorState.get(v) match {
            case Some(targetState) =>
              Map(
                "name" -> targetState.path.toStringWithAddress(abbsolteAddress),
                "className" -> targetState.className,
                "path" -> targetState.path.toStringWithAddress(abbsolteAddress),
                "dealMsgNumber" -> targetState.dealMsgNumber.toLongExact,
                "lastRunTime" -> targetState.lastRunTime,
                "lastFinishTime" -> targetState.lastFinishTime,
                "avgDealMsgDuration" -> targetState.avgDealMsgDuration.doubleValue(),
                "constructTime" -> targetState.constructTime,
                "msgOnRestart" -> targetState.msgOnRestart.orNull,
                "exceptionOnRestart" -> targetState.exceptionOnRestart.orNull,
                "children" ->
                  bs.map(t => loop(t)))
            case None =>
              Map(
                "name" -> v.toStringWithAddress(abbsolteAddress),
                "children" -> bs.map(t => loop(t)))
          }
      }
    }

    loop(tree)
  }

  override def receive: Receive = {
    case FetchActorStarted(actor) => //监控每一个actor的创建
      tree = tree |>
        (_.insertChild(_ == actor.path.parent.parent)(actor.path.parent)) |>
        (_.insertChild(_ == actor.path.parent)(actor.path))
    case FetchActorReStarted(actor) => //某actor重启了 uid已经变更了，但是path不变

    case FetchActorTerminated(actor) => //监控每一个actor的销毁
      tree = tree.deleteSubTree(_ == actor.path)
    case stateRes: FetchAkkaMonitorStateResult =>
      actorState += (stateRes.path -> stateRes)
    case "JSONTREE" | "jsonTree" =>
      sender() ! JsonUtil.toJson(genTreeMap())
    case "JSONLIST" | "jsonList" =>
      sender() ! JsonUtil.toJson(genTreeList())
    case (ctx: ImperativeRequestContext, "jsonTree") =>
      ctx.complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, JsonUtil.toJson(genTreeMap()))))
    case (ctx: ImperativeRequestContext, "jsonList") =>
      val jsonArray = genTreeList().filter(obj => obj.get("className").isDefined) //去除虚拟节点
      ctx.complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, JsonUtil.toJson(jsonArray))))
  }
}