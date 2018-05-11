package s.com.eoi.dbentity

import s.com.eoi.util.DatabaseService
import slick.lifted.TableQuery

object EntityTable {

  val databaseService: DatabaseService = DatabaseService.databaseService

  val handoverlists = new TableQuery(tag => new Handoverlist(tag))
  val users = new TableQuery(tag => new User(tag))
}
