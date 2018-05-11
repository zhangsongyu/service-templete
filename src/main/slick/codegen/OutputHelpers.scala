package slick.codegen

import java.io.{BufferedWriter, File, FileWriter}

/** Output-related code-generation utilities. */
trait OutputHelpers {
  def code: String

  /** The parent type of the generated main trait. This can be overridden in subclasses. */
  def parentType: Option[String] = None

  /**
   * The generated code stored in a map that associates the scala filename with the generated code (this map contains one entry per table).
   */
  def codePerTable: Map[String, String]

  /**
    * The generated code used to generate the container class.
    */
  def codeForContainer: String

  /** Indents all but the first line of the given string
   *  No indent is added to empty lines.
   */
  def indent(code: String): String

  def indentCaseClass(code: String): String

  def indentTableQuery(code: String): String

  /** Writes given content to a file.
   *  Ensures the file ends with a newline character.
   *  @group Output
   */
  def writeStringToFile(content: String, folder:String, pkg: String, fileName: String): Unit = {
    val folder2 : String = folder + "/" + (pkg.replace(".", "/")) + "/"
    new File(folder2).mkdirs()
    val file = new File(folder2 + fileName)
    if (!file.exists()) {
      file.createNewFile();
    }
    val fw = new FileWriter(file.getAbsoluteFile())
    val bw = new BufferedWriter(fw)
    bw.write(content)
    if (!content.endsWith("\n")) bw.write("\n")
    bw.close()
  }

  /**
   * Generates code and writes it to a file.
   * Creates a folder structure for the given package inside the given srcFolder
   * and places the new file inside or overrides the existing one.
   * @group Output
   * @param folder target folder, in which the package structure folders are placed
   * @param profile Slick profile that is imported in the generated package (e.g. slick.jdbc.H2Profile)
   * @param pkg Scala package the generated code is placed in (a subfolder structure will be created within srcFolder)
   * @param container The name of a trait and an object the generated code will be placed in within the specified package.
   * @param fileName Name of the output file, to which the code will be written
   */
  def writeToFile(profile: String, folder:String, pkg: String, container:String="EntityTable", fileName: String="EntityTable.scala"): Unit = {
    writeStringToFile(packageCode(profile, pkg, container, parentType), folder, pkg, fileName)
  }

   /**
   * Generates code and writes it to multiple files.
   * Creates a folder structure for the given package inside the given srcFolder
   * and places the new files inside or overrides the existing one.
   * @group Output
   * @param folder target folder, in which the output files are placed
   * @param profile Slick profile that is imported in the generated package (e.g. scala.slick.driver.H2Driver)
   * @param pkg Scala package the generated code is placed in (a subfolder structure will be created within srcFolder)
   * @param container The name of a trait and an object the generated code will be placed in within the specified package.
   */
  def writeToMultipleFiles(profile: String, folder: String, pkg: String, container: String = "EntityTable"): Unit = {
    // Write the container file (the file that contains the stand-alone object).
    writeStringToFile(packageContainerCode(profile, pkg, container), folder, pkg, container + ".scala")
    // Write one file for each table.
    codePerTable.foreach {
      case (tableName, tableCode) => writeStringToFile(packageTableCode(tableName, tableCode, pkg, container), folder, pkg, handleQuotedNamed(tableName)+".scala")
    }
  }

  private def handleQuotedNamed(tableName: String) = {
    if (tableName.endsWith("`")) s"${tableName.init}Table`" else s"${tableName}Table"
  }

  /**
    * Generates code providing the data model as trait and object in a Scala package
    *
    * @group Basic customization overrides
    * @param profile   Slick profile that is imported in the generated package (e.g. slick.jdbc.H2Profile)
    * @param pkg       Scala package the generated code is placed in
    * @param container The name of a trait and an object the generated code will be placed in within the specified package.
    */
  def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
    s"""
package s.com.eoi.dbentity

import s.com.eoi.util.DatabaseService

object ${container} {

  ${indentCaseClass(code)}


  ${indent(code)}
}
      """.trim()
    }

  /**
   * Generates code providing the stand-alone slick data model for immediate use.
   * @group Basic customization overrides
   * @param profile Slick profile that is imported in the generated package (e.g. scala.slick.driver.H2Driver)
   * @param pkg Scala package the generated code is placed in
   * @param container The name of a trait and an object the generated code will be placed in within the specified package.
   */
  def packageContainerCode(profile: String, pkg: String, container: String = "Tables"): String = {
//    val mixinCode = codePerTable.keys.toList.sorted.map(tableName => s"${handleQuotedNamed(tableName) }").mkString("extends ", "\n  with ", "")
    s"""
package s.com.eoi.dbentity

import s.com.eoi.util.DatabaseService
import slick.lifted.TableQuery

object ${container} {

  val databaseService: DatabaseService = DatabaseService.databaseService

  ${indentTableQuery(code)}
}
      """.trim()
  }

  /**
   * Generates code for the given table. The tableName and tableCode parameters should come from the #codePerTable map.
   * @group Basic customization overrides
   * @param tableName : the name of the table
   * @param tableCode : the generated code for the table.
   * @param pkg Scala package the generated code is placed in
   * @param container The name of the container
   */
  def packageTableCode(tableName: String, tableCode: String, pkg: String, container: String): String = {
    s"""
package s.com.eoi.dbentity

${indentCaseClass(tableCode)}
${indent(tableCode)}
""".trim()
  }
}
