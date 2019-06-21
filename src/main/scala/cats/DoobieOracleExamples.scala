package cats

import java.sql.Timestamp
import java.time.OffsetDateTime

import cats.effect.{ContextShift, IO}
import doobie.Transactor
import doobie.free.connection.ConnectionIO
import doobie.util.{Meta, Read}
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor.Aux

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

object DoobieOracleExamples {

  import cats._
  import cats.implicits._

  import doobie.syntax
  import doobie.implicits._

  implicit val ex = ExecutionContext.global
  implicit val cs = IO.contextShift(ex)

  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "oracle.jdbc.driver.OracleDriver",
    "jdbc:oracle:thin:@localhost:1521/xe",
    "SYS as SYSDBA",
    "Duwamish9"
  )

  case class DbBoolean(boolean: Boolean)

  object DbBoolean {
    implicit val MetaDbBoolean: Meta[DbBoolean] = Meta[String].timap {
      case "01" => DbBoolean(true)
      case "00" => DbBoolean(false)
    } {
      case DbBoolean(true) => "01"
      case DbBoolean(false) => "00"
    }
  }

  def main(args: Array[String]): Unit = {

    //    31-OCT-18 10.16.29.572000000 AM
    //    30-OCT-18 03.48.43.973000000 PM
    //    30-OCT-18 03.19.38.385809000 PM
    //    30-OCT-18 03.21.20.921259000 PM
    //    30-OCT-18 03.23.59.210325000 PM
    //    31-OCT-18 10.21.06.907000000 AM
    //    30-OCT-18 03.32.20.033974000 PM
    //    30-OCT-18 03.51.23.381000000 PM
    //    31-OCT-18 10.23.33.770000000 AM

    val orderCreated = transaction {
      fr"select * FROM CustomerOrder"
        .query[(Int, String, String)]
        .to[List]
    }

    orderCreated.map(c => println("orderCreated: " + c)).unsafeRunSync()

    //read boolean

    val resBool = selectAll[String] {
      fr"select active FROM CustomerOrder"
    }

    val resBoolToStr = selectAll[String] {
      fr"SELECT CASE WHEN active IS NULL THEN 'true' else 'false' END FROM CustomerOrder"
    }

    println(resBoolToStr.unsafeRunSync())

    ////
    ////
    ////

    import DbBoolean._

    fr"select active FROM CustomerOrder"
      .query[DbBoolean]
      .to[List]
      .transact(transactor)
      .unsafeRunSync()
      .foreach(println)

//    DbBoolean(true)
//    DbBoolean(true)
//    DbBoolean(true)
//    DbBoolean(true)
//    DbBoolean(true)
//    DbBoolean(true)
//    DbBoolean(true)
//    DbBoolean(true)
//    DbBoolean(true)

    val dbBoolean = selectAll[DbBoolean] {
      fr"select active FROM CustomerOrder"
    }

    println("dbBoolean: " + dbBoolean.unsafeRunSync())
  }

  def selectAll[a: Read](tx: Fragment): IO[List[a]] = {
    tx
      .query[a]
      .to[List]
      .transact(transactor)
  }

  def transaction[a](tx: ConnectionIO[a]): IO[a] = {
    tx.transact(transactor)
  }

  val date: OffsetDateTime = OffsetDateTime.now()
//
//  fr"INSERT INTO CustomerOrder VALUES(2, 'Porcupine Tree', '01', $date);"
//    .update
//    .run

}
