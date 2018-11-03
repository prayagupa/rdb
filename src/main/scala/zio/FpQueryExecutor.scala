package zio

import java.sql.{Connection, DriverManager}

import cats.effect.IO

import scala.collection.mutable.ListBuffer

object FpQueryExecutor {

  import cats._
  import cats.implicits._

  //  import scalaz._
  //  import scalaz.zio.IO

  val connectionManager: IO[ConnectionIO] = IO(new ConnectionIO(
    url = "jdbc:mysql://localhost:3306/updupd",
    driver = "com.mysql.cj.jdbc.Driver",
    username = "root",
    password = "r00t"
  ))

  class ConnectionIO(url: String,
                     driver: String,
                     username: String,
                     password: String) {

    private val connection = DriverManager.getConnection(url, username, password)

    def query[a](query: String): IO[List[(String, String, Int)]] = IO {
      val rs = connection.createStatement().executeQuery(query)
      val list = ListBuffer.empty[Tuple3[String, String, Int]]

      while (rs.next()) {
        list.append(
          (rs.getString("warehouse"),
            rs.getString("sku"),
            rs.getInt("qty")
          )
        )
      }

      list.toList
    }
  }

  def queryWithSharedConnection: IO[List[(String, String, Int)]] = {
    for {
      connection <- connectionManager
      results <-
        IO(connection.query("""select * from Inventory""")).unsafeRunSync()
    } yield results
  }

  def main(args: Array[String]): Unit = {
    val res = queryWithSharedConnection.unsafeRunSync()
    println(res)
  }

}
