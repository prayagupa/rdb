
import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor.Aux

import scala.collection.immutable
import scala.concurrent.ExecutionContext

object DoobieExamples {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  /**
    * [error]  both lazy value AsyncBlobIO in trait Instances of type => cats.effect.Async[doobie.free.BlobIO]
    * [error]  and lazy value AsyncCallableStatementIO in trait Instances of type => cats.effect.Async[doobie.free.CallableStatementIO]
    * [error]  match expected type cats.effect.Async[M]
    * [error]   val transactor = Transactor.fromDriverManager(
    * [error]                                                ^
    * [error] one error found
    *
    * solution: fromDriverManager[M[_] = IO]
    **/
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    driver = "com.mysql.cj.jdbc.Driver",
    url = "jdbc:mysql://localhost:3306/updupd",
    user = "root",
    pass = "r00t"
  )

  def main(args: Array[String]): Unit = {

    val program = 41.pure[ConnectionIO]

    val io = program.transact(transactor)

    val result = io.unsafeRunSync

    println(result)

    ///
    ///
    ///

    sql"select warehouse from Inventory"
      .query[String]
      .to[List]
      .transact(transactor)
      .unsafeRunSync()
      .foreach(println)

    ////
    ////
    ////
    val yo = transactor.yolo
    import yo._

    sql"select sku, warehouse from Inventory"
      .query[String]
      .check
      .unsafeRunSync()

    ////
    ////

    final case class Inventory(warehouse: String, sku: String, qty: Int)
    object Inventory {
      private val select = fr"select warehouse, sku, qty"
      private val from = fr"from Inventory"

      def findAll: IO[List[Inventory]] = {
        (select ++ from)
          .query[Inventory]
          .to[List]
          .transact(transactor)
      }
    }

    Inventory.findAll.attempt.map {
      case Right(a) =>
        println("result: " + a)
      case Left(value) =>
        println("error: " + value)
    }.unsafeRunSync()

  }
}
