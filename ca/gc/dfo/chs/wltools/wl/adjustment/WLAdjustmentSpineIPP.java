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
//import java.util.Iterator;
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
  //     in-between the tide gauges locations being processed (INPUT ONLY) 
  private Map<String, MeasurementCustomBundle> scLocsNonAdjData= null;

  // --- To store the non-ajusted WL predictions OR non-adjusted FMF data at the two ship channel point locations that are
  //     the nearest to the nearest tide gauges locations (INPUT ONLY)
  private Map<String, MeasurementCustomBundle>
    tgsNearestSCLocsNonAdjData= new HashMap<String, MeasurementCustomBundle>(2); //null;

  private String fmfReferenceDateTimeStr= null;
    
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

    final String mmi= "WLAdjustmentSpineIPP main constructor ";

    slog.info(mmi+"start");

    try {
      this.lowerSideScLocStrId.length();	
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    try {
      this.upperSideScLocStrId.length();	
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    try {
       WLToolsIO.getOutputDataFormat();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    // --- Verfiy the output file(s) format before going further
    if (!WLToolsIO.getOutputDataFormat().equals(IWLToolsIO.Format.CHS_JSON.name())) {
      throw new RuntimeException(mmi+"Invalid output file(s) data format -> "+WLToolsIO.getOutputDataFormat()+" for the adjustment tool!");
    }

    // --- Need to have the --fmfReferenceDateTimeStr arg option defined
    if (!argsMap.keySet().contains("--fmfReferenceDateTimeStr")) {
      throw new RuntimeException(mmi+"Must have the --referenceDateTimeStr=<datetime reference string> defind in the argsMap");
    }

    this.fmfReferenceDateTimeStr= argsMap.get("--fmfReferenceDateTimeStr");

    slog.info(mmi+"this.fmfReferenceDateTimeStr="+this.fmfReferenceDateTimeStr);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
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

    // --- Get all the paths of the ship channel points locations non-adjusted WL prediction files
    final List<Path> nsTidePredInputDataDirFilesList= WLToolsIO.
      getRelevantFilesList(nsTidePredInputDataInfoStrSplit[1], "*"+this.scLocFNameCommonPrefix+"*"+IWLToolsIO.JSON_FEXT);

    this.scLocsNonAdjData= new HashMap<String, MeasurementCustomBundle>();

    slog.info(mmi+"Reading the non-adjusted WL data for all the in-between ship channel points locations");
    
    // --- Now read the related non-adjusted WL prediction (or non-adjusted FMF) data for the in-between ship channel
    //     points locations.
    //
    //     TODO: Loop on the String keys of the this.scLocsDistances HashMap (as it is done in the getAdjustment method)
    //           instead of looping on the related integer indices.      
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

      slog.debug(mmi+"Reading scLocFilePath="+scLocFilePath+" for scLocFNameSpecSubStr="+scLocFNameSpecSubStr);
      
      this.scLocsNonAdjData.put(scLocFNameSpecSubStr,
				new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInJsonFmt(scLocFilePath, -1L, 0.0)));
       
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    }
    
    slog.info(mmi+"Done reading the non-adjusted WL data for all the in-between ship channel points locations");

    // --- now read the non-adjusted WL prediction (or non-adjusted FMF) at the two ship channel locations
    //     that are the nearest to the two tide gauges being processed. Use the convention that the ship channel
    //     locations are ordered from lower to upper indices.

    if (this.scLoopStartIndex == 0 ) {
      throw new RuntimeException(mmi+"this.scLoopStartIndex cannot be 0 here!");
    } 

    // --- Lower side ship channel location: subtract 1 from scLoopStartIndex to build its proper str id
    //final String lowerSideScLocStrId= this.scLocFNameCommonPrefix +
    //  IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(this.scLoopStartIndex-1);

    final String lowerSideScLocFile= WLToolsIO.
      getSCLocFilePath(nsTidePredInputDataDirFilesList, this.lowerSideScLocStrId);

    this.tgsNearestSCLocsNonAdjData.put(this.lowerSideScLocStrId,
			                new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInJsonFmt(lowerSideScLocFile, -1L, 0.0)));
    
    // --- upper side ship channel location: add 1 from scLoopEndIndex to build its proper str id
    //final String upperSideScLocStrId= this.scLocFNameCommonPrefix +
    //  IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(this.scLoopEndIndex+1);

    final String upperSideScLocFile= WLToolsIO.
      getSCLocFilePath(nsTidePredInputDataDirFilesList, this.upperSideScLocStrId);

    this.tgsNearestSCLocsNonAdjData.put(this.upperSideScLocStrId,
	     		                new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInJsonFmt(upperSideScLocFile, -1L, 0.0)));
 
    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
  } // --- main constructor

  ///**
  // * Comments please.
  // */
  final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

    final String mmi= "getAdjustment: ";

    slog.info(mmi+"start, this.spinePPWriteCtrl="+this.spinePPWriteCtrl.name());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    try {
      outputDirectory.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"outputDirectory cannot be null here!!");
    }

    // --- Verfiy the output file(s) format before going further
    if (!WLToolsIO.getOutputDataFormat().equals(IWLToolsIO.Format.CHS_JSON.name())) {
      throw new RuntimeException(mmi+"Invalid output file(s) data format -> "+WLToolsIO.getOutputDataFormat()+" for the adjustment tool!");
    }

    // --- Get the 1st (otherwise said the least recent in time) Instant object related
    //     to the lower side tide gauge
    final Instant lowSideLocLeastRecentInstant=
      tgsNearestSCLocsAdjFMF.get(this.lowerSideScLocTGId).getLeastRecentInstantCopy();

    // --- Get the 1st (otherwise said the least recent in time) Instant object related
    //     to the upper side tide gauge    
    final Instant uppSideLocLeastRecentInstant=
      tgsNearestSCLocsAdjFMF.get(this.upperSideScLocTGId).getLeastRecentInstantCopy();

    // --- Find which of those two Instant object is the most recent in time.
    //     (this is done to ensure tha tw ehave time synchronization between
    //      the two sets of adjusted FMF WL data)
    final String tgLocWithMostRecentInstantId= lowSideLocLeastRecentInstant.
      isAfter(uppSideLocLeastRecentInstant) ? this.lowerSideScLocTGId : this.upperSideScLocTGId;

    slog.info(mmi+"lowSideLocLeastRecentInstant="+lowSideLocLeastRecentInstant.toString());
    slog.info(mmi+"uppSideLocLeastRecentInstant="+uppSideLocLeastRecentInstant.toString());
    //slog.info(mmi+"tgLocMostRecentInstant="+tgLocMostRecentInstant.toString());
    slog.info(mmi+"tgLocWithMostRecentInstantId="+tgLocWithMostRecentInstantId);
    
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    // --- Get a SortedSet of the Instants objects of the lower side
    //     adj. FMF data (NOTE: we would get the exact same SortedSet using
    //     the upper side adj. FMF data)
    final SortedSet<Instant> adjFMFInstantsSet= this.
      tgsNearestSCLocsAdjFMF.get(tgLocWithMostRecentInstantId).getInstantsKeySetCopy();

    // --- Allocate memory for the Map <String, List<MeasurementCustom>>
    //     that is used to store the adjusted "forecasted" WLs for all the
    //     ship channel points locations.
    this.scLocsAdjLTFP= new HashMap<String, List<MeasurementCustom>>();

    // --- Allocate the two List<MeasurementCustom> objects for the two ship channel points
    //     locations that are the nearest to the two tide gauges being processed
    //     The data stored will simply be the already adj. FMF for these two locations. 
    this.scLocsAdjLTFP.put(this.lowerSideScLocStrId, new ArrayList<MeasurementCustom>());
    this.scLocsAdjLTFP.put(this.upperSideScLocStrId, new ArrayList<MeasurementCustom>());

    // --- Get the String keys of all the in-between ship channel points locations
    //     to loop on them to apply the WL adjustements at their locations.
    //     NOTE: We do not need a SortedSet here, the order is not important
    //     for the in-between ship channel points locations for the remainder of the processing
    //final Set<String> scLocsDistancesKeySet= this.scLocsDistances.keySet();
    //final SortedSet<String> scLocsKeySet= new TreeSet<String>(this.scLocsNonAdjData.keySet());
    final Set<String> scLocsKeySet= this.scLocsNonAdjData.keySet();

    // --- Allocate all the List<MeasurementCustom> objects that are in-between the
    //     two ship channel points locations that are the nearest to the two tide gauges being processed
    for (final String scLocStrId: scLocsKeySet ) {
      this.scLocsAdjLTFP.put(scLocStrId, new ArrayList<MeasurementCustom>());
    }

    // --- Spare costly division operations in loops, multiply by the inverted value instead.
    final double tgsNearestsLocsDistRadInv= 1.0/this.tgsNearestsLocsDistRad;
	
    // --- Loop on all the Instants of the adj. FMF data to adjust the WLs (either
    //     WL predictions or non-adj. FMF data) for the in-between ship channel
    //     points locations. We use a simple spatial linear interpolation of the
    //     (adj. FMF WL at TGs - non-adjusted WL at TGs) residuals using the distance
    //     in radians between the ship channel location that is the nearest to the
    //     lower side TG and the in-between ship channel point location where we
    //     have to adjust the WLs.
    for(final Instant adjFMFInstant: adjFMFInstantsSet) {
	
	//slog.info(mmi+"adjFMFInstant="+adjFMFInstant.toString());

      // --- Get the adj. FMF WL at the lower side ship channel location
      final MeasurementCustom lowerSideSCLocAdjFMFMc= this.
	tgsNearestSCLocsAdjFMF.get(this.lowerSideScLocTGId).getAtThisInstant(adjFMFInstant);

      // --- Set the lower side ship channel location adj. MeasurementCustom object directly
      //     using its FMF WL MeasurementCustom (no need for interp. here)
      this.scLocsAdjLTFP.get(this.lowerSideScLocStrId).add( new MeasurementCustom(lowerSideSCLocAdjFMFMc) );

      // --- Get the adj. FMF WL at the upper side ship channel location
      final MeasurementCustom upperSideSCLocAdjFMFMc= this.
	tgsNearestSCLocsAdjFMF.get(this.upperSideScLocTGId).getAtThisInstant(adjFMFInstant);

      // --- Set the upper side ship channel location adj. MeasurementCustom object directly
      //     using its FMF WL MeasurementCustom (no need for interp. here)     
      this.scLocsAdjLTFP.get(this.upperSideScLocStrId).add( new MeasurementCustom(upperSideSCLocAdjFMFMc) );

      // --- Now get the two (adj. FMF WL at TGs - non-adjusted WL at TGs) residuals for the lower and
      //     upper side locations
      final double lowerSideAdjFMFValue= lowerSideSCLocAdjFMFMc.getValue();
      final double upperSideAdjFMFValue= upperSideSCLocAdjFMFMc.getValue();
      
      //slog.info(mmi+"lowerSideAdjFMFValue="+lowerSideAdjFMFValue);
      //slog.info(mmi+"upperSideAdjFMFValue="+upperSideAdjFMFValue);

      final double lowerSideNonAdjValue= this.
	tgsNearestSCLocsNonAdjData.get(this.lowerSideScLocStrId).getAtThisInstant(adjFMFInstant).getValue();

      final double upperSideNonAdjValue= this.
	tgsNearestSCLocsNonAdjData.get(this.upperSideScLocStrId).getAtThisInstant(adjFMFInstant).getValue();

      //slog.info(mmi+"lowerSideNonAdjValue="+lowerSideNonAdjValue);
      //slog.info(mmi+"upperSideNonAdjValue="+upperSideNonAdjValue);

      final double lowerSideResValue= lowerSideAdjFMFValue - lowerSideNonAdjValue;
      final double upperSideResValue= upperSideAdjFMFValue - upperSideNonAdjValue;

      //slog.info(mmi+"lowerSideResValue="+lowerSideResValue);
      //slog.info(mmi+"upperSideResValue="+upperSideResValue);

      // --- Now adjust the non-adjusted WL prediction (or non-adjusted FMF) data for the in-between ship channel
      //     points locations using the two residual values and the distance from the lower side ship channel location
      //     NOTE: Order is not really important here since we have stored the related distances in the this.scLocsDistances HashMap
      for (final String scLocStrId: scLocsKeySet ) { //scLocsDistancesKeySet) {
	  
	  //slog.info(mmi+"scLocStrId="+scLocStrId);

	final double scLocDistRad= this.scLocsDistances.get(scLocStrId);

	//slog.info(mmi+"scLocDistRad="+scLocDistRad);

	final double scNonAdjValue= this.scLocsNonAdjData.
	  get(scLocStrId).getAtThisInstant(adjFMFInstant).getValue();

	//slog.info(mmi+"scNonAdjValue="+scNonAdjValue);

	// --- Using just two costly multiplications here instead of four.
	//     NOTE: recall that the scLocDistRad is the distance between the lower side
	//           ship channel location and the in-between ship channel point location
	//           being processed.
        final double scAdjValue= scNonAdjValue + lowerSideResValue +
	  tgsNearestsLocsDistRadInv * scLocDistRad * (upperSideResValue - lowerSideResValue);
	  //(tgsNearestsLocsDistRadInv * scLocDistRad) * upperSideResValue + (1.0 - tgsNearestsLocsDistRadInv * scLocDistRad) * lowerSideResValue;

        //slog.info(mmi+"scAdjValue="+scAdjValue+"\n");

	// --- Uncertainty is 0.0 here for now.
	this.scLocsAdjLTFP.get(scLocStrId).add(new MeasurementCustom(adjFMFInstant, scAdjValue, 0.0));

	//if (scLocStrId.equals("gridPoint-1059")) {
	//slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);
	//}
	  
      } // --- for (final String scLocStrId: scLocsDistancesKeySet) inner loop block
	  
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
    } // --- for(final Instant adjFMFInstant: adjFMFInstantsSet) outer loop block

    slog.info(mmi+"writing the adj. WLs for all the ship channel point locations that are in-between the two tide gauges");

    final String outputFileNamesPrfx= this.fmfReferenceDateTimeStr +
      IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + IWLAdjustmentIO.ADJ_HFP_ATTG_FNAME_PRFX;
    
    // --- Now write the CHS_JSON output files for all the in between ship channel points locations.
    for (final String scLocStrId: scLocsKeySet ) {

	//final String scLocWLAdjOutFile= outputDirectory + File.separator + fmfReferenceDateTimeStr +
	//IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + IWLAdjustmentIO.ADJ_HFP_ATTG_FNAME_PRFX + scLocStrId + IWLToolsIO.JSON_FEXT;

      final String scLocWLAdjOutFName= outputFileNamesPrfx + scLocStrId;
      
      slog.debug(mmi+"scLocWLAdjOutFName="+scLocWLAdjOutFName);

      WLToolsIO.writeToOutputDir(this.scLocsAdjLTFP.get(scLocStrId),
				 IWLToolsIO.Format.CHS_JSON, scLocWLAdjOutFName, outputDirectory);
      
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    }

    // --- Now write the CHS_JSON output files for the two ship channel point locations.
    //     that are the nearest to the two tide gauges that define the point location range
    //     being processed. It is controlled by the this.spinePPWriteCtrl attribute.
    
    if (this.spinePPWriteCtrl == IWLAdjustment.SpinePPWriteCtrl.LOWER_SIDE || 
	  this.spinePPWriteCtrl == IWLAdjustment.SpinePPWriteCtrl.BOTH_SIDES ) {
	
      // --- Lower side
      final String lowerSideScLocWLAdjOutFName= outputFileNamesPrfx + this.lowerSideScLocStrId;

      slog.info(mmi+"Writing the adj. WLs of the ship channel point location related to the lower side tide gauge");
      
      WLToolsIO.writeToOutputDir(this.scLocsAdjLTFP.get(this.lowerSideScLocStrId),
   			         IWLToolsIO.Format.CHS_JSON, lowerSideScLocWLAdjOutFName, outputDirectory);
    }
    
    if (this.spinePPWriteCtrl == IWLAdjustment.SpinePPWriteCtrl.UPPER_SIDE || 
	  this.spinePPWriteCtrl == IWLAdjustment.SpinePPWriteCtrl.BOTH_SIDES ) {

      // --- Upper side
      final String upperSideScLocWLAdjOutFName= outputFileNamesPrfx + this.upperSideScLocStrId;

      slog.info(mmi+"Writing the adj. WLS of the ship channel point location related to the the upper side tide gauge");
      
      WLToolsIO.writeToOutputDir(this.scLocsAdjLTFP.get(this.upperSideScLocStrId),
        			 IWLToolsIO.Format.CHS_JSON, upperSideScLocWLAdjOutFName, outputDirectory);
    }
    
    slog.info(mmi+"end");

    // --- Now 

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- return null here to signal to the main class that
    //     all the results have already been written in the
    //     output folder by this class 
    return null;
    
    //return this.locationAdjustedData; //adjustmentRet;
  }
}
