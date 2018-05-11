package s.com.eoi.conf

import java.util.{ArrayList => JArrayList}

import scala.collection.JavaConverters._

class AgentConf extends ClientConf[AgentConf] {

  val configs = getConfigs("agent")

  val hosts = get[JArrayList[String]]("hosts")

  val port = get("port")
  val heartBeatTimeOut = get[Int]("heartBeatTimeOut").getOrElse(2)
  val metadata: Map[String, String] = get[JArrayList[String]]("metadata").get.asScala
    .map(_.split("="))
    .filter(_.length == 2)
    .map(arr => (arr(0) -> arr(1))).toMap
}

object AgentConf {
  def apply() = new AgentConf()
}
