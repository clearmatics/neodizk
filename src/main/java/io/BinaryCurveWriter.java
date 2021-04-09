package io;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.fields.AbstractFieldElement;
import algebra.fields.AbstractFieldElementExpanded;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * Base class for binary writers. Handles writing of fixed-width BigInteger values (in big-endian
 * form).
 */
public abstract class BinaryCurveWriter<
        FrT extends AbstractFieldElementExpanded<FrT>,
        G1T extends AbstractG1<G1T>,
        G2T extends AbstractG2<G2T>>
    extends DataOutputStream implements AbstractCurveWriter<FrT, G1T, G2T> {

  protected BinaryCurveWriter(OutputStream outStream_) {
    super(outStream_);
  }

  protected void writeBigInteger(final BigInteger bigint, final int numBytes) throws IOException {
    final byte[] bigintBytes = bigint.toByteArray();
    final int leadingZeros = numBytes - bigintBytes.length;
    if (leadingZeros < 0) {
      throw new IOException("unexpected bigint length");
    }

    byte[] zeroes = new byte[leadingZeros];
    write(zeroes);
    write(bigintBytes);
  }

  protected static <FieldT extends AbstractFieldElement<FieldT>> int computeSizeBytes(
      final FieldT one) {
    final FieldT minusOne = one.zero().sub(one);
    final int sizeBits = minusOne.bitSize();
    return (sizeBits + 7) / 8;
  }
}
