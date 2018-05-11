package s.com.eoi.util

//作者：Nathan蒋航
//日期：2017.07.26
import org.apache.poi.ss.usermodel.{Cell, CellStyle, DataFormat, Row}
import org.apache.poi.xssf.streaming.SXSSFWorkbook

object ExcelUtils {
  def writeRow(wb: SXSSFWorkbook, list: List[Any], row: Row, startIndex: Int = 0) = {
    //向excel的某行写一行数据
    //默认从第0列开始写 0列 1列 2列 3列 4列 5列 ...
    val cellStyle = createCellStyle(wb)
    list.zipWithIndex.foreach {
      case (item, index) => {
        val cell = row.createCell(startIndex + index)
        cell.setCellStyle(cellStyle)
        cell.setCellType(Cell.CELL_TYPE_STRING)
        cell.setCellValue(item.toString)
      }
    }
  }

  def createCellStyle(wb: SXSSFWorkbook) = {
    val cellStyle: CellStyle = wb.createCellStyle()
    val format: DataFormat = wb.createDataFormat()
    cellStyle.setDataFormat(format.getFormat("0")) //单元格都是String，不带任何格式 避免科学计数法
    cellStyle
  }

  private[this] def generateMapFieldNameToFieldValue(cc: Any): Map[String, Any] = {
    // 把一个对象实例写成键值对的形式
    cc.getClass.getDeclaredFields.toList.foldRight(Map[String, Any]())((field, map) => {
      field.setAccessible(true)
      val value = field.get(cc) match {
        case Some(v) => v
        case None => ""
        case _ => field.get(cc)
      }
      map + (field.getName -> value)
    })
  }

}
