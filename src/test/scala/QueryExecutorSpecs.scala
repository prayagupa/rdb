
/**
  * Created by prayagupd
  * on 1/31/17.
  */
class QueryExecutorSpecs extends org.scalatest.FunSuite {

  val TableName = "Inventory"

  test("sends 100 requests with same connection being opened") {
    val total = Range.inclusive(1, 100).map { i =>
      QueryExecutor
        .queryWithSharedConnection(s"select * from $TableName", ("warehouse", "sku", "qty"))
    }.reduce((a, b) => a + b)

    println("================ shared =======================")
    println("===================================================")
    println(s"=================total: ${total / (1000 * 1000)}ms=======================")
    println(s"=================average: ${total / (100 * 1000 * 1000)}ms=======================")
    println("================ shared =======================")
  }

  //TODO execute above as concurrent requests

  test("sends 100 requests with individual connection") {
    val total = Range.inclusive(1, 100).map { i =>
      QueryExecutor.querySeparateConnection(s"select * from $TableName", ("warehouse", "sku", "qty"))
    }.reduce((a, b) => a + b)

    println("================ Individual ==========================")
    println("======================================================")
    println(s"=================total: ${total / (1000 * 1000)}ms=======================")
    println(s"=================average: ${total / (1000 * 1000 * 100)}ms=============")
    println("================ Individual ==========================")
  }


}
