package prover;

import static algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFr;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryReader;
import algebra.curves.barreto_naehrig.bn254a.BN254aG1;
import algebra.curves.barreto_naehrig.bn254a.BN254aG2;
import algebra.fields.AbstractFieldElementExpanded;
import configuration.Configuration;
import io.AssignmentReader;
// import profiler.utils.SparkUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.cli.*;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;
import zk_proof_systems.zkSNARK.grothBGM17.DistributedProver;
import zk_proof_systems.zkSNARK.grothBGM17.ZKSnarkObjectReader;

public class Prover {
  public static void main(String[] args) throws IOException {
    System.out.println("Distributed Prover (PoC implementation)");

    var options = new Options();
    options.addOption(new Option("h", "help", false, "Display this message"));
    options.addOption(new Option("l", "local", false, "Run on local simulated cluster"));
    options.addOption(new Option("t", "test", false, "Run trivial test to verify setup"));
    options.addOption(new Option("p", "primary-size", true, "Size of primary input (1)"));
    options.addOption(new Option("o", "output", true, "Output file (proof.bin)"));

    try {
      var parser = new BasicParser();
      var cmdLine = parser.parse(options, args);
      var trailing = cmdLine.getArgs();
      System.out.println("trailing: " + String.join(", ", trailing));

      if (cmdLine.hasOption("help")) {
        print_usage(options);
        return;
      }

      if (cmdLine.hasOption("test")) {
        runTest(cmdLine.hasOption("local"));
        return;
      }

      final int primaryInputSize = Integer.parseInt(cmdLine.getOptionValue("primary-size", "1"));

      // Extract command line arguments and call run.
      if (trailing.length != 2) {
        System.err.println("error: invalid number of arguments\n");
        print_usage(options);
        System.exit(1);
      }

      runBN254a(primaryInputSize, trailing[0], trailing[1], cmdLine.hasOption("local"));
    } catch (ParseException e) {
      System.err.println("error: " + e.getMessage());
    }
  }

  static void print_usage(Options options) {
    new HelpFormatter().printHelp("prover <PROVING-KEY-FILE> <ASSIGNMENT-FILE>", options);
  }

  static JavaSparkContext createSparkContext(boolean local) {
    final var sessionBuilder = SparkSession.builder().appName("prover");

    if (local) {
      sessionBuilder.master("local");
    }

    final SparkSession spark = sessionBuilder.getOrCreate();

    spark.sparkContext().conf().set("spark.files.overwrite", "true");
    // TODO: reinstate this when it can be made to work
    //   spark.sparkContext().conf().set(
    //     "spark.serializer",
    //     "org.apache.spark.serializer.KryoSerializer");
    //   spark.sparkContext().conf().registerKryoClasses(SparkUtils.zksparkClasses());
    return new JavaSparkContext(spark.sparkContext());
  }

  static void runTest(boolean local) throws IOException {
    var sc = createSparkContext(local);

    final int numPartitions = 64;
    final long numGroups = 16;
    final int numValues = 1024;

    var values = new ArrayList<Tuple2<Long, Long>>(numValues);
    for (long i = 0; i < numValues; ++i) {
      values.add(new Tuple2<Long, Long>(i % numGroups, i));
    }

    final Function2<Long, Long, Long> reduceSum =
        (x, y) -> {
          return x + y;
        };

    final var pairsRDD = sc.parallelizePairs(values, numPartitions);
    final var reducedRDD = pairsRDD.reduceByKey(reduceSum, numPartitions);
    final var reduced = reducedRDD.collect();

    // Check by summing everything
    final long expect = ((numValues - 1) * numValues) / 2;
    long sum = 0;
    for (var tuple : reduced) {
      sum = sum + tuple._2;
    }
    if (expect != sum) {
      System.out.println("reduced: " + String.valueOf(reduced));
      throw new RuntimeException(
          "expected " + String.valueOf(expect) + ", saw " + String.valueOf(sum));
    }

    System.out.println("TEST PASSED");
    sc.stop();
  }

  static void runBN254a(
      final int primaryInputSize,
      final String provingKeyFile,
      final String assignmentFile,
      final boolean local)
      throws IOException {

    System.out.println(" provingKeyFile: " + provingKeyFile);
    System.out.println(" assignmentFile: " + assignmentFile);

    var provingKeyReader =
        new ZKSnarkObjectReader<BN254aFr, BN254aG1, BN254aG2>(
            new BN254aBinaryReader(new FileInputStream(provingKeyFile)));
    var assignmentReader =
        new AssignmentReader<BN254aFr, BN254aG1, BN254aG2>(
            new BN254aBinaryReader(new FileInputStream(assignmentFile)));

    Prover.<BN254aFr, BN254aG1, BN254aG2>run(
        primaryInputSize, provingKeyReader, assignmentReader, local, BN254aFr.ONE);
  }

  static <
          FrT extends AbstractFieldElementExpanded<FrT>,
          G1T extends AbstractG1<G1T>,
          G2T extends AbstractG2<G2T>>
      void run(
          final int primaryInputSize,
          final ZKSnarkObjectReader<FrT, G1T, G2T> provingKeyReader,
          final AssignmentReader<FrT, G1T, G2T> assignmentReader,
          final boolean local,
          final FrT oneFr)
          throws IOException {
    var sc = createSparkContext(local);

    // TODO: make these configurable.
    final int numExecutors = 16;
    final int numCores = 2;
    final int numMemory = 16;
    final int numPartitions = 64;
    final StorageLevel storageLevel = StorageLevel.MEMORY_AND_DISK_SER();

    final int batchSize = 1024;
    final var provingKeyRDD =
        provingKeyReader.readProvingKeyRDD(primaryInputSize, sc, numPartitions, batchSize);
    final var primFullRDD =
        assignmentReader.readPrimaryFullRDD(primaryInputSize, oneFr, sc, numPartitions, batchSize);

    final var config =
        new Configuration(numExecutors, numCores, numMemory, numPartitions, sc, storageLevel);

    final var proof =
        DistributedProver.prove(provingKeyRDD, primFullRDD._1, primFullRDD._2, oneFr, config);

    sc.stop();
  }
}
