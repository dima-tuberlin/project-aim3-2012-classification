package de.tuberlin.dima.aim3.oc.input.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import eu.stratosphere.pact.common.type.Value;

public abstract class BaseValue implements Value {

  public BaseValue() {
    super();
  }

  protected void writeUTFNullSafe(DataOutput out, String input)
      throws IOException {
    if (input == null) {
      input = "KFWEUF_$§R%WDF§";
    }
    out.writeUTF(input);
  }

  protected String readUTFNullSafe(DataInput in) throws IOException {
    String value = in.readUTF();
    if (value.equals("KFWEUF_$§R%WDF§")) {
      return null;
    }
    return value;
  }

}