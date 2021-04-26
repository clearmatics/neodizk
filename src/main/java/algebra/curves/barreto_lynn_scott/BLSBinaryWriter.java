package algebra.curves.barreto_lynn_scott;

import algebra.curves.barreto_lynn_scott.BLSFields.BLSFq;
import algebra.curves.barreto_lynn_scott.BLSFields.BLSFq2;
import algebra.curves.barreto_lynn_scott.BLSFields.BLSFr;
import algebra.curves.barreto_lynn_scott.abstract_bls_parameters.AbstractBLSG1Parameters;
import algebra.curves.barreto_lynn_scott.abstract_bls_parameters.AbstractBLSG2Parameters;
import io.BinaryCurveWriter;
import java.io.IOException;
import java.io.OutputStream;

public class BLSBinaryWriter<
        BLSFrT extends BLSFr<BLSFrT>,
        BLSFqT extends BLSFq<BLSFqT>,
        BLSFq2T extends BLSFq2<BLSFqT, BLSFq2T>,
        BLSG1T extends BLSG1<BLSFrT, BLSFqT, BLSG1T, BLSG1ParametersT>,
        BLSG2T extends BLSG2<BLSFrT, BLSFqT, BLSFq2T, BLSG2T, BLSG2ParametersT>,
        BLSG1ParametersT extends AbstractBLSG1Parameters<BLSFrT, BLSFqT, BLSG1T, BLSG1ParametersT>,
        BLSG2ParametersT extends
            AbstractBLSG2Parameters<BLSFrT, BLSFqT, BLSFq2T, BLSG2T, BLSG2ParametersT>>
    extends BinaryCurveWriter<BLSFrT, BLSG1T, BLSG2T> {

  private final int FrSizeBytes;
  private final int FqSizeBytes;

  public BLSBinaryWriter(OutputStream outStream_, BLSG1ParametersT g1Params) {
    super(outStream_);
    FrSizeBytes = computeSizeBytes(g1Params.oneFr());
    FqSizeBytes = computeSizeBytes(g1Params.ONE().X.one());
  }

  @Override
  public void writeFr(final BLSFrT fr) throws IOException {
    writeBigInteger(fr.toBigInteger(), FrSizeBytes);
  }

  @Override
  public void writeG1(final BLSG1T g1) throws IOException {
    final BLSG1T g1_affine = g1.toAffineCoordinates();
    writeFq(g1_affine.getX());
    writeFq(g1_affine.getY());
  }

  @Override
  public void writeG2(final BLSG2T g2) throws IOException {
    final BLSG2T g2_affine = g2.toAffineCoordinates();
    writeFq2(g2_affine.getX());
    writeFq2(g2_affine.getY());
  }

  protected void writeFq(final BLSFqT fq) throws IOException {
    writeBigInteger(fq.toBigInteger(), FqSizeBytes);
  }

  protected void writeFq2(final BLSFq2T fq2) throws IOException {
    var f2 = fq2.element();
    writeBigInteger(f2.c0.toBigInteger(), FqSizeBytes);
    writeBigInteger(f2.c1.toBigInteger(), FqSizeBytes);
  }
}
