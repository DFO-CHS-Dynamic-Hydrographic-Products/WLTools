package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.FMSConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
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

    this.modelforecasts= wlAdjObj.getNearestModelData();

    this.qualityControlForecasts= null;

    final Instant lastPredMcInstant=
      this.predictions.get(this.predictions.size()-1).getEventDate();

    slog.info(mmi+"lastPredMcInstant="+lastPredMcInstant.toString());
    slog.info(mmi+"this.referenceTime="+this.referenceTime.toString());

    final long refTimeSeconds= this.referenceTime.toEpochSeconds();
    final long lastPrdSeconds= lastPredMcInstant.toEpochSeconds();

    if ( lastPrdSeconds <= refTimeSeconds) {
      throw new RuntimeException(mmi+"Cannot have lastPredSeconds <= refTimeSeconds !!");
    }

    this.durationHoursInFuture= (lastPrdSeconds - refTimeSeconds)/SECONDS_PER_HOUR;

    slog.info(mmi+"this.durationHoursInFuture="+this.durationHoursInFuture);
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

  }

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
