package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.health.vistafhirquery.service.util.BiMap;
import java.util.Map;

/**
 * For clear ids, we have to remove characters that not allowed in FHIR. This produces an escaped
 * sequence string that uses 'x' as the escape character. For commonly occuring characters, like
 * ';', abbreviations are provided to help keep IDs short.
 *
 * <p>For example "H;6929384.839997;14" will become "Hxs6929384.839997xs14
 *
 * <p>Uncommon illegal characters will be encoded with 'x' + ASCII VALUE + 'x'. For example,
 * "NoPlaceLike~" will be "NoPlaceLikex126x".
 */
public class FhirCompliantCharacters {
  /** Illegal character to safe legal abbreviation. */
  private static final BiMap<Character, Character> ABBREVIATIONS =
      new BiMap<>(Map.of(';', 's', ':', 'c', '^', 'g'));

  /** Replace 'x' escaped alternatives with original illegal characters. */
  public static String decodeNonCompliantCharacters(String in) {
    var out = new StringBuilder(in.length());
    boolean escapeSequenceStarted = false;
    var escaped = new StringBuilder(4);
    for (char c : in.toCharArray()) {
      if (c == 'x' && !escapeSequenceStarted) {
        /*
         * We've encountered the start of an escape sequence. We'll either find a single letter
         * abbreviation next, a series of numbers followed by an 'x', or another 'x'.
         */
        escapeSequenceStarted = true;
        escaped.setLength(0);
      } else if (c == 'x') {
        /*
         * An escape sequence is ending. This is either a double escaped 'x' or the end of an
         * uncommon ascii character.
         */
        if (escaped.isEmpty()) {
          /* We found an escaped 'x' because we didn't accumulate anything in between. */
          out.append('x');
        } else {
          /*
           * We found the end of escaped uncommon ascii character. What ever is in between is a
           * character integer value.
           */
          out.append((char) Integer.parseInt(escaped.toString()));
        }
        /* Regardless of what we found, we're done with this escape sequence. */
        escaped.setLength(0);
        escapeSequenceStarted = false;
      } else if (escapeSequenceStarted) {
        /*
         * We've started an escape sequence. We'll either find an abbreviation or part of an
         * uncommon ascii integer value.
         */
        Character abbreviation = ABBREVIATIONS.rightToLeft(c, null);
        if (abbreviation == null) {
          /* This is part of some uncommon ascii character value. */
          escaped.append(c);
        } else {
          /* We found an abbreviation, so this is the end of the escape sequence. */
          out.append(abbreviation);
          escaped.setLength(0);
          escapeSequenceStarted = false;
        }
      } else {
        /*
         * This is not the start, middle, or end of an escape sequence. It's just a normal FHIR
         * safe character.
         */
        out.append(c);
      }
    }
    return out.toString();
  }

  /** Replace illegal characters with 'x' escaped alternatives. */
  public static String encodeNonCompliantCharacters(String in) {
    var out = new StringBuilder((int) (in.length() * 1.20));
    for (char c : in.toCharArray()) {
      if (c == 'x') {
        /* Double escape the escape character. */
        out.append("xx");
      } else if (Character.isLetterOrDigit(c) || c == '.' || c == '-') {
        /* FHIR legal ID characters (except x) pass through unchanged. */
        out.append(c);
      } else {
        /*
         * We need to add an escape marker (x) and the abbreviation if available. If an
         * abbreviation is not available, we'll use the characters ASCII value to encode with a
         * closing escape marker, e.g. ~ become x126x. This allows us to handle any ASCII value
         * that might pop up.
         */
        out.append('x');
        Character escaped = ABBREVIATIONS.leftToRight(c, null);
        if (escaped == null) {
          out.append((int) c);
          out.append('x');
        } else {
          out.append(escaped);
        }
      }
    }
    return out.toString();
  }
}
