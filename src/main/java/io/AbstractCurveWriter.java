package io;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;
import algebra.fields.AbstractFieldElement;
import java.io.IOException;

/**
 * The interface for a writer class, writing elements of the scalar field and groups G1 and G2 for
 * some curve.
 */
public interface AbstractCurveWriter<
    FrT extends AbstractFieldElement<FrT>,
    G1T extends AbstractG1<G1T>,
    G2T extends AbstractG2<G2T>> {
  public abstract void writeFr(final FrT fr) throws IOException;

  public abstract void writeG1(final G1T g1) throws IOException;

  public abstract void writeG2(final G2T g2) throws IOException;
}
