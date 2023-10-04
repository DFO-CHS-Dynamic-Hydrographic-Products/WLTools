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

  private double fallBackCoeff= 0.0; //null;

  private double timeLagMinutes= 0.0; //null;

  /**
   *
   */
  public FMSStationCovarianceConfig(final String tgLocationId,
                                    final double autoRegDeltaTMinutes, final JsonObject stnCovCfgJsonObj) {

    final String mmi= "FMSStationCovarianceConfig main constructor: ";

    slog.info(mmi+"start");

    try {
      stnCovCfgJsonObj.size();

    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    if (!stnCovCfgJsonObj.containsKey(LEGACY_STN_ID_JSON_KEY)) {
      throw new RuntimeException(mmi+"Invalid key -> "+"\""+
                                 LEGACY_STN_ID_JSON_KEY+"\""+ " for stnCovCfgJsonObj JsonObject !!");
    }

    this.stationId= stnCovCfgJsonObj.
      getString(LEGACY_STN_ID_JSON_KEY);

    slog.info(mmi+"this.stationId="+this.stationId);

    if (!stnCovCfgJsonObj.containsKey(LEGACY_STN_COV_TLAG_MINS_JSON_KEY)) {
      throw new RuntimeException(mmi+"Invalid key -> "+"\""+
                                 LEGACY_STN_COV_TLAG_MINS_JSON_KEY+"\""+ " for stnCovCfgJsonObj JsonObject !!");
    }

    // --- Set the time lag in minutes for this covariance item using its value
    //    from the Json config file
    this.timeLagMinutes= stnCovCfgJsonObj.
      getJsonNumber(LEGACY_STN_COV_TLAG_MINS_JSON_KEY).doubleValue();

    // --- Now use the deltaTMinutes of the processed tide gauge location for
    //     the time lag for the auto-regressive part (it is always the case)
    if (this.stationId.equals(tgLocationId)) {

      slog.info(mmi+"Same TG location id.: Using the auto-regressive time lag");
      this.timeLagMinutes= autoRegDeltaTMinutes;
    }

    slog.info(mmi+"this.timeLagMinutes="+this.timeLagMinutes);

    if (!stnCovCfgJsonObj.containsKey(LEGACY_STN_COV_FALLBACK_COEFF_JSON_KEY)) {
      throw new RuntimeException(mmi+"Invalid key -> "+"\""+
                                 LEGACY_STN_COV_FALLBACK_COEFF_JSON_KEY+"\""+ " for stnCovCfgJsonObj JsonObject !!");
    }

    this.fallBackCoeff= stnCovCfgJsonObj.
      getJsonNumber(LEGACY_STN_COV_FALLBACK_COEFF_JSON_KEY).doubleValue();

    slog.info(mmi+"this.fallBackCoeff="+this.fallBackCoeff);

    slog.info(mmi+"end");
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);
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

  final public String getStationId() {
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
    return whoAmI+"{" +
        "stationId='" + this.stationId + '\'' +
        ", timeLagMinutes=" + this.timeLagMinutes +
        ", fallBackCoeff=" + this.fallBackCoeff +
        "}";
  }
}
