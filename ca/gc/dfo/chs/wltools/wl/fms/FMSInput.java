package ca.gc.dfo.chs.wltools.wl.fms;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

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

  private List<MeasurementCustom> observations;

  private List<MeasurementCustom> predictions;

  private List<MeasurementCustom> forecasts;

  /**
   *
   */
  //public FMSConfig(final Float stdErrSigma) {
  //}

  public FMSInput(final WLAdjustmentType wlAdjObj) {

    //super(argsMap, wlAdjObj);
    super(wlAdjObj)

    this.predictions= wlAdjObj.getLocationPredData();

    this.observations= wlAdjObj.getNearestObsData();

    this.forecasts= wlAdjObj.getNearestModelData();
  }

  public List<MeasurementCustom> getObservations() {
    return this.observations;
  }

  public List<MeasurementCustom> getPredictions() {
    return this.predictions;
  }

  public List<MeasurementCustom> getForecasts() {
    return this.forecasts;
  }
}
