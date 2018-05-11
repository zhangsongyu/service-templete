package s.com.eoi.util.websocket

import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.Message

object Protocol {
  type RoomId = String

  case class JoinRoom(tokenId: String) //wsActor加入房间

  case class Connected(room: ActorRef)

  trait ReturnMsg //一个room里的wsActor给同房间里的其他的actor发送消息

  case class ToSingleWsActorMsg(tokenId: String, msg: Message) extends ReturnMsg

  case class ToMultiWsActorMsg(list: List[String], msg: Message) extends ReturnMsg

  case class InComingMsg(msg: Message) //room发给wsActor TextMessage BinaryMessage

  case class OutGoingMsg(msg: Message) //wsActor发给room TextMessage BinaryMessage
}