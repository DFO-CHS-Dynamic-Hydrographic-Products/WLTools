package ca.gc.dfo.chs.wltools.wl.fms;

import org.slf4j.Logger;
import javax.json.JsonObject;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.IFMSConfig;
import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSTime;
import ca.gc.dfo.chs.wltools.wl.fms.StationCovarianceConfig;

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

  private Float fallBackError;

  private List<StationCovarianceConfig> stationCovCfg= new LinkedList<StationCovarianceConfig>();

  /**
   *
   */
   public FMSResidualConfig(final JsonObject fmsResCfgJsonObj) {

     this.method= fmsResCfgJsonObj.
       getString(LEGACY_RESIDUAL_METH_JSON_KEY);

     this.fallBackError= fmsResCfgJsonObj.
       getJsonNumber(LEGACY_RESIDUAL_FALLBACK_ERR_JSON_KEY).getFloat();

     //this.stationCovCfg= new 

     for (final JsonObject stnCovCfgJsonObj: fmsResCfgJsonObj.getJsonArray()) {
       this.stationCovCfg.add( new StationCovarianceConfig(stnCovCfgJsonObj) );
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

  final public getFallBackError() {
    return this.fallBackError;
  }

  final public void setMethod(final String method) {
    this.method= method;
  }

  final public void setFallBackError(final Float fallBackError) {
    this.fallBackError= fallBackError;
  }

  @Override
  public String toString() {
    return whoAmi+"{" +
        "method=" + this.getMethod() + ", " +
        "tauhours=" + this.getTauHours() + ", " +
        "deltatminutes=" + this.getDeltaTMinutes() + ", " +
        "fallbackerror=" + this.getFallBackError() // + ", " +
        //"stationCovariance=" + Arrays.toString(this.covariance.toArray()) +
        "}";
  }
}
