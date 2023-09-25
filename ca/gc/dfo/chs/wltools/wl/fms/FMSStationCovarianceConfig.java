package ca.gc.dfo.chs.wltools.wl.fms;

import org.slf4j.Logger;
import javax.json.JsonObject;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.IFMSConfig;

//---
//---
//---

/**
 * 
 */
final public class FMSStationCovarianceConfig implements IFMSConfig {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.FMSStationCovarianceConfig";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  private String stationId= null;

  private double fallBackCoeff = null;

  private double timeLagMinutes= null;

  /**
   *
   */
  public FMSStationCovarianceConfig(final JsonObject stnCovCfgJsonObj) {

    this.stationId= stnCovCfgJsonObj.
      getString(LEGACY_STN_COV_JSON_KEY);

    this.timeLagMinutes= stnCovCfgJsonObj.
      getJsonNumber(LEGACY_STN_COV_TLAG_MINS_JSON_KEY).doubleValue();

    this.fallBackCoeff= stnCovCfgJsonObj.
      getJsonNumber(LEGACY_STN_COV_FALLBACK_COEFF_JSON_KEY).doubleValue();
  }

  ///**
  // *
  // */
  //public StationCovarianceConfig(final String stationId,
  //                               final double timeLagMinutes,
  //                               final double fallBackCoeff ) {
  //  this.stationId= stationId;
  //  this.fallBackCoeff= fallBackCoeff;
  //  this.timeLagMinutes= timeLagMinutes;
  //}

  final String getStationId() {
    return this.stationId;
  }

  final public double getTimeLagMinutes() {
    return this.timeLagMinutes;
  }

  final public double getFallBackCoeff() {
    return this.fallBackCoeff;
  }

  final public void setTimeLagMinutes(final double timeLagMinutes) {
    this.timeLagMinutes= timeLagMinutes;
  }

  //final public void getFallBackCoeff(final Float fallBackCoeff) {
  //  this.fallBackCoeff= fallBackCoeff;
  //}

  @Override
  final public String toString() {
    return whoAmi+"{" +
        "stationId='" + this.stationId + '\'' +
        ", timeLagMinutes=" + this.timeLagMinutes +
        ", fallBackCoeff=" + this.fallBackCoeff +
        "}";
  }
}
