package s.com.eoi.util.websocket

import akka.actor.ActorRef
import s.com.eoi.util.eoiAkka.WrapperActor
import s.com.eoi.util.websocket.Protocol._

class WSActor(tokenId: String, room: ActorRef, exe: PartialFunction[InComingMsg, ReturnMsg]) extends WrapperActor { //tokenId鉴别身份，可以是EOI_AUTH
  override def receive: Receive = {
    case Connected(outGoing: ActorRef) =>
      room ! JoinRoom(tokenId)
      context.become(connected(outGoing))
  }

  def connected(outGoing: ActorRef): Receive = {
    case i: InComingMsg =>
      room ! exe(i) //room起到总线的作用
    case OutGoingMsg(msg) =>
      outGoing ! msg //发送给浏览器
  }
}