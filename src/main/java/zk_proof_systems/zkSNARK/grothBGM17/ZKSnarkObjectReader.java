package zk_proof_systems.zkSNARK.grothBGM17;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.fields.AbstractFieldElementExpanded;
import io.BinaryCurveReader;
import io.R1CSReader;
import java.io.IOException;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import zk_proof_systems.zkSNARK.grothBGM17.objects.ProvingKey;
import zk_proof_systems.zkSNARK.grothBGM17.objects.ProvingKeyRDD;
import zk_proof_systems.zkSNARK.grothBGM17.objects.VerificationKey;

public class ZKSnarkObjectReader<
    FrT extends AbstractFieldElementExpanded<FrT>,
    G1T extends AbstractG1<G1T>,
    G2T extends AbstractG2<G2T>> {

  final BinaryCurveReader<FrT, G1T, G2T> reader;

  public ZKSnarkObjectReader(final BinaryCurveReader<FrT, G1T, G2T> reader_) {
    reader = reader_;
  }

  public ProvingKey<FrT, G1T, G2T> readProvingKey() throws IOException {
    final G1T alphaG1 = reader.readG1();
    final G1T betaG1 = reader.readG1();
    final G2T betaG2 = reader.readG2();
    final G1T deltaG1 = reader.readG1();
    final G2T deltaG2 = reader.readG2();
    final var queryA = reader.readArrayList(() -> reader.readG1NoThrow());
    final var queryB = reader.readSparseVectorAsArrayList(() -> readKnowledgeCommit());
    final var queryH = reader.readArrayList(() -> reader.readG1NoThrow());
    final var deltaABCG1 = reader.readArrayList(() -> reader.readG1NoThrow());
    final var r1cs = (new R1CSReader<FrT, G1T, G2T>(reader)).readR1CS();

    return new ProvingKey<FrT, G1T, G2T>(
        alphaG1, betaG1, betaG2, deltaG1, deltaG2, deltaABCG1, queryA, queryB, queryH, r1cs);
  }

  public VerificationKey<G1T, G2T> readVerificationKey() throws IOException {
    return new VerificationKey<G1T, G2T>(
        reader.readG1(),
        reader.readG2(),
        reader.readG2(),
        reader.readAccumulationVectorAsArrayList(() -> reader.readG1NoThrow()));
  }

  public ProvingKeyRDD<FrT, G1T, G2T> readProvingKeyRDD(
      int primaryInputSize, JavaSparkContext sc, int numPartitions, int batchSize)
      throws IOException {
    final G1T alphaG1 = reader.readG1();
    final G1T betaG1 = reader.readG1();
    final G2T betaG2 = reader.readG2();
    final G1T deltaG1 = reader.readG1();
    final G2T deltaG2 = reader.readG2();
    final JavaPairRDD<Long, G1T> queryA =
        reader.readVectorAsPairRDD(() -> reader.readG1NoThrow(), 0, sc, numPartitions, batchSize);
    final JavaPairRDD<Long, Tuple2<G1T, G2T>> queryB =
        reader.readSparseVectorAsPairRDD(() -> readKnowledgeCommit(), sc, numPartitions, batchSize);
    final JavaPairRDD<Long, G1T> queryH =
        reader.readVectorAsPairRDD(() -> reader.readG1NoThrow(), 0, sc, numPartitions, batchSize);
    final JavaPairRDD<Long, G1T> deltaABCG1 =
        reader.readVectorAsPairRDD(
            () -> reader.readG1NoThrow(), primaryInputSize + 1, sc, numPartitions, batchSize);
    final var r1cs =
        (new R1CSReader<FrT, G1T, G2T>(reader)).readR1CSRDD(sc, numPartitions, batchSize);

    return new ProvingKeyRDD<FrT, G1T, G2T>(
        alphaG1, betaG1, betaG2, deltaG1, deltaG2, deltaABCG1, queryA, queryB, queryH, r1cs);
  }

  /**
   * Specialized tuple reader to switch the order of components (binary data stored with G2 first,
   * whereas the format used here has G1 first).
   */
  protected Tuple2<G1T, G2T> readKnowledgeCommit() {
    try {
      final G2T kcG2 = reader.readG2();
      final G1T kcG1 = reader.readG1();
      return new Tuple2<G1T, G2T>(kcG1, kcG2);
    } catch (IOException e) {
      return null;
    }
  }
}
