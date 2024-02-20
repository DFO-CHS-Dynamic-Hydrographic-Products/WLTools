package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.net.URL;
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
//import java.net.URLEncoder;
import java.nio.file.Paths;
import java.io.InputStream;
import java.util.SortedSet;
import java.util.ArrayList;
//import java.util.ListIterator;
import java.nio.file.Files;
import java.util.Collection;
import java.util.NavigableSet;
//import java.nio.file.PathMatcher;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.DirectoryStream;
import java.nio.file.StandardCopyOption;
//import java.io.UnsupportedEncodingException;
//import java.nio.file.DirectoryIteratorException;

// ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

// ---
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// // ---
// import as.hdfql.HDFql;
// import as.hdfql.HDFqlCursor;
// import as.hdfql.HDFqlConstants;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.TideGaugeConfig;
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
final public class WLAdjustmentSpineFPP extends WLAdjustmentSpinePP implements IWLAdjustment {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpineFPP: ";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  private boolean doAdjust= true;
    
  // --- TODO: Define the iwlsApiBaseUrl String with an --iwlsApiBaseUrl option passed to the main script. 
  private final String iwlsApiBaseUrl= "https://api.test.iwls.azure.cloud.dfo-mpo.gc.ca/api/v1/stations";
    
  private Map<String,WLSCReachIntrpUnit> scReachIntrpUnits= null;

    //private Map<TideGaugeConfig, MeasurementCustomBundle> wloMCBundles= null;

  private List<MeasurementCustomBundle> mcbsFromS104DCF8= null;

  private Map<TideGaugeConfig,Double> tgsResiduals= null;

  // --- To store the non-ajusted WL predictions OR non-adjusted FMF data at the ship channel point locations that are
  //     in-between the tide gauges locations being processed (INPUT ONLY) 
  //private Map<String, MeasurementCustomBundle> scLocsNonAdjData= null;

  // --- To store the non-ajusted WL predictions OR non-adjusted FMF data at the two ship channel point locations that are
  //     the nearest to the nearest tide gauges locations (INPUT ONLY)
  //private Map<String, MeasurementCustomBundle>
  //  tgsNearestSCLocsNonAdjData= new HashMap<String, MeasurementCustomBundle>(2); //null;

  //private String fmfReferenceDateTimeStr= null;
    
  /**
   * Comments please!
   */
  public WLAdjustmentSpineFPP() {
    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  // ---
  public WLAdjustmentSpineFPP(/*@NotNull*/ final HashMap<String,String> argsMap) {

    super(IWLAdjustment.Type.SpineFPP,argsMap);

    final String mmi= "WLAdjustmentSpineFPP main constructor: ";

    slog.info(mmi+"start");

    try {
      this.locations.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"this.locations cannot be null here !");
    }

    if (this.locations.size() == 0) {
      throw new RuntimeException(mmi+"this.locations cannot be empty here !");
    }

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

    try {
      WLToolsIO.getOutputDataFormat();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe+"WLToolsIO.getOutputDataFormat() cannot be null here !");
    }

    // --- Verify the output file(s) format before going further
    //if (!WLToolsIO.getOutputDataFormat().equals(IWLToolsIO.Format.DHP_S104_DCF8.name())) {
    
    if (!WLToolsIO.getOutputDataFormat().equals(IWLToolsIO.Format.LEGACY_ASCII.name())) {
      throw new RuntimeException(mmi+"Invalid output file(s) data format -> "+
				 WLToolsIO.getOutputDataFormat()+" for the SpineFPP adjustment type sub-tool!");
    }

    if (!argsMap.containsKey("--localDownloadDirectory")) {
      throw new RuntimeException(mmi+
	"Must have the --localDownloadDirectory=<path of the folder where to download data> defined in the argsMap !!");
    }

    final String localDownloadDirectory= argsMap.get("--localDownloadDirectory");

    slog.info(mmi+"localDownloadDirectory="+localDownloadDirectory);

