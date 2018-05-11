package s.com.eoi.util.websocket

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import s.com.eoi.util.websocket.Protocol.{Connected, InComingMsg, ReturnMsg, ToSingleWsActorMsg}

object WSUtil {
  def newUser(tokenId: String, exe: PartialFunction[InComingMsg, ReturnMsg])(implicit system: ActorSystem): Flow[Message, Message, _] = {
    WSRoom.addRoom() //目前就一个room
    val defaultRoom = WSRoom.apply()
    val userActor = system.actorOf(Props(clazz = classOf[WSActor], tokenId, defaultRoom, exe), tokenId)
    val inComingMessage: Sink[Message, _] = Flow[Message].map(msg => InComingMsg(msg)).to(Sink.actorRef[InComingMsg](userActor, PoisonPill))
    val outGoingMessage: Source[Message, _] = Source.actorRef[Message](1000, OverflowStrategy.dropTail).mapMaterializedValue { outActor =>
      userActor ! Connected(outActor)
    }
    Flow.fromSinkAndSource(inComingMessage, outGoingMessage)
  }

  def handleWS(tokenId: String, exe: PartialFunction[InComingMsg, ReturnMsg])(implicit system: ActorSystem): Route = {
    handleWebSocketMessages(newUser(tokenId, exe))
  }

  val exe: PartialFunction[InComingMsg, ReturnMsg] = {
    case InComingMsg(_msg) =>
      val msg = _msg.asTextMessage.getStrictText
      ToSingleWsActorMsg("1", TextMessage.apply(msg.reverse + "jianghang"))
  }
}