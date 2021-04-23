package zk_proof_systems.zkSNARK.grothBGM17.objects;

import algebra.curves.AbstractG1;
import algebra.curves.AbstractG2;

/** Groth16-BGM17 Argument */
public class Proof<G1T extends AbstractG1<G1T>, G2T extends AbstractG2<G2T>> {

  private final G1T gA;
  private final G2T gB;
  private final G1T gC;

  public Proof(final G1T _gA, final G2T _gB, final G1T _gC) {
    gA = _gA;
    gB = _gB;
    gC = _gC;
  }

  public G1T gA() {
    return gA;
  }

  public G2T gB() {
    return gB;
  }

  public G1T gC() {
    return gC;
  }

  public boolean equals(final Proof<G1T, G2T> o) {
    return gA.equals(o.gA) && gB.equals(o.gB) && gC.equals(o.gC);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (!(o instanceof Proof<?, ?>)) {
      return false;
    }
    return (equals((Proof<G1T, G2T>) o));
  }
}
