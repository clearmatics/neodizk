package algebra.curves.barreto_naehrig;

import algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFr;
import algebra.curves.barreto_naehrig.bn254a.BN254aG1;
import algebra.curves.barreto_naehrig.bn254a.BN254aG2;
import algebra.curves.barreto_naehrig.bn254a.BN254aGT;
import algebra.curves.barreto_naehrig.bn254a.BN254aPairing;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG1Parameters;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG2Parameters;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aGTParameters;
import algebra.curves.barreto_naehrig.bn254b.BN254bFields.BN254bFr;
import algebra.curves.barreto_naehrig.bn254b.BN254bG1;
import algebra.curves.barreto_naehrig.bn254b.BN254bG2;
import algebra.curves.barreto_naehrig.bn254b.BN254bGT;
import algebra.curves.barreto_naehrig.bn254b.BN254bPairing;
import algebra.curves.barreto_naehrig.bn254b.bn254b_parameters.BN254bG1Parameters;
import algebra.curves.barreto_naehrig.bn254b.bn254b_parameters.BN254bG2Parameters;
import algebra.curves.barreto_naehrig.bn254b.bn254b_parameters.BN254bGTParameters;
import org.junit.jupiter.api.Test;

import algebra.curves.GenericBilinearityTest;

public class BNBilinearityTest extends GenericBilinearityTest {
  @Test
  public void BN254aTest() {
    final BN254aG1 g1One = BN254aG1Parameters.ONE;
    final BN254aG2 g2One = BN254aG2Parameters.ONE;
    final BN254aGT gtOne = BN254aGTParameters.ONE;
    final BN254aFr fieldFactory = new BN254aFr(6);
    final BN254aPairing pairing = new BN254aPairing();

    final BN254aG1 P = g1One.mul(fieldFactory.random(5L, null));
    final BN254aG2 Q = g2One.mul(fieldFactory.random(6L, null));

    PairingTest(P, Q, gtOne, fieldFactory, pairing);
    PairingTest(g1One, g2One, gtOne, fieldFactory, pairing);
  }

  @Test
  public void BN254bTest() {
    final BN254bG1 g1One = BN254bG1Parameters.ONE;
    final BN254bG2 g2One = BN254bG2Parameters.ONE;
    final BN254bGT gtOne = BN254bGTParameters.ONE;
    final BN254bFr fieldFactory = new BN254bFr(6);
    final BN254bPairing pairing = new BN254bPairing();

    final BN254bG1 P = g1One.mul(fieldFactory.random(5L, null));
    final BN254bG2 Q = g2One.mul(fieldFactory.random(6L, null));

    PairingTest(P, Q, gtOne, fieldFactory, pairing);
    PairingTest(g1One, g2One, gtOne, fieldFactory, pairing);
  }
}