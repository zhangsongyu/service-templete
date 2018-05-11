package s.com.eoi.util

import kafka.admin.TopicCommand
import kafka.admin.TopicCommand.TopicCommandOptions
import kafka.utils.ZkUtils
import org.apache.kafka.common.security.JaasUtils
import org.slf4j.LoggerFactory
import s.com.eoi.conf.{KafkaConf, KafkaTopicConf}

import scala.collection.JavaConverters._

object KafkaTopicUtils {

  val logger = LoggerFactory.getLogger(this.getClass)

  val topicConf = KafkaTopicConf()
  val kafkaConf = KafkaConf()

  val partitions = kafkaConf.getPartitions()
  val replicas = kafkaConf.getReplications().getOrElse(2)

  val maxMessageBytes = kafkaConf.maxMessageBytes

  val zkConnect = kafkaConf.getZookeeperConnect()
  val zkUtils = ZkUtils(zkConnect, 30000, 30000, JaasUtils.isZkSecurityEnabled())

  def init() = {

    val topics = getAllTopics
    KafkaTopicConf().configs.asScala.foreach {
      case (key, value) =>
        val topic = value.toString
        if (!topics.contains(topic)) {
          if (KafkaTopicConf.specialTopics.contains(topic)) { // 特殊topic, 创建时需特殊处理
            val args = s" --zookeeper $zkConnect --config max.message.bytes=$maxMessageBytes --config cleanup.policy=compact --config delete.retention.ms=43200000 --config retention.ms=3600000 --config segment.ms=3600000 --partitions $partitions --replication-factor $replicas --topic $topic --create".split(" ")
            createTopic(new TopicCommandOptions(args))
          } else {
            val args = s" --zookeeper $zkConnect --config max.message.bytes=$maxMessageBytes --partitions $partitions --replication-factor $replicas --topic $topic --create".split(" ")
            createTopic(new TopicCommandOptions(args))
          }
          logger.info(s"Topic '$topic' create success.")
        } else {
          logger.info(s"Topic '$topic' already exists.")
        }
    }
  }

  def getAllTopics() = {
    zkUtils.getAllTopics().sorted
  }

  def createTopic(opts: TopicCommandOptions) = {
    TopicCommand.createTopic(zkUtils, opts)
  }

  def describeTopic(opts: TopicCommandOptions): Unit = {
    TopicCommand.describeTopic(zkUtils, opts)
  }
}
