/* @file
 *****************************************************************************
 * @author     This file is part of zkspark, developed by SCIPR Lab
 *             and contributors (see AUTHORS).
 * @copyright  MIT license (see LICENSE file)
 *****************************************************************************/

package algebra.curves.mock.abstract_fake_parameters;

import algebra.curves.mock.FakeGT;
import algebra.fields.mock.fieldparameters.LargeFpParameters;

public abstract class AbstractFakeGTParameters {

  public abstract LargeFpParameters FqParameters();

  public abstract FakeGT ONE();
}
