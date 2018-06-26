import java.sql.ResultSet
import java.sql.{Connection, DriverManager}

import QueryExecutor.sharedConn

/**
  * Created by prayagupd
  * on 1/31/17.
  */

object QueryExecutor {

  val url = "jdbc:mysql://localhost:3306/updupd"
  val driver = "com.mysql.cj.jdbc.Driver"
  val username = "root"
  val password = "root"
  var sharedConn:Connection = _

  Class.forName(driver)
  sharedConn = DriverManager.getConnection(url, username, password)

  def queryWithSharedConnection(query: String, fields: Tuple2[String, String]): Long = {
    val startTime = System.nanoTime()
    try {
      val statement = sharedConn.createStatement
      val rs = statement.executeQuery(query)
      while (rs.next) {
        val v1 = rs.getString(fields._1)
        val v2 = rs.getString(fields._2)
        println(s"$fields._1 => $v1, $fields._2 => $v2")
      }
      println(s"time=${(startTime - System.nanoTime())/(1000 * 1000 * 1000)}")
      startTime
    } catch {
      case e: Exception =>
        e.printStackTrace
        0
    }
    0
  }

  def querySeparateConnection(query: String, fields: Tuple2[String, String]): Long = {

    val connection = DriverManager.getConnection(url, username, password)

    val startTime = System.nanoTime()
    try {
      val statement = connection.createStatement
      val rs = statement.executeQuery(query)
      while (rs.next) {
        val v1 = rs.getString(fields._1)
        val v2 = rs.getString(fields._2)
        println(s"$fields._1 => $v1, $fields._2 => $v2")
        println(s"time=${(startTime - System.nanoTime())/(1000 * 1000 * 1000)}")
      }
      startTime
    } catch {
      case e: Exception =>
        e.printStackTrace
        startTime
    } finally {
      connection.close
    }
    startTime
  }
}
