package s.com.eoi.util

import s.com.eoi.common.KafkaHttpMessage

object KafkaHttpMessageUtil {

  def getKafkaHttpMessageResult(kafkaHttpMessage: KafkaHttpMessage): Map[String, Any] = {
    kafkaHttpMessage.result.asInstanceOf[Map[String, Any]]
  }
}
