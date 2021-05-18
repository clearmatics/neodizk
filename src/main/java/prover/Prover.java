package prover;

import static algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFr;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.curves.AbstractGT;
import algebra.curves.AbstractPairing;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377BinaryReader;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377BinaryWriter;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377Fields.BLS12_377Fr;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377G1;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377G2;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377GT;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377Pairing;
import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryReader;
import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryWriter;
import algebra.curves.barreto_naehrig.bn254a.BN254aG1;
import algebra.curves.barreto_naehrig.bn254a.BN254aG2;
import algebra.curves.barreto_naehrig.bn254a.BN254aGT;
import algebra.curves.barreto_naehrig.bn254a.BN254aPairing;
import algebra.fields.AbstractFieldElementExpanded;
import configuration.Configuration;
import io.AssignmentReader;
// import profiler.utils.SparkUtils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.function.Function;
import org.apache.commons.cli.*;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;
import zk_proof_systems.zkSNARK.grothBGM17.DistributedProver;
import zk_proof_systems.zkSNARK.grothBGM17.Verifier;
import zk_proof_systems.zkSNARK.grothBGM17.ZKSnarkObjectReader;
import zk_proof_systems.zkSNARK.grothBGM17.ZKSnarkObjectWriter;

public class Prover {
  public static void main(String[] args) throws IOException {
    System.out.println("Distributed Prover (PoC implementation)");

    var options = new Options();
    options.addOption(new Option("h", "help", false, "Display this message"));
    options.addOption(new Option("l", "local", false, "Run on local simulated cluster"));
    options.addOption(new Option("t", "test", false, "Run trivial test to verify setup"));
    options.addOption(new Option("p", "primary-size", true, "Size of primary input (1)"));
    options.addOption(new Option("o", "output", true, "Output file (proof.bin)"));
    options.addOption(new Option("c", "curve", true, "Curve name: bn254a or bls12-377 (bn254a)"));
    options.addOption(
        new Option("v", "vk", true, "(Optional) Verification key file to verify resulting proof"));

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
      final String outputFile = cmdLine.getOptionValue("output", "proof.bin");
      final String vkFile = cmdLine.getOptionValue("vk", null);

      // Extract command line arguments and call run.
      if (trailing.length != 2) {
        System.err.println("error: invalid number of arguments\n");
        print_usage(options);
        System.exit(1);
      }

      final String curve = cmdLine.getOptionValue("curve", "bn254a");
      switch (curve) {
        case "bn254a":
          runBN254a(
              primaryInputSize,
              trailing[0],
              trailing[1],
              outputFile,
              cmdLine.hasOption("local"),
              vkFile);
          break;
        case "bls12-377":
          runBLS12_377(
              primaryInputSize,
              trailing[0],
              trailing[1],
              outputFile,
              cmdLine.hasOption("local"),
              vkFile);
          break;
        default:
          throw new ParseException("invalid curve: " + curve);
      }

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
      final String outputFile,
      final boolean local,
      final String vkFileOrNull)
      throws IOException {

    System.out.println(" provingKeyFile: " + provingKeyFile);
    System.out.println(" assignmentFile: " + assignmentFile);
    System.out.println(" outputFile: " + outputFile);

    final Function<InputStream, ZKSnarkObjectReader<BN254aFr, BN254aG1, BN254aG2>>
        createZKObjectReader =
            (stream) ->
                new ZKSnarkObjectReader<BN254aFr, BN254aG1, BN254aG2>(
                    new BN254aBinaryReader(stream));
    final Function<OutputStream, ZKSnarkObjectWriter<BN254aFr, BN254aG1, BN254aG2>>
        createZKObjectWriter =
            (stream) ->
                new ZKSnarkObjectWriter<BN254aFr, BN254aG1, BN254aG2>(
                    new BN254aBinaryWriter(stream));
    final Function<InputStream, AssignmentReader<BN254aFr, BN254aG1, BN254aG2>>
        createAssignmentReader =
            (stream) ->
                new AssignmentReader<BN254aFr, BN254aG1, BN254aG2>(new BN254aBinaryReader(stream));

    Prover.<BN254aFr, BN254aG1, BN254aG2, BN254aGT, BN254aPairing>run(
        primaryInputSize,
        provingKeyFile,
        assignmentFile,
        outputFile,
        local,
        vkFileOrNull,
        createZKObjectReader,
        createZKObjectWriter,
        createAssignmentReader,
        BN254aFr.ONE,
        new BN254aPairing());
  }

