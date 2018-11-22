package cats

import java.util.UUID

import cats.implicits._
import doobie.implicits._

import cats.effect.{ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor.Aux
import doobie.util.update.Update
import doobie.{ConnectionIO, ExecutionContexts, Transactor}
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

object Db {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  lazy val poolTransactor: Resource[IO, HikariTransactor[IO]] =
    for {
      connectionPool <- ExecutionContexts.fixedThreadPool[IO](32)
      transactorPool <- ExecutionContexts.cachedThreadPool[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        "com.mysql.cj.jdbc.Driver",
        "jdbc:mysql://localhost:3306/updupd",
        "root",
        "r00t",
        connectEC = connectionPool,
        transactEC = transactorPool
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
}

object DoobieMySQLExamples {

  import InventoryApi._

  val logger = LoggerFactory.getLogger(DoobieMySQLExamples.getClass)

  def main(args: Array[String]): Unit = {

    val program = 41.pure[ConnectionIO]

    val io =
      Db.poolTransactor.use { conn =>
        program.transact(conn)
      }

    val result = io.unsafeRunSync

    logger.info("pure " + result)
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

    val res = Inventory.insert("San Bruno", "sku-008", 10)
    Thread.sleep(1000)
    println("inserted data: " + res)

    println("===========================================")
    //Inventory.lastTransactionId

    def rollback(implicit executionContext: ExecutionContext) = {
      val transaction1: doobie.ConnectionIO[Int] =
        fr"INSERT INTO Inventory(warehouse, sku, qty) VALUES('warehouse-good', 'good-sku-1', 11)"
          .update
          .run

      val transaction2: doobie.ConnectionIO[Int] =
        fr"INSERT INTO Inventory(warehouse, sku, qty) VALUES('warehouse-bad', 'good-sku-2', 'i m not number')"
          .update
          .run

      val results =
        Db.poolTransactor.use { connection =>
          (transaction1, transaction2)
            .mapN(_ + _)
            .transact(connection)
        }
        .unsafeToFuture()

      import scala.concurrent.duration._
      val res = Await.result(results, 3 seconds)
      println("rollback: " + res)

      res
    }

    //rollback(ExecutionContext.Implicits.global)
    //InventoryApi.Inventory.batchInsert
  }

  def transact[a](f: ConnectionIO[a]): IO[a] = {
    logger.info("making transaction: ")
    Db.poolTransactor.use { tr =>
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

    //fields can not be interpolated
    private val select = fr"select warehouse, sku, qty"
    private val from = fr"from Inventory"

    def findAll: IO[List[Inventory]] = {
      (select ++ from)
        .query[Inventory]
        .to[List]
        .transact(Db.connection)
    }

    def find = {
      val yo = Db.connection.yolo

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
        .transact(Db.connection)
        .unsafeToFuture()

      stuff
    }

    def uuid1 = UUID.randomUUID().toString

    def uuid = uuid1.take(10)

    def batchInsert = {
      val values = List.fill(2)(
        (s"warehouse-$uuid", s"sku-$uuid", Random.nextInt())
      )
      val sql = "INSERT INTO Inventory (warehouse, sku, qty) VALUES (?, ?, ?)"

      val resultSet =
        Db.poolTransactor.use { conn =>
          Update[(String, String, Int)](sql)
            .updateMany(values)
            .transact(conn)
        }.unsafeToFuture()

      import scala.concurrent.duration._
      val r = Await.result(resultSet, 3 seconds)
      println("batch inserted: " + r)
    }

    def lastTransactionId = {

      val sql1 = "UPDATE Inventory SET warehouse = 'SF new-warehouse' WHERE id=1;"
      val sql2 = "INSERT INTO Inventory (warehouse, sku, qty) VALUES('TAC', 'SSSKKKUUU', 100);"
      val sql3 = "select last_insert_id();"

      val t1: doobie.ConnectionIO[Int] = Fragment.const(sql1).update.run
      val t2: doobie.ConnectionIO[Int] = Fragment.const(sql2)
        .update
        .withUniqueGeneratedKeys[Int]("id")

      val trx = Db.poolTransactor.use { conn =>
        (for {
          _ <- fr"UPDATE Inventory SET warehouse = 'SF new-warehouse' WHERE id=1;".update.run
          insertedId <- fr"INSERT INTO Inventory (warehouse, sku, qty) VALUES('TAC', 'SSSKKKUUU', 'hello');"
            .update
            .withUniqueGeneratedKeys[Int]("id")
        } yield insertedId)
          .transact(conn)
      }.unsafeRunSync()

      println("lastInserted: " + trx)
    }
  }

}
