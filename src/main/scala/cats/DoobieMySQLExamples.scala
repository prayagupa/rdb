package cats

import java.util.UUID

import cats.DoobieMySQLExamples.connection
import cats.effect.{ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.update.Update
import doobie.{ConnectionIO, ExecutionContexts, Transactor}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

object DoobieMySQLExamples {

  import cats.implicits._

  import doobie.implicits._

  import InventoryApi._

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  lazy val poolTransactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      te <- ExecutionContexts.cachedThreadPool[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        "com.mysql.cj.jdbc.Driver",
        "jdbc:mysql://localhost:3306/updupd",
        "root",
        "r00t",
        connectEC = ce,
        transactEC = te
      )
    } yield xa

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
  val connection: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    driver = "com.mysql.cj.jdbc.Driver",
    url = "jdbc:mysql://localhost:3306/updupd",
    user = "root",
    pass = "r00t"
  )

  def main(args: Array[String]): Unit = {

    val program = 41.pure[ConnectionIO]

    val io = program.transact(connection)

    val result = io.unsafeRunSync

    println(result)
    //Thread.sleep(10000)

    ///
    ///
    ///
    val resp = transact {
      sql"select warehouse from Inventory"
        .query[String]
        .to[List]
    }.unsafeRunSync()

    println("select: " + resp)

    //Thread.sleep(100000)

    ////
    ////
    ////

    Inventory.findAll.attempt.map {
      case Right(a) =>
        println("result: " + a)
      case Left(value) =>
        println("error: " + value)
    }.unsafeRunSync()

    val res = Inventory.insert("San Bruno", "sku-004", 10)
    Thread.sleep(1000)
    println("inserted data: " + res)

    def rollback(implicit executionContext: ExecutionContext) = {
      val transaction1: doobie.ConnectionIO[Int] =
        fr"insert into Inventory(warehouse, sku, qty) values('warehouse-good', 'good-sku-1', 11)"
          .update
          .run

      val transaction2: doobie.ConnectionIO[Int] =
        fr"insert into Inventory(warehouse, sku, qty) values('warehouse-bad', 'good-sku-2', 'i m not number')"
          .update
          .run

      val results = (transaction1, transaction2)
        .mapN(_ + _)
        .transact(connection)
        .unsafeToFuture()

      import scala.concurrent.duration._
      val res = Await.result(results, 3 seconds)
      println("rollback: " + res)

      res
    }

    //rollback(ExecutionContext.Implicits.global)

    //batchExample
  }

  def transact[a](f: ConnectionIO[a]): IO[a] = {
    poolTransactor.use { tr =>
      f.transact(tr)
    }
  }
}


object InventoryApi {

  final case class Inventory(warehouse: String, sku: String, qty: Int)

  object Inventory {
    val warehouse = "warehouse"
    val sku = "sku"
    val qty = "qty"

    import cats._
    import cats.implicits._

    import doobie.syntax
    import doobie.implicits._

    //fields can not be interpolated
    private val select = fr"select warehouse, sku, qty"
    private val from = fr"from Inventory"

    def findAll: IO[List[Inventory]] = {
      (select ++ from)
        .query[Inventory]
        .to[List]
        .transact(connection)
    }

    def find = {
      val yo = connection.yolo

      import yo._

      sql"select sku, warehouse from Inventory"
        .query[String]
        .check
        .unsafeRunSync()
    }

    def insert(warehouse: String, sku: String, qty: Int): Future[Int] = {
      val stuff = fr"insert into Inventory(warehouse, sku, qty) values($warehouse, $sku, $qty)"
        .update
        .withUniqueGeneratedKeys[Int]("id")
        .transact(connection)
        .unsafeToFuture()

      stuff
    }

    def uuid1 = UUID.randomUUID().toString

    def uuid = uuid1.take(10)

    def batchInsert = {
      val values = List.fill(4)(
        (s"warehouse-$uuid", s"sku-$uuid", Random.nextInt())
      )
      val sql = "INSERT INTO Inventory (warehouse, sku, qty) VALUES (?, ?, ?)"

      val resultSet = Update[(String, String, Int)](sql)
        .updateMany(values)
        .transact(connection)
        .unsafeToFuture()

      import scala.concurrent.duration._
      val r = Await.result(resultSet, 3 seconds)
      println("batch: " + r)
    }
  }

}