  static void runBLS12_377(
      final int primaryInputSize,
      final String provingKeyFile,
      final String assignmentFile,
      final String outputFile,
      final boolean local,
      final String vkFileOrNull)
      throws IOException {

    System.out.println(" provingKeyFile: " + provingKeyFile);
    System.out.println(" assignmentFile: " + assignmentFile);
    System.out.println(" outputFile: " + outputFile);

    final Function<InputStream, ZKSnarkObjectReader<BLS12_377Fr, BLS12_377G1, BLS12_377G2>>
        createZKSnarkObjectReader =
            (stream) ->
                new ZKSnarkObjectReader<BLS12_377Fr, BLS12_377G1, BLS12_377G2>(
                    new BLS12_377BinaryReader(stream));
    final Function<OutputStream, ZKSnarkObjectWriter<BLS12_377Fr, BLS12_377G1, BLS12_377G2>>
        createZKSnarkObjectWriter =
            (stream) ->
                new ZKSnarkObjectWriter<BLS12_377Fr, BLS12_377G1, BLS12_377G2>(
                    new BLS12_377BinaryWriter(stream));
    final Function<InputStream, AssignmentReader<BLS12_377Fr, BLS12_377G1, BLS12_377G2>>
        createAssignmentReader =
            (stream) ->
                new AssignmentReader<BLS12_377Fr, BLS12_377G1, BLS12_377G2>(
                    new BLS12_377BinaryReader(stream));

    Prover.<BLS12_377Fr, BLS12_377G1, BLS12_377G2, BLS12_377GT, BLS12_377Pairing>run(
        primaryInputSize,
        provingKeyFile,
        assignmentFile,
        outputFile,
        local,
        vkFileOrNull,
        createZKSnarkObjectReader,
        createZKSnarkObjectWriter,
        createAssignmentReader,
        BLS12_377Fr.ONE,
        new BLS12_377Pairing());
  }

  static <
          FrT extends AbstractFieldElementExpanded<FrT>,
          G1T extends AbstractG1<G1T>,
          G2T extends AbstractG2<G2T>,
          GTT extends AbstractGT<GTT>,
          PairingT extends AbstractPairing<G1T, G2T, GTT>>
      void run(
          final int primaryInputSize,
          final String pkFile,
          final String assignmentFile,
          final String proofFile,
          final boolean local,
          final String vkFileOrNull,
          final Function<InputStream, ZKSnarkObjectReader<FrT, G1T, G2T>> createZKSnarkObjectReader,
          final Function<OutputStream, ZKSnarkObjectWriter<FrT, G1T, G2T>>
              createZKSnarkObjectWriter,
          final Function<InputStream, AssignmentReader<FrT, G1T, G2T>> createAssignmenReader,
          final FrT oneFr,
          final PairingT pairing)
          throws IOException {
    var sc = createSparkContext(local);

    // TODO: make these configurable.
    final int numExecutors = 16;
    final int numCores = 2;
    final int numMemory = 16;
    final int numPartitions = 8;
    final StorageLevel storageLevel = StorageLevel.MEMORY_AND_DISK_SER();

    final int batchSize = 32 * 1024;

    // Read proving key
    final var pkStream = new FileInputStream(pkFile);
    final var provingKeyReader = createZKSnarkObjectReader.apply(pkStream);
    final var provingKeyRDD =
        provingKeyReader.readProvingKeyRDD(primaryInputSize, sc, numPartitions, batchSize);
    pkStream.close();

    // Read assignment
    final var assignmentStream = new FileInputStream(assignmentFile);
    final var assignmentReader = createAssignmenReader.apply(assignmentStream);
    final var primFullRDD =
        assignmentReader.readPrimaryFullRDD(primaryInputSize, oneFr, sc, numPartitions, batchSize);
    assignmentStream.close();

    if (!provingKeyRDD.r1cs().isSatisfied(primFullRDD._1, primFullRDD._2)) {
      throw new RuntimeException("assignment does not satisfy r1cs");
    }

    final var config =
        new Configuration(numExecutors, numCores, numMemory, numPartitions, sc, storageLevel);

    final var proof =
        DistributedProver.prove(provingKeyRDD, primFullRDD._1, primFullRDD._2, oneFr, config);
    sc.stop();

    if (vkFileOrNull != null) {
      System.out.println("Checking roof against '" + vkFileOrNull + "' ...");
      System.out.println(" reading verifcation key '" + vkFileOrNull + "' ...");

      final var vkStream = new FileInputStream(vkFileOrNull);
      final var vkReader = createZKSnarkObjectReader.apply(vkStream);
      final var vk = vkReader.readVerificationKey();
      vkStream.close();

      if (!Verifier.verify(vk, primFullRDD._1, proof, pairing, config)) {
        throw new RuntimeException("proof rejected by verifier");
      }
      System.out.println(" proof is VALID");
    }

    final var proofStream = new FileOutputStream(proofFile);
    final var proofWriter = createZKSnarkObjectWriter.apply(proofStream);
    proofWriter.writeProof(proof);
    proofStream.close();
  }
}
