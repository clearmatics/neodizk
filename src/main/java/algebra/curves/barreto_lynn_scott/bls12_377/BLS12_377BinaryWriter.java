package algebra.curves.barreto_lynn_scott.bls12_377;

import algebra.curves.barreto_lynn_scott.BLSBinaryWriter;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377Fields.BLS12_377Fq;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377Fields.BLS12_377Fq2;
import algebra.curves.barreto_lynn_scott.bls12_377.BLS12_377Fields.BLS12_377Fr;
import algebra.curves.barreto_lynn_scott.bls12_377.bls12_377_parameters.BLS12_377G1Parameters;
import algebra.curves.barreto_lynn_scott.bls12_377.bls12_377_parameters.BLS12_377G2Parameters;
import java.io.OutputStream;

public class BLS12_377BinaryWriter
    extends BLSBinaryWriter<
        BLS12_377Fr,
        BLS12_377Fq,
        BLS12_377Fq2,
        BLS12_377G1,
        BLS12_377G2,
        BLS12_377G1Parameters,
        BLS12_377G2Parameters> {
  public BLS12_377BinaryWriter(OutputStream outStream_) {
    super(outStream_, new BLS12_377G1Parameters());
  }
}
