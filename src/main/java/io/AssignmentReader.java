package io;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.fields.AbstractFieldElementExpanded;
import common.PairRDDAggregator;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import relations.objects.Assignment;
import scala.Tuple2;

/** Read assignment objects given for Assignment objects. */
public class AssignmentReader<
    FieldT extends AbstractFieldElementExpanded<FieldT>,
    G1T extends AbstractG1<G1T>,
    G2T extends AbstractG2<G2T>> {

  final BinaryCurveReader<FieldT, G1T, G2T> reader;

  public AssignmentReader(final BinaryCurveReader<FieldT, G1T, G2T> reader_) {
    reader = reader_;
  }

  /**
   * FieldT.one is inserted at position 0, and hence the resulting primary input assignment will
   * have length primaryInputSize + 1.
   */
  public Tuple2<Assignment<FieldT>, Assignment<FieldT>> readPrimaryAuxiliary(
      final int primaryInputSize, final FieldT one) throws IOException {
    // Assignments are written as a single collection. Here, we split it into
    // primary and auxiliary Assignment objects.
    final long numEntries = reader.readLongLE();
    if (numEntries < (long) (primaryInputSize)) {
      throw new IOException(
          "insufficient entries reading Assignment (" + String.valueOf(numEntries) + ")");
    }
    final int auxInputSize = Math.toIntExact(numEntries - primaryInputSize);

    // Read primary values
    final var primary = new Assignment<FieldT>(readPrimaryInput(primaryInputSize, one));

    // Read auxiliary values
    final var aux =
        new Assignment<FieldT>(reader.readArrayListN(() -> reader.readFrNoThrow(), auxInputSize));
    return new Tuple2<Assignment<FieldT>, Assignment<FieldT>>(primary, aux);
  }

  public Tuple2<Assignment<FieldT>, JavaPairRDD<Long, FieldT>> readPrimaryFullRDD(
      final int primaryInputSize,
      final FieldT one,
      final JavaSparkContext sc,
      final int numPartitions,
      final int batchSize)
      throws IOException {
    final long numEntries = reader.readLongLE();

    // Load primary inputs.
    final var primary = new Assignment<FieldT>(readPrimaryInput(primaryInputSize, one));

    // RDD aggregator for the full assignment. Insert all primary input values.
    var rddAggregator = new PairRDDAggregator<Long, FieldT>(sc, numPartitions, batchSize);

    long idx = 0;
    for (var f : primary.elements()) {
      rddAggregator.add(idx++, f);
    }

    // Add all remaining values.  +1 to account for the extra ONE added to the
    // primary inputs.
    for (; idx < numEntries + 1; ++idx) {
      rddAggregator.add(idx, reader.readFr());
    }

    var fullAssignment = rddAggregator.aggregate();
    return new Tuple2<Assignment<FieldT>, JavaPairRDD<Long, FieldT>>(primary, fullAssignment);
  }

  ArrayList<FieldT> readPrimaryInput(final int primaryInputSize, final FieldT one)
      throws IOException {
    final var primaryA = new ArrayList<FieldT>(primaryInputSize + 1);
    primaryA.add(one);
    reader.extendArrayListN(
        primaryA, () -> reader.readFrNoThrow(), Math.toIntExact(primaryInputSize));
    return primaryA;
  }
}