    if (!argsMap.containsKey("--inputFileURLToGet")) {
      throw new RuntimeException(mmi+
	"Must have the --inputFileURLToGet=<Complete https URL of the S104 DCF8 file that has to be downloaded> defined in the argsMap !!");
    }

    final String inputFileURLToGet= argsMap.get("--inputFileURLToGet");

    slog.info(mmi+"inputFileURLToGet="+inputFileURLToGet);
    
    final String inputFileNameToGet= new File(inputFileURLToGet).getName();

    slog.info(mmi+"inputFileNameToGet="+inputFileNameToGet);
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);

    final String inputFileDldLocalDest= localDownloadDirectory + File.separator + inputFileNameToGet;

    slog.info(mmi+"inputFileDldLocalDest="+inputFileDldLocalDest);
    
    boolean success= true;
    
    // --- 
    try {
      Files.copy( new URL(inputFileURLToGet).openStream(),
		  Paths.get(inputFileDldLocalDest), StandardCopyOption.REPLACE_EXISTING);
      
    } catch (IOException ioe) {
      slog.warn(mmi+" Problem with the 1st try for the "+inputFileURLToGet+" download !");
      success= false;		
    }

    slog.info(mmi+"1st download try done, success="+success);

    if (!success) {	
      try {
        Files.copy( new URL(inputFileURLToGet).openStream(),
		    Paths.get(inputFileDldLocalDest), StandardCopyOption.REPLACE_EXISTING);
	
      } catch (IOException ioe) {
	//throw new RuntimeException(mmi+ioe+" Also a problem with the 2nd try for the "+inputFileURLToGet+" download !");
        slog.warn(mmi+" Also a problem with the 2nd try for the "+inputFileURLToGet+" download !");
      }	
    }

    if (success) {
      slog.info(mmi+"Success for the "+inputFileURLToGet+" download to local destination: "+inputFileDldLocalDest);
    } else {
      slog.warn(mmi+"Had a problem to download the S104 DCF8 input file, will use the previous input file!");
    }

    // --- Check for the S104 DCF8 input file existence on local disk whatever its download has succeded or not
    if (!WLToolsIO.checkForFileExistence(inputFileDldLocalDest)) {
      throw new RuntimeException(mmi+" inputFileDldLocalDest -> "+inputFileDldLocalDest+" not found !");
    }

    // --- Now get a List of MeasurementCustomBundle objects from the S104 DCF8 input HDF5 file
    //     that contains the full model WL forecast (FMF) for all the ship channel points locations. 
    this.mcbsFromS104DCF8= WLToolsIO.getMCBsFromS104DCF8File(inputFileDldLocalDest);

    final long fmfTimeIntrvSeconds= this.mcbsFromS104DCF8.get(0).getDataTimeIntervallSeconds();

    slog.info(mmi+"fmfTimeIntrvSeconds="+fmfTimeIntrvSeconds);
    //slog.info(mmi+"debug exit 0");
    ///System.exit(0);

    // --- 
    final Instant whatTimeIsItNow= Instant.now();

    final Instant fmfLeastRecentInstant= this.mcbsFromS104DCF8.get(0).getLeastRecentInstantCopy();
    slog.info(mmi+"fmfLeastRecentInstant="+fmfLeastRecentInstant.toString());

    final Instant fmfMostRecentInstant= this.mcbsFromS104DCF8.get(0).getMostRecentInstantCopy();
    slog.info(mmi+"fmfMostRecentInstant="+fmfMostRecentInstant.toString());

    // --- Now check if we have at least sufficient days of data in the future for the S104 DCF8 input file.
    //     (If not we stop the exec??)
    final long timeDiffSeconds= fmfMostRecentInstant.getEpochSecond() - whatTimeIsItNow.getEpochSecond();

    final long fmfNbDaysInFutr= timeDiffSeconds/SECONDS_PER_DAY;
    slog.info(mmi+"fmfNbDaysInFutr="+fmfNbDaysInFutr);

    if (fmfNbDaysInFutr < 0L)   {
      throw new RuntimeException(mmi+"Invalid full model forecast data: it is completely in the past compared to now !!");
    }

    final long fmfNbHoursInFutr= timeDiffSeconds/SECONDS_PER_HOUR;
    slog.info(mmi+"fmfNbHoursInFutr="+fmfNbHoursInFutr);

    if (fmfNbHoursInFutr < 0)   {
      throw new RuntimeException(mmi+"Invalid full model forecast data: less than 72 hours in the future compared to now !!");
    }

    if (fmfNbDaysInFutr < SPINE_FPP_WARN_NBDAYS_INFUTR) {
      slog.warn(mmi+"fmfNbDaysInFutr < "+SPINE_FPP_WARN_NBDAYS_INFUTR+" days in the future compared to now  !!"); 
    }   
    
    slog.info(mmi+"Now getting the last WLO data for the TGs from the IWLS API");
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    
    // --- TODO: Now check if we have at least 15 days of data in the future for the S104 DCF8 input file.
    //     If not we stop the exec.
    // --- TODO: Tell the HDFql world to use just one thread here.
    
    // --- Get the info about the QUE IWLS "stations" to determine
    //     their ids (string) 
    final JsonArray iwlsStationsInfo= WLToolsIO
      .getJsonArrayFromAPIRequest(this.iwlsApiBaseUrl+"?chs-region-code=QUE");

    try {
      iwlsStationsInfo.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }
   
    if (iwlsStationsInfo.size() == 0) {
      throw new RuntimeException(mmi+"iwlsStationsInfo.size() cannot be 0 here !!");
    }
    
    //slog.info(mmi+"iwlsStationsInfo.getJsonObject(2).getString(code)="+iwlsStationsInfo.getJsonObject(2).getString("code"));
    //System.out.flush();
    
    // ---
    //Map<TideGaugeConfig, String> tgsData= new HashMap<TideGaugeConfig,String>();
    //this.wloMCBundles= new HashMap<TideGaugeConfig, MeasurementCustomBundle>();
    Map<TideGaugeConfig, MeasurementCustomBundle> wloMCBundles= new HashMap<TideGaugeConfig, MeasurementCustomBundle>();

    //// --- 
    //final Instant whatTimeIsItNow= Instant.now();

    // --- Subtract SHORT_TERM_FORECAST_TS_OFFSET_SECONDS from now
    //     (Normally 6 hours in seconds in the past)
    final Instant timeOffsetInPast= whatTimeIsItNow.minusSeconds(SHORT_TERM_FORECAST_TS_OFFSET_SECONDS);
    final Instant timeOffsetInFutr= whatTimeIsItNow.plusSeconds(MIN_FULL_FORECAST_DURATION_SECONDS);

    final String timeOffsetInPastStr= timeOffsetInPast.toString();
    final String timeOffsetInFutrStr= timeOffsetInFutr.toString();

    final String timeOffsetInPastReqStr= timeOffsetInPastStr.substring(0,13) + IWLToolsIO.IWLS_DB_DTIME_END_STR;
    final String timeOffsetInFutrReqStr= timeOffsetInFutrStr.substring(0,13) + IWLToolsIO.IWLS_DB_DTIME_END_STR;

    slog.info(mmi+"timeOffsetInPastReqStr="+timeOffsetInPastReqStr);
    slog.info(mmi+"timeOffsetInFutrReqStr="+timeOffsetInFutrReqStr);
    //System.out.flush();

    // --- Build the String for the IWLS API request in terms of time frame
    final String timePastFutrFrameReqStr=
      "data?time-series-code=wlo&from="+timeOffsetInPastReqStr+"&to="+timeOffsetInFutrReqStr;
    
    // slog.info(mmi+"debug exit 0");
    // System.exit(0);
    
    // --- Loop on the TGs of this.locations ArrayList (TGs num str ids.
    //     passed as argument to the main program.
    for (int tgIter= 0; tgIter < this.locations.size(); tgIter++) {

      final TideGaugeConfig tgCfg= this.locations.get(tgIter);

      //tgCfg.setConfig(this.mainJsonTGInfoMapObj.getJsonObject());
	
      final String tgNumStrCode= tgCfg.getIdentity();

      slog.info(mmi+"tgNumStrCode="+tgNumStrCode);
      //System.out.flush();
      
      String iwlsStnId= null;

      // --- Loop on all the IWLS stations retreived from the request on the IWLS API
      //     for getting stations informations.
      for (int jsoIter= 0; jsoIter < iwlsStationsInfo.size(); jsoIter++) {

	final JsonObject iwlsStnInfo= iwlsStationsInfo.getJsonObject(jsoIter);
	  
	final String checkLocNumStrId= iwlsStnInfo.getString(IWLToolsIO.IWLS_DB_TG_NUM_STRID_KEY);

        //final String checkLocName= iwlsStnInfo.getString("officialName");
	//if (checkLocName.equals("Lanoraie")) {
	//if (tgNumStrCode.equals("15860")) {
	//  slog.info(mmi+"Lanoraie?: checkLocName="+checkLocName);
	//}

	if (checkLocNumStrId.equals(tgNumStrCode)) {
	  iwlsStnId= iwlsStnInfo.getString(IWLToolsIO.IWLS_DB_TG_STR_ID_KEY);
	  break;
	}
      } // --- inner for loop block

      // --- TODO: Put the code of this if-else block in another method of
      //     the same class or inside another class
      if (iwlsStnId == null) {
	slog.warn(mmi+"WARNING!: No metadata info was found for TG -> "+tgNumStrCode);
	
      } else {

	slog.info(mmi+"Using IWLS id -> "+iwlsStnId+" for TG -> "+tgNumStrCode);
	//System.out.flush();

        final String tgWLOAPIRequest= this.iwlsApiBaseUrl +
	  File.separator + iwlsStnId + File.separator + timePastFutrFrameReqStr;

	slog.info(mmi+"tgWLOAPIRequest="+tgWLOAPIRequest);
	//System.out.flush();

	// --- Get the more recent WLO data for this TG from the IWLS DB
	final JsonArray iwlsJSTGWLOData= WLToolsIO
	  .getJsonArrayFromAPIRequest(tgWLOAPIRequest);

	if (iwlsJSTGWLOData.size() == 0) {
	  slog.warn(mmi+"WARNING!: Seems that there is no WLO data for TG -> "+tgNumStrCode+", skipping it !!");  
	  continue; // --- the inner for loop
	}

        tgCfg.setConfig(this.mainJsonTGInfoMapObj.getJsonObject(tgNumStrCode));
	
        //slog.info(mmi+"iwlsJSTGWLOData.getJsonObject(0).getJsonNumber(value).doubleValue()="+
	//	  iwlsJSTGWLOData.getJsonObject(0).getJsonNumber("value").doubleValue());

	slog.info(mmi+"bef. getMCBFromIWLSJsonArray()");
	//System.out.flush();

	// --- Get the MeasurementCustomBundle object of the valid WLO data
	//     for this TG but only for timestamps that are consistent with the fmfTimeIntrvSeconds
	final MeasurementCustomBundle checkMcb= WLToolsIO
	  .getMCBFromIWLSJsonArray(iwlsJSTGWLOData, fmfTimeIntrvSeconds, tgCfg.getZcVsVertDatum(), true);

	slog.info(mmi+"aft. getMCBFromIWLSJsonArray()");
	//System.out.flush();

	// --- Add the MeasurementCustomBundle object of the valid WLO data
	//     for this TG. If checkMcb == null this means that there is either
	//     not enough valid WLO data or no data at all for this TG
	if ( checkMcb != null ) {
	    
	  wloMCBundles.put(tgCfg, checkMcb);
	  
          slog.info(mmi+"Got "+wloMCBundles.get(tgCfg).size()+" valid WLO data for TG -> "+tgNumStrCode);
	  
	} else {
	  slog.warn(mmi+"WARNING!!: Not enough valid WLO data or no data at all for this TG now-> "+tgNumStrCode);
	}

	//System.out.flush();
	
	//slog.info(mmi+"debug exit 0");
        //System.exit(0);
	
      } // --- End if-else block
    } // --- outer for loop block

    Set<TideGaugeConfig> tgsWithValidWLOData= wloMCBundles.keySet();

    slog.info(mmi+"Got "+tgsWithValidWLOData.size()+" TGs with valid WLO data before checking time sync. with FMF data");
    //System.out.flush();

    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    
    // --- Now check if the most recent WLO data timestamp is more recent than the
    //     least recent model forecast data timestamp for all the tide gauges that
    //     have valid WLO data. If the most recent time stamp of the WLO data of
    //     a given tide gauge is before the least recent model forecast data timestamp
    //     then this WLO data for this tide gauge is useless for the Spine FPP WL adjustments
    //     and we remove this tide gauge from this.wloMCBundles Map of MeasurementCustomBundle
    //     objects. Also determine the least recent valid Instant of the WLO data to use it to
    //     synchronize the (WLO-FMF) residuals for their spatial interp. between the tide gauges.

    Instant tgsLeastRecentValidWLOInstant= Instant.ofEpochSecond((long)Integer.MAX_VALUE);
    
    for (final TideGaugeConfig tgCfg: tgsWithValidWLOData) {
       
	//slog.info(mmi+"Checking WLO data time sync with the FMF data for tide gauge -> "+ tgCfg.getIdentity());

      final Instant tgWLOMostRecentInstant= wloMCBundles.get(tgCfg).getMostRecentInstantCopy();

      if (tgWLOMostRecentInstant.isBefore(fmfLeastRecentInstant)) {
	  
	slog.warn(mmi+"WARNING: most recent WLO data timestamp is in the past compared to FMF data for tide gauge -> "+
		  tgCfg.getIdentity()+", rejecting it for the adjustments!");

	wloMCBundles.remove(tgCfg);
      }

      tgsLeastRecentValidWLOInstant= (tgWLOMostRecentInstant
	.isBefore(tgsLeastRecentValidWLOInstant) ) ? tgWLOMostRecentInstant : tgsLeastRecentValidWLOInstant;
    }

    // --- Update the tgsWithValidWLOData Set in case one TG was removed
    tgsWithValidWLOData= wloMCBundles.keySet();

    slog.info(mmi+"Got "+tgsWithValidWLOData.size()+" TGs with valid WLO data after checking time sync. with FMF data");

    slog.info(mmi+"tgsLeastRecentValidWLOInstant="+tgsLeastRecentValidWLOInstant.toString());
    //System.out.flush();

    //for (final TideGaugeConfig tgc: tgsWithValidWLOData) {
    //	slog.info(mmi+"tgc id="+tgc.getIdentity());
    //}
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    
    // --- Build a Map<TideGaugeConfig,Double> for the (WLO-FMF) residuals.
    //Map<TideGaugeConfig,Double> tgsResiduals= new HashMap<TideGaugeConfig,Double>(this.locations.size());
    this.tgsResiduals= new HashMap<TideGaugeConfig,Double>(this.locations.size());
    
    // --- Set the residuals at the TGs locations. The residual
    //     Double object is set to null if the WLO data is not
    //     available for a given TG
    for (int tgLocIdx= 0; tgLocIdx < this.locations.size(); tgLocIdx++) {

      final TideGaugeConfig tgCfg= this.locations.get(tgLocIdx);

      //slog.info(mmi+"tgCfg num. str id="+tgCfg.getIdentity());
      //final int tgNearestScLocIndex= Integer.parseInt(tgCfg
      //	.getNearestSpinePointId().split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[1]);
      //slog.info(mmi+"tgNearestScLocIndex="+tgNearestScLocIndex);
      //slog.info(mmi+"debug exit 0");
      //System.out.flush();
      //System.exit(0);   
      
      if (tgsWithValidWLOData.contains(tgCfg)) {

	if (!tgCfg.isConfigOkay()) {
	  throw new RuntimeException(mmi+"Must have tgCfg.isConfigOkay() == true here for TG -> "+tgCfg.getIdentity());
	}
	  
        final int tgNearestScLocIndex= Integer.parseInt(tgCfg
	  .getNearestSpinePointId().split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[1]);

        slog.info(mmi+"tgNearestScLocIndex="+tgNearestScLocIndex);
	    
	slog.info(mmi+"Got some valid WLO for TG -> "+tgCfg.getIdentity());
	//System.out.flush();

	// --- Use the getNearestTSMCWLDataNeighbor here in case
	//     the tgsLeastRecentValidWLOInstant does not exists
	//     for the WLO data of this tide gauge. Use the
	//     fmfTimeIntrvSeconds time threshold here.
	final MeasurementCustom wloMCCheck= wloMCBundles.get(tgCfg)
	  .getNearestTSMCWLDataNeighbor(tgsLeastRecentValidWLOInstant, fmfTimeIntrvSeconds);

        if (wloMCCheck == null) {

	  slog.warn(mmi+"WARNING: no WLO data at Instant ->"+tgsLeastRecentValidWLOInstant.toString()+" for TG ->"+tgCfg.getIdentity());	  
	  tgsResiduals.put(tgCfg, null);
	  
	} else { 
	
	  final double tgWLOAtInstant= wloMCCheck.getValue();
	  //  .get(tgCfg).getAtThisInstant(tgsLeastRecentValidWLOInstant).getValue();

          final MeasurementCustom fmfMCCheck= this.mcbsFromS104DCF8
	    .get(tgNearestScLocIndex).getAtThisInstant(tgsLeastRecentValidWLOInstant);

	  try {
	    fmfMCCheck.getValue();
	  } catch (NullPointerException npe) {
	    throw new RuntimeException(mmi+npe+"fmfMCCheck cannot be null here !!");
	  }
	  
	  final double tgFMFAtInstant= fmfMCCheck.getValue();
	  //  .get(tgNearestScLocIndex).getAtThisInstant(tgsLeastRecentValidWLOInstant).getValue();
	  
	  this.tgsResiduals.put(tgCfg,tgWLOAtInstant-tgFMFAtInstant);
	
          slog.info(mmi+"tgWLOAtInstant="+tgWLOAtInstant);
	  slog.info(mmi+"tgFMFAtInstant="+tgFMFAtInstant);
	  slog.info(mmi+"(WLO - FMF) residual="+this.tgsResiduals.get(tgCfg)+" at TG -> "+tgCfg.getIdentity());
	  //System.out.flush();

	  //slog.info(mmi+"debug exit 0");
          //System.exit(0);
	}
	
      } else {
	slog.warn(mmi+"WARNING: No valid WLO to use for residual at TG -> "+tgCfg.getIdentity());
	this.tgsResiduals.put(tgCfg, null);
      }	
    }

    // --- Set the residuals of the upstreammost TG at 0.0
    //     in case it is not available
    if (this.tgsResiduals.get(this.locations.get(0)) == null ) {

      slog.warn(mmi+"WARNING: No valid WLO to use for residual at upstreammost TG -> "+
		this.locations.get(0).getIdentity()+", just setting it at 0.0");
      
      this.tgsResiduals.put(this.locations.get(0), 0.0);
    }
    
    // --- Set the residuals of the downstreammost TG at 0.0
    //     in case it is not available
    if (this.tgsResiduals.get(this.locations.get(this.locations.size()-1)) == null ) {
	
      slog.warn(mmi+"WARNING: No valid WLO to use for residual at downstreammost TG -> "+
		this.locations.get(this.locations.size()-1).getIdentity()+", just setting it at 0.0");
	
      this.tgsResiduals.put(this.locations.get(this.locations.size()-1), 0.0);
    }

    // --- Now check if we do not have two adjacent TGs without (WLO-FMF) residuals to use.
    //     DO not do any adjustment if it is the case and simply update the output file
    //     with the FMF WL values of the S104 DCF8 file and the related uncertainties.
    //     We also need to remove the TGs where there is no residual to use for the
    //     Spine FPP adjustments.
    //boolean doAdjust= true;
    this.doAdjust= true;

    Map<TideGaugeConfig,TideGaugeConfig> upstreamDownstreamTGPairs= new HashMap<TideGaugeConfig,TideGaugeConfig>();
    
    for (int tgLocIdx= 1; tgLocIdx < this.locations.size(); tgLocIdx++) {

      final TideGaugeConfig dnstreamTGCfg= this.locations.get(tgLocIdx);
      final TideGaugeConfig upstreamTGCfg= this.locations.get(tgLocIdx-1);

      // --- Set doAdjust to false if two neighbor TGs have no (WLO-FMF) residuals to use
      //     and break the loop. No Spine FPP adjustment will be done when this happens.
      if ( (this.tgsResiduals.get(dnstreamTGCfg) == null) && (this.tgsResiduals.get(upstreamTGCfg) == null) ) {

	slog.warn(mmi+"WARNING: No valid residual to use at TGs neighbors -> "+
		 dnstreamTGCfg .getIdentity()+" and -> "+upstreamTGCfg.getIdentity()+", No Spine FPP adjustment will be done here!!");
	
	this.doAdjust= false;  
	break;
      }

      // --- Replace the dnstreamTGCfg by its nearest downstream TG neighbor
      if (this.tgsResiduals.get(dnstreamTGCfg) == null) {
	  
	slog.warn(mmi+"WARNING: Removing TG -> "+dnstreamTGCfg.getIdentity()+
		  " from the Spine FPP adjustments and replacing it with its nearest downstream neighbor-> "+this.locations.get(tgLocIdx+1).getIdentity());
	
        upstreamDownstreamTGPairs.put(upstreamTGCfg,this.locations.get(tgLocIdx+1));
		
      } else {
	  
        // --- We have valid residuals to use for both upstreamTGCfg and dnstreamTGCfg here.
        upstreamDownstreamTGPairs.put(upstreamTGCfg,dnstreamTGCfg);
      }

      // --- simply continue with the next TG in case the upstreamTGCfg has
      //     no valid residual to use, this case was handled by the previous
      //     loop iteration
      if (this.tgsResiduals.get(upstreamTGCfg) == null) {
	continue;
      }
    }

    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    // // --- Do (or not do) the Spine FPP adjustments
    // if (!this.doAdjust) {
    //   slog.warn(mmi+"WARNING: Cannot do the Spine FPP adjustment, the FMF data as read from the HDF5 file will be used in the output file");
    // } else {
	
    if (this.doAdjust) {
	
      slog.info(mmi+"Now setting up the Map<String,WLSCReachIntrpUnit> for doing the Spine FPP adjustments");

      for (final TideGaugeConfig upstreamTGCfg: upstreamDownstreamTGPairs.keySet()) {

	final TideGaugeConfig dnstreamTGCfg= upstreamDownstreamTGPairs.get(upstreamTGCfg);
	  
	slog.info(mmi+"TG residual interp pair: upstreamTGCfg -> "+
		  upstreamTGCfg.getIdentity()+ " with dnstreamTGCfg -> "+dnstreamTGCfg.getIdentity());
	  
      }
      
      //slog.info(mmi+"debug exit 0");
      //System.exit(0);
      
    } // --- if else block

    // --- TODO: Write the output file according to the required format.
    
    slog.info(mmi+"end");
    
    slog.info(mmi+"debug exit 0");
    System.exit(0);
  }

  // --- 
  final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

    final String mmi= "getAdjustment: ";
      
    List<MeasurementCustom> ret= null;
    
    slog.info(mmi+"start");
    
    
    slog.info(mmi+"end");
    slog.info(mmi+"debug exit 0");
    System.exit(0);     
     
    return ret;
  }
}
