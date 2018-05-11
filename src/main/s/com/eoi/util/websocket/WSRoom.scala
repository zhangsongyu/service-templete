package s.com.eoi.util.websocket

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import s.com.eoi.util.eoiAkka.WrapperActor
import s.com.eoi.util.websocket.Protocol.{JoinRoom, OutGoingMsg, ToMultiWsActorMsg, ToSingleWsActorMsg}

class WSRoom(roomId: String) extends WrapperActor {
  var onLineUser = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case JoinRoom(tokenId) =>
      val from = sender()
      onLineUser += (tokenId -> from)
      context.watch(from)
    case ToSingleWsActorMsg(tokenId, msg) =>
      onLineUser(tokenId) ! OutGoingMsg(msg)
    case ToMultiWsActorMsg(list, msg) =>
      list.map(i => onLineUser(i)).foreach(a => a ! OutGoingMsg(msg))
    case Terminated(actor) =>
      context.unwatch(actor)
      onLineUser = onLineUser.filter(_._2 != actor)
      log.info(s"actor:$actor exits out")
  }
}

object WSRoom {

  import s.com.eoi.util.websocket.Protocol.RoomId

  private val rooms = new ConcurrentHashMap[RoomId, ActorRef]()
  private val defaultRoom: RoomId = "defaultRoom"

  def apply(roomId: RoomId = defaultRoom): ActorRef = rooms.get(roomId)

  def addRoom(roomId: RoomId = defaultRoom)(implicit system: ActorSystem): Unit = rooms.containsKey(roomId) match {
    case false =>
      val roomActor = system.actorOf(Props(classOf[WSRoom], roomId), roomId)
      rooms.put(roomId, roomActor)
    case _ =>
  }
}