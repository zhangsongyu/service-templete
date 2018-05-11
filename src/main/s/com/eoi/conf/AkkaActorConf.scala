package s.com.eoi.conf

/**
  * Created by zhi on 2017/8/15.
  */
class AkkaActorConf extends ClientConf[AkkaActorConf] {

  val configs = getConfigs("akkaCluster")

  def clusterNodes() =s""" "${get[String]("clusterNodes").get.split(",").mkString("""","""")}" """

  def hostname() = get[String]("hostname").get

  def port() = get[Int]("port").get

}

object AkkaActorConf {
  def apply() =  new AkkaActorConf()
}