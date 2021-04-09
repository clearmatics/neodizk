package algebra.curves.barreto_naehrig.bn254a;

import algebra.curves.barreto_naehrig.BNBinaryWriter;
import algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFq;
import algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFq2;
import algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFr;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG1Parameters;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG2Parameters;
import java.io.OutputStream;

public class BN254aBinaryWriter
    extends BNBinaryWriter<
        BN254aFr, BN254aFq, BN254aFq2, BN254aG1, BN254aG2, BN254aG1Parameters, BN254aG2Parameters> {
  public BN254aBinaryWriter(OutputStream outStream) {
    super(outStream, new BN254aG1Parameters());
  }
}
