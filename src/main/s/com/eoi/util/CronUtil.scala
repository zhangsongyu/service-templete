package s.com.eoi.util

import java.text.SimpleDateFormat

import com.cronutils.builder.CronBuilder
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.field.expression.And
import com.cronutils.model.field.expression.FieldExpressionFactory._
import org.quartz.TriggerUtils
import org.quartz.impl.triggers.CronTriggerImpl
import s.com.eoi.common.{MsgCode, ServiceCommon}

import scala.collection.JavaConverters._

object CronUtil extends ServiceCommon {
  /**
    *
    * @param cron
    * @param numTimes
    * @return cron最近numTimes次运行时间
    */
  def getRunTimes(cron: String, numTimes: Option[Int] = Option(5)): Seq[String] = {
    val ct = new CronTriggerImpl()
    try {
      ct.setCronExpression(cron)
      val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val ld = TriggerUtils.computeFireTimes(ct, null, numTimes.getOrElse(5))
      val sld = ld.asScala
      sld.map(dateFormat.format)
    } catch {
      case ex: Throwable => Seq()
    }
  }

  /**
    *
    * @param req
    * @return 通过req生成cron表达式
    */
  def genCronExpression(req: CronBuildReq) = {
    var str = ""
    try {
      req.dom.fixList = req.dow.nums
      val cron = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
      str = req.year(req.dow(req.month(req.dom(req.hours(req.minutes(req.seconds(cron))))))).instance().asString()
      respSuccess(MsgCode.success, "success", Map("expression" -> str, "runTimes" -> getRunTimes(str, Some(5)))
      )
    } catch {
      case ex: Throwable => respFail(MsgCode.notMatch, s"表达式格式错误${ex.getMessage}")
    }
  }

  /**
    *
    * @param seconds 秒
    * @param minutes 分
    * @param hours   时
    * @param dom     天(Day-of-Month)
    * @param month   月
    * @param dow     每周(Day-of-Week)
    * @param year    年
    */
  case class CronBuildReq(seconds: Seconds, minutes: Minutes, hours: Hours, dom: Dom, month: Month, dow: Dow, year: Year)

  /**
    *
    * @param startTime 通过startTimestamp计算初始的秒数
    * @param interval  执行周期
    */
  case class Seconds(startTime: Option[Int], interval: Option[Int]) extends BuildCronSMH {
    def apply(cb: CronBuilder): CronBuilder = {
      interval match {
        case Some(iv) => if (iv == 1 && startTime.contains(0)) cb.withSecond(always()) else cb.withSecond(every(on(startTime.getOrElse(0)), iv))
        case None => cb.withSecond(on(startTime.getOrElse(0)))
      }
      cb
    }
  }

  case class Minutes(startTime: Option[Int], interval: Option[Int]) extends BuildCronSMH {
    def apply(cb: CronBuilder): CronBuilder = {
      interval match {
        case Some(iv) => if (iv == 1 && startTime.contains(0)) cb.withMinute(always()) else cb.withMinute(every(on(startTime.getOrElse(0)), iv))
        case None => cb.withMinute(on(startTime.getOrElse(0)))
      }
      cb
    }
  }

  case class Hours(startTime: Option[Int], interval: Option[Int]) extends BuildCronSMH {
    def apply(cb: CronBuilder): CronBuilder = {
      interval match {
        case Some(iv) => if (iv == 1 && startTime.contains(0)) cb.withHour(always()) else cb.withHour(every(on(startTime.getOrElse(0)), iv))
        case None => cb.withHour(on(startTime.getOrElse(0)))
      }
      cb
    }
  }

  /**
    *
    * @param fixList 月周存在冲突，不可同时存在，不可同时为问号或星号
    */
  case class Dom(nums: Option[List[Int]], var fixList: Option[List[Int]]) extends BuildCronDMWY {
    def apply(cb: CronBuilder): CronBuilder = {
      nums match {
        case Some(n) =>
          val andItem = new And()
          n.foreach(f => andItem.and(on(f)))
          cb.withDoM(andItem)
        case None =>
          fixList match {
            case Some(w) => cb.withDoM(questionMark())
            case None => cb.withDoM(always())
          }

      }
    }
  }

  /**
    *
    * @param nums 月数可复选
    */
  case class Month(num: Option[Int], nums: Option[List[Int]]) extends BuildCronDMWY {
    def apply(cb: CronBuilder): CronBuilder = {
      /*      nums match {
              case Some(n) =>
                val andItem = new And()
                n.foreach(f => andItem.and(on(f)))
                cb.withMonth(andItem)
              case None => cb.withMonth(always)
            }*/
      num match {
        case Some(n) =>
          cb.withMonth(on(n))
        case None => cb.withMonth(always)
      }
    }
  }

  case class Dow(nums: Option[List[Int]]) extends BuildCronDMWY {
    def apply(cb: CronBuilder): CronBuilder = {
      nums match {
        case Some(n) =>
          val andItem = new And()
          n.foreach(f => andItem.and(on(f)))
          cb.withDoW(andItem)
        case None => cb.withDoW(questionMark)
      }
    }
  }

  case class Year(nums: Option[List[Int]]) extends BuildCronDMWY {
    def apply(cb: CronBuilder): CronBuilder = {
      nums match {
        case Some(n) =>
          val andItem = new And()
          n.foreach(f => andItem.and(on(f)))
          cb.withYear(andItem)
        case None => cb.withYear(always)
      }
    }
  }

  trait BuildCronSMH {
    val startTime: Option[Int]
    val interval: Option[Int]
  }

  trait BuildCronDMWY {
    val nums: Option[List[Int]]
  }

}
