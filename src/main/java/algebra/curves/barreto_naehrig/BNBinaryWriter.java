package algebra.curves.barreto_naehrig;

import static algebra.curves.barreto_naehrig.BNFields.BNFq;
import static algebra.curves.barreto_naehrig.BNFields.BNFq2;
import static algebra.curves.barreto_naehrig.BNFields.BNFr;

import algebra.curves.barreto_naehrig.abstract_bn_parameters.AbstractBNG1Parameters;
import algebra.curves.barreto_naehrig.abstract_bn_parameters.AbstractBNG2Parameters;
import io.BinaryCurveWriter;
import java.io.IOException;
import java.io.OutputStream;

/** Base class of binary writer for all BN curves. */
public class BNBinaryWriter<
        BNFrT extends BNFr<BNFrT>,
        BNFqT extends BNFq<BNFqT>,
        BNFq2T extends BNFq2<BNFqT, BNFq2T>,
        BNG1T extends BNG1<BNFrT, BNFqT, BNG1T, BNG1ParametersT>,
        BNG2T extends BNG2<BNFrT, BNFqT, BNFq2T, BNG2T, BNG2ParametersT>,
        BNG1ParametersT extends AbstractBNG1Parameters<BNFrT, BNFqT, BNG1T, BNG1ParametersT>,
        BNG2ParametersT extends
            AbstractBNG2Parameters<BNFrT, BNFqT, BNFq2T, BNG2T, BNG2ParametersT>>
    extends BinaryCurveWriter<BNFrT, BNG1T, BNG2T> {

  private final int FrSizeBytes;
  private final int FqSizeBytes;

  public BNBinaryWriter(OutputStream outStream_, BNG1ParametersT g1Params) {
    super(outStream_);
    FrSizeBytes = computeSizeBytes(g1Params.oneFr());
    FqSizeBytes = computeSizeBytes(g1Params.ONE().X.one());
  }

  @Override
  public void writeFr(final BNFrT fr) throws IOException {
    writeBigInteger(fr.toBigInteger(), FrSizeBytes);
  }

  @Override
  public void writeG1(final BNG1T g1) throws IOException {
    final BNG1T g1_affine = g1.toAffineCoordinates();
    writeFq(g1_affine.getX());
    writeFq(g1_affine.getY());
  }

  @Override
  public void writeG2(final BNG2T g2) throws IOException {
    final BNG2T g2_affine = g2.toAffineCoordinates();
    writeFq2(g2_affine.getX());
    writeFq2(g2_affine.getY());
  }

  protected void writeFq(final BNFqT fq) throws IOException {
    writeBigInteger(fq.toBigInteger(), FqSizeBytes);
  }

  protected void writeFq2(final BNFq2T fq2) throws IOException {
    var f2 = fq2.element();
    writeBigInteger(f2.c0.toBigInteger(), FqSizeBytes);
    writeBigInteger(f2.c1.toBigInteger(), FqSizeBytes);
  }
}
