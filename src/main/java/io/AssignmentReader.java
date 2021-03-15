package io;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.fields.AbstractFieldElementExpanded;
import common.Utils;
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

  public Tuple2<Assignment<FieldT>, JavaPairRDD<Long, FieldT>> readPrimaryAuxiliaryRDD(
      final int primaryInputSize,
      final FieldT one,
      final JavaSparkContext sc,
      final int numPartitions,
      final int batchSize)
      throws IOException {
    // Expect that a full batch is a multiple of numPartitions.
    assert batchSize % numPartitions == 0;

    final long numEntries = reader.readLongLE();

    // numBatches = ceil((numEntries + 1) / batchSize)
    //            = (numEntries + 1 + (batchSize - 1)) `div` batchSize
    //            = (numEntries + batchSize) `div` batchSize
    final int numBatches = Math.toIntExact((numEntries + batchSize) / batchSize);

    // Some short-cuts below assume that the primary inputs do not exceed the
    // batch size.
    assert (primaryInputSize + 1) <= batchSize;

    // Load primary inputs.
    final var primary = new Assignment<FieldT>(readPrimaryInput(primaryInputSize, one));

    // Convert to an array (to be used to hold batches), and extend to create a
    // full batch. Subsequent batches will be added to this.
    ArrayList<Tuple2<Long, FieldT>> batch = Utils.convertToPairs(primary.elements());
    long entryIdx = batch.size();

    reader.extendArrayListWithIndicesN(
        batch,
        () -> reader.readFrNoThrow(),
        Math.toIntExact(Math.min(numEntries + 1 - entryIdx, batchSize - batch.size())),
        entryIdx);
    JavaPairRDD<Long, FieldT> fullAssignment = sc.parallelizePairs(batch, numPartitions);
    entryIdx = batch.size();

    // `<=` here, due to the extra FieldT.one added at index 0
    while (entryIdx <= numEntries) {
      final int entriesToRead = Math.toIntExact(Math.min(numEntries + 1 - entryIdx, batchSize));

      // Note that the existing array cannot be re-used, since
      // sc.parallelizePairs seems to retain a reference to it, and read the
      // data asynchroously.
      batch = new ArrayList<Tuple2<Long, FieldT>>(entriesToRead);
      reader.extendArrayListWithIndicesN(
          batch, () -> reader.readFrNoThrow(), entriesToRead, entryIdx);

      fullAssignment = fullAssignment.union(sc.parallelizePairs(batch, numPartitions));

      entryIdx += entriesToRead;
    }

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
