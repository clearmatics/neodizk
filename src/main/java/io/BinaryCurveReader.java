package io;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.fields.AbstractFieldElement;
import algebra.fields.AbstractFieldElementExpanded;
import common.PairRDDAggregator;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.function.Supplier;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 * Base class for binary readers. Handles reading fixed-width BigInteger values (in big-endian
 * form).
 */
public abstract class BinaryCurveReader<
        FrT extends AbstractFieldElementExpanded<FrT>,
        G1T extends AbstractG1<G1T>,
        G2T extends AbstractG2<G2T>>
    extends DataInputStream implements AbstractCurveReader<FrT, G1T, G2T> {

  protected BinaryCurveReader(InputStream inStream_) {
    super(inStream_);
  }

  /**
   * Version of readFr which returns null instead of throwing on error (for use in functional-style
   * function composition.
   */
  public FrT readFrNoThrow() {
    try {
      return readFr();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Version of readG1 which returns null instead of throwing on error (for use in functional-style
   * function composition.
   */
  public G1T readG1NoThrow() {
    try {
      return readG1();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Version of readG2 which returns null instead of throwing on error (for use in functional-style
   * function composition.
   */
  public G2T readG2NoThrow() {
    try {
      return readG2();
    } catch (IOException e) {
      return null;
    }
  }

  protected BigInteger readBigInteger(final int numBytes) throws IOException {
    final byte[] bytes = readNBytes(numBytes);
    if (bytes.length != numBytes) {
      throw new IOException("unexpected end of stream");
    }
    return new BigInteger(bytes);
  }

  public int readIntLE() throws IOException {
    final long vBE = (long) readInt() & 0xffffffffl;
    return (int)
        (((vBE & 0xffl) << 24)
            | ((vBE << 8) & 0x00ff0000l)
            | ((vBE >> 8) & 0x0000ff00l)
            | ((vBE >> 24) & 0xffl));
  }

  public long readLongLE() throws IOException {
    final long iL = ((long) readIntLE()) & 0xffffffffl;
    final long iH = ((long) readIntLE()) & 0xffffffffl;
    return (iH << 32) | iL;
  }

  public <T1, T2> Tuple2<T1, T2> readTuple2(
      final Supplier<T1> reader1, final Supplier<T2> reader2) {
    return new Tuple2<T1, T2>(reader1.get(), reader2.get());
  }

  public <T> ArrayList<T> readArrayList(final Supplier<T> reader) throws IOException {
    final long size = readLongLE();
    return readArrayListN(reader, Math.toIntExact(size));
  }

  public <T> ArrayList<T> readArrayListN(final Supplier<T> reader, final int numElements)
      throws IOException {
    ArrayList<T> elements = new ArrayList<T>(numElements);
    for (long i = 0; i < numElements; ++i) {
      // It seems exceptions are not supported through Function / Supplier,
      // etc. Workaround using nulls.
      T value = reader.get();
      if (value == null) {
        throw new IOException("failed to read element");
      }
      elements.add(value);
    }
    return elements;
  }

  public <T> void extendArrayListN(
      final ArrayList<T> elements, final Supplier<T> reader, final int numElements)
      throws IOException {
    elements.ensureCapacity(elements.size() + numElements);
    for (long i = 0; i < numElements; ++i) {
      // Work-around for exceptions in Suppliers.  See readArrayListN.
      T value = reader.get();
      if (value == null) {
        throw new IOException("failed to read element");
      }
      elements.add(value);
    }
  }

  public <T> void extendArrayListWithIndicesN(
      final ArrayList<Tuple2<Long, T>> elements,
      final Supplier<T> reader,
      final int numElements,
      long startIdx)
      throws IOException {
    elements.ensureCapacity(elements.size() + numElements);
    for (long i = 0; i < numElements; ++i) {
      // Work-around for exceptions in Suppliers.  See readArrayListN.
      T value = reader.get();
      if (value == null) {
        throw new IOException("failed to read element");
      }
      elements.add(new Tuple2<Long, T>(startIdx++, value));
    }
  }

  public <T> ArrayList<T> readArrayListNoThrow(final Supplier<T> reader) {
    try {
      return readArrayList(reader);
    } catch (IOException e) {
      return null;
    }
  }

  /** Read a sparse vector into an ArrayList, where missing values are `null`. */
  public <T> ArrayList<T> readSparseVectorAsArrayList(final Supplier<T> reader) throws IOException {
    return readSparseVectorAsArrayList(reader, 0);
  }

  /**
   * Read a sparse vector, adding some offset to the indices of values in the ArrayList. i.e. offset
   * 0 will read in the expected way (entry with index 0 in the sparse vector appears at index 0 in
   * the resulting ArrayList), and offset 8 will place entry with index 0 in the sparse vector at
   * index 8 in the resulting ArrayList.
   */
  public <T> ArrayList<T> readSparseVectorAsArrayList(final Supplier<T> reader, final int offset)
      throws IOException {
    assert (offset >= 0);
    readLongLE(); // skip unused domain_size
    final long numEntries = readLongLE();
    var entries = new ArrayList<T>(Math.toIntExact(numEntries + offset));
    for (long i = 0; i < numEntries; ++i) {
      final int idx = Math.toIntExact(readLongLE()) + offset;
      final T val = reader.get();

      // Extend the array list with `null` entries, or insert at the given
      // index.
      if (idx < entries.size()) {
        entries.set(idx, val);
      } else {
        while (entries.size() < idx) {
          entries.add(null);
        }
        entries.add(val);
      }
    }

    return entries;
  }

  public <T> ArrayList<T> readAccumulationVectorAsArrayList(final Supplier<T> reader)
      throws IOException {
    // An accumulation_vector is a `first` element, followed by a sparse vector.
    final T first = reader.get();
    ArrayList<T> elements = readSparseVectorAsArrayList(reader, 1);
    assert (elements.get(0) == null);

    // Insert `first` into the ArrayList created by sparse array, and return.
    elements.set(0, first);
    return elements;
  }

  /**
   * Read a vector of values into a JavaPairRDD<Long, T>, where indices (the keys) are generated as
   * the implicit indices in the list, with some offset added.
   */
  public <T> JavaPairRDD<Long, T> readVectorAsPairRDD(
      final Supplier<T> reader,
      final long offset,
      final JavaSparkContext sc,
      final int numPartitions,
      final int batchSize)
      throws IOException {
    final long numEntries = readLongLE();
    final var aggregator = new PairRDDAggregator<Long, T>(sc, numPartitions, batchSize);
    for (long i = 0; i < numEntries; ++i) {
      aggregator.add(i + offset, reader.get());
    }
    return aggregator.aggregate();
  }

  public <T> JavaPairRDD<Long, T> readSparseVectorAsPairRDD(
      final Supplier<T> reader,
      final JavaSparkContext sc,
      final int numPartitions,
      final int batchSize)
      throws IOException {
    readLongLE(); // skip unused domain_size
    final long numEntries = readLongLE();
    final var aggregator = new PairRDDAggregator<Long, T>(sc, numPartitions, batchSize);
    for (long i = 0; i < numEntries; ++i) {
      aggregator.add(readLongLE(), reader.get());
    }
    return aggregator.aggregate();
  }

  protected static <FieldT extends AbstractFieldElement<FieldT>> int computeSizeBytes(
      final FieldT one) {
    final FieldT minusOne = one.zero().sub(one);
    final int sizeBits = minusOne.bitSize();
    return (sizeBits + 7) / 8;
  }
}
