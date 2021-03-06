/* @file
 *****************************************************************************
 * @author     This file is part of zkspark, developed by SCIPR Lab
 *             and contributors (see AUTHORS).
 * @copyright  MIT license (see LICENSE file)
 *****************************************************************************/

package algebra.curves.mock.fake_parameters;

import algebra.curves.mock.FakeG2;
import algebra.curves.mock.abstract_fake_parameters.AbstractFakeG2Parameters;
import algebra.fields.mock.fieldparameters.LargeFpParameters;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

public class FakeG2Parameters extends AbstractFakeG2Parameters implements Serializable {

  private LargeFpParameters FqParameters;

  private FakeG2 ZERO;
  private FakeG2 ONE;

  private ArrayList<Integer> fixedBaseWindowTable;

  public LargeFpParameters FqParameters() {
    if (FqParameters == null) {
      FqParameters = new LargeFpParameters();
    }

    return FqParameters;
  }

  public FakeG2 ZERO() {
    if (ZERO == null) {
      ZERO = new FakeG2(BigInteger.ZERO, this);
    }

    return ZERO;
  }

  public FakeG2 ONE() {
    if (ONE == null) {
      ONE = new FakeG2(BigInteger.ONE, this);
    }

    return ONE;
  }

  public ArrayList<Integer> fixedBaseWindowTable() {
    if (fixedBaseWindowTable == null) {
      fixedBaseWindowTable = new ArrayList<>(0);
    }

    return fixedBaseWindowTable;
  }
}
