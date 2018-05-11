package s.com.eoi.util

import java.io.{File, InputStream}
import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.ContentDispositionTypes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import s.com.eoi.util.ExecutorService.configServiceExecutionContext
import s.com.eoi.util.Utils._

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by jacky on 16/10/24.
  */
object FileUtil {
  def exists(fileName: String): Boolean = {
    new File(fileName).exists()
  }

  def parsingDownloadResponse(responseFuture: Future[(Source[ByteString, Any], String)]): Future[HttpResponse] = {
    //作者:Nathan蒋航
    //这里实现了简单的小体积文件下载功能
    //用法比如: akka-http中 complete(parsingDownloadReponse(Future((StatusCodes.OK,Source.single(ByteString(generateExcelData())),"workbook.xls"))))
    //Array[Byte] => ByteString => Source[ByteString,Any] 转换，比如下载excel文件，那么需要就是该文件的ByteArray
    val response = responseFuture.map {
      case (source, fileName) => {
        val entity = HttpEntity(ContentTypes.`application/octet-stream`, source)
        val disposition = headers.`Content-Disposition`.apply(ContentDispositionTypes.inline, Map("filename" -> fileName, "name" -> "fieldName"))
        HttpResponse(StatusCodes.OK, entity = entity).addHeader(disposition)
      }
    }
    response
  }

  //文件上传服务  baseDir参数:文件应该存放在哪里 urlPath是上传action的url，比如默认的为ip:port/upload
  //返回Route，用~与其他路由串联，最后用Http().hanle()使用它
  def uploadFile(baseDir: String, urlPath: String = "upload", fieldName: String = "upload")(implicit system: ActorSystem, ma: ActorMaterializer) = {
    val router = path(urlPath) {
      post {
        fileUpload(fieldName) {
          //fileUpload接收form表单中的字段名 <input type="file" name="$fieldName">
          case (fileInfo, fileStream) =>
            val basepath = baseDir
            val sink = FileIO.toPath(Paths.get(basepath) resolve fileInfo.fileName)
            val writeResult = fileStream.runWith(sink)
            onSuccess(writeResult) { result =>
              result.status match {
                //利用MediaTypes中封装了更多的mime类型，比如 MediaTypes.`application/excel`等等
                case Success(_) =>
                  complete(HttpEntity(ContentType(MediaTypes.`application/json`.withParams(Map("charset" -> "utf-8"))), s"{'message':'ok,written ${result.count} bytes'}".getBytes("UTF-8")))
                case Failure(_) =>
                  complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"{'message':'error'}"))
              }
            }
        }
      }
    }
    router
  }


  //f函数用于用户验证 比如防盗链等等 由调用者自行实现 :)
  //返回route 由用户自行组合
  //测试在FileUtils$Test中
  //请求的路由比如 /itoa/file?fileName=111.txt
  def fileDownload(baseDir: String, prefix: String = "itoa", infix: String = "file")(f: HttpRequest => Boolean) = {
    val route =
      path(prefix / infix) {
        get {
          parameter("fileName".as[String]) {
            fileName: String =>
              extractRequest { request =>
                f(request) match {
                  case true =>
                    exists(baseDir + "/" + fileName) match {
                      case true => //文件存在
                        val disposition = headers.`Content-Disposition`(ContentDispositionTypes.inline, Map("filename" -> fileName, "name" -> "fieldName"))
                        complete(HttpResponse(status = StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/octet-stream`, Source.single(fileToByteString(baseDir + "/" + fileName)))).addHeader(disposition))
                      case false => //文件不存在
                        complete(HttpResponse(status = StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`application/json`,"""{"result":"error","message":"file not exists!!!"}""")))
                    }
                  case false =>
                    complete(HttpEntity(ContentTypes.`application/json`,"""{"result":"error"}"""))
                }
              }
          }
        }
      }
    route
  }

  // File=>ByteString的转换
  def fileToByteString(path: String) = {
    ByteString(Files.readAllBytes(Paths.get(path)))
  }

  //获取文件的MIME类型，其实是通过拓展名来识别MIME类型(实际上，拓展名和MIME类型没有任何关系)
  def mimeType(fileName: String) = {
    val ExtensionRegex ="""^.*\.(.*)$""".r
    fileName match {
      case ExtensionRegex(extension) => MediaTypes.forExtension(extension)
      case _ => MediaTypes.`application/octet-stream`
    }
  }

  /***
    * Source => InputStream
    * @param byteSource
    * @return
    */
  def convertSourceToInputStream(byteSource: Source[ByteString, Any])(implicit system: ActorSystem, mat: ActorMaterializer) = {
    import akka.stream.scaladsl.StreamConverters
    byteSource.runWith(
      StreamConverters.asInputStream(3 s)
    )
  }

  /***
    * InputStream => String
    * @param input
    * @param encoding
    * @return
    */
  def convertInputStreamToString(input: InputStream, encoding: String = "UTF-8") = {
    import org.apache.commons.io.IOUtils
    var result = ""
    try {
      result = IOUtils.toString(input, encoding)
    } catch {
      case e: Exception =>
        input.close()
        e.printStackTrace()
    } finally {
      if (input != null) input.close()
    }
    result
  }

  /***
    * Source => String
    * @param byteSource
    * @param system
    * @param mat
    * @return
    */
  def convertSourceToString(byteSource: Source[ByteString, Any])(implicit system: ActorSystem, mat: ActorMaterializer) = {
    convertSourceToInputStream(byteSource) |> (convertInputStreamToString(_))
  }
}
