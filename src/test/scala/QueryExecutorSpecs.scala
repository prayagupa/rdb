
/**
  * Created by prayagupd
  * on 1/31/17.
  */
class QueryExecutorSpecs extends org.scalatest.FunSuite {

  val TableName = "Inventory"

  ignore("sends 100 requests with same connection being opened") {
    val average = Range.inclusive(1, 100).map { i =>
      QueryExecutor.queryWithSharedConnection(s"select * from $TableName", ("warehouse", "sku"))
    }.reduce((a, b) => a + b) / 100

    println("================ shared =======================")
    println("===================================================")
    println(s"=================$average=======================")
    println("================ shared =======================")
  }

  //TODO execute above as concurrent requests

  test("sends 100 requests with individual connection") {
    val average = Range.inclusive(1, 100).map { i =>
      QueryExecutor.querySeparateConnection(s"select * from $TableName", ("warehouse", "sku"))
    }.reduce((a, b) => a + b) / 100

    println("================ Individual =======================")
    println("===================================================")
    println(s"=================$average=======================")
    println("================ shared =======================")
  }


}
