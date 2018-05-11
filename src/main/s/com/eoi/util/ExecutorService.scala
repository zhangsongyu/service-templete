package s.com.eoi.util

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import s.com.eoi.conf.AkkaActorConf

import scala.concurrent.ExecutionContext

/**
  * Created by jacky on 16/10/19.
  */
object ExecutorService {
  val agentServiceThreadPool = Executors.newFixedThreadPool(20)
  implicit val agentServiceExecutionContext = ExecutionContext.fromExecutorService(agentServiceThreadPool)

  val dataSetServiceThreadPool = Executors.newFixedThreadPool(20)
  implicit val dataSetServiceExecutionContext = ExecutionContext.fromExecutorService(dataSetServiceThreadPool)

  val appServiceThreadPool = Executors.newFixedThreadPool(10)
  implicit val appServiceExecutionContext = ExecutionContext.fromExecutorService(appServiceThreadPool)

  val datasourceServiceThreadPool = Executors.newFixedThreadPool(10)
  implicit val datasourceExecutionContext = ExecutionContext.fromExecutorService(datasourceServiceThreadPool)

  val userServiceThreadPool = Executors.newFixedThreadPool(10)
  implicit val userExecutionContext = ExecutionContext.fromExecutorService(userServiceThreadPool)

  val jobServiceThreadPool = Executors.newFixedThreadPool(20)
  implicit val jobServiceExecutionContext = ExecutionContext.fromExecutorService(jobServiceThreadPool)

  val configServiceThreadPool = Executors.newFixedThreadPool(5)
  implicit val configServiceExecutionContext = ExecutionContext.fromExecutorService(configServiceThreadPool)

  val splServiceThreadPool = Executors.newFixedThreadPool(10)
  implicit val splServiceExecutionContext = ExecutionContext.fromExecutorService(splServiceThreadPool)

  val alarmServiceThreadPool = Executors.newFixedThreadPool(10)
  implicit val alarmServiceExecutionContext = ExecutionContext.fromExecutorService(alarmServiceThreadPool)

  val separateColdHotThreadPool = Executors.newFixedThreadPool(10)
  implicit val separateColdHotExecutionContext = ExecutionContext.fromExecutorService(separateColdHotThreadPool)

  val noticeServiceThreadPool = Executors.newFixedThreadPool(10)
  implicit val noticeServiceExecutionContext = ExecutionContext.fromExecutorService(noticeServiceThreadPool)

  val brainServiceThreadPool = Executors.newFixedThreadPool(10)
  implicit val brainServiceExecutionContext = ExecutionContext.fromExecutorService(brainServiceThreadPool)

  val healthServiceThreadPool = Executors.newFixedThreadPool(10)
  implicit val healthServiceExecutionContext = ExecutionContext.fromExecutorService(healthServiceThreadPool)

  val tagServiceThreadPool = Executors.newFixedThreadPool(5)
  implicit val tagServiceExecutionContext = ExecutionContext.fromExecutorService(tagServiceThreadPool)

  private val config = ConfigFactory.parseString(s"""akka.cluster.seed-nodes=[${AkkaActorConf().clusterNodes()}]""")
    .withFallback(ConfigFactory.parseString(s"""akka.remote.netty.tcp.hostname="${AkkaActorConf().hostname()}""""))
    .withFallback(ConfigFactory.parseString(s"""akka.remote.netty.tcp.port=${AkkaActorConf().port()}"""))
    .withFallback(ConfigFactory.load())

  implicit val system = ActorSystem("itoa", config)
  implicit val mat = ActorMaterializer()
}
