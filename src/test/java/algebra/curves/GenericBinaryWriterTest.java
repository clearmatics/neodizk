package algebra.curves;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import algebra.fields.AbstractFieldElementExpanded;
import io.BinaryCurveReader;
import io.BinaryCurveWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

public class GenericBinaryWriterTest<
    FrT extends AbstractFieldElementExpanded<FrT>,
    G1T extends AbstractG1<G1T>,
    G2T extends AbstractG2<G2T>> {
  public void testBinaryWriter(
      Function<InputStream, BinaryCurveReader<FrT, G1T, G2T>> mkReader,
      Function<OutputStream, BinaryCurveWriter<FrT, G1T, G2T>> mkWriter,
      FrT frOne,
      G1T g1One,
      G2T g2One)
      throws IOException {
    var os = new ByteArrayOutputStream();

    // Write 1, -1, 2, -2 in Fr followed by the encodings in G1 and G2.
    {
      var writer = mkWriter.apply(os);

      writer.writeFr(frOne);
      writer.writeFr(frOne.construct(-1));
      writer.writeFr(frOne.construct(2));
      writer.writeFr(frOne.construct(-2));
      writer.writeG1(g1One);
      writer.writeG1(g1One.mul(frOne.construct(-1)));
      writer.writeG1(g1One.mul(frOne.construct(2)));
      writer.writeG1(g1One.mul(frOne.construct(-2)));
      writer.writeG2(g2One);
      writer.writeG2(g2One.mul(frOne.construct(-1)));
      writer.writeG2(g2One.mul(frOne.construct(2)));
      writer.writeG2(g2One.mul(frOne.construct(-2)));
      writer.flush();
      os.flush();
    }

    final var buffer = os.toByteArray();
    assertNotEquals(0, buffer.length);

    final var is = new ByteArrayInputStream(buffer);
    final var reader = mkReader.apply(is);
    assertEquals(frOne, reader.readFr());
    assertEquals(frOne.construct(-1), reader.readFr());
    assertEquals(frOne.construct(2), reader.readFr());
    assertEquals(frOne.construct(-2), reader.readFr());
    assertEquals(g1One, reader.readG1());
    assertEquals(g1One.mul(frOne.construct(-1)), reader.readG1());
    assertEquals(g1One.mul(frOne.construct(2)), reader.readG1());
    assertEquals(g1One.mul(frOne.construct(-2)), reader.readG1());
    assertEquals(g2One, reader.readG2());
    assertEquals(g2One.mul(frOne.construct(-1)), reader.readG2());
    assertEquals(g2One.mul(frOne.construct(2)), reader.readG2());
    assertEquals(g2One.mul(frOne.construct(-2)), reader.readG2());
  }
}
