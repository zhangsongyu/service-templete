package s.com.eoi.dbentity

final case class UserEntity(id: Long, name: Option[String] = None)

  import slick.jdbc.MySQLProfile.api._

  class User(tag: Tag) extends Table[UserEntity](tag, "user") {
    def * = (id, name) <> (UserEntity.tupled, UserEntity.unapply)

    def ? = (Rep.Some(id), name).shaped.<>({ r => import r._; _1.map(_ => UserEntity.tupled((_1.get, _2))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))


    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Default(None))

    def getOrder(sortByFields: String, fieldsAscDesc: String = "asc") = {
      (sortByFields, fieldsAscDesc) match {
        case ("id", "asc") => id.asc
        case ("id", _) => id.desc
             
        case ("name", "asc") => name.asc
        case ("name", _) => name.desc
             
      }
    }
             
  }
