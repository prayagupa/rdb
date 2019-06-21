import java.sql.{Connection, DriverManager, ResultSet, Timestamp}
import java.time.{Instant, OffsetDateTime, ZoneId, ZoneOffset}
import java.util.TimeZone

object RawOracleSqlExeccutor {

  import io_eff.OracleSqlExecutor.Db._

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

    def dateExample: Unit = {
      val tz = ZoneId.of("America/Los_Angeles")

      val now2 = Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant)
      val now = Timestamp.from(Instant.now())

      val y = now2.toLocalDateTime
      //val statement = sharedConn.createStatement
      //val r = statement.executeUpdate(s"INSERT INTO CustomerOrder VALUES(2, 'Porcupine Tree', '01', '$date', 'dd-Mon-yy hh.mm.ss.zzz')")

      //select TO_CHAR(TO_DATE('01-MAR-19 12.05.19.922613 AM', 'yy hh.mm.ss')) from dual;

      val sql1 = s"INSERT INTO CustomerOrder (id, name, active, created) VALUES(?, ?, ?, ?)"
      val statement1 = sharedConn.prepareStatement(sql1)
      statement1.setInt(1, 4)
      statement1.setString(2, "PT")
      statement1.setString(3, "01")
      statement1.setTimestamp(4, now2)
      statement1.executeQuery()

      val s2 = sharedConn.createStatement()
      val r2 = s2.executeQuery("SELECT created FROM CustomerOrder")

      while (r2.next()) {
        println(r2.getTimestamp("created"))
      }

      sharedConn.close()
    }

    //read(List.empty)
    dateExample

  }

  //select  TO_CHAR(TO_DATE('01-03-2010', 'dd-mm-yyyy')) from dual;
}
