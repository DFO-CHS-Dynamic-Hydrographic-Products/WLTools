//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools;

/**
 * Comments please!
 */
public interface IWLToolsIO {

  // --- the cfg main folder MUST exists alongside the main Java classes files folder.
  final static String PKG_CFG_MAIN_DIR= "cfg/";

  // --- Define those in the tidal package??
  final static String PKG_CFG_TIDAL_DIR= "/tidal/";
  final static String PKG_CFG_TIDAL_STATIONARY_DIR= PKG_CFG_TIDAL_DIR + "/stationary/";
  final static String PKG_CFG_TIDAL_NON_STATIONARY_DIR= PKG_CFG_TIDAL_DIR + "/nonStationary/";

  final static String PKG_CFG_TIDAL_NON_STATIONARY_STAGE_DISCH_CLUSTERS_DIRNAME= "/dischargeClusters/";
  final static String PKG_CFG_TIDAL_NON_STATIONARY_STAGE_CLIM_DISCH_DIRNAME= "/stagedClimatologyDischarges/";
}
