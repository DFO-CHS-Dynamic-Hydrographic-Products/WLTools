package ca.gc.dfo.chs.wltools.wl.fms;

import org.slf4j.Logger;
import java.time.Instant;
import javax.json.JsonObject;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSDT;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentType;


//---
//---
//---

/**
 * FM Service master class.
 */
abstract public class FMSConfig extends LegacyFMSDT {

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
  private String mergeWithSSFModel= null;

  private double stdErrSigma;

  // --- The time duration in the future of the WLF-QC data.
  //     TODO: Verify if it is really used, not yer clear if
  //           it is relevant or not (2023-09-20)
  //private double durationHours;

  // --- ssfMergeDurationHours is the time duration in hours to use to
  //     merge the FMS WLF-QC to a storm (and-or fresh water) surge WL
  //     forecast (if any) coming from a numerical model result that
  //     includes a storm (or fresh water) surge signal.
  //     (ssf stands for Storm Surge Forecast)
  private double ssfMergeDurationHours;

  // --- fmsResidualConfig object must be defined (i.e. not null)
  //     and set for all TG stations.
  private FMSResidualConfig fmsResidualConfig= null;

  // --- fmsTidalRemnantConfig is relevant only for TG stations where
  //     the tidal signal (or energy) is significant otherwise it is null
  //     (e.g. in the Great Lakes or upstream the Portneuf TG in the St. Lawrence.
  private FMSTidalRemnantConfig fmsTidalRemnantConfig= null;

  /**
   *
   */
  //public FMSConfig(final Float stdErrSigma) {
  //}

  //public FMSConfig(final Map<String,String> argsMap, final WLAdjustmentType wlAdjObj ) {
  //public FMSConfig( final WLAdjustmentType wlAdjObj ) {
  public FMSConfig( final String stationId,
                    final HBCoords stationHBCoords,
                    final JsonObject fmsConfigJsonObj ) {

    final String mmi= "MSConfig( final WLAdjustmentType wlAdjObj) constructor: ";

    this.stationId= stationId; //wlAdjObj.getIdentity();

    this.stationHBCoords= stationHBCoords;

    // --- TODO: Add code that calculates the estimated forecast uncertainty
    this.stdErrSigma= 0.0;

    this.deltaTMinutes= fmsConfigJsonObj.getString(LEGACY_DELTA_MINS_JSON_KEY);

    this.mergeWithSSFModel= fmsConfigJsonObj.getString(LEGACY_MERGE_JSON_KEY); //wlAdjObj.getStormSurgeForecastModelName();

    final long predDataTimeIntervallSeconds= wlAdjObj.
      getDataTimeIntervallSeconds(wlAdjObj.getPredictions());

    final long forecastDataTimeIntervallSeconds= wlAdjObj.
      getDataTimeIntervallSeconds(wlAdjObj.getForecasts());

    if (predDataTimeIntervallSeconds > forecastDataTimeIntervallSeconds) {
      throw new RuntimeException(mmi+"Cannot have predDataTimeIntervallSeconds > forecastDataTimeIntervallSecond !!");
    }

    // --- We use the forecastDataTimeIntervallSeconds as the deltaTMinutes
    //     for the legacy FMS deltaTMinutes attribute
    this.deltaTMinutes= ( (double) forecastDataTimeIntervallSeconds/ITimeMachine.SECONDS_PER_MINUTE ) );

    if (fmsConfigJsonObj.contains(LEGACY_DELTA_MINS_JSON_KEY)) {
      this.deltaTMinutes= fmsConfigJsonObj.
         getJsonNumber(LEGACY_DELTA_MINS_JSON_KEY).doubleValue();
    }

    this.ssfMergeDurationHours=
      IFMS.DEFAULT_STORM_SURGE_FORECAST_MERGE_HOURS;

    if (fmsConfigJsonObj.contains(LEGACY_MERGE_HOURS_JSON_KEY)) {

      this.ssfMergeDurationHours= fmsConfigJsonObj.
         getJsonNumber(LEGACY_MERGE_HOURS_JSON_KEY).doubleValue();
    }

    this.fmsResidualConfig= new
      FMSResidualConfig(fmsConfigJsonObj.getJsonObject(LEGACY_RESIDUAL_JSON_KEY));

    if (fmsConfigJsonObj.contains(LEGACY_TIDAL_REMNANT_JSON_KEY)) {

      this.fmsTidalRemnantConfig= new
        FMSTidalRemnantConfig(fmsConfigJsonObj.getJsonObject(LEGACY_TIDAL_REMNANT_JSON_KEY));
    }

    this.referenceTime= Instant.now(Clock.systemUTC());
  }

  final public String getStationId() {
    return this.stationId;
  }

  final public HBCoords getStationHBCoords() {
    return this.stationHBCoords;
  }

  final public getFMSResidualConfig() {
    return this.FMSResidualConfig;
  }

  final public FMSTidalRemnantConfig getFMSTidalRemnantConfig() {
    return this.FMSTidalRemnantConfig;
  }

  final public Instant getReferenceTime() {
    return this referenceTime;
  }

  final public long getReferenceTimeInSeconds() {
    return this referenceTime.getEpochSecond();
  }

  final public String getMergeWithSSFModel() {
    return this.mergeWithSSFModel;
  }

  //final public double getDurationHours() {
  //  return this.durationHours;
  //}

  final public double getStdErrSigma() {
    return this.stdErrSigma;
  }

  final public double getSsfMergeDurationHours() {
    return this.ssfMergeDurationHours;
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
