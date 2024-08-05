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
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.ListIterator;
import java.nio.file.Files;
import java.util.Collection;
import java.util.NavigableSet;
import java.nio.file.FileSystems;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryIteratorException;

//---
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
import ca.gc.dfo.chs.wltools.wl.WLSCReachIntrpUnit;
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

  private WLSCReachIntrpUnit scReachIntrpUnit= null;
    
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
      this.locations.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"this.locations cannot be null here !");
    }
    
    if (this.locations.size() != 2) {
      throw new RuntimeException(mmi+"this.location.size() != 2 !!");
    }
    
    // --- Allocate only two items for the HashMap<String, MeasurementCustomBundle>
    //     this.tgsNearestSCLocsAdjFM object here.
    this.tgsNearestSCLocsAdjFMF= new HashMap<String, MeasurementCustomBundle>(2); 

    try {
      this.shipChannelPointLocsTCInputDir.hashCode();	
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }   
    
    try {
      this.mainJsonTGInfoMapObj.hashCode();	
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }   

    // --- Instantiate the WLSCReachIntrpUnit unique object
    this.scReachIntrpUnit= new WLSCReachIntrpUnit(this.shipChannelPointLocsTCInputDir,
						  this.mainJsonTGInfoMapObj, this.locations.get(0), this.locations.get(1));
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);    
    
    try {
      this.scReachIntrpUnit.getLowerSideScLocStrId().length();	
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    try {
      this.scReachIntrpUnit.getUpperSideScLocStrId().length();	
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    try {
      WLToolsIO.getOutputDataFormat();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    // --- Now read the adjusted FMF data at the two ship channel locations
    //     that are the nearest to the two tide gauges being processed.
    if (!argsMap.keySet().contains("--adjFMFAtTGSInputDataInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --adjFMFAtTGSInputDataInfo=<Folder where to find all the adj. FMF at tide gauges> defined in argsMap");
    }

    // ---
    final String adjFMFAtTGSInputDataInfo= argsMap.get("--adjFMFAtTGSInputDataInfo");

    final String [] adjFMFAtTGSInputDataInfoStrSplit=
      adjFMFAtTGSInputDataInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (adjFMFAtTGSInputDataInfoStrSplit.length != 2 ) {
      throw new RuntimeException(mmi+"ERROR: adjFMFAtTGSInputDataInfoStrSplit.length != 2 !!!");
    }

    // --- Get the input file(s) format from the adjFMFAtTGSInputDataInfoStrSplit two Strings array
    final String adjFMFAtTGSInputDataInfoFileFmt= adjFMFAtTGSInputDataInfoStrSplit[0];

    // --- CHS_JSON for now for the SpineIPP type
    if (!adjFMFAtTGSInputDataInfoFileFmt.equals(IWLToolsIO.Format.CHS_JSON.name())) {
	
      throw new RuntimeException(mmi+"Only the:"+IWLToolsIO.Format.CHS_JSON.name()+
                                 " adj. FMF WL data input file format allowed for now!!");
    }

    final String adjFMFAtTGSInputDataDir= adjFMFAtTGSInputDataInfoStrSplit[1];

    slog.info(mmi+"adjFMFAtTGSInputDataDir="+adjFMFAtTGSInputDataDir);
      
    // --- Get the paths of all the ship channel points locations adjusted FMF WL
    //     data input files in a List<Path> object 
    final List<Path> adjFMFAtTGSInputDataDirFilesList= WLToolsIO.
      getRelevantFilesList(adjFMFAtTGSInputDataDir, "*"+IWLAdjustmentIO.ADJ_HFP_ATTG_FNAME_PRFX+"*"+IWLToolsIO.JSON_FEXT);

    // --- Get the path of the ship channel point location adjusted FMF WL data
    //     that is the nearest to the lower side (in terms of ship channel locations indices)
    //     tide gauge.
    final String fmfAdjAtLowerSideTGFile= WLToolsIO.
      getSCLocFilePath(adjFMFAtTGSInputDataDirFilesList, this.scReachIntrpUnit.getLowerSideScLocTGId()+IWLToolsIO.JSON_FEXT);

    slog.info(mmi+"fmfAdjAtLowerSideTGFile="+fmfAdjAtLowerSideTGFile);

    // --- Read the adjusted FMF WL data for the ship channel point location
    //     that is the nearest to the lower side tide gauge.
    this.tgsNearestSCLocsAdjFMF.put(this.scReachIntrpUnit.getLowerSideScLocTGId(),
        			    new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInCHSJsonFmt(fmfAdjAtLowerSideTGFile, -1L, 0.0, null)));

    // --- Get the path of the ship channel point location adjusted FMF WL data
    //     that is the nearest to the upper side (in terms of ship channel locations indices)
    //     tide gauge.
    final String fmfAdjAtUpperSideTGFile= WLToolsIO
      .getSCLocFilePath(adjFMFAtTGSInputDataDirFilesList, this.scReachIntrpUnit.getUpperSideScLocTGId()+IWLToolsIO.JSON_FEXT);

    slog.info(mmi+"fmfAdjAtUpperSideTGFile="+fmfAdjAtUpperSideTGFile);

      // --- Read the adjusted FMF WL data for the ship channel point location
      //     that is the nearest to the upper side tide gauge.
    this.tgsNearestSCLocsAdjFMF.put(this.scReachIntrpUnit.getUpperSideScLocTGId(),
				    new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInCHSJsonFmt(fmfAdjAtUpperSideTGFile, -1L, 0.0, null)));  

    // --- Verfiy the output file(s) format before going further
    if (!WLToolsIO.getOutputDataFormat().equals(IWLToolsIO.Format.CHS_JSON.name())) {
      throw new RuntimeException(mmi+"Invalid output file(s) data format -> "+
				 WLToolsIO.getOutputDataFormat()+" for the adjustment tool!");
    }

    // --- Need to have the --fmfReferenceDateTimeStr arg option defined
    if (!argsMap.keySet().contains("--fmfReferenceDateTimeStr")) {
      throw new RuntimeException(mmi+"Must have the --referenceDateTimeStr=<datetime reference string> defind in the argsMap");
    }

    this.fmfReferenceDateTimeStr= argsMap.get("--fmfReferenceDateTimeStr");

    slog.info(mmi+"this.fmfReferenceDateTimeStr="+this.fmfReferenceDateTimeStr);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    // // --- Check if the non-adjusted full model forecast is available for this run.
    // //     (If yes then it will be used instead of the NS Tide prediction at the
    // //      ship channel location being processed)
    // if (argsMap.keySet().contains("--nonAdjFMFInputDataInfo")) {
    //   this.nonAdjFMFInputDataInfo= argsMap.get("--nonAdjFMFInputDataInfo");
    //   throw new RuntimeException(mmi+"Usage of the non-adjusted full model forecast not ready yet!!");
    //    //slog.info(mmi+"Will use this.nonAdjFMFInputDataInfo="+this.nonAdjFMFInputDataInfo);  
    // }
    
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
    final List<Path> nsTidePredInputDataDirFilesList= WLToolsIO
      .getRelevantFilesList(nsTidePredInputDataInfoStrSplit[1], "*"+this.scReachIntrpUnit.getScLocFNameCommonPrefix()+"*"+IWLToolsIO.JSON_FEXT);

    this.scLocsNonAdjData= new HashMap<String, MeasurementCustomBundle>();

    if (this.scReachIntrpUnit.getScLoopStartIndex() == 0 ) {
      throw new RuntimeException(mmi+"this.scReachIntrpUnit.getScLoopStartIndex() cannot be 0 here!");
    } 
    
    slog.info(mmi+"Reading the non-adjusted WL data for all the in-between ship channel points locations");
    
    // --- Now read the related non-adjusted WL prediction data for the in-between ship channel
    //     points locations.
    //
    //     TODO: Loop on the String keys of the this.scLocsDistances HashMap (as it is done in the getAdjustment method)
    //           instead of looping on the related integer indices.      
    for (int idx= this.scReachIntrpUnit.getScLoopStartIndex();
	   idx <= this.scReachIntrpUnit.getScLoopEndIndex(); idx++) {

      // --- Specific file name prefix string to use for the ship channel point location
      //     being processed
      final String scLocFNameSpecSubStr=
	this.scReachIntrpUnit.getScLocFNameCommonPrefix() +
	  IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(idx);	

      //slog.info(mmi+"idx="+idx+", scLocFNameSpecSubStr="+scLocFNameSpecSubStr);
      
      final String scLocFilePath= WLToolsIO
     	.getSCLocFilePath(nsTidePredInputDataDirFilesList, scLocFNameSpecSubStr +IWLToolsIO.JSON_FEXT);

      try {
        scLocFilePath.length();
      } catch (NullPointerException npe) {
        throw new RuntimeException(mmi+"Non-adjusted input file with name extension -> "+
				   scLocFNameSpecSubStr+ IWLToolsIO.JSON_FEXT + " not found");
      }

      //slog.info(mmi+"idx="+idx+". Reading scLocFilePath="+scLocFilePath+" for scLocFNameSpecSubStr="+scLocFNameSpecSubStr);
      
      this.scLocsNonAdjData.put(scLocFNameSpecSubStr,
				new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInCHSJsonFmt(scLocFilePath, -1L, 0.0, null)));
       
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    }
    
    slog.info(mmi+"Done reading the non-adjusted WL data for all the in-between ship channel points locations");
    
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    // --- now read the non-adjusted NSTide prediction WL data at the two ship channel locations
    //     that are the nearest to the two tide gauges being processed. Use the convention that the ship channel
    //     locations are ordered from lower to upper indices.

    slog.info(mmi+"this.scReachIntrpUnit.getLowerSideScLocStrId()="+this.scReachIntrpUnit.getLowerSideScLocStrId());

    final String lowerSideScLocFile= WLToolsIO
      .getSCLocFilePath(nsTidePredInputDataDirFilesList, this.scReachIntrpUnit.getLowerSideScLocStrId() + IWLToolsIO.JSON_FEXT);

    try {
      lowerSideScLocFile.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"Non-adjusted input file with name extension -> "+
				 this.scReachIntrpUnit.getLowerSideScLocStrId() + IWLToolsIO.JSON_FEXT + " not found");
    }
    
    slog.info(mmi+"lowerSideScLocFile="+lowerSideScLocFile);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    this.tgsNearestSCLocsNonAdjData.put(this.scReachIntrpUnit.getLowerSideScLocStrId(),
			                new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInCHSJsonFmt(lowerSideScLocFile, -1L, 0.0, null)));
   
    slog.info(mmi+"this.scReachIntrpUnit.getUpperSideScLocStrId()="+this.scReachIntrpUnit.getUpperSideScLocStrId());
    
    final String upperSideScLocFile= WLToolsIO
      .getSCLocFilePath(nsTidePredInputDataDirFilesList, this.scReachIntrpUnit.getUpperSideScLocStrId() + IWLToolsIO.JSON_FEXT);

    try {
      upperSideScLocFile.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"Non-adjusted input file with name extension -> "+
				 this.scReachIntrpUnit.getUpperSideScLocStrId() + IWLToolsIO.JSON_FEXT + " not found");
    }
   
    slog.info(mmi+"upperSideScLocFile="+upperSideScLocFile);

    this.tgsNearestSCLocsNonAdjData.put(this.scReachIntrpUnit.getUpperSideScLocStrId(),
	     		                new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInCHSJsonFmt(upperSideScLocFile, -1L, 0.0, null)));

 
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

    final String lowerSideScLocTGId= this.scReachIntrpUnit.getLowerSideScLocTGId();
    final String upperSideScLocTGId= this.scReachIntrpUnit.getUpperSideScLocTGId();

    slog.info(mmi+"lowerSideScLocTGId="+lowerSideScLocTGId);
    slog.info(mmi+"upperSideScLocTGId="+upperSideScLocTGId);

    final String lowerSideScLocStrId= this.scReachIntrpUnit.getLowerSideScLocStrId();
    final String upperSideScLocStrId= this.scReachIntrpUnit.getUpperSideScLocStrId();

    slog.info(mmi+"lowerSideScLocStrId="+lowerSideScLocStrId);
    slog.info(mmi+"upperSideScLocStrId="+upperSideScLocStrId);
    
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);    
    
    // --- Get the 1st (otherwise said the least recent in time) Instant object related
    //     to the lower side tide gauge
    final Instant lowSideLocLeastRecentInstant=
      tgsNearestSCLocsAdjFMF.get(lowerSideScLocTGId).getLeastRecentInstantCopy();

    // --- Get the 1st (otherwise said the least recent in time) Instant object related
    //     to the upper side tide gauge    
    final Instant uppSideLocLeastRecentInstant=
      tgsNearestSCLocsAdjFMF.get(upperSideScLocTGId).getLeastRecentInstantCopy();

    // --- Find which of those two Instant object is the most recent in time.
    //     (this is done to ensure tha tw ehave time synchronization between
    //      the two sets of adjusted FMF WL data). The lowerSideScLocTGId is
    //      selected if both Instant objects have the same timestamp value.
    final String tgLocWithMostRecentInstantId= lowSideLocLeastRecentInstant.
      isAfter(uppSideLocLeastRecentInstant) ? lowerSideScLocTGId : upperSideScLocTGId;

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
    this.scLocsAdjLTFP.put(lowerSideScLocStrId, new ArrayList<MeasurementCustom>());
    this.scLocsAdjLTFP.put(upperSideScLocStrId, new ArrayList<MeasurementCustom>());

    // --- Get the String keys of all the in-between ship channel points locations
    //     to loop on them to apply the WL adjustements at their locations.
    //     NOTE: We do not need a SortedSet here, the order is not important
    //     for the in-between ship channel points locations for the remainder
    //     of the processing
    //final Set<String> scLocsDistancesKeySet= this.scLocsDistances.keySet();
    //final SortedSet<String> scLocsKeySet= new TreeSet<String>(this.scLocsNonAdjData.keySet());
    final Set<String> scLocsKeySet= this.scLocsNonAdjData.keySet();

    // --- Allocate all the List<MeasurementCustom> objects that are in-between the
    //     two ship channel points locations that are the nearest to the two tide gauges being processed
    for (final String scLocStrId: scLocsKeySet ) {
      this.scLocsAdjLTFP.put(scLocStrId, new ArrayList<MeasurementCustom>());
    }

    // --- Spare costly division operations in loops, multiply by the inverted value instead.
    final double tgsNearestsLocsDistRadInv= 1.0/this.scReachIntrpUnit.getTgsNearestsLocsDistRad();
	
    // --- Loop on all the Instants of the adj. FMF data to adjust the WLs (either
    //     WL predictions or non-adj. FMF data) for the in-between ship channel
    //     points locations. We use a simple spatial linear interpolation of the
    //     (adj. FMF WL at TGs - non-adjusted WL at TGs) residuals using the distance
    //     in radians between the ship channel location that is the nearest to the
    //     lower side TG and the in-between ship channel point location where we
    //     have to adjust the WLs.
    for (final Instant adjFMFInstant: adjFMFInstantsSet) {
	
	//slog.info(mmi+"adjFMFInstant="+adjFMFInstant.toString());

      // --- Get the adj. FMF WL at the lower side ship channel location.
      //     (need to use the lowerSideScLocTGId String key for the lower side tide gauge here)
      final MeasurementCustom lowerSideSCLocAdjFMFMc= this
	.tgsNearestSCLocsAdjFMF.get(lowerSideScLocTGId).getAtThisInstant(adjFMFInstant);

      // --- Set the lower side ship channel location adj. MeasurementCustom object directly
      //     using its FMF WL MeasurementCustom (no need for interp. here). Need to use
      //     the related lower side ship channel point location String key here.
      this.scLocsAdjLTFP.get(lowerSideScLocStrId).add( new MeasurementCustom(lowerSideSCLocAdjFMFMc) );

      // --- Get the adj. FMF WL at the upper side ship channel location
      //     (need to use the upperSideScLocTGId String key for the upper side tide gauge here)
      final MeasurementCustom upperSideSCLocAdjFMFMc= this
	.tgsNearestSCLocsAdjFMF.get(upperSideScLocTGId).getAtThisInstant(adjFMFInstant);

      // --- Set the upper side ship channel location adj. MeasurementCustom object directly
      //     using its FMF WL MeasurementCustom (no need for interp. here). Need to use
      //     the related upper side ship channel point location String key here.     
      this.scLocsAdjLTFP.get(upperSideScLocStrId).add( new MeasurementCustom(upperSideSCLocAdjFMFMc) );

      // --- Now get the two (adj. FMF WL at TGs - non-adjusted WL at TGs) residuals for the lower and
      //     upper side locations
      final double lowerSideAdjFMFValue= lowerSideSCLocAdjFMFMc.getValue();
      final double upperSideAdjFMFValue= upperSideSCLocAdjFMFMc.getValue();

      //slog.info(mmi+"lowerSideAdjFMFValue="+lowerSideAdjFMFValue);
      //slog.info(mmi+"upperSideAdjFMFValue="+upperSideAdjFMFValue);
      //slog.info(mmi+"lowerSideScLocStrId="+lowerSideScLocStrId);
      //slog.info(mmi+"upperSideScLocStrId="+upperSideScLocStrId);

      final double lowerSideAdjFMFValUncrt= lowerSideSCLocAdjFMFMc.getUncertainty();
      final double upperSideAdjFMFValUncrt= upperSideSCLocAdjFMFMc.getUncertainty();

      //slog.info(mmi+"lowerSideAdjFMFValUncrt="+lowerSideAdjFMFValUncrt);
      //slog.info(mmi+"upperSideAdjFMFValUncrt="+upperSideAdjFMFValUncrt);
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
      final double lowerSideNonAdjValue= this
	.tgsNearestSCLocsNonAdjData.get(lowerSideScLocStrId).getAtThisInstant(adjFMFInstant).getValue();

      final double upperSideNonAdjValue= this
	.tgsNearestSCLocsNonAdjData.get(upperSideScLocStrId).getAtThisInstant(adjFMFInstant).getValue();

      //slog.info(mmi+"lowerSideNonAdjValue="+lowerSideNonAdjValue);
      //slog.info(mmi+"upperSideNonAdjValue="+upperSideNonAdjValue);

      final double lowerSideResValue= lowerSideAdjFMFValue - lowerSideNonAdjValue;
      final double upperSideResValue= upperSideAdjFMFValue - upperSideNonAdjValue;

      //slog.info(mmi+"lowerSideResValue="+lowerSideResValue);
      //slog.info(mmi+"upperSideResValue="+upperSideResValue);

      // --- Avoid doing the same upperSideResValue - lowerSideResValue subtraction
      //     for all the ship channel point locations for the Instant being processed
      final double uppResValMinusLowResVal= upperSideResValue - lowerSideResValue;

      // --- Avoid doing the same upperSideAdjFMFValUncrt - lowerSideAdjFMFValUncrt subtraction
      //     for all the ship channel point locations for the Instant being processed
      final double uppUctValMinusLowUctVal= upperSideAdjFMFValUncrt - lowerSideAdjFMFValUncrt;
       
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
      // --- Now adjust the non-adjusted WL prediction (or non-adjusted FMF) data for the in-between ship channel
      //     points locations using the two residual values and the distance from the lower side ship channel location
      //     NOTE: Order is not really important here since we have stored the related distances in the this.scLocsDistances HashMap
      for (final String scLocStrId: scLocsKeySet ) { //scLocsDistancesKeySet) {
	  
	//slog.info(mmi+"scLocStrId="+scLocStrId);

	final double scLocDistRad= this
	  .scReachIntrpUnit.getDistanceForScLoc(scLocStrId);

	//slog.info(mmi+"scLocDistRad="+scLocDistRad);
	//slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);	

	final double scNonAdjValue= this.scLocsNonAdjData.
	  get(scLocStrId).getAtThisInstant(adjFMFInstant).getValue();

	//slog.info(mmi+"scNonAdjValue="+scNonAdjValue);

	// --- Avoid doing the same mult. two times for adjusting both
	//     the WL value and its related uncertainty
	final double interpFactor= tgsNearestsLocsDistRadInv * scLocDistRad;

	// --- Using just two costly multiplications here instead of four.
	//     NOTE: recall that the scLocDistRad is the distance between the lower side
	//           ship channel location and the in-between ship channel point location
	//           being processed.
        final double scAdjValue= scNonAdjValue + lowerSideResValue + interpFactor * uppResValMinusLowResVal; //(upperSideResValue - lowerSideResValue);
      
	  //tgsNearestsLocsDistRadInv * scLocDistRad * (upperSideResValue - lowerSideResValue);
	  //(tgsNearestsLocsDistRadInv * scLocDistRad) * upperSideResValue + (1.0 - tgsNearestsLocsDistRadInv * scLocDistRad) * lowerSideResValue;

        //slog.info(mmi+"scAdjValue="+scAdjValue+"\n");

	// --- Linearly interpolate the uncertainties at the ship channel location
	//     being processed.
	final double scAdjValUncrt= lowerSideAdjFMFValUncrt + interpFactor * uppUctValMinusLowUctVal; //(upperSideAdjFMFValUncrt - lowerSideAdjFMFValUncrt);
	
	//tgsNearestsLocsDistRadInv * scLocDistRad * (upperSideAdjFMFValUncrt - lowerSideAdjFMFValUncrt) ;

        //slog.info(mmi+"scAdjValUncrt="+scAdjValUncrt);
	//slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);
      	
	// --- Set the MeasurementCustom with the interpolated values (WL, related uncertainty)
	//     for the adjFMFInstant at at the ship channel location being processed.
	this.scLocsAdjLTFP.get(scLocStrId).add(new MeasurementCustom(adjFMFInstant, scAdjValue, scAdjValUncrt));

	//if (scLocStrId.equals("gridPoint-49")) {
	//slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);
	//}
	  
      } // --- for (final String scLocStrId: scLocsDistancesKeySet) inner loop block
	  
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
    } // --- for(final Instant adjFMFInstant: adjFMFInstantsSet) outer loop block

    slog.info(mmi+"writing the adj. WLs for all the ship channel point locations that are in-between the two tide gauges");
    
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
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
      final String lowerSideScLocWLAdjOutFName= outputFileNamesPrfx + lowerSideScLocStrId;

      slog.info(mmi+"Writing the adj. WLs of the ship channel point location related to the lower side tide gauge");
      
      WLToolsIO.writeToOutputDir(this.scLocsAdjLTFP.get(lowerSideScLocStrId),
   			         IWLToolsIO.Format.CHS_JSON, lowerSideScLocWLAdjOutFName, outputDirectory);
    }
    
    if (this.spinePPWriteCtrl == IWLAdjustment.SpinePPWriteCtrl.UPPER_SIDE || 
	  this.spinePPWriteCtrl == IWLAdjustment.SpinePPWriteCtrl.BOTH_SIDES ) {

      // --- Upper side
      final String upperSideScLocWLAdjOutFName= outputFileNamesPrfx + upperSideScLocStrId;

      slog.info(mmi+"Writing the adj. WLS of the ship channel point location related to the the upper side tide gauge");
      
      WLToolsIO.writeToOutputDir(this.scLocsAdjLTFP.get(upperSideScLocStrId),
        			 IWLToolsIO.Format.CHS_JSON, upperSideScLocWLAdjOutFName, outputDirectory);
    }
    
    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- return null here to signal to the main class that
    //     all the results have already been written in the
    //     output folder by this class 
    return null;
    
    //return this.locationAdjustedData; //adjustmentRet;
  }
}
