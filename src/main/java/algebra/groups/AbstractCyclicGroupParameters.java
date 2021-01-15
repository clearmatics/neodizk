package algebra.groups;

/**
 * Generic class to represent the parameters defining a cyclic group
 */
public abstract class AbstractCyclicGroupParameters<GroupT extends AbstractCyclicGroupParameters<GroupT>> {
  // Generator
  public abstract GroupT ONE();

  // Identity
  public abstract GroupT ZERO();

  // TODO: Add: the order etc.
}
