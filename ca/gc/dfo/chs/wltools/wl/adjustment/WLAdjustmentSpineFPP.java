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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;
import java.util.SortedSet;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Collection;
import java.util.NavigableSet;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.DirectoryStream;
import java.nio.file.StandardCopyOption;

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

  private Instant whatTimeIsItNow= null;

    //private Instant fmfBegAdjustInstantPast= null;

  private Instant fmfBegAdjustInstantFutr= null;

  private SortedSet<Instant> fmfInstantsInPast= null;

  private long fmfTimeIntrvSeconds= -1L;
    
  // --- TODO: Define the iwlsApiBaseUrl String with an --iwlsApiBaseUrl option passed to the main script. 
  //private final String iwlsApiBaseUrl= "https://api.test.iwls.azure.cloud.dfo-mpo.gc.ca/api/v1/stations";
  private final String iwlsApiBaseUrl="https://api-iwls.dfo-mpo.gc.ca/api/v1/stations";
    
  private Map<String,WLSCReachIntrpUnit> scReachIntrpUnits= null;

    //private Map<TideGaugeConfig, MeasurementCustomBundle> wloMCBundles= null;

  private List<MeasurementCustomBundle> mcbsFromS104DCF8= null;

  //private Map<TideGaugeConfig,Double> tgsResiduals= null;
  private Map<TideGaugeConfig,Map<Instant,Double>> tgsResiduals= null;

  private Map<TideGaugeConfig,Double> tgsResidualsAvgs= null;
    
  private Map<TideGaugeConfig,TideGaugeConfig> upstreamDownstreamTGPairs= null;
    
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
    
    // --- 
    this.whatTimeIsItNow= Instant.now();

    final String whatTimeIsItNowStr= whatTimeIsItNow.toString();

    slog.info(mmi+"whatTimeIsItNowStr="+(whatTimeIsItNowStr));

    final String whatTimeIsItNowStrHH= whatTimeIsItNowStr.substring(0,14)+"00:00Z";

    slog.info(mmi+"whatTimeIsItNowStrHH="+whatTimeIsItNowStrHH);
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);   
    
    // --- Get the Instant object that represents the actual hh hour at hh:00:00
    //     in order to have the Spine API working properly (i.e. for its timestamps
    //     offsets used for old-school array indexing). We will use it to do FMF WL
    //     adjustments in the past until we reach the Instant that was determined
    //     in the future.
    final Instant fmfBegAdjustInstantPast= Instant.parse(whatTimeIsItNowStrHH);

    //slog.info(mmi+"this.fmfBegAdjustInstantPast="+this.fmfBegAdjustInstantPast.toString());
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);    

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

    //final long fmfTimeIntrvSeconds= this.mcbsFromS104DCF8.get(0).getDataTimeIntervallSeconds();
    this.fmfTimeIntrvSeconds= this.mcbsFromS104DCF8.get(0).getDataTimeIntervallSeconds();

    slog.info(mmi+"this.fmfTimeIntrvSeconds="+this.fmfTimeIntrvSeconds);
    //slog.info(mmi+"debug exit 0");
    ///System.exit(0);

    // --- 
    //final Instant whatTimeIsItNow= Instant.now();
    //this.whatTimeIsItNow= Instant.now();

    final Instant fmfLeastRecentInstant= this.mcbsFromS104DCF8.get(0).getLeastRecentInstantCopy();
    slog.info(mmi+"fmfLeastRecentInstant="+fmfLeastRecentInstant.toString());

    final Instant fmfMostRecentInstant= this.mcbsFromS104DCF8.get(0).getMostRecentInstantCopy();
    slog.info(mmi+"fmfMostRecentInstant="+fmfMostRecentInstant.toString());

    // --- Now check if we have at least sufficient days of data in the future for the S104 DCF8 input file.
    //     (If not we stop the exec??)
    final long timeDiffSeconds= fmfMostRecentInstant.getEpochSecond() - this.whatTimeIsItNow.getEpochSecond();

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
    System.out.flush();
    
    // --- TODO: Now check if we have at least 15 days of data in the future for the S104 DCF8 input file.
    //     If not we stop the exec.
    // --- TODO: Tell the HDFql world to use just one thread here.
    
    // --- Get the info about the QUE IWLS "stations" to determine
    //     their ids (string) 
    final JsonArray iwlsStationsInfo= WLToolsIO
      .getJsonArrayFromAPIRequest(this.iwlsApiBaseUrl+"?chs-region-code=QUE");

    slog.info(mmi+"Done with populating the JsonArray with the IWLS API request results");
    System.out.flush();

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
    //     (Normally 6 hours in seconds in the past) for the time offset
    //     in the past we want to have.
    final Instant timeOffsetInPast= this.whatTimeIsItNow.minusSeconds(SHORT_TERM_FORECAST_TS_OFFSET_SECONDS);

    // --- Add MIN_FULL_FORECAST_DURATION_SECONDS from now
    //     (Normally at least 48 hours in seconds in the future) for the time offset
    //     in the future we want to have.
    final Instant timeOffsetInFutr= this.whatTimeIsItNow.plusSeconds(MIN_FULL_FORECAST_DURATION_SECONDS);

    final String timeOffsetInPastStr= timeOffsetInPast.toString();
    final String timeOffsetInFutrStr= timeOffsetInFutr.toString();

    final String timeOffsetInPastReqStr= timeOffsetInPastStr.substring(0,13) + IWLToolsIO.IWLS_DB_DTIME_END_STR;
    final String timeOffsetInFutrReqStr= timeOffsetInFutrStr.substring(0,13) + IWLToolsIO.IWLS_DB_DTIME_END_STR;

    slog.info(mmi+"timeOffsetInPastReqStr="+timeOffsetInPastReqStr);
    slog.info(mmi+"timeOffsetInFutrReqStr="+timeOffsetInFutrReqStr);
    System.out.flush();

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
	System.out.flush();

	// --- Get the MeasurementCustomBundle object of the valid WLO data
	//     for this TG but only for timestamps that are consistent with the fmfTimeIntrvSeconds
	final MeasurementCustomBundle checkMcb= WLToolsIO
	  .getMCBFromIWLSJsonArray(iwlsJSTGWLOData, this.fmfTimeIntrvSeconds, tgCfg.getZcVsVertDatum(), true); // true);

	slog.info(mmi+"aft. getMCBFromIWLSJsonArray()");
	System.out.flush();

	// --- Add the MeasurementCustomBundle object of the valid WLO data
	//     for this TG. If checkMcb == null this means that there is either
	//     not enough valid WLO data or no data at all for this TG
	if ( checkMcb != null ) {
	    
	  wloMCBundles.put(tgCfg, checkMcb);
	  
          slog.info(mmi+"Got "+wloMCBundles.get(tgCfg).size()+" valid WLO data for TG -> "+tgNumStrCode);
	  
	} else {
	  slog.warn(mmi+"WARNING!!: Not enough valid WLO data or no data at all for this TG now-> "+tgNumStrCode);
	}

	System.out.flush();

	//if (tgNumStrCode.equals("03335")) {
	//slog.info(mmi+"debug exit 0");
        //System.exit(0);
	//}

	slog.info(mmi+"Done with getting WLO for TG -> "+tgNumStrCode);
	
      } // --- End if-else block
    } // --- outer for loop block

    //Set<TideGaugeConfig> tgsWithValidWLOData= wloMCBundles.keySet();

    //// --- Debug: Remove when done.
    //wloMCBundles.clear();

    slog.info(mmi+"Got "+ wloMCBundles.keySet().size()+" TGs with valid WLO data before checking time sync. with FMF data");
    System.out.flush();

    if (wloMCBundles.keySet().size() == 0) {
      slog.warn(mmi+"wloMCBundles.keySet().size() == 0 !! No TGs with valid WLO data !!");
    }

    //slog.info(mmi+"debug exit 0");
    //System.exit(0);

    // --- Define an Instant in the future of astronomic proportion.
    //final Instant astronomicFuturInstant= Instant.ofEpochSecond(Instant.MAX.getEpochSecond()-3600L);

    // --- Copy this astronomicFuturInstant in tgsLeastRecentValidWLOInstant to use it as a check.
    //Instant tgsLeastRecentValidWLOInstant= astronomicFuturInstant.plusSeconds(0L);
    //Instant tgsLeastRecentValidWLOInstant= Instant.ofEpochSecond(Instant.MAX.getEpochSecond()-3600L);

    // --- Define an Instant at the UNIX zero time reference to use to determine the most recent
    //     valid Instant of the WLO data
    Instant tgsMostRecentValidWLOInstant= Instant.ofEpochSecond(0L);

    Set<TideGaugeConfig> tgsWithValidWLOData= new HashSet<TideGaugeConfig>();

    // --- Loop on the tide gauges that have valid WLO data.
    //    (Note that it it very unlikely that we can have 0 tide gauges with valid data
    //     but it is not impossible)
    for (final TideGaugeConfig tgCfg: wloMCBundles.keySet()) {
       
      slog.info(mmi+"Checking WLO data time sync with the FMF data for tide gauge -> "+ tgCfg.getIdentity());

      final Instant tgWLOMostRecentInstant= wloMCBundles.get(tgCfg).getMostRecentInstantCopy();

      slog.info(mmi+"tgWLOMostRecentInstant="+tgWLOMostRecentInstant.toString());

      if (tgWLOMostRecentInstant.isBefore(fmfLeastRecentInstant)) {
	  
	slog.warn(mmi+"WARNING: most recent WLO data timestamp is in the past compared to FMF data for tide gauge -> "+
		  tgCfg.getIdentity()+", rejecting it for the adjustments!");

	//wloMCBundles.remove(tgCfg);
	//tgsWithValidWLOData.remove(tgCfg);
	
	continue;
      }

	//tgsLeastRecentValidWLOInstant= (tgWLOMostRecentInstant
	//.isBefore(tgsLeastRecentValidWLOInstant) ) ? tgWLOMostRecentInstant : tgsLeastRecentValidWLOInstant;

      tgsMostRecentValidWLOInstant= (tgWLOMostRecentInstant
	.isAfter(tgsMostRecentValidWLOInstant) ) ? tgWLOMostRecentInstant : tgsMostRecentValidWLOInstant;

      tgsWithValidWLOData.add(tgCfg);
    }

    // --- Update the tgsWithValidWLOData Set in case one TG was removed
    //tgsWithValidWLOData= wloMCBundles.keySet();
    
    slog.info(mmi+"Got "+tgsWithValidWLOData.size()+" TGs with valid WLO data after checking time sync. with FMF data");

    //if (tgsLeastRecentValidWLOInstant.equals())

    if (tgsWithValidWLOData.size() != 0)  {

	//slog.info(mmi+"tgsLeastRecentValidWLOInstant="+tgsLeastRecentValidWLOInstant.toString());
      slog.info(mmi+"tgsMostRecentValidWLOInstant="+tgsMostRecentValidWLOInstant.toString());

      // --- previous code
      // --- Define the Instant at which we will begin the adjustments of the FMF
      //     (i.e. it is the 1st Instant after the leastRecentValidWLOInstant of all
      //     the existing valid WLO Instants for all the tide gauges used).
      //this.fmfBegAdjustInstant= tgsLeastRecentValidWLOInstant.plusSeconds(fmfTimeIntrvSeconds);

      // --- NEW CODE
      // --- Define the Instant in the future at which we will begin the adjustments of the FMF
      //     (i.e. it is the 1st Instant after the tgsMostRecentValidWLOInstant of all
      //     the existing valid WLO Instants for all the tide gauges used).
      this.fmfBegAdjustInstantFutr= tgsMostRecentValidWLOInstant.plusSeconds(fmfTimeIntrvSeconds);     

    } else {

      // --- NOTE: This is highly unlikely to happen but in this case it is obvious that no adjustement of the
      //     FMF data will be done and this case is handled later in the code when we check if we have two
      //     neighbor TGs that do not have any valid WLO data.
      slog.warn(mmi+"tgsWithValidWLOData.size() == 0 !!, need to use the actual time to define this.fmfBegAdjustInstantFutr Instant !!");

      // --- Get the FMF Instant that is either this.whatTimeIsItNow itself (very unlikely) or
      //     just after this.whatTimeIsItNow Instant.
      this.fmfBegAdjustInstantFutr= this.mcbsFromS104DCF8.get(0)
	.getInstantsKeySetCopy().tailSet(this.whatTimeIsItNow).first(); //.plusSeconds(fmfTimeIntrvSeconds);
    }
    
    slog.info(mmi+"this.fmfBegAdjustInstantFutr="+this.fmfBegAdjustInstantFutr.toString());
    System.out.flush();

    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    
    // --- Build a Map<TideGaugeConfig,Double> for the (WLO-FMF) residuals.
    //Map<TideGaugeConfig,Double> tgsResiduals= new HashMap<TideGaugeConfig,Double>(this.locations.size());
    //this.tgsResiduals= new HashMap<TideGaugeConfig,Double>(this.locations.size());
    //this.tgsResiduals= new HashMap<TideGaugeConfig,MeasurementCustomBundle>(this.locations.size());
    this.tgsResiduals= new HashMap<TideGaugeConfig, Map<Instant,Double>> (this.locations.size());

    //final SortedSet<Instant> fmfInstantsInPast= this.mcbsFromS104DCF8
    this.fmfInstantsInPast= this.mcbsFromS104DCF8.get(0)
      .getInstantsKeySetCopy().subSet(fmfBegAdjustInstantPast,this.fmfBegAdjustInstantFutr);

    slog.info(mmi+"this.fmfInstantsInPast.first="+this.fmfInstantsInPast.first().toString());
    slog.info(mmi+"this.fmfInstantsInPast.last="+this.fmfInstantsInPast.last().toString());
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);

    this.tgsResidualsAvgs= new HashMap<TideGaugeConfig,Double>(this.locations.size());
    
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

      //this.tgsResiduals.put(tgCfg, null);
      //this.tgsResidualsAvgs.put(tgCfg, 0.0);

      double tgFMFResidualsAvg= 0.0;

      // --- Could possibly have somt TG without WLO data.
      if (tgsWithValidWLOData.contains(tgCfg)) {

	if (!tgCfg.isConfigOkay()) {
	  throw new RuntimeException(mmi+"Must have tgCfg.isConfigOkay() == true here for TG -> "+tgCfg.getIdentity());
	}
	  
        final int tgNearestScLocIndex= Integer.parseInt(tgCfg
	  .getNearestSpinePointId().split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[1]);

        slog.info(mmi+"tgNearestScLocIndex="+tgNearestScLocIndex);
	    
	slog.info(mmi+"Got some valid WLO for TG -> "+tgCfg.getIdentity());
	//System.out.flush();

	this.tgsResiduals.put(tgCfg, new HashMap<Instant,Double>());

	//this.tgsResidualsAvgs.put(tgCfg, 0.0);

	// --- Loop on the Instant objects in this.fmfInstantsInPast
        for (final Instant instantInPast: this.fmfInstantsInPast) {
	
	  // --- Use the getNearestTSMCWLDataNeighbor here in case
	  //     the instantInPast does not exists
	  //     for the WLO data of this tide gauge. Use the
	  //     this.fmfTimeIntrvSeconds time threshold here.
	  final MeasurementCustom wloMCCheck= wloMCBundles.get(tgCfg)
	    .getNearestTSMCWLDataNeighbor(instantInPast,this.fmfTimeIntrvSeconds);
	  
	  // --- No WLO data that is at least MAX_WLO_NEAREST_TIME_DATA_INTERVAL_SECONDS in the past compared to
	  //     the tgsMostRecentValidWLOInstant, remove this tide gauge from the spatial interpolation procedure
	  //     and consider it as another simple grid point
          if (wloMCCheck == null) {

	    slog.warn(mmi+"WARNING: no WLO data at Instant ->"+instantInPast.toString()+" for TG -> "+tgCfg.getIdentity());
	    //	      " for TG -> "+tgCfg.getIdentity()+" which will not be used for the spatio-temporal interpolation of the residuals");  
	    //tgsResiduals.put(tgCfg, null);
	  
	  } else { 
	
	    final double tgWLOAtInstant= wloMCCheck.getValue();
	    //  .get(tgCfg).getAtThisInstant(tgsLeastRecentValidWLOInstant).getValue();

            final MeasurementCustom fmfMCCheck= this.mcbsFromS104DCF8
              .get(tgNearestScLocIndex).getAtThisInstant(instantInPast);		
	    //.get(tgNearestScLocIndex).getAtThisInstant(tgsMostRecentValidWLOInstant);
	    //.get(tgNearestScLocIndex).getAtThisInstant(tgsLeastRecentValidWLOInstant);

	    try {
	      fmfMCCheck.getValue();
	    } catch (NullPointerException npe) {
	      throw new RuntimeException(mmi+npe+"fmfMCCheck cannot be null here !!");
	    }
	  
	    final double tgFMFAtInstant= fmfMCCheck.getValue();
	    //  .get(tgNearestScLocIndex).getAtThisInstant(tgsLeastRecentValidWLOInstant).getValue();

	    final double fmfResidual= tgWLOAtInstant-tgFMFAtInstant;
	  
	    //this.tgsResiduals.put(tgCfg,tgWLOAtInstant-tgFMFAtInstant);
	    this.tgsResiduals.get(tgCfg).put(instantInPast,fmfResidual);
	
            //slog.info(mmi+"tgWLOAtInstant="+tgWLOAtInstant);
	    //slog.info(mmi+"tgFMFAtInstant="+tgFMFAtInstant);
	    //slog.info(mmi+"(WLO - FMF) residual="+this.tgsResiduals.get(tgCfg).get(instantInPast)+
	    //          " at TG -> "+tgCfg.getIdentity()+" at Instant -> "+instantInPast.toString());
	    //System.out.flush();

	    tgFMFResidualsAvg += fmfResidual;
	    //this.tgsResidualsAvgs.put(tgCfg, this.tgsResidualsAvgs.get(tgCfg) += fmfResidual);

	    //slog.info(mmi+"debug exit 0");
            //System.exit(0);
	    
	  } // --- inner if-else block
	} // ---  Loop on the Instant objects in this.fmfInstantsInPast

        if (this.tgsResiduals.get(tgCfg).size()==0) {
	    //throw new RuntimeException(mmi+"this.tgsResiduals.get(tgCfg).size() cannot be 0 here !!");
            
	    slog.warn(mmi+"No valid WLO found for TG -> "+tgCfg.getIdentity()+
		      " in the near past, this TG location will be simply considered as another grid point");

	    this.tgsResiduals.put(tgCfg, null);
	    
	} else {
	    
	  this.tgsResidualsAvgs.put(tgCfg, tgFMFResidualsAvg/this.tgsResiduals.get(tgCfg).size());
	  
	  // --- re-Loop on the Instant objects in this.fmfInstantsInPast to fill-up
	  //     possible missing FMF residuals with their average for this TG
          for (final Instant instantInPast: this.fmfInstantsInPast) {
	      
	    if (!this.tgsResiduals.get(tgCfg).containsKey(instantInPast)) {
		
	      slog.warn(mmi+"Missing residual for TG -> "+tgCfg.getIdentity()+" at Instant -> "+
			instantInPast.toString()+" replacing it by the TG residuals average -> "+this.tgsResidualsAvgs.get(tgCfg));
	      
	      this.tgsResiduals.get(tgCfg).put(instantInPast, this.tgsResidualsAvgs.get(tgCfg));
	    }
	  }

	  slog.info(mmi+"this.tgsResidualsAvgs="+this.tgsResidualsAvgs.get(tgCfg)+" for TG -> "+tgCfg.getIdentity());
	}
	
	//slog.info(mmi+"debug exit 0");
        //System.exit(0);	
	
      } else {
	
	slog.warn(mmi+"No valid WLO to use for residual at TG -> "+
		  tgCfg.getIdentity()+" this TG will be simply considered as another grid point");
	
	this.tgsResiduals.put(tgCfg, null);
      }	
    }

    //slog.info(mmi+"debug exit 0");
    //System.exit(0);

    // --- Set the residuals of the upstreammost TG at 0.0
    //     in case it is not available
    if (this.tgsResiduals.get(this.locations.get(0)) == null ) {

      final TideGaugeConfig tgCfg= this.locations.get(0);
	
      slog.warn(mmi+"WARNING: No valid WLO to use for residual at upstreammost TG -> "+
		tgCfg.getIdentity()+", just setting its residuals at 0.0");
      
      this.tgsResiduals.put(tgCfg, new HashMap<Instant,Double>());
      
      for (final Instant instantInPast: this.fmfInstantsInPast) {
	this.tgsResiduals.get(tgCfg).put(instantInPast,0.0);
      }
    }
    
    // --- Set the residuals of the downstreammost TG at 0.0
    //     in case it is not available
    if (this.tgsResiduals.get(this.locations.get(this.locations.size()-1)) == null ) {

      final TideGaugeConfig tgCfg= this.locations.get(this.locations.size()-1);	
      	
      slog.warn(mmi+"WARNING: No valid WLO to use for residual at downstreammost TG -> "+
		tgCfg.getIdentity()+", just setting its residuals at 0.0");

      this.tgsResiduals.put(tgCfg, new HashMap<Instant,Double>());
      
      for (final Instant instantInPast: this.fmfInstantsInPast) {
        this.tgsResiduals.get(tgCfg).put(instantInPast,0.0);
      }
      
      //this.tgsResiduals.put(this.locations.get(this.locations.size()-1), 0.0);
    }

    // --- Now check if we do not have two adjacent TGs without (WLO-FMF) residuals to use.
    //     DO not do any adjustment if it is the case and simply update the output file
    //     with the FMF WL values of the S104 DCF8 file and the related uncertainties.
    //     We also need to remove the TGs where there is no residual to use for the
    //     Spine FPP adjustments.
    //boolean doAdjust= true;
    this.doAdjust= true;

    //Map<TideGaugeConfig,TideGaugeConfig> upstreamDownstreamTGPairs= new HashMap<TideGaugeConfig,TideGaugeConfig>();
    this.upstreamDownstreamTGPairs= new HashMap<TideGaugeConfig,TideGaugeConfig>();
    
    for (int tgLocIdx= 1; tgLocIdx < this.locations.size(); tgLocIdx++) {

      TideGaugeConfig dnstreamTGCfg= this.locations.get(tgLocIdx);
      TideGaugeConfig upstreamTGCfg= this.locations.get(tgLocIdx-1);

      slog.info(mmi+"Checking TGs pair: upstreamTGCfg="+
		upstreamTGCfg.getIdentity()+" and dnstreamTGCfg="+dnstreamTGCfg.getIdentity());

      // --- Set doAdjust to false if two neighbor TGs have no (WLO-FMF) residuals to use
      //     and break the loop. No Spine FPP adjustment will be done when this happens.
      //     NOTE: This handles the cast where no WLO data is available for all TGs.
      if ( (this.tgsResiduals.get(dnstreamTGCfg) == null) && (this.tgsResiduals.get(upstreamTGCfg) == null) ) {

	slog.warn(mmi+"No valid residual to use at TGs neighbors -> "+
		 dnstreamTGCfg .getIdentity()+" and -> "+upstreamTGCfg.getIdentity()+", No Spine FPP adjustment will be done for this run!!");
	
	this.doAdjust= false;  
	break;
      }

      // --- Simply continue with the next TG in case the upstreamTGCfg has
      //     no valid residual to use, this case was handled by the previous
      //     loop iteration
      if (this.tgsResiduals.get(upstreamTGCfg) == null) {
	slog.warn(mmi+"this.tgsResiduals.get(upstreamTGCfg) == null, skipping this TG -> "+upstreamTGCfg.getIdentity()+" as an upstream TG");
	continue;
      }
      
      // --- Replace the dnstreamTGCfg by its nearest downstream TG neighbor            
      if (this.tgsResiduals.get(dnstreamTGCfg) == null) {
	  
	slog.warn(mmi+"Removing TG -> "+dnstreamTGCfg.getIdentity()+
		  " from the Spine FPP adjustments and replacing it with its nearest downstream neighbor-> "+this.locations.get(tgLocIdx+1).getIdentity());

        // --- NOTE: The downstream most TG residual located at this.locations.get(tgLocIdx)
        //           is never null in this.tgsResiduals so we never get there for this last TG
	//           and this get should always works. 
	dnstreamTGCfg= this.locations.get(tgLocIdx+1);	
      } 

      upstreamDownstreamTGPairs.put(upstreamTGCfg,dnstreamTGCfg);

      slog.info(mmi+"Valid TGs pair to use: upstreamTGCfg="+
		upstreamTGCfg.getIdentity()+" and dnstreamTGCfg="+dnstreamTGCfg.getIdentity());
 
    } // --- for loop block
	
    // --- TODO: Write the output file according to the required format.    
    slog.info(mmi+"end, this.doAdjust="+this.doAdjust);
    
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
  }

  // --- 
  final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

    final String mmi= "getAdjustment: ";
      
    //List<MeasurementCustom> ret= null;
    //Map<Integer,MeasurementCustomBundle> mcbOutForSpine= null;
    List<MeasurementCustomBundle> mcbOutForSpine= null;
    
    slog.info(mmi+"start");

    //final Instant fmfBegAdjustInstantPast= this.fmfInstantsInPast.first();
    
    // --- Get the Instants that are in the future compared to the last
    //     valid WLO Instant used in the main constructor for this class.
    //final SortedSet<Instant> fmfInstantsInFuture= this.mcbsFromS104DCF8
    //  .get(0).getInstantsKeySetCopy().tailSet(this.fmfBegAdjustInstantFutr);
    //slog.info(mmi+"this.fmfBegAdjustInstantFutr="+this.fmfBegAdjustInstantFutr.toString());
    //slog.info(mmi+"fmfInstantsInFuture.first()="+fmfInstantsInFuture.first());

    // --- Now need to use the Instant objects to include the time elapsed since the last hour at 00 mins and 00 seconds
    //     in order to get the timestamps offsets being compatible with the old-school array indexing used by the actual Spine API.
    final SortedSet<Instant> fmfInstantsPastAndFuture= this.mcbsFromS104DCF8
      .get(0).getInstantsKeySetCopy().tailSet(this.fmfInstantsInPast.first());

    slog.info(mmi+"this.fmfInstantsInPast.first()="+this.fmfInstantsInPast.first());
    slog.info(mmi+"this.fmfInstantsInPast.last()="+this.fmfInstantsInPast.last());
    slog.info(mmi+"this.fmfBegAdjustInstantFutr="+this.fmfBegAdjustInstantFutr.toString());
    slog.info(mmi+"fmfInstantsPastAndFuture.first()="+fmfInstantsPastAndFuture.first());
    slog.info(mmi+"fmfInstantsPastAndFuture.last()="+fmfInstantsPastAndFuture.last());
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    
    // --- Allocate the temp Map<Integer,List<MeasurementCustom>> that will be
    //     used in nested loops for the FMF WL adj. processing for all the ship
    //     channel point locations.
    Map<Integer,List<MeasurementCustom>> mcOutForSpineMap= new HashMap<Integer,List<MeasurementCustom>>();

    // --- Initialize it accordingly using the ship channel point locations indices.
    for (Integer scLocIdx= 0; scLocIdx < this.mcbsFromS104DCF8.size(); scLocIdx++) {
     //mcOutForSpineMap.put(scLocIdx, new ArrayList<MeasurementCustom>( fmfInstantsInFuture.size() ) );
      mcOutForSpineMap.put(scLocIdx, new ArrayList<MeasurementCustom>( fmfInstantsPastAndFuture.size() ) );
    }

    // --- Create the ArrayList of MeasurementCustomBundle objects which will be used
    //     to write the results in the required file format. 
    mcbOutForSpine= new ArrayList<MeasurementCustomBundle>(this.mcbsFromS104DCF8.size());

    // --- It is unlikely that the this.doAdjust would be false
    //     but not impossible.
    if (this.doAdjust) {
	
      slog.info(mmi+"Now setting up the Map<String,WLSCReachIntrpUnit> for doing the Spine FPP adjustments");

      try {
	this.upstreamDownstreamTGPairs.size(); 
      } catch (NullPointerException npe) {
	throw new RuntimeException(mmi+npe+"this.upstreamDownstreamTGPairs cannot be null here !!");
      }

      if (this.upstreamDownstreamTGPairs.size() == 0) {
	throw new RuntimeException(mmi+"this.upstreamDownstreamTGPairs cannot be empty here !!"); 
      }

      // --- Need to use a Map object to keep track of the processing already done for
      //     the upstream and downstream tide gauges in terms of WL FPP adjustments
      //     to avoid duplicating the same result at the same Instant timestamp in the
      //     local mcOutForSpineMap object
      Map<TideGaugeConfig,List<Instant>> tgsInstantsCtrlMap= new HashMap<TideGaugeConfig,List<Instant>>();
      
      // --- Loop on the tide gauges spatial interp. pairs for doing the FPP adjustment type
      //     for all the ship channel point locations.
      for (final TideGaugeConfig upstreamTGCfg: this.upstreamDownstreamTGPairs.keySet()) {

	if (!tgsInstantsCtrlMap.containsKey(upstreamTGCfg)) {
	  tgsInstantsCtrlMap.put(upstreamTGCfg, new ArrayList<Instant>());
	}

	// --- Get the TideGaugeConfig object of the downstream TG that is paired
	//     with the upstream tide gauge which is used as a key in the upstreamDownstreamTGPairs Map
	final TideGaugeConfig dnstreamTGCfg= upstreamDownstreamTGPairs.get(upstreamTGCfg);

        if (!tgsInstantsCtrlMap.containsKey(dnstreamTGCfg)) {
	  tgsInstantsCtrlMap.put(dnstreamTGCfg, new ArrayList<Instant>());
	}
	
	slog.info(mmi+"TG residual interp pair: upstreamTGCfg -> "+
		  upstreamTGCfg.getIdentity()+ " with dnstreamTGCfg -> "+dnstreamTGCfg.getIdentity());
      
	final String upsDnsTGsPair= upstreamTGCfg.getIdentity() +
	  IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR + dnstreamTGCfg.getIdentity();

        slog.info(mmi+"Instantiating the WLSCReachIntrpUnit for the TGs pair -> "+upsDnsTGsPair);
      	
	//this.scReachIntrpUnits.put(upsDnsTGsPair,
	// 			   new WLSCReachIntrpUnit(this.shipChannelPointLocsTCInputDir, this.mainJsonTGInfoMapObj, upstreamTGCfg, dnstreamTGCfg));

        // --- Instantiate the WLSCReachIntrpUnit object which holds the spatial interp. parameters
	//     for this specific part of the local ship channel between the upstream and the dnstream
	//     neighbor tide gauges being processed.
	final WLSCReachIntrpUnit scReachIntrpUnit= new
	  WLSCReachIntrpUnit(this.shipChannelPointLocsTCInputDir, this.mainJsonTGInfoMapObj, upstreamTGCfg, dnstreamTGCfg);
      
	//final double upsTGResidual= this.tgsResiduals.get(upstreamTGCfg);
	//final double dnsTGResidual= this.tgsResiduals.get(dnstreamTGCfg);
	//slog.info(mmi+"upsTGResidual="+upsTGResidual);
	//slog.info(mmi+"dnsTGResidual="+dnsTGResidual);

	double timeOffsetFromLastResidual= this.fmfTimeIntrvSeconds;

	// --- Indices range to use for the ship channel point locations
	//     that are in-between the upstream and the downstream TGs.
	final int scLocEndIdx= scReachIntrpUnit.getScLoopEndIndex();
	final int scLocSrtIdx= scReachIntrpUnit.getScLoopStartIndex();

	if (scLocEndIdx <= scLocSrtIdx) {
	  throw new RuntimeException(mmi+"Inconsistency between the scLocEndIdx -> "+scLocEndIdx+
				     " and the scLocSrtIdx -> "+scLocSrtIdx+", we must have scLocEndIdx > scLocSrtIdx here !");
	}
	
        // --- index of the ship channel point location that is the nearest to
	//     the upstream TG location
	final int upsTGScLocIdx= scReachIntrpUnit.getTg0NearestSCLocIndex();

	// --- index of the ship channel point location that is the nearest to
	//     the downstream TG location
	final int dnsTGScLocIdx= scReachIntrpUnit.getTg1NearestSCLocIndex();

	if (dnsTGScLocIdx <= upsTGScLocIdx) {
	  throw new RuntimeException(mmi+"Inconsistency between the dnsTGScLocIdx -> "+dnsTGScLocIdx+
				     " and the upsTGScLocIdx -> "+upsTGScLocIdx+", we must have dnsTGScLocIdx > upsTGScLocIdx here !");
	}

	if (scLocSrtIdx != upsTGScLocIdx+1) {
	  throw new RuntimeException(mmi+"Inconsistency between the scLocSrtIdx -> "+scLocSrtIdx+
				     " and the upsTGScLocIdx -> "+upsTGScLocIdx+", we must have scLocSrtIdx == upsTGScLocIdx + 1 here !");
	}
	
	if (scLocEndIdx != dnsTGScLocIdx-1) {
	  throw new RuntimeException(mmi+"Inconsistency between the scLocEndIdx -> "+scLocEndIdx+
				     " and the dnsTGScLocIdx -> "+dnsTGScLocIdx+", we must have scLocEndIdx == dnsTGScLocIdx - 1 here !");
	}
	
	slog.info(mmi+"upsTGScLocIdx="+upsTGScLocIdx);
        slog.info(mmi+"scLocSrtIdx="+scLocSrtIdx);
	slog.info(mmi+"scLocEndIdx="+scLocEndIdx);
	slog.info(mmi+"dnsTGScLocIdx="+dnsTGScLocIdx);

	// // --- NOTE: The lower side location (in terms of ship channel point indices)
	// //     here refers to the nearest ship channel point location to the upstream TG.
	// //     So it is not an error.
	// final String upsScLocIdStr= scReachIntrpUnit.getLowerSideScLocStrId();
	// // --- NOTE: The upper side location (in terms of ship channel point indices)
	// //     here refers to the nearest ship channel point location to the dnstream TG.
	// //     So it is not an error.
	// final String dnsScLocIdStr= scReachIntrpUnit.getUpperSideScLocStrId();
	// slog.info(mmi+"upsScLocIdStr="+upsScLocIdStr);
	// slog.info(mmi+"dnsScLocIdStr="+dnsScLocIdStr);
	// slog.info(mmi+"debug exit 0");
        // System.exit(0);

	final double lastUpsTGResidual= this.tgsResiduals.get(upstreamTGCfg).get(this.fmfInstantsInPast.last());
	final double lastDnsTGResidual= this.tgsResiduals.get(dnstreamTGCfg).get(this.fmfInstantsInPast.last());
	
	slog.info(mmi+"lastUpsTGResidual="+lastUpsTGResidual);
	slog.info(mmi+"lastDnsTGResidual="+lastDnsTGResidual);
	//slog.info(mmi+"debug exit 0");
        //System.exit(0);
	
	// --- Get the time decaying factor for the residual at the upstream TG
	final double upsShortTermFMFTSOffsetSecInv=
	  1.0/(IWLAdjustment.SHORT_TERM_FORECAST_TS_OFFSET_SECONDS * (1.0 + Math.exp(Math.abs(lastUpsTGResidual))));

        // --- Get the time decaying factor for the residual at the downstream TG      
	final double dnsShortTermFMFTSOffsetSecInv=
	  1.0/(IWLAdjustment.SHORT_TERM_FORECAST_TS_OFFSET_SECONDS * (1.0 + Math.exp(Math.abs(lastDnsTGResidual))));	

        // --- Spare costly division operations in nested loops, just multiply by the inverted denominator value
	//     instead for the distance in radians between the upstream and downstream TGs that will be used
	//     in the spatial interpolations.
        final double tgsNearestsLocsDistRadInv= 1.0/scReachIntrpUnit.getTgsNearestsLocsDistRad();
	
	//Map<Integer,List<MeasurementCustom>> scLocsAdjValuesMap= new HashMap<Integer,List<MeasurementCustom>>();
	//scLocsAdjValuesMap.put(upsTGScLocIdx, new ArrayList<MeasurementCustom>(fmfInstantsInFuture.size()));
	//scLocsAdjValuesMap.put(dnsTGScLocIdx, new ArrayList<MeasurementCustom>(fmfInstantsInFuture.size()));
        //for (int scLocIterIdx= scLocSrtIdx; scLocIterIdx <= scLocEndIdx; scLocIterIdx++) {
	//  scLocsAdjValuesMap.put(scLocIterIdx, new ArrayList<MeasurementCustom>(fmfInstantsInFuture.size()));
	//}
	
	// --- Loop on the FMF Instant of the future (compared to the last valid WLO used for the residuals) 
	//for (final Instant fmfInstantFutr: fmfInstantsInFuture) {
	for (final Instant fmfInstantIter: fmfInstantsPastAndFuture) { 
	    
	  slog.info(mmi+"fmfInstantIter="+fmfInstantIter.toString());
	    
	  double upsTGResTimeDecayingFactor= 1.0;
	  double dnsTGResTimeDecayingFactor= 1.0;

	  double upsTGResidual= 0.0;
	  double dnsTGResidual= 0.0;

	  if (fmfInstantIter.isAfter(this.fmfInstantsInPast.last())) {

	    // --- in the future, get the last FMF residuals 
	    upsTGResidual= lastUpsTGResidual;
	    dnsTGResidual= lastDnsTGResidual;
	    
	    // --- Time decaying factor for the upstream side (a function
	    //     of the last residual value at the upstream TG)
	    upsTGResTimeDecayingFactor= Math
	      .exp(-timeOffsetFromLastResidual * upsShortTermFMFTSOffsetSecInv);

	    // --- Time decaying factor for the dnstream side (a function
	    //     of the last residual value at the dnstream TG)	    
	    dnsTGResTimeDecayingFactor= Math
	      .exp(-timeOffsetFromLastResidual * dnsShortTermFMFTSOffsetSecInv);	    
	    
	  } else {

	    // --- In the past, get the needed FMF residuals
	    upsTGResidual= this.tgsResiduals.get(upstreamTGCfg).get(fmfInstantIter);
	    dnsTGResidual= this.tgsResiduals.get(dnstreamTGCfg).get(fmfInstantIter);
          }

	  slog.info(mmi+"upsTGResidual="+upsTGResidual);
	  slog.info(mmi+"dnsTGResidual="+dnsTGResidual);
	  slog.info(mmi+"debug exit 0");
	  System.exit(0);	  

	  final MeasurementCustom upsMcFMFAtInstantIter= this
	    .mcbsFromS104DCF8.get(upsTGScLocIdx).getAtThisInstant(fmfInstantIter);
	  
	  // --- non-adj. FMF WL value of ship channel point location that
	  //     is the nearest to the upstream TG location at this fmfInstantIter Instant
	  final double upsTGScLocNonAdjValue= upsMcFMFAtInstantIter.getValue();	  
	  
	  // --- FMF WL offset to add to the upsTGScLocNonAdjValue to adjust it
	  //     in function of the upsTGResidual value and its related Time decaying facto
	  final double upsTGScLocAdjOffet= upsTGResidual * upsTGResTimeDecayingFactor;
	  
	  // --- Get the adj. FMF WL value of ship channel point location that
	  //     is the nearest to the upstream TG location at this fmfInstantFutr Instant
	  final double upsTGScLocAdjValue= upsTGScLocNonAdjValue + upsTGScLocAdjOffet; //upsTGResidual * upsTGResTimeDecayingFactor;
 
	  // --- FMF MeasurementCustom of ship channel point location that
	  //     is the nearest to the downstream TG location at this fmfInstantIter Instant	  
          final MeasurementCustom dnsMcFMFAtInstantIter= this
	    .mcbsFromS104DCF8.get(dnsTGScLocIdx).getAtThisInstant(fmfInstantIter);
	      
	  // --- non-adj. FMF WL value of ship channel point location that
	  //     is the nearest to the downstream TG location at this fmfInstantFutr Instant
          final double dnsTGScLocNonAdjValue= dnsMcFMFAtInstantIter.getValue();

      	  // --- FMF WL offset to add to the dnsTGScLocNonAdjValue to adjust it
	  //     in function of the dnsTGResidual value and its related Time decaying facto
          final double dnsTGScLocAdjOffet= dnsTGResidual * dnsTGResTimeDecayingFactor;

	  // --- Get the adj. FMF WL value of ship channel point location that
	  //     is the nearest to the downstream TG location at this fmfInstantFutr Instant
          final double dnsTGScLocAdjValue= dnsTGScLocNonAdjValue + dnsTGScLocAdjOffet; //dnsTGResidual * dnsTGResTimeDecayingFactor;

	  // slog.info(mmi+"timeOffsetFromLastResidual="+timeOffsetFromLastResidual);

	  //slog.info(mmi+"upsTGResTimeDecayingFactor="+upsTGResTimeDecayingFactor);
          //slog.info(mmi+"upsTGScLocNonAdjValue="+upsTGScLocNonAdjValue);
	  //slog.info(mmi+"upsTGScLocAdjOffet="+upsTGScLocAdjOffet);
	  //slog.info(mmi+"upsTGScLocAdjValue="+upsTGScLocAdjValue);

	  //slog.info(mmi+"dnsTGResTimeDecayingFactor="+dnsTGResTimeDecayingFactor);
	  //slog.info(mmi+"dnsTGScLocNonAdjValue="+dnsTGScLocNonAdjValue);
	  //slog.info(mmi+"dnsTGScLocAdjOffet="+dnsTGScLocAdjOffet);
	  //slog.info(mmi+"dnsTGScLocAdjValue="+dnsTGScLocAdjValue);
	  //slog.info(mmi+"debug exit 0");
	  //System.exit(0);

	  //slog.info(mmi+"tgsInstantsCtrlMap.get(upstreamTGCfg).size()="+tgsInstantsCtrlMap.get(upstreamTGCfg).size());
	  
          // --- Avoid duplicating the adj. WL value for the same Instant
	  //     for the tide gauges point locations at the ends of this ship
	  //     channel section (reach) if fmfInstantFutr already exists in
	  //     the tgsInstantsCtrlMap
          if (!tgsInstantsCtrlMap.get(upstreamTGCfg).contains(fmfInstantIter)) {
	  
	    // --- Take the uncertainties as thet are from the FMF 4 times/day runs for now
            mcOutForSpineMap.get(upsTGScLocIdx)
	      .add(new MeasurementCustom(fmfInstantIter.plusSeconds(0L), upsTGScLocAdjValue, upsMcFMFAtInstantIter.getUncertainty()));

	    tgsInstantsCtrlMap.get(upstreamTGCfg).add(fmfInstantIter.plusSeconds(0L));
	  }

	  if (!tgsInstantsCtrlMap.get(dnstreamTGCfg).contains(fmfInstantIter)) {    
	      
	    mcOutForSpineMap.get(dnsTGScLocIdx)
	      .add(new MeasurementCustom(fmfInstantIter.plusSeconds(0L), dnsTGScLocAdjValue, dnsMcFMFAtInstantIter.getUncertainty()));

	    tgsInstantsCtrlMap.get(dnstreamTGCfg).add(fmfInstantIter.plusSeconds(0L));
	  }

	  //slog.info(mmi+"debug exit 0");
          //System.exit(0);

	  // --- Avoid doing this same subtraction for all the ship channel point locations
	  //     for the Instant being processed. It is this difference in the time-decaying
	  //     WL adj, offset that is spatially interpolated on the ship channel points
	  //     location to adjust their WL accordingly
	  final double dnsAdjValMinusUpsAdjVal= dnsTGScLocAdjOffet - upsTGScLocAdjOffet;

	  // --- Loop on the ship channel point locations that are in-between the
	  //     two neighbor tide gauges (upstream -> downstream).
	  for (int scLocIterIdx= scLocSrtIdx; scLocIterIdx <= scLocEndIdx; scLocIterIdx++) {
	      
            // --- Specific file name prefix string to use for the ship channel point location
            //     being processed
            final String scLocFNameSpecSubStr=
	      scReachIntrpUnit.getScLocFNameCommonPrefix() +
	        IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(scLocIterIdx);	

            slog.debug(mmi+"scLocFNameSpecSubStr="+scLocFNameSpecSubStr);

	    // --- Get the great circle distance (in radians) of this in-between ship channel point location
	    //     from the ship channel point location that is the nearest to the upstream TG location,
	    final double scLocDistRadFromUpsTG= scReachIntrpUnit.getDistanceForScLoc(scLocFNameSpecSubStr);

	    slog.debug(mmi+"scLocDistRadFromUpsTG="+scLocDistRadFromUpsTG);

	    final double interpFactor= tgsNearestsLocsDistRadInv * scLocDistRadFromUpsTG;
	    
	    // --- Get the non-adj. FMF WL value at this ship channel point location at
	    //     the fmfInstantFutr Instant
            final MeasurementCustom scLocMcAtInstantIter= this
	      .mcbsFromS104DCF8.get(scLocIterIdx).getAtThisInstant(fmfInstantIter);
	    
	    // --- Get the non-adj. FMF WL value at this ship channel point location at
	    //     the fmfInstantFutr Instant.
	    final double nonAdjFMFSCWLVal= scLocMcAtInstantIter.getValue();

	    // --- Apply the spatial interpolation of the time decaying upstream and downstream
	    //     WL adjustment offsets values using the short-cutted calculation to speed-up the exec.
	    final double adjFMFSCWLVal= nonAdjFMFSCWLVal + upsTGScLocAdjOffet + interpFactor * dnsAdjValMinusUpsAdjVal;
	    
            // --- Put the adjFMFSCWLVal with its uncertainty in the mcOutForSpineMap for this
	    //     ship channel point location.
	    mcOutForSpineMap.get(scLocIterIdx)
	      .add(new MeasurementCustom(fmfInstantIter.plusSeconds(0L), adjFMFSCWLVal, scLocMcAtInstantIter.getUncertainty()));
	    
	    slog.debug(mmi+"nonAdjFMFSCWLVal="+nonAdjFMFSCWLVal);
	    slog.debug(mmi+"adjFMFSCWLVal="+adjFMFSCWLVal);
	    slog.debug(mmi+"End for scLocIterIdx="+scLocIterIdx);
	    //slog.info(mmi+"debug exit 0");
            //System.exit(0);
	    
	  } // --- for loop block scLocIter

	  // --- incr the timeOffsetFromLastResidual for the next iteration on time. 
	  timeOffsetFromLastResidual += this.fmfTimeIntrvSeconds;
	  
	  //slog.info(mmi+"End for fmfInstantFutr="+fmfInstantFutr.toString());
	  //slog.info(mmi+"debug exit 0");
          //System.exit(0);
	  
	} // --- for loop block fmfInstantFutr

	slog.info(mmi+"Done with TG residual interp pair: upstreamTGCfg -> "+
		  upstreamTGCfg.getIdentity()+ " with dnstreamTGCfg -> "+dnstreamTGCfg.getIdentity());

	//slog.info(mmi+"debug exit 0");
        //System.exit(0);
	      
      } // --- for loop block upstreamTGCfg

      // --- Loop on all the ship channel point locations indices again
      for (Integer scLocIterIdx= 0; scLocIterIdx< this.mcbsFromS104DCF8.size(); scLocIterIdx++ ) {
	mcbOutForSpine.add(new MeasurementCustomBundle(mcOutForSpineMap.get(scLocIterIdx)));
      }
      
    } else {
	
      slog.warn(mmi+"WARNING: doAdjust == false !! simply use the non-ajusted FMF data for the Spine outputs.");

      // ---
      for (Integer scLocIterIdx= 0; scLocIterIdx< this.mcbsFromS104DCF8.size(); scLocIterIdx++ ) {

	//slog.info(mmi+"scLocIterIdx="+scLocIterIdx);
	  
	//// --- IMPORTANT: Need to only use the Instant objects of the future here.
        //for (final Instant fmfInstantFutr: fmfInstantsInFuture) {
	for (final Instant fmfInstantIter: fmfInstantsPastAndFuture) { 
	  mcOutForSpineMap.get(scLocIterIdx)
	    .add(this.mcbsFromS104DCF8.get(scLocIterIdx).getAtThisInstant(fmfInstantIter));
	}
	
	mcbOutForSpine.add(new MeasurementCustomBundle(mcOutForSpineMap.get(scLocIterIdx)));
      }
      
      //mcbOutForSpine= this.mcbsFromS104DCF8;
    }

    // --- Now write the needed Spine output file with the wanted output
    //     format using the mcbOutForSpine List of MeasurementCustomBundle
    
    WLToolsIO.writeSpineAPIInputData(this.whatTimeIsItNow, mcbOutForSpine);
      
    slog.info(mmi+"end");
    
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);     

    // --- return null here to signal to the main class that
    //     all the results have already been written in the
    //     output folder by this class 
    return null;
  }
}
