package common;

import io.TestWithData;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import profiler.utils.SparkUtils;

/**
 * Base class of tests which use a JavaSparkContext. Extends TestWithData, so that tests can also
 * use that functionality.
 */
public abstract class TestWithSparkContext extends TestWithData {

  static JavaSparkContext sc = null;

  @BeforeAll
  public static void setUp() throws Exception {
    final SparkConf conf = new SparkConf().setMaster("local").setAppName("assignmentReaderTest");
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    conf.registerKryoClasses(SparkUtils.zksparkClasses());
    sc = new JavaSparkContext(conf);
  }

  @AfterAll
  public static void tearDown() throws Exception {
    sc.close();
    sc = null;
  }

  protected static JavaSparkContext getSparkContext() {
    return sc;
  }
}
