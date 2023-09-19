package ca.gc.dfo.chs.wltools.wl.fms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSTime;

//---
//---
//---

/**
 * FM Service master class.
 */
final public class FMSTidalRemnantConfig extends LegacyFMSTime {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.FMSTidalRemnantConfig";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Default constructor.
   */
  public FMSTidalRemnantConfig(final Float maxEps1,  final Float maxEps2
                               final Float tauHours, final Float deltaTMinutes) {

    super(tauHours,deltaTMinutes);

    this.maxEps1= maxEps1;
    this.maxEps2= maxEps2;
  }

  final public getMaxEps1() {
    return this.maxEps1;
  }

  final public Float getMaxEps2() {
    return this.maxEps2;
  }

  final public void setMaxEps1(final Float maxEps1) {
    this.maxEps1= maxEps1;
  }

  final public void setMaxEps2(final Float maxEps2) {
    this.maxEps2= maxEps2
  }

  @Override
  public String toString() {

    return whoAmI+"{" +
        "deltaTMinutes=" + this.getDeltaTMinutes() + ", " +
        "tauHours=" + this.getTauHours() + ", " +
        "maxEps1=" + this.getMaxEps1() + ", " +
        "maxEps2=" + this.getMaxEps2() + "}";
  }

}
