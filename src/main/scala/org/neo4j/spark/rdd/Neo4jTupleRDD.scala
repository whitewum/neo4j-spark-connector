package org.neo4j.spark.rdd

import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.neo4j.spark.Neo4jConfig
import org.neo4j.spark.utils.{Neo4jSessionAwareIterator, Neo4jUtils}

import scala.collection.JavaConverters._

class Neo4jTupleRDD(@transient sc: SparkContext, val query: String, val parameters: Seq[(String, AnyRef)])
  extends RDD[Seq[(String, AnyRef)]](sc, Nil) {

  private val config = Neo4jConfig(sc.getConf)

  override def compute(split: Partition, context: TaskContext): Iterator[Seq[(String, AnyRef)]] = {
    new Neo4jSessionAwareIterator(config, query, parameters.toMap.asJava, false).map(record => {
      record.asMap().asScala.toSeq.map(t => (t._1, Neo4jUtils.convert(t._2)))
    })
  }

  override protected def getPartitions: Array[Partition] = Array(new Neo4jPartition())
}

object Neo4jTupleRDD {
  def apply(sc: SparkContext, query: String, parameters: Seq[(String, AnyRef)] = Seq.empty) = new Neo4jTupleRDD(sc, query, parameters)
}


