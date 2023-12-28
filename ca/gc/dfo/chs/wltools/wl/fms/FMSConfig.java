package ca.gc.dfo.chs.wltools.wl.fms;

import java.time.Clock;
import java.time.Instant;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.wltools.wl.fms.IFMSConfig;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSDT;
import ca.gc.dfo.chs.wltools.wl.fms.FMSResidualConfig;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentType;

//---
//---
//---

/**
 * FM Service master class.
 */
abstract public class FMSConfig extends LegacyFMSDT implements IFMSConfig {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.FMSConfig";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  private String stationId;

  private HBCoords stationHBCoords;

  protected Instant referenceTime= null;

  // --- The name of the model used for the storm surge forecast signal.
  private String mergeWithFullModelForecast= null;

  // --- The IWLS legacy (hard-coded) config defines
  //     stdErrSigma at 2,0 as a default for all TG
  //     locations.
  private double stdErrSigma= 2.0;

  // --- The time duration in the future of the WLF-QC data.
  //     AND also for the full forecast model data.
  //     TODO: Verify if it is really used, not yer clear if
  //           it is relevant or not (2023-09-20)
  protected double durationHoursInFuture;

  // --- ssfMergeDurationHours is the time duration in hours to use to
  //     merge the FMS WLF-QC to a full (atmos forcings and-or fresh water)
  //     model WL forecast (if any) coming from a numerical model output.
  private int fmfMergeDurationHours;

  // --- fmsResidualConfig object must be defined (i.e. not null)
  //     and set for all TG stations.
  protected FMSResidualConfig fmsResidualConfig= null;

  // --- fmsTidalRemnantConfig is relevant only for TG stations where
  //     the tidal signal (or energy) is significant otherwise it is null
  //     (e.g. in the Great Lakes or upstream the Portneuf TG in the St. Lawrence.
  protected FMSTidalRemnantConfig fmsTidalRemnantConfig= null;

  /**
   *
   */
  //public FMSConfig(final Float stdErrSigma) {
  //}

  //public FMSConfig(final Map<String,String> argsMap, final WLAdjustmentType wlAdjObj ) {
  //public FMSConfig( final WLAdjustmentType wlAdjObj ) {
  //public FMSConfig( final String stationId,
  //                  final HBCoords stationHBCoords,
  //                  final JsonObject fmsConfigJsonObj ) {

  // ---
  public FMSConfig(final WLLocation wlLocation, final Instant refTime) {

    //super();

    final String mmi= "FMSConfig main constructor: ";

    slog.info(mmi+"start");

    // --- WLLocation extends the HBCoords class
    this.stationHBCoords= (HBCoords) wlLocation; //stationHBCoords;

    try {
      wlLocation.getIdentity();

    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    this.stationId= wlLocation.getIdentity(); // stationId; //wlAdjObj.getIdentity();

    slog.info(mmi+"this.stationId="+this.stationId);
    //slog.info(mmi+"this.getClass().getSuperclass().getSimpleName()="+this.getClass().getSuperclass().getSimpleName());

    // --- The Json object we want is using this class name string as a key.
    //     (Need to use getSuperclass() because it is inherited by a child class.
    final String thisClassSimpleName= this.
      getClass().getSuperclass().getSimpleName();

    //final JsonObject wllFMSConfigJsonObj= wlLocation.
    //  getJsonCfgObj().getJsonObject(thisClassSimpleName)

    final JsonObject tgJsonCfgObj= wlLocation.getJsonCfgObj();

    if (!tgJsonCfgObj.containsKey(thisClassSimpleName)) {

      throw new RuntimeException(mmi+"Invalid string key -> "+
                                 thisClassSimpleName+" for tgJsonCfgObj !!");
    }

    final JsonObject wllFMSConfigJsonObj=
       tgJsonCfgObj.getJsonObject(thisClassSimpleName);

    try {
      wllFMSConfigJsonObj.size();

    } catch (NullPointerException npe) {
       throw new RuntimeException(mmi+npe);
    }

    // --- TODO: Add code that calculates the estimated forecast uncertainty
    //this.stdErrSigma= 0.0;

    slog.info(mmi+"Default this.stdErrSigma="+this.stdErrSigma);

    //this.deltaTMinutes= wllFMSConfigJsonObj.getString(LEGACY_DELTA_MINS_JSON_KEY);

    this.mergeWithFullModelForecast=
      wllFMSConfigJsonObj.getString(LEGACY_MERGE_JSON_KEY); //wlAdjObj.getStormSurgeForecastModelName();

    slog.info(mmi+"this.mergeWithFullModelForecast="+this.mergeWithFullModelForecast);

    // --- Get the duration in hours for the merge of the full
    //     model forecast data with the simple QC forecast if
    //     it is defined in the Json config. for this TG
    this.fmfMergeDurationHours=
      IFMS.DEFAULT_FULL_MODEL_FORECAST_MERGE_HOURS;

    if (wllFMSConfigJsonObj.containsKey(LEGACY_MERGE_HOURS_JSON_KEY)) {

      this.fmfMergeDurationHours= wllFMSConfigJsonObj.
        getJsonNumber(LEGACY_MERGE_HOURS_JSON_KEY).intValue();
    }

    // --- Define this.referenceTime with the actual system UTC time
    this.referenceTime= refTime; //Instant.now(Clock.systemUTC());

    // --- Need to define time for debug here need to remove
    //    this when debug and validation will be done.
    //this.referenceTime= Instant.parse("2023-10-04T11:45:00Z");

    slog.info(mmi+"this.referenceTime="+this.referenceTime.toString());

    slog.info(mmi+"end");
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);
  }

  // ---
  final public String getStationId() {
    return this.stationId;
  }

  final public HBCoords getStationHBCoords() {
    return this.stationHBCoords;
  }

  final public FMSResidualConfig getFMSResidualConfig() {
    return this.fmsResidualConfig;
  }

  final public FMSTidalRemnantConfig getFMSTidalRemnantConfig() {
    return this.fmsTidalRemnantConfig;
  }

  final public Instant getReferenceTime() {
    return this.referenceTime;
  }

  final public long getReferenceTimeInSeconds() {
    return this.referenceTime.getEpochSecond();
  }

  final public String getMergeWithFullModelForecast() {
    return this.mergeWithFullModelForecast;
  }

  final public double getDurationHoursInFuture() {
    return this.durationHoursInFuture;
  }

  final public double getStdErrSigma() {
    return this.stdErrSigma;
  }

  final public int getFMFMergeDurationHours() {
    return this.fmfMergeDurationHours;
  }

  //final public void setStationId(final String stationId) {
  //  this.stationId= stationId;
  //}

  //final public void setMergeTo(final String mergeTo) {
  //  this.mergeTo= mergeTo;
  //}

  //final public void setDurationHours(final Float durationHours) {
  //  this.durationHours= durationHours;
  //}

  //final public void setStdErrSigma(final Float stdErrSigma) {
  //  this.stdErrSigma= stdErrSigma;
  //}

  //final public void setMergeDurationHours(final Float mergeDurationHours) {
  //  this.mergeDurationHours= mergeDurationHours;
  //}

  //@Override
  //public String toString() {
  //  return "Forecast{" +
  //      "deltaTMinutes=" + this.getDeltaTMinutes() + ", " +
  //      "durationHours=" + this.getDurationHours() + ", " +
  //      "stdErrSigma=" + this.getStdErrSigma() + ", " +
  //      "mergeTo=" + this.getMergeTo() + ", " +
  //      "mergeDurationHours=" + this.getMergeDurationHours() + "}";
  //}
}
