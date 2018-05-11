package s.com.eoi.util

import java.lang.reflect.{ParameterizedType, Type}

import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import mesosphere.jackson.CaseClassModule
import scala.reflect.ClassTag

/**
  * Created by jacky on 16/10/18.
  */
object JsonUtil extends JacksonSupport {

  val defaultObjectMapper = new ObjectMapper() with ScalaObjectMapper
  val module = new DefaultScalaModule with CaseClassModule
  defaultObjectMapper.registerModule(module)
  defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  //  defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
  defaultObjectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
  defaultObjectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
  //  defaultObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, true) //格式化输出

  def toJson(value: Any): String = {
    defaultObjectMapper.writeValueAsString(value)
  }

  def fromJson[T: Manifest](json: String): T = {
    defaultObjectMapper.readValue[T](json, typeReference[T])
  }

  def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }

  private[this] def typeFromManifest(m: Manifest[_]): Type =
    if (m.typeArguments.isEmpty) m.runtimeClass
    else new ParameterizedType {
      def getRawType = m.runtimeClass

      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray

      def getOwnerType = null
    }
}


trait JacksonSupport {

  import JsonUtil._

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset) => data.decodeString(charset.nioCharset.name)
      }

  /**
    * HTTP entity => `A`
    */
  implicit def jacksonUnmarshaller[A](
                                       implicit ct: ClassTag[A],
                                       objectMapper: ScalaObjectMapper = defaultObjectMapper,
                                       m: Manifest[A]
                                     ): FromEntityUnmarshaller[A] = {
    jsonStringUnmarshaller.map(
      data => objectMapper.readValue[A](data)
    )
  }

  /**
    * `A` => HTTP entity
    */
  implicit def jacksonToEntityMarshaller[Object](
                                                  implicit objectMapper: ObjectMapper = defaultObjectMapper
                                                ): ToEntityMarshaller[Object] = {
    Jackson.marshaller[Object](objectMapper)
  }
}