//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools;

/**
 * Comments please!
 */
public interface IWLToolsIO {

  String JSON_FEXT= ".json";

  String VALUE_JSON_KEY= "value";
  String INSTANT_JSON_KEY= "eventDate";
  String UNCERTAINTY_JSON_JEY= "uncertainty";

  String INPUT_DATA_FMT_SPLIT_CHAR= ":";
  String OUTPUT_DATA_FMT_SPLIT_CHAR= "-";

  String ISO8601_DATETIME_SEP_CHAR= "T";
  String ISO8601_YYYYMMDD_SEP_CHAR= OUTPUT_DATA_FMT_SPLIT_CHAR;

  // ---
  enum Format {
    CHS_JSON,
    DHP_S104_DCF8
    //CSV
  }

  // --- the cfg main folder MUST exists alongside the main Java classes files folder.
  String PKG_CFG_MAIN_DIR= "cfg/";

  // --- Define those in the tidal package??
  String PKG_CFG_TIDAL_DIR= "/tidal/";
  String PKG_CFG_TIDAL_STATIONARY_DIR= PKG_CFG_TIDAL_DIR + "/stationary/";
  String PKG_CFG_TIDAL_NON_STATIONARY_DIR= PKG_CFG_TIDAL_DIR + "/nonStationary/";

  String PKG_CFG_TIDAL_NON_STATIONARY_STAGE_DISCH_CLUSTERS_DIRNAME= "/dischargeClusters/";
  String PKG_CFG_TIDAL_NON_STATIONARY_STAGE_CLIM_DISCH_DIRNAME= "/stagedClimatologyDischarges/";

  String SHIP_CHANNEL_POINTS_DEF_DIRNAME= "/channelGridPointsInfoDef/";
}
