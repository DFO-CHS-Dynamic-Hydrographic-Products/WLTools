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
import java.util.Iterator;
import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.ListIterator;
import java.nio.file.Files;
import java.util.Collection;
import java.util.NavigableSet;
//import java.nio.file.PathMatcher;
import java.nio.file.FileSystems;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryIteratorException;

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
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpinePP;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

/**
 * Comments please!
 */
final public class WLAdjustmentSpineIPP extends WLAdjustmentSpinePP {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpineIPP: ";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- To store the non-ajusted WL predictions OR non-adjusted FMF data at the ship channel point locations that are
  //     the nearest to the nearest tide gauges locations (INPUT ONLY)
  protected Map<String, MeasurementCustomBundle> tgsNearestSCLocationsNonAdjData= null;

  // --- To store the non-ajusted WL predictions OR non-adjusted FMF data at the ship channel point locations that are
  //     in-between the tide gauges locations being processed (INPUT ONLY) 
  protected Map<String, MeasurementCustomBundle> scLocsNonAdjData= null;
    
  /**
   * Comments please!
   */
  public WLAdjustmentSpineIPP() {
    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  // ---
  public WLAdjustmentSpineIPP(/*@NotNull*/ final HashMap<String,String> argsMap) {

    super(IWLAdjustment.Type.SpineIPP,argsMap);

    final String mmi=
      "WLAdjustmentSpineIPP(final WLAdjustment.Type adjType, final Map<String,String> argsMap) constructor ";

    slog.info(mmi+"start");
    
    // --- Check if the non-adjusted full model forecast is available for this run.
    //     (If yes then it will be used instead of the NS Tide prediction at the
    //      ship channel location being processed)
    if (argsMap.keySet().contains("--nonAdjFMFInputDataInfo")) {

      this.nonAdjFMFInputDataInfo= argsMap.get("--nonAdjFMFInputDataInfo");

      throw new RuntimeException(mmi+"Usage of the non-adjusted full model forecast not ready yet!!");
       //slog.info(mmi+"Will use this.nonAdjFMFInputDataInfo="+this.nonAdjFMFInputDataInfo);  
    }
    
    // --- Now need to consider where to find the ship channel points locations
    //     WL prediction files 
    if (!argsMap.keySet().contains("--nsTidePredInputDataInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --nsTidePredInputDatInfo=<> defined in argsMap");
    }

    final String nsTidePredInputDataInfo= argsMap.get("--nsTidePredInputDataInfo");

    final String [] nsTidePredInputDataInfoStrSplit=
      nsTidePredInputDataInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (nsTidePredInputDataInfoStrSplit.length != 2 ) {
      throw new RuntimeException(mmi+"ERROR: nsTidePredInputDataInfoStrSplit.length != 2 !!!");
    }

    final String nsTidePredInputDataInfoFileFmt= nsTidePredInputDataInfoStrSplit[0];
    //tidalConstsInputInfo.split(IWLLocation.ID_SPLIT_CHAR)[0];

    if (!nsTidePredInputDataInfoFileFmt.equals(IWLToolsIO.Format.CHS_JSON.name())) {
	
      throw new RuntimeException(mmi+"Only the:"+IWLToolsIO.Format.CHS_JSON.name()+
                                 " WL prediction input file format allowed for now!!");
    }

    // --- Build a Path object for the folder where we can find the ship channel
    //     points locations WL prediction files  
    final Path nsTidePredInputDataDir= FileSystems.
      getDefault().getPath(nsTidePredInputDataInfoStrSplit[1]);

    slog.info(mmi+"nsTidePredInputDataDir="+nsTidePredInputDataDir.toString());

    // --- List all the ship channel points locations WL prediction files
    //     using a DirectoryStream<Path> object     
    DirectoryStream<Path> nsTidePredInputDataDirFilesDS= null;
    
    try {
      nsTidePredInputDataDirFilesDS= Files.
	newDirectoryStream(nsTidePredInputDataDir, "*"+this.scLocFNameCommonPrefix+"*"+IWLToolsIO.JSON_FEXT);
      
    } catch (IOException ioex) {
      throw new RuntimeException(mmi+ioex);
    }

    // --- Now put the ship channel points locations WL prediction files
    //     Path object in a List to be able to find the relevant ones.
    List<Path> nsTidePredInputDataDirFilesList= new ArrayList<Path>();

    for (final Path predInputFilePath: nsTidePredInputDataDirFilesDS) {
      nsTidePredInputDataDirFilesList.add(predInputFilePath);
    }

    if (nsTidePredInputDataDirFilesList.size() == 0) {
      throw new RuntimeException(mmi+"nsTidePredInputDataDirFilesList cannot be empty here!");
    }

    this.scLocsNonAdjData= new HashMap<String, MeasurementCustomBundle>();
    
    // --- Now read the related WL prediction data for the in-between ship channel
    //     points locations.
    for (int idx= this.scLoopStartIndex; idx <= this.scLoopEndIndex; idx++) {

      // --- Specific file name prefix string to use for the ship channel point location
      //     being processed
      final String scLocFNameSpecSubStr= this.scLocFNameCommonPrefix +
	IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(idx);	

      //slog.info(mmi+"scLocFNameSpecSubStr="+scLocFNameSpecSubStr);
      
      final String scLocFilePath= WLToolsIO.
     	getSCLocFilePath(nsTidePredInputDataDirFilesList, scLocFNameSpecSubStr);

      try {
        scLocFilePath.length();
      } catch (NullPointerException npe) {
        throw new RuntimeException(mmi+npe);
      }

      slog.info(mmi+"Reading scLocFilePath="+scLocFilePath+" for scLocFNameSpecSubStr="+scLocFNameSpecSubStr);
      
      this.scLocsNonAdjData.put(scLocFNameSpecSubStr,
				new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInJsonFmt(scLocFilePath, -1L, 0.0)));
       
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    }

    // --- now read the non-adjusted WL prediction (or non-adjusted FMF) at the two ship channel locations
    //     that are the nearest to the two tide gauges being processed. Use the convention that the ship channel
    //     locations are ordered from lower to upper indices.

    if (this.scLoopStartIndex == 0 ) {
      throw new RuntimeException(mmi+"this.scLoopStartIndex cannot be 0 here!");
    } 

    // --- Lower side ship channel location: subtract 1 from scLoopStartIndex to build its proper str id
    final String lowerSideScLocStrId= this.scLocFNameCommonPrefix +
      IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(this.scLoopStartIndex-1);

    final String lowerSideScLocFile= WLToolsIO.
      getSCLocFilePath(nsTidePredInputDataDirFilesList, lowerSideScLocStrId);

    this.scLocsNonAdjData.put(lowerSideScLocStrId,
			      new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInJsonFmt(lowerSideScLocFile, -1L, 0.0)));
    
    // --- upper side ship channel location: add 1 from scLoopEndIndex to build its proper str id
    final String upperSideScLocStrId= this.scLocFNameCommonPrefix +
      IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(this.scLoopEndIndex+1);

    final String upperSideScLocFile= WLToolsIO.
      getSCLocFilePath(nsTidePredInputDataDirFilesList, upperSideScLocStrId);

    this.scLocsNonAdjData.put(upperSideScLocStrId,
			      new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInJsonFmt(upperSideScLocFile, -1L, 0.0)));
    
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);
    
  } // --- main constructor

  ///**
  // * Comments please.
  // */
  final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

    final String mmi= "getAdjustment: ";

    slog.info(mmi+"start");

    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    return this.locationAdjustedData; //adjustmentRet;
  }
}
