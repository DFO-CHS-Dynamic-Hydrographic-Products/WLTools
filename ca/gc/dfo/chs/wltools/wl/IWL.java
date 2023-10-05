//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

/**
 *
 */

/**
 * Generic interface for WL family of interfaces amd classes.
 */
public interface IWL {

  /**
   * int constant to get the PREDICTION WLType from an array of objects references.
   */
  int PREDICTION= WLType.PREDICTION.ordinal();

  /**
   * int constant to get the OBSERVATION WLType from an array of objects references.
   */
  int OBSERVATION= WLType.OBSERVATION.ordinal();

  /**
   * int constant to get the QC_FORECAST WLType from an array of objects references.
   *  (QC stands for Quality Control)
   */
  int QC_FORECAST= WLType.QC_FORECAST.ordinal();

  /**
   * int constant to get the EXT_STORM_SURGE WLType from an array of objects references.
   */
  //int EXT_STORM_SURGE = WLType.EXT_STORM_SURGE.ordinal();
  int MODEL_FORECAST= WLType.MODEL_FORECAST.ordinal();

  /**
   * Constant default number of WLType for array size allocation.
   */
  int NB_WL_TYPES = WLType.values().length;

  double MINIMUM_UNCERTAINTY_METERS= 0.005; // --- Half-centimeter
  double MAXIMUM_UNCERTAINTY_METERS= 1.5;

  double PREDICTIONS_ERROR_ESTIMATE_METERS = 0.5;

  /**
   * Define the water levels types used to clearly identify which is which in the code
   */
  enum WLType {

    /**
     * PREDICTION WL type.
     */
    PREDICTION("wlp"),

    /**
     * OBSERVATION WL type.
     */
    OBSERVATION("wlo"),

    /**
     * QC_FORECAST WL type.
     */
    QC_FORECAST("wlf-qc"),

    /**
     * MODEL_FORECAST (river discharge and-or storm surge) WL type.
     */
    MODEL_FORECAST("wlf-mdl");

    /**
     * EXT_STORM_SURGE WL type.
     */
    //EXT_STORM_SURGE("wls");

    /**
     * ASCII file name extension going with a given WL type.
     */
    public final String asciiFileExt;

    WLType(final String asciiFileExt) {
      this.asciiFileExt = asciiFileExt;
    }
  }

  /**
   * Define the two possible storm surge WL types.
   */
  enum StormSurgeWLType {

    /**
     * Full storm-surge Z value including tidal component.
     */
    WLSSF_FULL,
    /**
     * De-tided storm-surge Z value without tidal component.
     */
    WLSSF_DE_TIDED
  }
  //int DEFAULT_NB_STATIONS= 1;
}
