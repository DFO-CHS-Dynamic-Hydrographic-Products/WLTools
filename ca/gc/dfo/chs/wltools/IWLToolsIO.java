package ca.gc.dfo.chs.wltools;

// --
import java.io.File;

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
  String ISO8601_HHMMSS_SEP_CHAR= INPUT_DATA_FMT_SPLIT_CHAR;
  String ISO8601_YYYYMMDD_SEP_CHAR= OUTPUT_DATA_FMT_SPLIT_CHAR;

  String ISO8601_UTC_EXT= "Z";

  // ---
  enum Format {
    CHS_JSON,
    DHP_S104_DCF8,
    LEGACY_ASCII
    //CSV
  }

  // --- the cfg main folder MUST exists at the same folder level as the the main Java classes lib folder.
  String PKG_CFG_MAIN_DIR= "cfg";

  // --- Define those in the tidal package??
  String PKG_CFG_TIDAL_DIR= File.separator + "tidal" + File.separator;

  String PKG_CFG_TIDAL_STATIONARY_DIR= PKG_CFG_TIDAL_DIR + File.separator + "stationary" +  File.separator;

  String PKG_CFG_TIDAL_NON_STATIONARY_DIR= PKG_CFG_TIDAL_DIR + File.separator + "nonStationary" + File.separator;

  String PKG_CFG_TIDAL_NON_STATIONARY_STAGE_DISCH_CLUSTERS_DIRNAME= File.separator + "dischargeClusters" + File.separator;

  String PKG_CFG_TIDAL_NON_STATIONARY_STAGE_CLIM_DISCH_DIRNAME= File.separator + "stagedClimatologyDischarges" + File.separator;

  String SHIP_CHANNEL_POINTS_DEF_DIRNAME=  File.separator + "channelGridPointsInfoDef" + File.separator;

  // --- IWLS specific:

  String IWLS_DB_TG_STR_ID_KEY= "id";
  String IWLS_DB_TG_NUM_STRID_KEY= "code";

  String IWLS_DB_DTIME_END_STR= "%3A00%3A00Z";

  String IWLS_DB_QCFLAG_KEY= "qcFlagCode";

  String IWLS_DB_QCFLAG_VALID= "1";
}
