import java.sql.ResultSet
import java.sql.{Connection, DriverManager}
import java.util.Properties

import RawMySqlQueryExecutor.sharedConn

/**
  * Created by prayagupd
  * on 1/31/17.
  */

object RawMySqlQueryExecutor {

  val url = "jdbc:mysql://localhost:3306/updupd"
  val driver = "com.mysql.cj.jdbc.Driver"
  val username = "root"
  val password = "r00t"

  val properties = new Properties()
  properties.put("user", username)
  properties.put("password", password)
  properties.put("connectTimeout", "10") //only connect timeout not transaction timeout

  val sharedConn1: Connection = DriverManager.getConnection(url, username, password)
  val sharedConn: Connection = DriverManager.getConnection(url, properties)

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
      println(s"time=${System.nanoTime() - startTime}")
      System.nanoTime() - startTime
    } catch {
      case e: Exception =>
        println(e.printStackTrace())
        0
    }
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
    } catch {
      case e: Exception =>
        e.printStackTrace()
    } finally {
      privateConnection.close()
    }
    System.nanoTime() - startTime
  }
}
