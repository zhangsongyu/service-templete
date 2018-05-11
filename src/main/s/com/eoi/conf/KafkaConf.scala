package s.com.eoi.conf

class KafkaConf extends ClientConf[KafkaConf] {

  val configs = getConfigs("kafka")

  def getBootstrapServers(): Option[String] = get("kafka.bootstrap.servers")

  def getReplications(): Option[Int] = get("replications") //副本数

  def getZookeeperConnect() = get("zookeeper.connect").getOrElse("localhost:2181")

  def getZookeeperFlag() = get("zookeeper.flag").getOrElse(1)

  def getPartitions() = get("partitions").getOrElse(1)

  def poolTime = get("polltimeout").getOrElse(5000)

  def maxMessageBytes = get("maxMessageBytes").getOrElse(10000120)
}

object KafkaConf {
  def apply() = new KafkaConf()
}