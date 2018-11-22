import java.io.FileReader
import java.sql.{Connection, Driver, DriverManager, Timestamp}

import cats.effect.IO
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.h2.tools.Server
import org.scalatest.{BeforeAndAfter, FunSpec}
import doobie._
import doobie.implicits._

import scala.concurrent.ExecutionContext

class InMemoryOracleSpec extends FunSpec {

  val inMemoryServer: Server = Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers")

  implicit val ex = ExecutionContext.global
  implicit val cs = IO.contextShift(ex)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:~/inmemory-db;MODE=Oracle",
    "sa",
    ""
  )

  def setup = {
    println("startup")
    inMemoryServer.start()

    fr"DROP TABLE IF EXISTS CustomerOrder; DROP SEQUENCE IF EXISTS CO_PK;"
      .update
      .run
      .transact(transactor)
      .unsafeRunSync()

    val transaction = for {
      a <- fr"CREATE TABLE CustomerOrder (id NUMBER NOT NULL, name VARCHAR(20), active VARCHAR(2), created TIMESTAMP);".update.run
      b <- fr"ALTER TABLE CustomerOrder ADD (CONSTRAINT co_pk PRIMARY KEY (ID));".update.run
      c <- fr"CREATE SEQUENCE co_pk START WITH 1;".update.run
    } yield a + b + c

    transaction
      .transact(transactor)
      .unsafeRunSync()

    println("setup complete")
  }

  def tearDown = {
    inMemoryServer.stop()
  }

  final case class CustomerOrder(id: Int, name: String, active: String, created: Timestamp)

  it("setup and insert") {
    setup
    val create = fr"INSERT INTO CustomerOrder VALUES(1, 'upd', '01', CURRENT_TIMESTAMP);"
      .update
      .run
      .transact(transactor)
      .unsafeRunSync()

    val get = fr"SELECT * FROM CustomerOrder;"
      .query[CustomerOrder]
      .to[List]
      .transact(transactor)
      .unsafeRunSync()

    println(get)

    tearDown
  }

  it("read from sql") {
    import org.h2.tools.RunScript

    inMemoryServer.start()

    val connection = DriverManager.getConnection(
      "jdbc:h2:~/inmemory-db;MODE=Oracle",
      "sa",
      ""
    )

    RunScript.execute(connection, new FileReader("db/1.sql"))

    val transactor: Aux[IO, Connection] = Transactor.fromConnection(connection, ex)

    val co = fr"SELECT * FROM CustomerOrder;"
      .query[CustomerOrder]
      .to[List]
      .transact(transactor)
      .unsafeRunSync()

    println(co)

    val co1 = fr"SELECT * FROM Inventory;"
      .query[CustomerOrder]
      .to[List]
      .transact(transactor)
      .unsafeRunSync()

    println(co1)

    //cleanup
    RunScript.execute(connection, new FileReader("db/2.sql"))
    tearDown
  }
}
