package ca.gc.dfo.chs.wltools.wl.fms;

/**
 *
 */

//---

//import ca.gc.dfo.chs.wltools.wl.IWL;
//import ca.gc.dfo.chs.wltools.util.ITimeMachine;
//import ca.gc.dfo.chs.wltools.util.ITrigonometry;
//import ca.gc.dfo.iwls.fmservice.modeling.fms.legacy.LegacyFMResidual;

/**
 * Interface for enum and constants for the FM Service bundle.
 */
public interface IFMSConfig {

  String LEGACY_MERGE_JSON_KEY= "merge";
  String LEGACY_TAU_HOURS_JSON_KEY= "tauHours";
  String LEGACY_MERGE_HOURS_JSON_KEY= "mergeHours";
  String LEGACY_DELTA_MINS_JSON_KEY= "deltaTMinutes";

  String LEGACY_RESIDUAL_JSON_KEY= "residual";
  String LEGACY_RESIDUAL_METH_JSON_KEY= "method";
  String LEGACY_RESIDUAL_FALLBACK_ERR_JSON_KEY= "fallBackError";

  String LEGACY_TIDAL_REMNANT_JSON_KEY= "tidalRemnant";
  String LEGACY_TIDAL_REMNANT_EPS1MAX_JSON_KEY= "maxEps1";
  String LEGACY_TIDAL_REMNANT_EPS2MAX_JSON_KEY= "maxEps2";

  String LEGACY_STN_COV_JSON_KEY= "stationId";
  String LEGACY_STN_COV_JSON_KEY= "covariance";
  String LEGACY_STN_COV_TLAG_MINS_JSON_KEY= "timeLagMinutes";
  String LEGACY_STN_COV_FALLBACK_COEFF_JSON_KEY= "fallBackCoeff";
}
