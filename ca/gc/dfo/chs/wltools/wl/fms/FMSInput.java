package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;
import java.time.Instant;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.wltools.wl.fms.FMSConfig;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentType;

//---
//---
//---

/**
 * FM Service master class.
 */
final public class FMSInput extends FMSConfig {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.FMSInput";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  private List<MeasurementCustom> observations= null;

  private List<MeasurementCustom> predictions= null;

  private List<MeasurementCustom> modelForecasts= null;

  private List<MeasurementCustom> qualityControlForecasts= null;

  /**
   *
   */
  //public FMSConfig(final Float stdErrSigma) {
  //}

  public FMSInput(final WLAdjustmentType wlAdjObj) {

    //super(argsMap, wlAdjObj);
    //super(wlAdjObj.getIdentity(),
    //      wlAdjObj.getLocation(),
    //      wlAdjObj.getLocation().getFmsJsonObject());

    super(wlAdjObj.getLocation());

    final String mmi= "FMSInput main constructor: ";

    try {
      wlAdjObj.getLocation();

    } catch (NullPointerException npe){
      throw new RuntimeException(mmi+npe);
    }

    final WLLocation wlLocation= wlAdjObj.getLocation();

    slog.info(mmi+"wlLocation.getIdentity()="+wlLocation.getIdentity());

    this.predictions= wlAdjObj.getLocationPredData();

    this.observations= wlAdjObj.getNearestObsData();

    this.modelForecasts= wlAdjObj.getNearestModelData();

    this.qualityControlForecasts= null;

    final int predDataTimeIntervallSeconds=
      MeasurementCustom.getDataTimeIntervallSeconds(this.predictions);

    final int forecastDataTimeIntervallSeconds=
      MeasurementCustom.getDataTimeIntervallSeconds(this.modelForecasts);

    if (predDataTimeIntervallSeconds > forecastDataTimeIntervallSeconds) {
      throw new RuntimeException(mmi+"Cannot have predDataTimeIntervallSeconds > forecastDataTimeIntervallSecond !!");
    }

    // --- We use the forecastDataTimeIntervallSeconds as the deltaTMinutes
    //     for the legacy FMS deltaTMinutes attribute
    this.deltaTMinutes= ( ((double)forecastDataTimeIntervallSeconds)/ITimeMachine.SECONDS_PER_MINUTE );

    slog.info(mmi+"this.deltaTMinutes="+this.deltaTMinutes);

    final Instant firstPredMcInstant=
      this.predictions.get(0).getEventDate();

    final Instant lastPredMcInstant= this.
      predictions.get(this.predictions.size()-1).getEventDate();

    slog.info(mmi+"firstPredMcInstant="+firstPredMcInstant.toString());
    slog.info(mmi+"lastPredMcInstant="+lastPredMcInstant.toString()+"\n");

    final Instant firstObsMcInstant=
      this.observations.get(0).getEventDate();

    final Instant lastObsMcInstant= this.
      observations.get(this.observations.size()-1).getEventDate();

    slog.info(mmi+"firstObsMcInstant="+firstObsMcInstant.toString());
    slog.info(mmi+"lastObsMcInstant="+lastObsMcInstant.toString()+"\n");

    final Instant firstFMFMcInstant=
      this.modelForecasts.get(0).getEventDate();

    final Instant lastFMFMcInstant= this.
      modelForecasts.get(this.modelForecasts.size()-1).getEventDate();

    slog.info(mmi+"firstFMFMcInstant="+firstFMFMcInstant.toString());
    slog.info(mmi+"lastFMFMcInstant="+lastFMFMcInstant.toString()+"\n");
    slog.info(mmi+"this.referenceTime="+this.referenceTime.toString()+"\n");

    // ---
    final long lastPrdSeconds= lastPredMcInstant.getEpochSecond();
    final long refTimeSeconds= this.referenceTime.getEpochSecond();

    if ( lastPrdSeconds <= refTimeSeconds) {
      throw new RuntimeException(mmi+"Cannot have lastPredSeconds <= refTimeSeconds !!");
    }

    this.durationHoursInFuture=
      (lastPrdSeconds - refTimeSeconds)/ITimeMachine.SECONDS_PER_HOUR;

    slog.info(mmi+"this.durationHoursInFuture="+this.durationHoursInFuture);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    final long firstPrdSeconds= this.
      predictions.get(0).getEventDate().getEpochSecond();

    if ( firstPrdSeconds >= refTimeSeconds) {
      throw new RuntimeException(mmi+"Cannot have firstPrdSeconds >= refTimeSeconds !!");
    }

    final double residualTauHours= (double)
      (refTimeSeconds - firstPrdSeconds)/ITimeMachine.SECONDS_PER_HOUR;

    slog.info(mmi+"residualTauHours="+residualTauHours);

    try {
      wlLocation.getJsonCfgObj();

    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    final JsonObject wllFMSConfigJsonObj= wlLocation.getJsonCfgObj();

    try {
      wllFMSConfigJsonObj.size();

    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    // --- Need to get the super-class name string for
    //     correct key indexing in the JsonObject.
    final String thisClassSimpleName= this.
      getClass().getSuperclass().getSimpleName();

    slog.info(mmi+"thisClassSimpleName="+thisClassSimpleName);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    if (!wllFMSConfigJsonObj.containsKey(thisClassSimpleName)) {
      throw new RuntimeException(mmi+"Invalid key -> "+"\""+
                                 thisClassSimpleName+"\""+ " for wllFMSConfigJsonObj JsonObject !!");
    }

    final JsonObject fmsConfigJsonObj=
      wllFMSConfigJsonObj.getJsonObject(thisClassSimpleName);

    if (!fmsConfigJsonObj.containsKey(LEGACY_RESIDUAL_JSON_KEY)) {
      throw new RuntimeException(mmi+"Invalid key -> "+"\""+
                                 LEGACY_RESIDUAL_JSON_KEY+"\""+ " for fmsConfigJsonObj JsonObject !!");
    }

    // --- Extract the residual config sub-JsonObject from the fmsConfigJsonObj itself
    final JsonObject fmsResidualCfgJsonObj=
      fmsConfigJsonObj.getJsonObject(LEGACY_RESIDUAL_JSON_KEY);

    this.fmsResidualConfig= new FMSResidualConfig(residualTauHours, this.getDeltaTMinutes(),
                                                  wlLocation.getIdentity(), fmsResidualCfgJsonObj);

    // --- Check if we have a tidal remnant config JsonObject to use for this TG location.
    //    (only where the tidal energy is significant)
    if (wllFMSConfigJsonObj.containsKey(LEGACY_TIDAL_REMNANT_JSON_KEY)) {

      slog.info(mmi+"Need to get tidal remnant config for TG location -> "+wlLocation.getIdentity());

      final JsonObject fmsTidalRemnantCfgJsonObj=
        wllFMSConfigJsonObj.getJsonObject(LEGACY_TIDAL_REMNANT_JSON_KEY);

      this.fmsTidalRemnantConfig= new
        FMSTidalRemnantConfig( residualTauHours, this.getDeltaTMinutes(), fmsTidalRemnantCfgJsonObj);

      slog.info(mmi+"Not tested yet! -> Debug exit 0");
      System.exit(0);
    }

    slog.info(mmi+"end");
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);
  }

  // ---
  public List<MeasurementCustom> getObservations() {
    return this.observations;
  }

  public List<MeasurementCustom> getPredictions() {
    return this.predictions;
  }

  public List<MeasurementCustom> getModelForecasts() {
    return this.modelForecasts;
  }

  public List<MeasurementCustom> getQualityControlForecasts() {
    return this.qualityControlForecasts;
  }
}
