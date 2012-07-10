package de.tuberlin.dima.aim3.oc.util;

/**
 * Utility class taking care of parsing String values into numerical or other
 * values.
 * 
 * Throws exceptions when any parse operation fails.
 * 
 * @author Florian Feigenbutz <florian.feigenbutz@campus.tu-berlin.de>
 * 
 */
public class ParserUtil {

  public static double parseDouble(String input) {
    try {
      return Double.parseDouble(input);
    } catch (Exception e) {
      throw new RuntimeException("Cannot parse value '" + input
          + "' as double value");
    }
  }

  public static int parseInt(String input) {
    try {
      return Integer.parseInt(input);
    } catch (Exception e) {
      throw new RuntimeException("Cannot parse value '" + input
          + "' as integer value");
    }
  }

  public static short parseShort(String input) {
    try {
      return Short.parseShort(input);
    } catch (Exception e) {
      throw new RuntimeException("Cannot parse value '" + input
          + "' as short value");
    }
  }

  public static long parseLong(String input) {
    try {
      return Long.parseLong(input);
    } catch (Exception e) {
      throw new RuntimeException("Cannot parse value '" + input
          + "' as long value");
    }
  }

  public static Boolean parseBoolean(String input) {
    try {
      return Boolean.parseBoolean(input);
    } catch (Exception e) {
      throw new RuntimeException("Cannot parse value '" + input
          + "' as boolean value");
    }
  }

}
