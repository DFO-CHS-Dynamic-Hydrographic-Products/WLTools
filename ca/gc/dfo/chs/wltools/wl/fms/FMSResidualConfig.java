package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;
import org.slf4j.Logger;
import javax.json.JsonObject;
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

  private List<FMSStationCovarianceConfig> stationsCovCfgList= new LinkedList<FMSStationCovarianceConfig>();

  /**
   *
   */
   public FMSResidualConfig(final JsonObject fmsResCfgJsonObj) {

     this.method= fmsResCfgJsonObj.
       getString(LEGACY_RESIDUAL_METH_JSON_KEY);

     this.fallBackError= fmsResCfgJsonObj.
       getJsonNumber(LEGACY_RESIDUAL_FALLBACK_ERR_JSON_KEY).doubleValue();

     //this.stationCovCfg= new

     for (final JsonObject stnCovCfgJsonObj: fmsResCfgJsonObj.getJsonArray()) {
       this.stationCovCfg.add( new FMSStationCovarianceConfig(stnCovCfgJsonObj) );
     }
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
    return this.stationsCovCfgList;
  }

  final public void setMethod(final String method) {
    this.method= method;
  }

  final public void setFallBackError(final double fallBackError) {
    this.fallBackError= fallBackError;
  }

  @Override
  final public String toString() {
    return whoAmi+"{" +
        "method=" + this.getMethod() + ", " +
        "tauhours=" + this.getTauHours() + ", " +
        "deltatminutes=" + this.getDeltaTMinutes() + ", " +
        "fallbackerror=" + this.getFallBackError() +
        //"stationCovariance=" + Arrays.toString(this.covariance.toArray()) +
        "}";
  }
}
