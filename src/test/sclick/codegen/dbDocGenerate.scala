package sclick.codegen

import org.junit.Test
import s.com.eoi.dbentity.EntityTable.databaseService
import slick.jdbc.GetResult

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DbDocGenerate {

  import databaseService._
  import databaseService.driver.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  case class TabeInfo(table_name: String, column_name: String, column_default: String, is_nullable: String, column_type: String, column_comment: String, data_type: String) {
    override def toString: String = {
      s"|$column_name|$column_type|$is_nullable|$column_default|$column_comment|"
    }

    def toFrontDoc: String = {
      val remark = data_type match {
        case "enum" => s"可选值:${column_type.substring(5).dropRight(1)}"
        case other => column_default match {
          case defaultValue: String => s"默认值:$defaultValue"
          case null => ""
        }
      }
      s"|$column_name|${typeMap.getOrElse(data_type, data_type)}|$column_comment|$remark|"

    }
  }

  val typeMap = Map("bigint" -> "long", "varchar" -> "string")

  implicit val getTabeInfoResult = GetResult(r => TabeInfo(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  val dbDocQuery = sql"""Select table_name,column_name,column_default
              ,is_nullable,column_type,column_comment,data_type
from INFORMATION_SCHEMA.COLUMNS
where  table_schema = 'itoaManagementv1.8'
""".as[TabeInfo]

  @Test
  def genDoc(): Unit = {
    val tt = Await.result(db.run(dbDocQuery), Duration.Inf)
    val sb = new StringBuffer
    val tst = tt.groupBy(_.table_name)

    val keys = tst.keys.toList.sorted
    for (tableName <- keys) {
      val info = tst(tableName)
      sb.append("##").append(tableName).append(":\n\n")
      sb.append("|字段名|类型|是否可空|默认值|备注|").append("\n")
      sb.append("|:--|:--|:--|:--|:--|:--|").append(":\n")
      info.foreach(i => sb.append(i.toString).append("\n"))
      sb.append("\n")

    }
    println(sb.toString)

  }

  @Test
  def genFrontReqDoc(): Unit = {
    val tt = Await.result(db.run(dbDocQuery), Duration.Inf)
    val sb = new StringBuffer
    val tst = tt.groupBy(_.table_name)

    val keys = tst.keys.toList.sorted
    for (tableName <- keys) {
      val info = tst(tableName)
      sb.append("##").append(tableName).append(":\n\n")
      sb.append("|字段名|类型|说明|备注|").append("\n")
      sb.append("|:--|:--|:--|:--|").append(":\n")
      info.foreach(i => sb.append(i.toFrontDoc).append("\n"))
      sb.append("\n")

    }
    println(sb.toString)

  }
}
