package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Collection;
import java.util.NavigableSet;
//import java.util.stream.Stream;
import java.nio.file.DirectoryStream;
//import java.nio.file.PathMatcher;
//import java.nio.file.FileSystems;

//import java.awt.geom.Point2D; //.Double;
//import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

/**
 * Comments please!
 */
abstract public class WLAdjustmentSpinePP extends WLAdjustmentType {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpinePP: ";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  protected String nonAdjFMFInputDataInfo= null;

    //private Map<String, Double> twoNearestTGInfo= new HashMap<String, Double>(2);

  // --- The List<MeasurementCustom> where to save the adjusted forecast
  //     at the Spine ship channel point locations being processed.
  protected Map<String, List<MeasurementCustom>> spineLocationAdjForecast= null;
  //private MeasurementCustomBundle spineLocationAdjForecast= null;

  // --- To store The initial NS_TIDE WL predictions at the Spine target location.
  // INPUT ONLY, not used if the spineLocationNonAdjForecast= is used
  //private List<MeasurementCustom> spineLocationNSTPred= null;

  // --- To store the non-adjusted WL NS Tide WL pred data at the Spine
  //     ship channel point locations (INPUT ONLY)
  //private List<MeasurementCustom> spineLocationNonAdjData= null;
  protected Map<String, MeasurementCustomBundle> spineLocationNonAdjData= null;

  // --- To store the NS_TIDE WL non-ajusted predictions at the Spine locations that are
  //     the nearest to the nearest tide gauges locations
  //     INPUT ONLY
  //private Map<String, List<MeasurementCustom>> tgsNearestSpineLocationsNonAdjData= null;
  protected Map<String, MeasurementCustomBundle> tgsNearestSpineLocationsNonAdjData= null;

  // --- To store the model adjusted forecast at the spine locations that are the
  //     nearest to the tide gauges locations used for the adjustments.
  //     INPUT ONLY
  //private Map<String, List<MeasurementCustom>> tgsNearestSpineLocationsAdjForecast= null;
  protected Map<String, MeasurementCustomBundle> tgsNearestSpineLocationsAdjForecast= null;

  //private IWLAdjustment.Type adjType= null;

  /**
   * Comments please!
   */
  public WLAdjustmentSpinePP() {
    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  // ---
  public WLAdjustmentSpinePP(final IWLAdjustment.Type spinePPType, final HashMap<String,String> argsMap) {
    super(spinePPType,argsMap);
  }    
    
  // ///**
  // // * Comments please.
  // // */
  // final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

  //   final String mmi= "getAdjustment: ";

  //   slog.info(mmi+"start");

  //   slog.info(mmi+"end");

  //   slog.info(mmi+"Debug System.exit(0)");
  //   System.exit(0);

  //   return this.locationAdjustedData; //adjustmentRet;
  // }
    
} // --- class WLAdjustmentSpinePP
