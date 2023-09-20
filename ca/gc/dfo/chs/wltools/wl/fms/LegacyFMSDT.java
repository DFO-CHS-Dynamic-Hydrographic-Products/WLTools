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

  protected Float deltaTMinutes= 0.0;

  /**
   * Default constructor.
   */
  public LegacyFMSDT(final FloatdeltaTMinutes) {
    this.deltaTMinutes= deltaTMinutes;
  }

  final public Float getDeltaTMinutes() {
    return this.deltaTMinutes;
  }

  final public void setDeltaTMinutes(final Float deltaTMinutes) {
    this.deltaTMinutes= deltaTMinutes;
  }
}
