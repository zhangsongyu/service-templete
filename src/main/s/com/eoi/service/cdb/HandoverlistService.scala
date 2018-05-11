package s.com.eoi.service.cdb

import s.com.eoi.common.{MsgCode, ServiceCommon}
import slick.jdbc.MySQLProfile.api._
import s.com.eoi.dbentity.EntityTable.databaseService._

import scala.concurrent.ExecutionContext.Implicits.global
import s.com.eoi.dbentity.EntityTable._
import s.com.eoi.dbentity._
import s.com.eoi.util.Utils

import scala.concurrent.Future

case class HandoverlistReq(handoverId: Int, handoverName: String, handoverSysname: String, handoverStarttime: String, pDbserverName: Option[String] = None, bDbserverName: Option[String] = None, pMwserverName: Option[String] = None, bMwserverName: Option[String] = None, pWasserverName: Option[String] = None, bWasserverName: Option[String] = None, pAppName: Option[String] = None, bAppName: Option[String] = None)

class HandoverlistService extends ServiceCommon {
  def testuser = {
    db.run(users.result).flatMap { f =>
      Future(f)
    }
  }


  def list(from: Option[Int], size: Option[Int]) = {
    val query = for {
      total <- handoverlists.length.result
      data <- handoverlists.drop(from.getOrElse(0)).take(size.getOrElse(10)).result
    } yield (total, data)
    db.run(query).flatMap { f =>
      Future {
        respSuccess(MsgCode.success, "success", f._2.map(Utils.ccToMap(_)), f._1)
      }
    }
  }

  def add(req: HandoverlistReq) = {
    db.run(handoverlists += HandoverlistEntity(handoverId = req.handoverId,
      handoverName = req.handoverName,
      handoverSysname = req.handoverSysname,
      handoverStarttime = req.handoverStarttime,
      pDbserverName = req.pDbserverName,
      bDbserverName = req.bDbserverName,
      pMwserverName = req.pMwserverName,
      bMwserverName = req.bMwserverName,
      pWasserverName = req.pWasserverName,
      bWasserverName = req.bWasserverName,
      pAppName = req.pAppName,
      bAppName = req.bAppName))
  }

  def delete(handoverId: Int) = {
    db.run(handoverlists.filter(_.handoverId === handoverId).delete)
  }

  def update(handoverId: Int, req: HandoverlistReq) = {
    val sql = for {
      up <- handoverlists.filter(_.handoverId === handoverId).map { h =>
        (h.handoverName,
          h.handoverSysname,
          h.handoverStarttime,
          h.pDbserverName,
          h.bDbserverName,
          h.pMwserverName,
          h.bMwserverName,
          h.pWasserverName,
          h.bWasserverName,
          h.pAppName,
          h.bAppName)
      }.update(
        req.handoverName,
        req.handoverSysname,
        req.handoverStarttime,
        req.pDbserverName,
        req.bDbserverName,
        req.pMwserverName,
        req.bMwserverName,
        req.pWasserverName,
        req.bWasserverName,
        req.pAppName,
        req.bAppName)

    } yield up
    db.run(sql.transactionally)
  }

  def getHandover(handoverId: Int) = {
    db.run(handoverlists.filter(_.handoverId === handoverId).result.headOption)
  }
}
