package ca.gc.dfo.chs.wltools.wl.fms;

/**
 *
 */

//---

import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.util.ITrigonometry;
//import ca.gc.dfo.iwls.fmservice.modeling.fms.legacy.LegacyFMResidual;

/**
 * Interface for enum and constants for the FM Service bundle.
 */
public interface IFMS extends IWL, ITimeMachine, ITrigonometry {

  /**
   * Default time-lag for temporal errors covariances computations in minutes.
   */
  long DEFAULT_AUX_COV_TIME_LAG_MINUTES = 15;

  //--- For possible future usage.
  //int NB_AUXILIARY_COV_DEFAULT= 1;
  //long DEFAULT_AUX_COV_TIME_LAG_MINUTES=45;

  // --- TODO: change FORECASTS_DURATION_HOURS to WLF_QC_NORMAL_DURATION_HOURS
  /**
   * Normal legacy WLF-QC "forecasts" (i.e. without external atmos. data forcings) duration in hours.
   */
  int QC_FORECASTS_DURATION_HOURS= 48;

  // --- TODO: change FORECASTS_DURATION_HOURS_MAX to WLF_QC_DURATION_HOURS_MAX
  /**
   * Maximum legacy WLF-QC "forecasts" (i.e. without external atmos. data forcings) duration in hours.
   */
  int QC_FORECASTS_DURATION_HOURS_MAX= 96;

  //--- For possible future usage.
  //int FORECASTS_DURATION_HOURS_MIN= FORECASTS_DURATION_HOURS;

  // --- TODO: Change FORECASTS_TIME_INCR_MINUTES to WLF_QC_TIME_INCR_MINUTES
  /**
   * 3 mins. as the default for constant time increments in seconds between each WL forecast data for a given WLO
   * station.
   * (It is normally overriden by a corresponding parameter coming from the database configuration objects)
   */
  int FORECASTS_TIME_INCR_MINUTES= 3; //--- 3 mins.

  // --- TODO: Change FORECASTS_TIME_INCR_MINUTES_MIN to WLF_QC_TIME_INCR_MINUTES_MIN
  /**
   * 1 min. as the minimum for constant time increment in seconds between each WL forecast data for a given WLO station.
   */
  int FORECASTS_TIME_INCR_MINUTES_MIN= 1;

  // ---  TODO: Change FORECASTS_TIME_INCR_MINUTES_MAX to WLF_QC_TIME_INCR_MINUTES_MAX
  /**
   * 15 mins. as the maximum for constant time increment in seconds between each WL forecast data for WLO stations.
   */
  int FORECASTS_TIME_INCR_MINUTES_MAX= 15;

  int DEFAULT_FULL_MODEL_FORECAST_MERGE_HOURS= 6;

  // --- Define the default time decay factor applied to gradually merge
  //     the full model forecast (which has a duration < 84 hours) with
  //     the long term WL prediction (NS_TIDE or climato) after the last
  //     timestamp of the full model forecast in the future.
  double FULL_MODEL_FORECAST_LONGTERM_MERGE_FACTOR= 1.0/24.0; //12.0;

  // --- TODO: Change FORECASTS_TIME_INCR_MINUTES_ALLOWED to WLF_QC_TIME_INCR_MINUTES_ALLOWED
  /**
   * Regroup the 3 FORECASTS_TIME_INCR_MINUTES* constants in an array.
   */
  int [] FORECASTS_TIME_INCR_MINUTES_ALLOWED= {
     FORECASTS_TIME_INCR_MINUTES_MIN,
     FORECASTS_TIME_INCR_MINUTES,
     FORECASTS_TIME_INCR_MINUTES_MAX
  };

  // --- TODO: Change FORECASTS_TIME_INCR_SECONDS_MIN to WLF_QC_TIME_INCR_SECONDS_MIN
  /**
   * FORECASTS_TIME_INCR_MINUTES_MIN in seconds
   */
  long FORECASTS_TIME_INCR_SECONDS_MIN = (long) FORECASTS_TIME_INCR_MINUTES_MIN * SECONDS_PER_MINUTE;

  //--- TODO: Change FORECASTS TIME_INCR_MINUTES_MAX to WLF_QC_TIME_INCR_SECONDS_MAX
  /**
   * FORECASTS_TIME_INCR_MINUTES_MAX in seconds.
   */
  long FORECASTS_TIME_INCR_SECONDS_MAX = (long) FORECASTS_TIME_INCR_MINUTES_MAX * SECONDS_PER_MINUTE;

  /**
   * The WL errors residuals method to use for the default(i.e. without external WL storm surge data) forecasts.
   */
  enum ResidualMethod {

    /**
     * The LEGACY DVFM method(with some modifications done to get something which is more in phase with the original
     * 1990 Smith and Thompson theory)
     */
    LEGACY("LEGACY");

    //KALMAN("KALMAN"),
    //SPECTRAL_NUDGING("SPECTRAL_NUDGING");

    /**
     * The String Id of the methods.
     */
    public final String id;

    ResidualMethod(final String id) {
      this.id = id;
    }
  }
}
