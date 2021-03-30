package io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377BinaryReader;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377Fields.BLS12_377Fr;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377G1;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377G2;
import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryReader;
import algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFr;
import algebra.curves.barreto_naehrig.bn254a.BN254aG1;
import algebra.curves.barreto_naehrig.bn254a.BN254aG2;
import algebra.fields.AbstractFieldElementExpanded;
import common.TestWithSparkContext;
import common.Utils;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.spark.api.java.JavaPairRDD;
import org.junit.jupiter.api.Test;
import relations.objects.Assignment;
import scala.Tuple2;

public class AssignmentReaderTest extends TestWithSparkContext {

  protected <FrT extends AbstractFieldElementExpanded<FrT>>
      Tuple2<Assignment<FrT>, Assignment<FrT>> expectPrimaryAuxiliary(FrT one) {
    // Test data has the form [15, -15, 16, -16, 17, -17].
    var prim =
        new Assignment<FrT>(
            new ArrayList<FrT>() {
              {
                add(one);
                add(one.construct(15));
              }
            });
    var aux =
        new Assignment<FrT>(
            new ArrayList<FrT>() {
              {
                add(one.construct(-15));
                add(one.construct(16));
                add(one.construct(-16));
                add(one.construct(17));
                add(one.construct(-17));
              }
            });
    return new Tuple2<Assignment<FrT>, Assignment<FrT>>(prim, aux);
  }

  protected <
          FrT extends AbstractFieldElementExpanded<FrT>,
          G1T extends AbstractG1<G1T>,
          G2T extends AbstractG2<G2T>>
      void testReaderAgainstData(FrT one, AssignmentReader<FrT, G1T, G2T> assignmentReader)
          throws IOException {

    final Tuple2<Assignment<FrT>, Assignment<FrT>> expectPrimAux = expectPrimaryAuxiliary(one);
    final Assignment<FrT> expectPrimary = expectPrimAux._1;
    final Assignment<FrT> expectAuxiliary = expectPrimAux._2;

    final Tuple2<Assignment<FrT>, Assignment<FrT>> primAux =
        assignmentReader.readPrimaryAuxiliary(1, one);
    final Assignment<FrT> primary = primAux._1;
    final Assignment<FrT> auxiliary = primAux._2;

    assertEquals(expectPrimary, primary);
    assertEquals(expectAuxiliary, auxiliary);
  }

  protected <
          FrT extends AbstractFieldElementExpanded<FrT>,
          G1T extends AbstractG1<G1T>,
          G2T extends AbstractG2<G2T>>
      void testReaderAgainstDataRDD(
          final FrT one,
          final AssignmentReader<FrT, G1T, G2T> assignmentReader,
          final int numPartitions,
          final int batchSize)
          throws IOException {

    final Tuple2<Assignment<FrT>, Assignment<FrT>> expectPrimAux = expectPrimaryAuxiliary(one);
    final Assignment<FrT> expectPrimary = expectPrimAux._1;
    final Assignment<FrT> expectAuxiliary = expectPrimAux._2;
    final ArrayList<FrT> expectFullArray = new ArrayList<FrT>(expectPrimary.elements());
    expectFullArray.addAll(expectAuxiliary.elements());
    final Assignment<FrT> expectFull = new Assignment<FrT>(expectFullArray);

    // Read the distributed version of the assignment.
    final Tuple2<Assignment<FrT>, JavaPairRDD<Long, FrT>> primFull =
        assignmentReader.readPrimaryFullRDD(1, one, getSparkContext(), numPartitions, batchSize);
    final Assignment<FrT> primary = primFull._1;
    final JavaPairRDD<Long, FrT> full = primFull._2;

    // Convert auxiliary to an Assignment object, and compare to the expected
    // data.
    final ArrayList<Tuple2<Long, FrT>> fullA = new ArrayList<Tuple2<Long, FrT>>(full.collect());
    final Assignment<FrT> fullLocal =
        new Assignment<FrT>(Utils.convertFromPairs(fullA, fullA.size()));

    System.out.println(" expectPrimary: " + String.valueOf(expectPrimary.elements()));
    System.out.println(" primary: " + String.valueOf(primary.elements()));
    System.out.println(" expectFull: " + String.valueOf(expectFull.elements()));
    System.out.println(" fullA: " + String.valueOf(fullA));
    System.out.println(" fullLocal: " + String.valueOf(fullLocal.elements()));

    assertEquals(expectPrimary, primary);
    assertEquals(expectFull, fullLocal);
  }

