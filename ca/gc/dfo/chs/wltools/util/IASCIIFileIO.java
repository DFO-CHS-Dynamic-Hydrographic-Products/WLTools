//package ca.gc.dfo.iwls.fmservice.modeling.util;
package ca.gc.dfo.chs.wltools.util;

/**
 * Created by Gilles Mercier on 2017-12-19.
 */

/**
 * Interface defining constants used for ASCII files IO
 */
public interface IASCIIFileIO extends ITimeMachine {

  /**
   * Generic String splitting String.
   */
  String SPLIT_STRING = "\\s+";

  enum AsciiFormats {

    /**
     * Legacy ODIN DB ASCII file format
     */
    ODIN("     Date & Time    ;   ", "%.3f");

    /**
     * Legacy ODIN DB ASCII file format header
     */
    final String header;

    /**
     * String format for conversion of doubleing point water levels.
     */
    final String numericFormat;

    AsciiFormats(final String header, final String wlNumericFormat) {

      this.header = header;
      this.numericFormat = wlNumericFormat;
    }
  }
}
