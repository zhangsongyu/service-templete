package s.com.eoi.util

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.slf4j.LoggerFactory
import s.com.eoi.conf.MgrDBConf
import s.com.eoi.dbentity.EntityTable

import scala.concurrent.Future
import scala.util.{Failure, Success}


/**
  * Created by jacky on 16/10/21.
  */

class DatabaseService() {
  private val logger = LoggerFactory.getLogger(this.getClass)
  val driver = slick.jdbc.MySQLProfile

  import driver.api._

  private val hikariConfig = new HikariConfig()
  val dbConf = MgrDBConf()
  val jdbcUrl = dbConf.drives().getOrElse("jdbc:mysql://192.168.31.46:3306/itoaManagement?characterEncoding=utf8&useSSL=false")
  val dbUser = dbConf.user().getOrElse("")
  val dbPassword = dbConf.password().getOrElse("")
  hikariConfig.setDriverClassName("com.mysql.jdbc.Driver")
  hikariConfig.setJdbcUrl(jdbcUrl)
  hikariConfig.setUsername(dbUser)
  hikariConfig.setPassword(dbPassword)
  hikariConfig.setConnectionTimeout(30000)
  hikariConfig.setMaxLifetime(30 * 1000 * 60)
  hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
  hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
  hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
  logger.info(s"dbUser = $dbUser,password len = ${dbPassword.length} jdbcUrl = $jdbcUrl")


  var dataSource: HikariDataSource = _
  try {
    dataSource = new HikariDataSource(hikariConfig)
  }
  catch {
    case ex: Exception =>
      logger.error("[DB connection error]", ex)
      throw ex
  }

  lazy val db = Database.forDataSource(dataSource, Some(dbConf.maxConnections().getOrElse(10)))
  db.createSession()
}

object DatabaseService {
  val databaseService = new DatabaseService()
  import databaseService.driver.api._

  def checkDBSchema() = {
    import ExecutorService.dataSetServiceExecutionContext
    Future.sequence {
      val ret = EntityTable.getClass.getDeclaredFields.toList.map {
        field =>
          field.setAccessible(true)
          field.get(EntityTable) match {
            case items: TableQuery[_] =>
              val query = for {
                h <- items.result.headOption
                c <- items.size.result
              } yield (h, c)
              databaseService.db.run(query.asTry).flatMap {
                case Success(result) =>
                  result match {
                    case (None, count) =>
                      Future(Map("tableName" -> field.getName, "result" -> true, "rowCount" -> count))
                    case (Some(_), count) =>
                      Future(Map("tableName" -> field.getName, "result" -> true, "rowCount" -> count))
                  }
                case Failure(e) =>
                  databaseService.logger.error(s"=====> 数据库表[${field.getName}]不匹配，${e.getMessage}")
                  Future(Map("tableName" -> field.getName, "result" -> false, "rowCount" -> -1, "warn" -> e.getMessage))
              }
            case obj@_ =>
              Future(Map("obj" -> obj.getClass, "igore" -> true)) //不是TableQuery类型，不管它
          }
      }
      ret
    }
  }
}