  @Test
  public void testAssignmentReaderBN254a() throws IOException {
    final var in = openTestFile("assignment_alt-bn128.bin");
    final var binReader = new BN254aBinaryReader(in);
    testReaderAgainstData(
        BN254aFr.ONE, new AssignmentReader<BN254aFr, BN254aG1, BN254aG2>(binReader));
  }

  @Test
  public void testAssignmentReaderBLS12_377() throws IOException {
    final var in = openTestFile("assignment_bls12-377.bin");
    final var binReader = new BLS12_377BinaryReader(in);
    testReaderAgainstData(
        BLS12_377Fr.ONE, new AssignmentReader<BLS12_377Fr, BLS12_377G1, BLS12_377G2>(binReader));
  }

  @Test
  public void testAssignmentReaderRDDBN254a_8_8() throws IOException {
    final var in = openTestFile("assignment_alt-bn128.bin");
    final var binReader = new BN254aBinaryReader(in);

    testReaderAgainstDataRDD(
        BN254aFr.ONE, new AssignmentReader<BN254aFr, BN254aG1, BN254aG2>(binReader), 8, 8);
  }

  @Test
  public void testAssignmentReaderRDDBN254a_2_2() throws IOException {
    final var in = openTestFile("assignment_alt-bn128.bin");
    final var binReader = new BN254aBinaryReader(in);

    testReaderAgainstDataRDD(
        BN254aFr.ONE, new AssignmentReader<BN254aFr, BN254aG1, BN254aG2>(binReader), 2, 2);
  }

  @Test
  public void testAssignmentReaderRDDBN254a_2_4() throws IOException {
    final var in = openTestFile("assignment_alt-bn128.bin");
    final var binReader = new BN254aBinaryReader(in);

    testReaderAgainstDataRDD(
        BN254aFr.ONE, new AssignmentReader<BN254aFr, BN254aG1, BN254aG2>(binReader), 2, 4);
  }

  @Test
  public void testAssignmentReaderRDDBLS12_377_8_8() throws IOException {
    final var in = openTestFile("assignment_bls12-377.bin");
    final var binReader = new BLS12_377BinaryReader(in);

    testReaderAgainstDataRDD(
        BLS12_377Fr.ONE,
        new AssignmentReader<BLS12_377Fr, BLS12_377G1, BLS12_377G2>(binReader),
        8,
        8);
  }

  @Test
  public void testAssignmentReaderRDDBLS12_377_2_2() throws IOException {
    final var in = openTestFile("assignment_bls12-377.bin");
    final var binReader = new BLS12_377BinaryReader(in);

    testReaderAgainstDataRDD(
        BLS12_377Fr.ONE,
        new AssignmentReader<BLS12_377Fr, BLS12_377G1, BLS12_377G2>(binReader),
        2,
        2);
  }

  @Test
  public void testAssignmentReaderRDDBLS12_377_2_4() throws IOException {
    final var in = openTestFile("assignment_bls12-377.bin");
    final var binReader = new BLS12_377BinaryReader(in);

    testReaderAgainstDataRDD(
        BLS12_377Fr.ONE,
        new AssignmentReader<BLS12_377Fr, BLS12_377G1, BLS12_377G2>(binReader),
        2,
        4);
  }
}
