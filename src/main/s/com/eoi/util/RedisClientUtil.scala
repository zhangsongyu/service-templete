package s.com.eoi.util

import java.util.{ArrayList => JArrayList}

import redis.clients.jedis._
import s.com.eoi.conf.RedisConf

object RedisClientUtil {

  private val redisConf = RedisConf()

  private var pool: ShardedJedisPool = _

  private val prefix = redisConf.prefix()

  private def clients(): ShardedJedisPool = {
    if (pool == null) {
      //建立分片服务器信息
      val shards = new JArrayList[JedisShardInfo]()

      redisConf.clusterList().get.foreach {
        s =>
          val sp = s.split(":")
          val si = new JedisShardInfo(sp(0), Integer.parseInt(sp(1)))
          if (sp.length > 2 && !"".equals(sp(2))) {
            si.setPassword(sp(2))
          }
          shards.add(si)
      }
      // 建立连接池配置参数
      val config = new JedisPoolConfig()

      config.setMaxTotal(200)

      config.setTestOnBorrow(true)
      // 设置最大阻塞时间，记住是毫秒数milliseconds
      config.setMaxWaitMillis(10000)
      // 设置空间连接
      config.setMaxIdle(10000)
      try {
        //建立分片连接对象
        pool = new ShardedJedisPool(config, shards)
      } catch {
        case e: Exception =>
          pool = new ShardedJedisPool(config, shards)
      }
    }
    pool
  }

  private def getRedisClient(): ShardedJedis = {
    clients.getResource()
  }

  private def redisFun[T](redisClient: ShardedJedis, func: => T): T = {
    val ret = func
    redisClient.close()
    ret
  }

  def get(key: String) = {
    if (exists(key)) {
      val redis = getRedisClient
      redisFun(redis, Some(redis.get(prefix + key)))
    } else None
  }


  def set(key: String, value: String) = {
    val redis = getRedisClient
    redisFun(redis, redis.set(prefix + key, value))
  }

  def set(key: String, value: String, seconds: Int) = {
    val redis = getRedisClient
    redisFun(redis, redis.setex(prefix + key, seconds, value))
  }

  def exists(key: String) = {
    val redis = getRedisClient
    redisFun[Boolean](redis, redis.exists(prefix + key))
  }

  def del(key: String) = {
    val redis = getRedisClient
    redisFun(redis, redis.del(prefix + key))
  }

  // 设置多少秒后过期
  def expire(key: String, second: Int) = {
    val redis = getRedisClient
    redisFun(redis, redis.expire(prefix + key, second))
  }

  //获取运行状态信息
  def getInfo() = {
    redisConf.clusterList() match {
      case Some(v) =>
        v.map { m =>
          val params = m.split(":")
          val shardInfo = new JedisShardInfo(params(0), params(1).toInt)
          if (params.length > 2) {
            shardInfo.setPassword(params(2))
          }
          val cli = new Jedis(shardInfo)

          val sr1 = """(# )?([a-z,A-Z]*)?\r\n(\w*):(.*)""".r.replaceAllIn(cli.info().toCharArray, """"$2":{"$3":"$4",},""")

          val sr2 = "{" + sr1.replace("""},"":{""".toCharArray, "") + "}"

          val sr3 = """,(\r\n)?}""".r.replaceAllIn(sr2, "}")

          val result = s"""{"${params(0)}:${params(1)}":""" + "\r\n".r.replaceAllIn(sr3, "") +"""}""""

          cli.close()

          JsonUtil.fromJson[Map[String, Any]](result)
        }
      case None => List[Map[String, Any]]()
    }
  }
}