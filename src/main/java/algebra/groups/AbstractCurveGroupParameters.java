package algebra.groups;

import java.util.ArrayList;

/**
 * Generic class to represent the parameters defining a group induced by the set
 * of points on an elliptic curve
 */
public abstract class AbstractCurveGroupParameters<GroupT extends AbstractCyclicGroupParameters<GroupT>> {

  // Identity - defined in AbstractCyclicGroupParameters
  // public abstract GroupT ZERO();

  // Generator - defined in AbstractCyclicGroupParameters
  // public abstract GroupT ONE();

  // Underlying field's additive identity
  public abstract GroupT zeroFr();

  // Underlying field's multiplicative identity
  public abstract GroupT oneFr();

  public abstract ArrayList<Integer> fixedBaseWindowTable();
}
