package s.com.eoi.util.eoiAkka

import akka.actor.{Actor, ReceiveTimeout, Stash}
import akka.cluster.Cluster
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import s.com.eoi.common.ImperativeRequestContext

import scala.concurrent.duration._

//by NathanJiang
class ClusterMonitor extends Actor with Stash {
  val cluster = Cluster(context.system)

  override def receive: Receive = {
    case (ctx: ImperativeRequestContext, directive: String) if (directive == "jsonList" || directive == "jsonTree") =>
      val members = cluster.state.members.toList //包含自身节点
      members.foreach { member =>
        context.actorSelection(s"${member.address}/user/akkaMonitorActor") ! directive // member.address akka.tcp://itoa@127.0.0.1:2551
      }
      context.become(healthLevel(ctx, members.length, Nil), discardOld = true) //利用FSM避免了局部变量的产生
      context.setReceiveTimeout(3 seconds)
  }

  def healthLevel(ctx: ImperativeRequestContext, memberNum: Int, jsonResult: List[String]): Receive = { //集群个数 用于接收到的信息个数
    case jsonStr: String =>
      (memberNum - 1) match {
        case 0 =>
          context.setReceiveTimeout(Duration.Inf) //完成任务 取消超时设置
          ctx.complete(HttpResponse(entity = HttpEntity(contentType = ContentTypes.`application/json`, string = s"[${(jsonStr :: jsonResult).mkString(",")}]")))
          context.unbecome()
          unstashAll() //完成该请求，处理其他请求
        case _ =>
          context.become(healthLevel(ctx, memberNum - 1, jsonStr :: jsonResult), discardOld = true)
      }
    case ReceiveTimeout =>
      context.setReceiveTimeout(Duration.Inf) //完成任务 取消超时设置
      ctx.complete(HttpResponse(entity = HttpEntity(contentType = ContentTypes.`application/json`, string = s"[${(jsonResult).mkString(",")}]")))
      context.become(receive, discardOld = true)
      unstashAll() //完成该请求，处理其他请求
    case _ =>
      stash() //处理某个请求中，但是收到了其他的请求
  }
}