package algebra.curves.barreto_naehrig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryReader;
import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryWriter;
import algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFr;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG1Parameters;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG2Parameters;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class BNBinaryWriterTest {
  @Test
  public void testBinaryWriter() throws IOException {
    var os = new ByteArrayOutputStream();

    // Write 1, -1, 2, -2 in Fr followed by the encodings in G1 and G2.
    {
      var writer = new BN254aBinaryWriter(os);

      writer.writeFr(BN254aFr.ONE);
      writer.writeFr(BN254aFr.ONE.construct(-1));
      writer.writeFr(BN254aFr.ONE.construct(2));
      writer.writeFr(BN254aFr.ONE.construct(-2));
      writer.writeG1(BN254aG1Parameters.ONE);
      writer.writeG1(BN254aG1Parameters.ONE.mul(BN254aFr.ONE.construct(-1)));
      writer.writeG1(BN254aG1Parameters.ONE.mul(BN254aFr.ONE.construct(2)));
      writer.writeG1(BN254aG1Parameters.ONE.mul(BN254aFr.ONE.construct(-2)));
      writer.writeG2(BN254aG2Parameters.ONE);
      writer.writeG2(BN254aG2Parameters.ONE.mul(BN254aFr.ONE.construct(-1)));
      writer.writeG2(BN254aG2Parameters.ONE.mul(BN254aFr.ONE.construct(2)));
      writer.writeG2(BN254aG2Parameters.ONE.mul(BN254aFr.ONE.construct(-2)));
      writer.flush();
      os.flush();
    }

    final var buffer = os.toByteArray();
    assertNotEquals(0, buffer.length);

    final var is = new ByteArrayInputStream(buffer);
    final var reader = new BN254aBinaryReader(is);
    assertEquals(BN254aFr.ONE, reader.readFr());
    assertEquals(BN254aFr.ONE.construct(-1), reader.readFr());
    assertEquals(BN254aFr.ONE.construct(2), reader.readFr());
    assertEquals(BN254aFr.ONE.construct(-2), reader.readFr());
    assertEquals(BN254aG1Parameters.ONE, reader.readG1());
    assertEquals(BN254aG1Parameters.ONE.mul(BN254aFr.ONE.construct(-1)), reader.readG1());
    assertEquals(BN254aG1Parameters.ONE.mul(BN254aFr.ONE.construct(2)), reader.readG1());
    assertEquals(BN254aG1Parameters.ONE.mul(BN254aFr.ONE.construct(-2)), reader.readG1());
    assertEquals(BN254aG2Parameters.ONE, reader.readG2());
    assertEquals(BN254aG2Parameters.ONE.mul(BN254aFr.ONE.construct(-1)), reader.readG2());
    assertEquals(BN254aG2Parameters.ONE.mul(BN254aFr.ONE.construct(2)), reader.readG2());
    assertEquals(BN254aG2Parameters.ONE.mul(BN254aFr.ONE.construct(-2)), reader.readG2());
  }
}
