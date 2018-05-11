package s.com.eoi.conf

class RedisConf extends ClientConf[RedisConf] {

  val configs = getConfigs("redis")

  //  def clusterList(): Option[JArrayList[String]] = get[JArrayList[String]]("clusterList")
  def clusterList(): Option[List[String]] = Some(get[String]("clusterList").get.split(",").toList)

  def prefix(): String = get[String]("prefix").getOrElse("")
}

object RedisConf {
  def apply() = new RedisConf()
}
