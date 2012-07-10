package de.tuberlin.dima.aim3.oc.input.jaxb.conversion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.mediawiki.xml.export_0.CommentType;
import org.mediawiki.xml.export_0.TextType;

import de.tuberlin.dima.aim3.oc.util.Constants;

public abstract class BaseConverterHandler implements NodeConverter {

  public BaseConverterHandler() {
    super();
    dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ",
        Constants.LOCALE);
  }

  protected final DateFormat dateFormatter;

  protected String getStringValue(Object input) {
    String outputValue;
    if (input == null) {
      outputValue = null;
    } else if (input instanceof CommentType) {
      outputValue = ((CommentType) input).getValue().trim();
    } else if (input instanceof TextType) {
      outputValue = ((TextType) input).getValue().trim();
    } else {
      outputValue = input.toString();
    }
    return input != null ? outputValue : null;
  }
}
