package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;
import java.time.Instant;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
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

    final String mmi= "FMSInput man constructor: ";

    this.predictions= wlAdjObj.getLocationPredData();

    this.observations= wlAdjObj.getNearestObsData();

    this.modelForecasts= wlAdjObj.getNearestModelData();

    this.qualityControlForecasts= null;

    final long predDataTimeIntervallSeconds=
      MeasurementCustom.getDataTimeIntervallSeconds(this.predictions);

    final long forecastDataTimeIntervallSeconds=
      MeasurementCustom.getDataTimeIntervallSeconds(this.modelForecasts);

    if (predDataTimeIntervallSeconds > forecastDataTimeIntervallSeconds) {
      throw new RuntimeException(mmi+"Cannot have predDataTimeIntervallSeconds > forecastDataTimeIntervallSecond !!");
    }

    // --- We use the forecastDataTimeIntervallSeconds as the deltaTMinutes
    //     for the legacy FMS deltaTMinutes attribute
    this.deltaTMinutes= ( ((double)forecastDataTimeIntervallSeconds)/ITimeMachine.SECONDS_PER_MINUTE );

    slog.info(mmi+"this.deltaTMinutes="+this.deltaTMinutes);

    final Instant lastPredMcInstant=
      this.predictions.get(this.predictions.size()-1).getEventDate();

    slog.info(mmi+"lastPredMcInstant="+lastPredMcInstant.toString());
    slog.info(mmi+"this.referenceTime="+this.referenceTime.toString());

    // ---
    final long lastPrdSeconds= lastPredMcInstant.getEpochSecond();
    final long refTimeSeconds= this.referenceTime.getEpochSecond();

    if ( lastPrdSeconds <= refTimeSeconds) {
      throw new RuntimeException(mmi+"Cannot have lastPredSeconds <= refTimeSeconds !!");
    }

    this.durationHoursInFuture=
      (lastPrdSeconds - refTimeSeconds)/ITimeMachine.SECONDS_PER_HOUR;

    slog.info(mmi+"this.durationHoursInFuture="+this.durationHoursInFuture);
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    final long firstPrdSeconds= this.
      predictions.get(0).getEventDate().getEpochSecond();

    if ( firstPrdSeconds >= refTimeSeconds) {
      throw new RuntimeException(mmi+"Cannot have firstPrdSeconds >= refTimeSeconds !!");
    }

    final double residualTauHours= (double)
      (refTimeSeconds - firstPrdSeconds)/ITimeMachine.SECONDS_PER_HOUR;

    //final double tidalTauHours=

    final JsonObject wllFMSConfigJsonObj=
      wlAdjObj.getLocation().getJsonCfgObj();

    final JsonObject fmsResidualCfgJsonObj=
      wllFMSConfigJsonObj.getJsonObject(LEGACY_RESIDUAL_JSON_KEY);

    this.fmsResidualConfig=
      new FMSResidualConfig(residualTauHours, this.getDeltaTMinutes(), fmsResidualCfgJsonObj);

    if (wllFMSConfigJsonObj.containsKey(LEGACY_TIDAL_REMNANT_JSON_KEY)) {

     final JsonObject fmsTidalRemnantCfgJsonObj=
       wllFMSConfigJsonObj.getJsonObject(LEGACY_TIDAL_REMNANT_JSON_KEY);

      this.fmsTidalRemnantConfig=
        new FMSTidalRemnantConfig( residualTauHours, this.getDeltaTMinutes(), fmsTidalRemnantCfgJsonObj);
    }
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
