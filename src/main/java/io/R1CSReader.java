package io;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.fields.AbstractFieldElementExpanded;
import common.PairRDDAggregator;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.spark.api.java.JavaSparkContext;
import relations.objects.LinearCombination;
import relations.objects.LinearTerm;
import relations.objects.R1CSConstraint;
import relations.objects.R1CSConstraints;
import relations.objects.R1CSConstraintsRDD;
import relations.r1cs.R1CSRelation;
import relations.r1cs.R1CSRelationRDD;

/**
 * Read binary encoded R1CS objects from a stream.
 *
 * <p>(TODO remove group parameters. These are only required for the binary reader - there is
 * currently only a single reader interface for both field and group elements).
 */
public class R1CSReader<
    FieldT extends AbstractFieldElementExpanded<FieldT>,
    G1T extends AbstractG1<G1T>,
    G2T extends AbstractG2<G2T>> {

  final BinaryCurveReader<FieldT, G1T, G2T> reader;

  public R1CSReader(final BinaryCurveReader<FieldT, G1T, G2T> reader_) {
    reader = reader_;
  }

  public R1CSRelation<FieldT> readR1CS() throws IOException {
    final int num_primary_inputs = Math.toIntExact(reader.readLongLE());
    final int num_auxiliary_inputs = Math.toIntExact(reader.readLongLE());
    final R1CSConstraints<FieldT> constraints = readConstraints();
    return new R1CSRelation<FieldT>(constraints, num_primary_inputs + 1, num_auxiliary_inputs);
  }

  public R1CSRelationRDD<FieldT> readR1CSRDD(JavaSparkContext sc, int numPartitions, int batchSize)
      throws IOException {
    final int numPrimaryInputs = Math.toIntExact(reader.readLongLE());
    final long numAuxiliaryInputs = reader.readLongLE();
    var constraints = readConstraintsRDD(sc, numPartitions, batchSize);
    return new R1CSRelationRDD<FieldT>(constraints, numPrimaryInputs + 1, numAuxiliaryInputs);
  }

  protected R1CSConstraints<FieldT> readConstraints() throws IOException {
    // Note, the non-throwing version is used here since support for exceptions
    // in Function, Supplier, etc is so bad.
    return new R1CSConstraints<FieldT>(reader.readArrayList(() -> readConstraintNoThrow()));
  }

  protected R1CSConstraintsRDD<FieldT> readConstraintsRDD(
      JavaSparkContext sc, int numPartitions, int batchSize) throws IOException {
    var As = new PairRDDAggregator<Long, LinearTerm<FieldT>>(sc, numPartitions, batchSize);
    var Bs = new PairRDDAggregator<Long, LinearTerm<FieldT>>(sc, numPartitions, batchSize);
    var Cs = new PairRDDAggregator<Long, LinearTerm<FieldT>>(sc, numPartitions, batchSize);

    // Iterate through all constraints, adding terms to A, B and C as
    // appropriate.
    final long numConstraints = reader.readLongLE();
    for (long constraintIdx = 0; constraintIdx < numConstraints; ++constraintIdx) {
      var constraint = readConstraint();
      addTermsRDD(As, constraintIdx, constraint.A());
      addTermsRDD(Bs, constraintIdx, constraint.B());
      addTermsRDD(Cs, constraintIdx, constraint.C());
    }

    return new R1CSConstraintsRDD<FieldT>(
        As.aggregate(), Bs.aggregate(), Cs.aggregate(), numConstraints);
  }

  protected R1CSConstraint<FieldT> readConstraint() throws IOException {
    return new R1CSConstraint<FieldT>(
        readLinearCombination(), readLinearCombination(), readLinearCombination());
  }

  protected void addTermsRDD(
      final PairRDDAggregator<Long, LinearTerm<FieldT>> terms,
      final long constraintIdx,
      final LinearCombination<FieldT> lc) {
    for (var term : lc.terms()) {
      terms.add(constraintIdx, term);
    }
  }

  protected LinearCombination<FieldT> readLinearCombination() throws IOException {
    final int size = reader.readIntLE();
    var terms = new ArrayList<LinearTerm<FieldT>>(size);
    for (int i = 0; i < size; ++i) {
      LinearTerm<FieldT> value = readLinearTerm();
      if (value == null) {
        return null;
      }
      terms.add(value);
    }
    return new LinearCombination<FieldT>(terms);
  }

  protected LinearTerm<FieldT> readLinearTerm() throws IOException {
    final long idx = reader.readLongLE();
    final FieldT coeff = reader.readFr();
    return new LinearTerm<FieldT>(idx, coeff);
  }

  protected R1CSConstraint<FieldT> readConstraintNoThrow() {
    try {
      return readConstraint();
    } catch (IOException e) {
      System.out.println("ERROR: failed to read constraint");
      return null;
    }
  }
}
