package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;
import java.util.LinkedList;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import ca.gc.dfo.chs.wltools.wl.fms.IFMSConfig;
import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSTime;
import ca.gc.dfo.chs.wltools.wl.fms.FMSStationCovarianceConfig;

//---
//---
//---

/**
 * FM Service master class.
 */
final public class FMSResidualConfig extends LegacyFMSTime implements IFMSConfig {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.FMSResidualConfig";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  private String method;

  private double fallBackError;

  private List<FMSStationCovarianceConfig>
    stationCovCfgList= new LinkedList<FMSStationCovarianceConfig>();

  /**
   *
   */
  public FMSResidualConfig(final double tauHours,
                           final double deltaTMinutes,
                           final String tgLocationId,
                           final JsonObject fmsResCfgJsonObj) {

    super(tauHours,deltaTMinutes);

    final String mmi= "FMSResidualConfig main constructor: ";

    slog.info(mmi+"start");

    try {
      fmsResCfgJsonObj.size();

    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    if (!fmsResCfgJsonObj.containsKey(LEGACY_RESIDUAL_METH_JSON_KEY)) {
      throw new RuntimeException(mmi+"Invalid key -> "+"\""+
                                 LEGACY_RESIDUAL_METH_JSON_KEY+"\""+ " for fmsResCfgJsonObj JsonObject !!");
    }

    this.method= fmsResCfgJsonObj.
      getString(LEGACY_RESIDUAL_METH_JSON_KEY);

    slog.info(mmi+"this.method="+this.method);

    if (!fmsResCfgJsonObj.containsKey(LEGACY_RESIDUAL_FALLBACK_ERR_JSON_KEY)) {
      throw new RuntimeException(mmi+"Invalid key -> "+"\""+
                                 LEGACY_RESIDUAL_FALLBACK_ERR_JSON_KEY+"\""+ " for fmsResCfgJsonObj JsonObject !!");
    }

    this.fallBackError= fmsResCfgJsonObj.
      getJsonNumber(LEGACY_RESIDUAL_FALLBACK_ERR_JSON_KEY).doubleValue();

    slog.info(mmi+"this.fallBackError="+this.fallBackError);

    if (!fmsResCfgJsonObj.containsKey(LEGACY_STN_COV_JSON_KEY)) {
       throw new RuntimeException(mmi+"Invalid key -> "+"\""+
                                  LEGACY_STN_COV_JSON_KEY+"\""+ " for fmsResCfgJsonObj JsonObject !!");
    }

    final JsonArray fmsResCovCfgJsonArray=
      fmsResCfgJsonObj.getJsonArray(LEGACY_STN_COV_JSON_KEY);

    if (fmsResCovCfgJsonArray.size() > 1) {
      throw new RuntimeException(mmi+
        "Legacy FMS covariance calculations have not been tested-validated for more than one covariance item yet!");
    }

    //for (final JsonObject stnCovCfgJsonObj: fmsResCfgJsonObj.getJsonArray()) {
    for (int objIter= 0; objIter < fmsResCovCfgJsonArray.size(); objIter++) {

      slog.info(mmi+"Getting legacy FMS covariance calculations parameters info at index= "+objIter);

      this.stationCovCfgList.add( new FMSStationCovarianceConfig( tgLocationId, this.deltaTMinutes,
                                                                  fmsResCovCfgJsonArray.getJsonObject(objIter)) ); //stnCovCfgJsonObj) );
    }

    slog.info(mmi+"end");
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);
  }

  /**
   *
   */
  //public FMSResidualConfig(final String method,  final Float fallBackError,
  //                         final Float tauHours, final Float deltaTMinutes) {
  //  super(tauHours, deltaTMinutes);
  //  this.method= method;
  //  this.fallBackError= fallBackError;
  //}

  final public String getMethod() {
    return this.method;
  }

  final public double getFallBackError() {
    return this.fallBackError;
  }

  final public List<FMSStationCovarianceConfig> getFMSStationCovarianceConfigList() {
    return this.stationCovCfgList;
  }

  final public void setMethod(final String method) {
    this.method= method;
  }

  final public void setFallBackError(final double fallBackError) {
    this.fallBackError= fallBackError;
  }

  @Override
  final public String toString() {
    return whoAmI+"{" +
        "method=" + this.getMethod() + ", " +
        "tauhours=" + this.getTauHours() + ", " +
        "deltatminutes=" + this.getDeltaTMinutes() + ", " +
        "fallbackerror=" + this.getFallBackError() +
        //"stationCovariance=" + Arrays.toString(this.covariance.toArray()) +
        "}";
  }
}
