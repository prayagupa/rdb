import java.sql.{Connection, DriverManager, ResultSet}

object RawOracleSqlExeccutor {

  import zio.OracleSqlExecutor.Db._
  val sharedConn: Connection = DriverManager.getConnection(url, username, password)

  def main(args: Array[String]): Unit = {

    implicit class ResultSetOps(resultSet: ResultSet) {
      def getBool(columnName: String): Boolean =
        resultSet.getString(columnName) match {
          case "true" | "01" => true
          case "false" | "00" => false
        }
    }

    def read(columns: List[String]) = {

      val start = System.currentTimeMillis()

      try {
        val statement = sharedConn.createStatement
        //val rs = statement.executeQuery("SELECT CASE WHEN active IS NULL THEN 'true' else 'false' END as active FROM orders")
        val rs = statement.executeQuery("SELECT owner, table_name FROM dba_tables") //WHERE ROWNUM <=1
        while (rs.next) {
          //        java.sql.SQLException: Invalid column type: getBoolean not implemented for class oracle.jdbc.driver.T4CRawAccessor
          //        at oracle.jdbc.driver.Accessor.unimpl(Accessor.java:414)
          //        at oracle.jdbc.driver.Accessor.getBoolean(Accessor.java:439)
          //        at oracle.jdbc.driver.OracleResultSetImpl.getBoolean(OracleResultSetImpl.java:640)
          //        at oracle.jdbc.driver.OracleResultSet.getBoolean(OracleResultSet.java:390)
          //        at RawOracleSqlExeccutor$.main(RawOracleSqlExeccutor.scala:28)
          //        at RawOracleSqlExeccutor.main(RawOracleSqlExeccutor.scala)
          // val res = rs.getBoolean("active")

          val r = rs.getString("owner")
          println("result: " + r)
        }
      } catch {
        case e: Throwable =>
          e.printStackTrace()
      } finally {
        println("closing connection")
        sharedConn.close()
      }

      val time = System.currentTimeMillis() - start
      println(s"time taken: ${time}ms")
    }

    def readSync = {

      val start = System.currentTimeMillis()

      try {
        val statement = sharedConn.createStatement
        //val rs = statement.executeQuery("SELECT CASE WHEN active IS NULL THEN 'true' else 'false' END as active FROM orders")
        val rs = statement.executeQuery("SELECT owner, table_name FROM dba_tables") //WHERE ROWNUM <=1
        while (rs.next) {
          //        java.sql.SQLException: Invalid column type: getBoolean not implemented for class oracle.jdbc.driver.T4CRawAccessor
          //        at oracle.jdbc.driver.Accessor.unimpl(Accessor.java:414)
          //        at oracle.jdbc.driver.Accessor.getBoolean(Accessor.java:439)
          //        at oracle.jdbc.driver.OracleResultSetImpl.getBoolean(OracleResultSetImpl.java:640)
          //        at oracle.jdbc.driver.OracleResultSet.getBoolean(OracleResultSet.java:390)
          //        at RawOracleSqlExeccutor$.main(RawOracleSqlExeccutor.scala:28)
          //        at RawOracleSqlExeccutor.main(RawOracleSqlExeccutor.scala)
          // val res = rs.getBoolean("active")

          val r = rs.getString("name")
          println("result: " + r)
        }
      } catch {
        case e: Throwable =>
          e.printStackTrace()
      } finally {
        println("closing connection")
        sharedConn.close()
      }

      val time = System.currentTimeMillis() - start
      println(s"time taken: ${time}ms")
    }

    read(List.empty)
  }
}
