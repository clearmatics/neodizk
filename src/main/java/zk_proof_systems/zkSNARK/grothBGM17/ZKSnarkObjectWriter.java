package zk_proof_systems.zkSNARK.grothBGM17;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.fields.AbstractFieldElementExpanded;
import io.BinaryCurveWriter;
import java.io.IOException;
import zk_proof_systems.zkSNARK.grothBGM17.objects.Proof;

public class ZKSnarkObjectWriter<
    FrT extends AbstractFieldElementExpanded<FrT>,
    G1T extends AbstractG1<G1T>,
    G2T extends AbstractG2<G2T>> {

  final BinaryCurveWriter<FrT, G1T, G2T> writer;

  public ZKSnarkObjectWriter(final BinaryCurveWriter<FrT, G1T, G2T> writer_) {
    writer = writer_;
  }

  public void writeProof(final Proof<G1T, G2T> proof) throws IOException {
    writer.writeG1(proof.gA());
    writer.writeG2(proof.gB());
    writer.writeG1(proof.gC());
  }
}
