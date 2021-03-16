package common;

import java.util.ArrayList;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import scala.collection.JavaConverters;

/**
 * Collect key-value pairs, creating intermediate RDDs of "batches" of a given size. Once all
 * entries have been processed, the intermediate RDDs are aggregated into a final single RDD. The
 * intention is to support creation of RDDs from files or iterables of data which do not fit in
 * memory.
 */
public class PairRDDAggregator<K, V> {

  final JavaSparkContext sc;
  final int numPartitions;
  final int batchSize;
  final ArrayList<JavaPairRDD<K, V>> batches;
  ArrayList<Tuple2<K, V>> currentBatch;

  public PairRDDAggregator(JavaSparkContext sc_, final int numPartitions_, final int batchSize_) {
    // Expect that a full batch is a multiple of numPartitions.
    assert batchSize_ % numPartitions_ == 0;

    sc = sc_;
    batchSize = batchSize_;
    numPartitions = numPartitions_;
    batches = new ArrayList<JavaPairRDD<K, V>>();
    initializeBatch();
  }

  public void add(final K key, final V value) {
    assert currentBatch != null;

    currentBatch.add(new Tuple2<K, V>(key, value));
    if (currentBatch.size() >= batchSize) {
      // Process the batch and start again.
      processBatch();
      initializeBatch();
    }
  }

  public JavaPairRDD<K, V> aggregate() {
    assert currentBatch != null;

    if (currentBatch.size() > 0) {
      processBatch();
    }

    return sc.union(JavaConverters.asScalaIteratorConverter(batches.iterator()).asScala().toSeq());
  }

  void processBatch() {
    batches.add(sc.parallelizePairs(currentBatch, numPartitions));
    currentBatch = null;
  }

  void initializeBatch() {
    assert currentBatch == null;
    currentBatch = new ArrayList<Tuple2<K, V>>(batchSize);
  }
}
