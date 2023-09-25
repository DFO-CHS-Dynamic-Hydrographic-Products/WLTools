package ca.gc.dfo.chs.wltools.wl.fms;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

//---
//---
//---

/**
 * FM Service master class.
 */
abstract public class LegacyFMSDT {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSDT";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  protected double deltaTMinutes= 0.0;

  /**
   * Default constructor.
   */
  public LegacyFMSDT(final double deltaTMinutes) {
    this.deltaTMinutes= deltaTMinutes;
  }

  final public double getDeltaTMinutes() {
    return this.deltaTMinutes;
  }

  final public void setDeltaTMinutes(final double deltaTMinutes) {
    this.deltaTMinutes= deltaTMinutes;
  }
}
