package s.com.eoi.dbentity

final case class HandoverlistEntity(handoverId: Int, handoverName: String, handoverSysname: String, handoverStarttime: String, pDbserverName: Option[String] = None, bDbserverName: Option[String] = None, pMwserverName: Option[String] = None, bMwserverName: Option[String] = None, pWasserverName: Option[String] = None, bWasserverName: Option[String] = None, pAppName: Option[String] = None, bAppName: Option[String] = None)

  import slick.jdbc.MySQLProfile.api._

  class Handoverlist(tag: Tag) extends Table[HandoverlistEntity](tag, "handoverlist") {
    def * = (handoverId, handoverName, handoverSysname, handoverStarttime, pDbserverName, bDbserverName, pMwserverName, bMwserverName, pWasserverName, bWasserverName, pAppName, bAppName) <> (HandoverlistEntity.tupled, HandoverlistEntity.unapply)

    def ? = (Rep.Some(handoverId), Rep.Some(handoverName), Rep.Some(handoverSysname), Rep.Some(handoverStarttime), pDbserverName, bDbserverName, pMwserverName, bMwserverName, pWasserverName, bWasserverName, pAppName, bAppName).shaped.<>({ r => import r._; _1.map(_ => HandoverlistEntity.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6, _7, _8, _9, _10, _11, _12))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))


    val handoverId: Rep[Int] = column[Int]("handover_id", O.AutoInc, O.PrimaryKey)
    val handoverName: Rep[String] = column[String]("handover_name")
    val handoverSysname: Rep[String] = column[String]("handover_sysname")
    val handoverStarttime: Rep[String] = column[String]("handover_starttime")
    val pDbserverName: Rep[Option[String]] = column[Option[String]]("P_dbserver_name", O.Default(None))
    val bDbserverName: Rep[Option[String]] = column[Option[String]]("B_dbserver_name", O.Default(None))
    val pMwserverName: Rep[Option[String]] = column[Option[String]]("P_mwserver_name", O.Default(None))
    val bMwserverName: Rep[Option[String]] = column[Option[String]]("B_mwserver_name", O.Default(None))
    val pWasserverName: Rep[Option[String]] = column[Option[String]]("P_wasserver_name", O.Default(None))
    val bWasserverName: Rep[Option[String]] = column[Option[String]]("B_wasserver_name", O.Default(None))
    val pAppName: Rep[Option[String]] = column[Option[String]]("P_app_name", O.Default(None))
    val bAppName: Rep[Option[String]] = column[Option[String]]("B_app_name", O.Default(None))

    def getOrder(sortByFields: String, fieldsAscDesc: String = "asc") = {
      (sortByFields, fieldsAscDesc) match {
        case ("handoverId", "asc") => handoverId.asc
        case ("handoverId", _) => handoverId.desc
             
        case ("handoverName", "asc") => handoverName.asc
        case ("handoverName", _) => handoverName.desc
             
        case ("handoverSysname", "asc") => handoverSysname.asc
        case ("handoverSysname", _) => handoverSysname.desc
             
        case ("handoverStarttime", "asc") => handoverStarttime.asc
        case ("handoverStarttime", _) => handoverStarttime.desc
             
        case ("pDbserverName", "asc") => pDbserverName.asc
        case ("pDbserverName", _) => pDbserverName.desc
             
        case ("bDbserverName", "asc") => bDbserverName.asc
        case ("bDbserverName", _) => bDbserverName.desc
             
        case ("pMwserverName", "asc") => pMwserverName.asc
        case ("pMwserverName", _) => pMwserverName.desc
             
        case ("bMwserverName", "asc") => bMwserverName.asc
        case ("bMwserverName", _) => bMwserverName.desc
             
        case ("pWasserverName", "asc") => pWasserverName.asc
        case ("pWasserverName", _) => pWasserverName.desc
             
        case ("bWasserverName", "asc") => bWasserverName.asc
        case ("bWasserverName", _) => bWasserverName.desc
             
        case ("pAppName", "asc") => pAppName.asc
        case ("pAppName", _) => pAppName.desc
             
        case ("bAppName", "asc") => bAppName.asc
        case ("bAppName", _) => bAppName.desc
             
      }
    }
             
  }
