package ca.gc.dfo.chs.wltools.wl.fms;

import org.slf4j.Logger;
import javax.json.JsonObject;
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
   *
   */
  private double maxEps1= 0.0;
  private double maxEps2= 0.0;

  /**
   *
   */
  public FMSTidalRemnantConfig(final JsonObject fmsTidalRemnantCfgJsonObj) {

    super( fmsTidalRemnantCfgJsonObj.getJsonNumber(LEGACY_TAU_HOURS_JSON_KEY).doubleValue(),
           fmsTidalRemnantCfgJsonObj.getJsonNumber(LEGACY_DELTA_MINS_JSON_KEY).doubleValue() ) ;

    this.maxEps1= fmsTidalRemnantCfgJsonObj.
     getJsonNumber(LEGACY_TIDAL_REMNANT_EPS1MAX_JSON_KEY).doubleValue();

    this.maxEps2= fmsTidalRemnantCfgJsonObj.
      getJsonNumber(LEGACY_TIDAL_REMNANT_EPS2MAX_JSON_KEY).doubleValue();
  }

  ///**
  // *
  // */
  //public FMSTidalRemnantConfig(final Float maxEps1,  final Float maxEps2
  //                             final Float tauHours, final Float deltaTMinutes) {
  //  super(tauHours,deltaTMinutes);
  //  this.maxEps1= maxEps1;
  //  this.maxEps2= maxEps2;
  //}

  final public double getMaxEps1() {
    return this.maxEps1;
  }

  final public double getMaxEps2() {
    return this.maxEps2;
  }

  final public void setMaxEps1(final double maxEps1) {
    this.maxEps1= maxEps1;
  }

  final public void setMaxEps2(final double maxEps2) {
    this.maxEps2= maxEps2
  }

  @Override
  final public String toString() {

    return whoAmI+"{" +
        "deltaTMinutes=" + this.getDeltaTMinutes() + ", " +
        "tauHours=" + this.getTauHours() + ", " +
        "maxEps1=" + this.getMaxEps1() + ", " +
        "maxEps2=" + this.getMaxEps2() + "}";
  }
}
