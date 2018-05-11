package s.com.eoi.routes

import akka.http.scaladsl.server.Directives._
import s.com.eoi.service.cdb.{HandoverlistReq, HandoverlistService}
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import s.com.eoi.common._

import s.com.eoi.util.JsonUtil._


class HandoverRoute {


  val service = new HandoverlistService
  val route =
    pathPrefix("cdb") {
      path("handover") {
        get {
          parameter("from".as[Int] ?, "size".as[Int] ?) { (from, size) =>
            complete(service.list(from, size))
          }
        } ~
          post {
            entity(as[HandoverlistReq]) { req =>
              complete(service.add(req))
            }
          }
      } ~
        path("handover" / IntNumber) { handoverId =>
          delete {
            complete(service.delete(handoverId))
          } ~
            put {
              entity(as[HandoverlistReq]) { req =>
                complete(service.update(handoverId, req))
              }
            } ~
            get {
              complete(service.getHandover(handoverId))
            }
        }~
      path("user"){
        get{
          complete(service.testuser)
        }
      }
    }
}
