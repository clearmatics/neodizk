package zk_proof_systems.zkSNARK.grothBGM17;

import static org.junit.jupiter.api.Assertions.assertEquals;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377BinaryReader;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377BinaryWriter;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377Fields.BLS12_377Fr;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377G1;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377G2;
import algebra.curves.barreto_lynn_scott.bls12_377.bls12_377_parameters.BLS12_377G1Parameters;
import algebra.curves.barreto_lynn_scott.bls12_377.bls12_377_parameters.BLS12_377G2Parameters;
import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryReader;
import algebra.curves.barreto_naehrig.bn254a.BN254aBinaryWriter;
import algebra.curves.barreto_naehrig.bn254a.BN254aFields.BN254aFr;
import algebra.curves.barreto_naehrig.bn254a.BN254aG1;
import algebra.curves.barreto_naehrig.bn254a.BN254aG2;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG1Parameters;
import algebra.curves.barreto_naehrig.bn254a.bn254a_parameters.BN254aG2Parameters;
import algebra.fields.AbstractFieldElementExpanded;
import io.BinaryCurveReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import zk_proof_systems.zkSNARK.grothBGM17.objects.Proof;

class ZKSnarkObjectWriterTest {

  protected <
          FrT extends AbstractFieldElementExpanded<FrT>,
          G1T extends AbstractG1<G1T>,
          G2T extends AbstractG2<G2T>>
      Proof<G1T, G2T> dummyProof(final FrT oneFr, final G1T oneG1, final G2T oneG2) {
    return new Proof<G1T, G2T>(
        oneG1.mul(oneFr.construct(-3)),
        oneG2.mul(oneFr.construct(-4)),
        oneG1.mul(oneFr.construct(-5)));
  }

  protected <
          FrT extends AbstractFieldElementExpanded<FrT>,
          G1T extends AbstractG1<G1T>,
          G2T extends AbstractG2<G2T>>
      void writeProof(
          final ZKSnarkObjectWriter<FrT, G1T, G2T> zkSnarkObjectWriter,
          final FrT oneFr,
          final G1T oneG1,
          final G2T oneG2)
          throws IOException {
    zkSnarkObjectWriter.writeProof(dummyProof(oneFr, oneG1, oneG2));
  }

  protected <
          FrT extends AbstractFieldElementExpanded<FrT>,
          G1T extends AbstractG1<G1T>,
          G2T extends AbstractG2<G2T>>
      void checkProofReader(
          final BinaryCurveReader<FrT, G1T, G2T> reader,
          final FrT oneFr,
          final G1T oneG1,
          final G2T oneG2)
          throws IOException {
    assertEquals(
        dummyProof(oneFr, oneG1, oneG2),
        new Proof<G1T, G2T>(reader.readG1(), reader.readG2(), reader.readG1()));
  }

  @Test
  public void testProofWriterBN254a() throws IOException {
    final BN254aFr oneFr = BN254aFr.ONE;
    final BN254aG1 oneG1 = BN254aG1Parameters.ONE;
    final BN254aG2 oneG2 = BN254aG2Parameters.ONE;

    var os = new ByteArrayOutputStream();
    var binwriter = new BN254aBinaryWriter(os);
    var writer = new ZKSnarkObjectWriter<BN254aFr, BN254aG1, BN254aG2>(binwriter);

    writeProof(writer, oneFr, oneG1, oneG2);
    binwriter.flush();
    os.flush();
    var buffer = os.toByteArray();

    var reader = new BN254aBinaryReader(new ByteArrayInputStream(buffer));
    checkProofReader(reader, oneFr, oneG1, oneG2);
  }

  @Test
  public void testProofWriterBLS12_377() throws IOException {
    final BLS12_377Fr oneFr = BLS12_377Fr.ONE;
    final BLS12_377G1 oneG1 = BLS12_377G1Parameters.ONE;
    final BLS12_377G2 oneG2 = BLS12_377G2Parameters.ONE;

    var os = new ByteArrayOutputStream();
    var binwriter = new BLS12_377BinaryWriter(os);
    var writer = new ZKSnarkObjectWriter<BLS12_377Fr, BLS12_377G1, BLS12_377G2>(binwriter);

    writeProof(writer, oneFr, oneG1, oneG2);
    binwriter.flush();
    os.flush();
    var buffer = os.toByteArray();

    var reader = new BLS12_377BinaryReader(new ByteArrayInputStream(buffer));
    checkProofReader(reader, oneFr, oneG1, oneG2);
  }
}
