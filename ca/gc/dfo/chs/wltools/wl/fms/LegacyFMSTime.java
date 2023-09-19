package ca.gc.dfo.chs.wltools.wl.fms;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSDT;

//---
//---
//---

/**
 * FM Service master class.
 */
abstract public class LegacyFMSTime extends LegacyFMSDT {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSTime";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  protected Float tauHours= 0.0;

  /**
   * Default constructor.
   */
  public LegacyFMSTime(final Float tauHours, final FloatdeltaTMinutes) {

    super(deltaTMinutes);

    this.tauHours= tauHours;
  }

  final public Float getTauHours() {
    return this.tauHours;
  }

  //final public Float getDeltaTMinutes() {
  //  return this.deltaTMinutes;
  //}
}
