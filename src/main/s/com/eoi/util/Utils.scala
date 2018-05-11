package s.com.eoi.util

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import scala.collection.immutable.ListMap

object Utils {

  def ccToMap(cc: AnyRef): Map[String, Any] = {
    val result = (ListMap[String, Any]() /: cc.getClass.getDeclaredFields) {
      (map, field) =>
        field.setAccessible(true)
        map + (field.getName -> field.get(cc))
    }
    result
  }

  private val md5Instance = java.security.MessageDigest.getInstance("MD5")

  implicit class PipeOps[I](i: I) {
    def |>[O](f: I => O):O = f(i)
  }

  implicit class StringUtil(val str: String) {
    def MD5: String = {
      md5Instance.digest(str.getBytes())
        .map(0xFF & _)
        .map("%02x".format(_))
        .mkString("")
        .toUpperCase
    }

    def extractScienceNotaion = {
      val sicenceNotation = "^[+-]?[0-9]+[.]?[0-9]*[eE][+-]?[0-9]+"
      str.matches(sicenceNotation) match {
        case true =>
          BigDecimal(str).toBigInt().toString(10)
        case false =>
          str
      }
    }
  }

  implicit class LongDateFormat(val long: Long) {
    def dateFormat(format: String = "yyyy-MM-dd HH:mm:ss"): String = {
      val dateFormat = new SimpleDateFormat(format)
      val date = new Date(long)
      dateFormat.format(date)
    }
  }

  implicit class DateFormat(val date: Date) {
    def dateFormat(format: String = "yyyy-MM-dd"): String = {
      val dateFormat = new SimpleDateFormat(format)
      dateFormat.format(date)
    }

    def toCalendar = {
      val calendar = Calendar.getInstance()
      calendar.setTime(date)
      calendar
    }
  }

  import scala.concurrent.duration._
  implicit class IntOps(i: Int) {
    // 纳秒
    def ns = i nanoseconds
    // 微秒
    def μs = i microseconds
    // 毫秒
    def ms = i milliseconds
    // 秒
    def s = i seconds
    // 分钟
    def m = i minutes
    // 小时
    def h = i hours
    // 天
    def d = i days
  }

  implicit class SeqOps(seq: Seq[Map[String, Any]]) {
    def orderBy(sortBy: String = "created", order: String = "desc"): List[Map[String, Any]] = {
      seq.toList.orderBy(sortBy, order)
    }
  }

  implicit class ListOps(seq: List[Map[String, Any]]) {
    def orderBy(sortBy: String = "created", order: String = "desc"): List[Map[String, Any]] = {
      order match {
        case "desc" =>
          if (List("created", "updated").contains(sortBy)) {
            seq.sortWith((map1, map2) => map1.getOrElse(sortBy, 1).toString.toLong > map2.getOrElse(sortBy, 1).toString.toLong)
          } else {
            seq.sortWith((map1, map2) => map1.getOrElse(sortBy, "").toString.toLowerCase > map2.getOrElse(sortBy, "").toString.toLowerCase)
          }
        case _ =>
          if (List("created", "updated").contains(sortBy)) {
            seq.sortWith((map1, map2) => map1.getOrElse(sortBy, 1).toString.toLong < map2.getOrElse(sortBy, 1).toString.toLong)
          } else {
            seq.sortWith((map1, map2) => map1.getOrElse(sortBy, "").toString.toLowerCase < map2.getOrElse(sortBy, "").toString.toLowerCase)
          }
      }
    }
  }

}
