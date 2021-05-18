package algebra.curves.barreto_naehrig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryReader;
import algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFr;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG1Parameters;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG2Parameters;
import io.AbstractCurveReaderTest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

public class BNBinaryReaderTest extends AbstractCurveReaderTest {
  @Test
  public void BN254aBinaryLoaderTest() throws IOException {
    final InputStream in = openTestFile("ec_test_data_alt-bn128.bin");
    final BN254aBinaryReader binReader = new BN254aBinaryReader(in);

    testReaderAgainstData(
        new BN254aFr(1), BN254aG1Parameters.ONE, BN254aG2Parameters.ONE, binReader);
  }

  @Test
  public void BN254aBinaryLoaderIntTypes() throws IOException {
    final var raw =
        new byte[] {
          // Int64 : 1
          0x01,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          // Int32 : 1
          0x01,
          0x00,
          0x00,
          0x00,
          // Int64 : 452022
          (byte) 0xb6,
          (byte) 0xe5,
          0x06,
          0x00,
          0x00,
          0x00,
          0x00,
          0x00,
          // Int32 : 452022
          (byte) 0xb6,
          (byte) 0xe5,
          0x06,
          0x00,
          // Int64 : -1
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          // Int32 : -1
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          // Int64 : 2^32 - 1
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          0x00,
          0x00,
          0x00,
          0x00,
          // Int64 : 255 << 24
          0x00,
          0x00,
          0x00,
          (byte) 0xff,
          0x00,
          0x00,
          0x00,
          0x00,
        };

    final BN254aBinaryReader binReader = new BN254aBinaryReader(new ByteArrayInputStream(raw));
    assertEquals(1l, binReader.readLongLE());
    assertEquals(1, binReader.readIntLE());
    assertEquals(452022l, binReader.readLongLE());
    assertEquals(452022, binReader.readIntLE());
    assertEquals(-1l, binReader.readLongLE());
    assertEquals(-1, binReader.readIntLE());
    assertEquals(0xffffffffl, binReader.readLongLE());
    assertEquals(0xffl << 24, binReader.readLongLE());
  }
}
