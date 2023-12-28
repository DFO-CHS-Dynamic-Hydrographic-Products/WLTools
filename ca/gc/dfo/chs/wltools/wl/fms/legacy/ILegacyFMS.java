//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

/**
 *
 */

//---

import ca.gc.dfo.chs.wltools.wl.fms.IFMS;

/**
 * Interface for Legacy DVFM method.
 */
public interface ILegacyFMS extends IFMS {

  /**
   * Maximum time duration used to go back in the past to get WL data for the Legacy method.
   */
  int MEMORY_TIME_SCALE_HOURS_MAX= 800;

  /**
   * Maximum time in minutes for the time increment to use between WL data for the Legacy method.
   */
  int DELTA_T_MINUTES_MAX= 60;

  /**
   * Dimension of vectors and matrices used for tidal remnant computations done by the Legacy method.
   */
  int TIDAL_REMNANT_DATA_DIM= 3;

  /**
   * Default value for the fall-back coefficient used by the Legacy method.
   * Normally overriden by the corresponding configuration parameter coming from a ForecastingContext object.
   */
  double DEFAULT_AUX_COV_FALL_BACK_COEFF= 0.95;

  /**
   * The Legacy residual type applied.
   */
  enum ResidualType {
    WITH_TIDAL_REMNANT,  //--- For most WL stations
    WITHOUT_TIDAL_REMNANT  //--- For upstream St-Lawrence and great-lakes WL stations
  }

  //--- For possible future usage.
  //int MEMORY_TIME_SCALE_FACTOR= 2;
  //double RESIDUAL_INIT_DOUBLE= 0.0;
  //double SECONDS_PER_MINUTE_DOUBLE= (double)SECONDS_PER_MINUTE;
  //double PREDICTION_ERROR_ESTIMATE_METERS= 0.5;
  //double TIME_DRV_FACTOR= (double)SECONDS_PER_MINUTE;
  //long DEFAULT_AUX_COV_TIME_LAG_MINUTES=15;
  //long DEFAULT_CONSTANT_SURGE_DURATION_SECONDS= SECONDS_PER_HOUR;

}
