package algebra.curves.barreto_lynn_scott;

import algebra.curves.GenericBinaryWriterTest;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377BinaryReader;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377BinaryWriter;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377Fields.BLS12_377Fr;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377G1;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377G2;
import algebra.curves.barreto_lynn_scott.bls12_377.bls12_377_parameters.BLS12_377G1Parameters;
import algebra.curves.barreto_lynn_scott.bls12_377.bls12_377_parameters.BLS12_377G2Parameters;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class BLSBinaryWriterTest
    extends GenericBinaryWriterTest<BLS12_377Fr, BLS12_377G1, BLS12_377G2> {
  @Test
  public void testBinaryWriterBLS12_377() throws IOException {
    testBinaryWriter(
        is -> new BLS12_377BinaryReader(is),
        os -> new BLS12_377BinaryWriter(os),
        BLS12_377Fr.ONE,
        BLS12_377G1Parameters.ONE,
        BLS12_377G2Parameters.ONE);
  }
}
