package de.tuberlin.dima.aim3.oc.util;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Contains common values used within various areas the application.
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public interface Constants {

  public static final Charset CHARSET = Charset.forName("UTF-8");

  public static final Locale LOCALE = Locale.ENGLISH;

  // public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

}
