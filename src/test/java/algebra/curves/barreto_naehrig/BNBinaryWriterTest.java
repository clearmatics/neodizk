package algebra.curves.barreto_naehrig;

import algebra.curves.GenericBinaryWriterTest;
import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryReader;
import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryWriter;
import algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFr;
import algebra.curves.barreto_naehrig.bn254a.BN254aG1;
import algebra.curves.barreto_naehrig.bn254a.BN254aG2;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG1Parameters;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG2Parameters;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class BNBinaryWriterTest extends GenericBinaryWriterTest<BN254aFr, BN254aG1, BN254aG2> {
  @Test
  public void testBinaryWriterBN254a() throws IOException {
    testBinaryWriter(
        is -> new BN254aBinaryReader(is),
        os -> new BN254aBinaryWriter(os),
        BN254aFr.ONE,
        BN254aG1Parameters.ONE,
        BN254aG2Parameters.ONE);
  }
}
