package cats

import scala.concurrent.ExecutionContext

object DoobiePostgresExamples {

  import cats.effect._
  import cats.implicits._
  import doobie.implicits._
  import doobie._

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val connection = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://???.us-east-1.redshift.amazonaws.com:5439/???",
    user = "???",
    pass = "???"
  )

  def main(args: Array[String]): Unit = {

    fr"SELECT customer_id from Customer limit 1"
      .query[String]
      .to[List]
      .transact(connection)
      .unsafeRunSync().foreach(println)
  }

}
