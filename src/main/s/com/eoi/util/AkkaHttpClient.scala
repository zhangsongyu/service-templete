package s.com.eoi.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import s.com.eoi.util.JsonUtil._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}


trait AkkaHttpClient {
  implicit val ec: ExecutionContext

  def aPost(url: String, body: Map[String, Any])(implicit system: ActorSystem, mat: ActorMaterializer): Future[HttpResponse] = {
    akkaHttpRequest(HttpMethods.POST, uri = url, entity = HttpEntity(ContentTypes.`application/json`, toJson(body).replace('\u00A0', ' ')))
  }

  def aPut(url: String, body: Map[String, Any])(implicit system: ActorSystem, mat: ActorMaterializer): Future[HttpResponse] = {
    akkaHttpRequest(HttpMethods.PUT, uri = url, entity = HttpEntity(ContentTypes.`application/json`, toJson(body).replace('\u00A0', ' ')))
  }

  def aDelete(url: String)(implicit system: ActorSystem, mat: ActorMaterializer): Future[HttpResponse] = {
    akkaHttpRequest(HttpMethods.DELETE, uri = url)
  }

  def aGet(url: String)(implicit system: ActorSystem, mat: ActorMaterializer): Future[HttpResponse] = {
    akkaHttpRequest(HttpMethods.GET, uri = url)
  }

  private def akkaHttpRequest(method: HttpMethod = HttpMethods.GET,
                              uri: akka.http.scaladsl.model.Uri = Uri./,
                              headers: immutable.Seq[HttpHeader] = Nil,
                              entity: RequestEntity = HttpEntity.Empty,
                              protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`)(implicit system: ActorSystem, mat: ActorMaterializer): Future[HttpResponse] = {
    Http().singleRequest(HttpRequest(method, uri, headers, entity, protocol))
      .flatMap {
        res =>
          Future(HttpResponse(status = res.status, entity = res.entity))
      }
  }
}