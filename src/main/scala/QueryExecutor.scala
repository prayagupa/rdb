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
  val password = "r00t"
  val sharedConn: Connection = DriverManager.getConnection(url, username, password)

  Class.forName(driver)

  def queryWithSharedConnection(query: String, fields: Tuple3[String, String, String]): Long = {
    val startTime = System.nanoTime()
    try {
      val statement = sharedConn.createStatement
      val rs = statement.executeQuery(query)
      while (rs.next) {
        val v1 = rs.getString(fields._1)
        val v2 = rs.getString(fields._2)
        val v3 = rs.getString(fields._3)
        println(s"${fields._1} => $v1, ${fields._2} => $v2, ${fields._3} => $v3")
      }
      println(s"time=${(startTime - System.nanoTime()) / (1000 * 1000 * 1000)}")
      startTime
    } catch {
      case e: Exception =>
        e.printStackTrace()
        0
    }
    0
  }

  def querySeparateConnection(query: String, fields: Tuple3[String, String, String]): Long = {

    val privateConnection = DriverManager.getConnection(url, username, password)

    val startTime = System.nanoTime()
    try {
      val statement = privateConnection.createStatement
      val rs = statement.executeQuery(query)
      while (rs.next) {
        val v1 = rs.getString(fields._1)
        val v2 = rs.getString(fields._2)
        val v3 = rs.getInt(fields._3)
        println(s"${fields._1} => $v1, ${fields._2} => $v2, ${fields._3} => $v3")
        println(s"time=${(startTime - System.nanoTime()) / (1000 * 1000 * 1000)}")
      }
      startTime
    } catch {
      case e: Exception =>
        e.printStackTrace()
        startTime
    } finally {
      privateConnection.close()
    }
    startTime
  }
}
