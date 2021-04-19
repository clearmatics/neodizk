/* @file
 *****************************************************************************
 * @author     This file is part of zkspark, developed by SCIPR Lab
 *             and contributors (see AUTHORS).
 * @copyright  MIT license (see LICENSE file)
 *****************************************************************************/

package algebra.fields.mock.fieldparameters;

import algebra.fields.Fp;
import algebra.fields.abstractfieldparameters.AbstractFpParameters;
import java.io.Serializable;
import java.math.BigInteger;

public class LargeFpParameters extends AbstractFpParameters implements Serializable {
  private BigInteger modulus;
  private BigInteger root;
  private Fp multiplicativeGenerator;

  /* The following variables are arbitrarily defined for testing purposes. */
  private BigInteger euler;
  private BigInteger t;
  private BigInteger tMinus1Over2;
  private Fp nqr;
  private Fp nqrTot;

  private Fp ZERO;
  private Fp ONE;

  public BigInteger modulus() {
    if (modulus == null) {
      modulus = new BigInteger("1532495540865888858358347027150309183618765510462668801");
    }

    return modulus;
  }

  public BigInteger root() {
    if (root == null) {
      // dtebbs:
      //
      // This was originally:
      //
      //   root = new BigInteger("6");
      //
      // where root had multiplicative order modulus-1. It has been changed
      // compatible with the other fields, so that root has order s(), as follows:
      //
      //   sage: r = 1532495540865888858358347027150309183618765510462668801
      //   sage: Fr = GF(r)
      //   sage: Fr(6).multiplicative_order()
      //   1532495540865888858358347027150309183618765510462668800
      //   sage: factor(r-1)
      //   2^43 * 5^2 * 7 * 59 * 131 * 12539 * 10272712826778845258848304364007
      //   sage: pow(Fr(6), (r-1)/(2^43))
      //   689649422299708619323149500019547423837788564927818732
      root = new BigInteger("689649422299708619323149500019547423837788564927818732");
    }

    return root;
  }

  public Fp multiplicativeGenerator() {
    if (multiplicativeGenerator == null) {
      multiplicativeGenerator = new Fp("6", this);
    }

    return multiplicativeGenerator;
  }

  public long numBits() {
    return modulus.bitLength();
  }

  public BigInteger euler() {
    if (euler == null) {
      euler = new BigInteger("5");
    }

    return euler;
  }

  public long s() {
    // sage: factor(r-1)
    // 2^43 * 5^2 * 7 * 59 * 131 * 12539 * 10272712826778845258848304364007
    return 43;
  }

  public BigInteger t() {
    if (t == null) {
      t = new BigInteger("5");
    }

    return t;
  }

  public BigInteger tMinus1Over2() {
    if (tMinus1Over2 == null) {
      tMinus1Over2 = new BigInteger("5");
    }

    return tMinus1Over2;
  }

  public Fp nqr() {
    if (nqr == null) {
      nqr = new Fp("6", this);
    }

    return nqr;
  }

  public Fp nqrTot() {
    if (nqrTot == null) {
      nqrTot = new Fp("6", this);
    }

    return nqrTot;
  }

  public Fp ZERO() {
    if (ZERO == null) {
      ZERO = new Fp(BigInteger.ZERO, this);
    }

    return ZERO;
  }

  public Fp ONE() {
    if (ONE == null) {
      ONE = new Fp(BigInteger.ONE, this);
    }

    return ONE;
  }
}
