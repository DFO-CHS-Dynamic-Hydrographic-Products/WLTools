package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.TreeSet;
import java.time.Instant;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.Collections;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonArrayBuilder;

// ---
import as.hdfql.HDFql;
import as.hdfql.HDFqlCursor;
import as.hdfql.HDFqlConstants;

// ---
import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.util.TimeMachine;
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.wl.IWLMeasurement;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.TideGaugeConfig;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.modeldata.ModelDataExtraction;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPred;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;

/**
 * Comments please!
 */
abstract public class WLAdjustmentIO implements IWLAdjustmentIO, IWLAdjustment {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //protected IWLAdjustmentIO.LocationType locationType= null;

  protected Set<String> argsMapKeySet= null; //argsMap.keySet();

  protected IWLAdjustment.Type adjType= null;

  // --- could be one tide gauge OR one ship channel point location.
  protected WLLocation location= null;

  // --- could be two or more tide gauges.
  protected List<TideGaugeConfig> locations= null;

  // --- To store the considered region bounding box
  //     EPSG:4326 coordinates (South-West corner at index 0
  //     and North-East corber at index 1). The
  //     region bounding box is built with the
  //     smallest (lon,lat) coordinates for the SW
  //     corner and the largest (lon,lat) coordinates
  //     for the North-East corner. This is normally
  //     used only by the SpineIPP and SpineFPP classes.
  //protected List<HBCoords> regionBBox= null;

  //protected String locationId= null;
  protected String locationIdInfo= null;

  protected String fullForecastModelName= "UNKNOWN";

  //protected DataType inputDataType= null;

  protected String tideGaugeWLODataFile= null;

  protected int minNumberOfObs= MIN_NUMBER_OF_OBS;

  protected boolean haveWLOData= true;

  protected Instant fmfLeadTimeInstant= null;

  protected IWLToolsIO.Format obsInputDataFormat= null;
  protected IWLToolsIO.Format predictInputDataFormat= null;

  protected DataTypesFormatsDef modelForecastInputDataFormat= null;

  protected double adjLocationLatitude= 0.0;
  protected double adjLocationLongitude= 0.0;
  protected double adjLocationZCVsVDatum= 0.0;

  //protected ArrayList<WLMeasurement> locationOriginalData= null;
  protected List<MeasurementCustom> locationPredData= null;
  protected List<MeasurementCustom> locationAdjustedData= null;

  //protected Map<String, ArrayList<WLMeasurement>> nearestObsData= null;
  //protected Map<String, ArrayList<WLMeasurement>> nearestModelData= null;

  protected Map<String, List<MeasurementCustom>> nearestObsData= null;
  //protected Map<String, List<MeasurementCustom>> nearestModelData= null; // []= { null, null };

  protected Instant mostRecentWLOInstant= null;
  //protected Instant leastRecentWLOInstant= null;

  protected MeasurementCustomBundle mcbWLO= null;  
    
  protected List< Map<String, List<MeasurementCustom>> > nearestModelData=
    new ArrayList< Map<String, List<MeasurementCustom>> > (FullModelForecastType.values().length);

  protected String modelForecastInputDataInfo= null;
  protected List<String> modelForecastInputDataFiles= null;
  //protected String= modelForecastInputDataInfo= null;

  //protected long obsDataTimeIntervalSeconds= IWLStationPred.TIME_NOT_DEFINED;
  protected long prdDataTimeIntervalSeconds= IWLStationPred.TIME_NOT_DEFINED;
  protected long fmfDataTimeIntervalSeconds= IWLStationPred.TIME_NOT_DEFINED;

  // --- Specific Map to store the nowcast (a.k.a. analysis) data at the
  //     the ship channel locations that are the nearest to the tide gauges.
  protected Map<String, List<MeasurementCustom>> nearestModelNowcastData= new HashMap<String, List<MeasurementCustom>>();
    
  //protected FMS fmsObj= null;
  //protected FMSInput fmsInputObj= null;
  //protected FMSFactory fmsFactoryObj= null;

  /**
   * Comments please!
   */
  public WLAdjustmentIO() {

    this.argsMapKeySet= null;

    this.location= null;

    //this.locationId=
    this.locationIdInfo= null;

    this.tideGaugeWLODataFile= null;

    this.obsInputDataFormat=
      this.predictInputDataFormat= null;

    this.modelForecastInputDataFormat = null;

    this.adjLocationZCVsVDatum=
      this.adjLocationLatitude=
        this.adjLocationLongitude= 0.0;

    this.locationPredData= null;
    this.locationAdjustedData= null;

    this.nearestObsData= null;
    //this.nearestModelData= new Map<String, List<MeasurementCustom>> [2];
    
    this.modelForecastInputDataInfo= null;
    this.modelForecastInputDataFiles= null;
    //this.modelInputDataFiles= null;

    //this.fmsObj= null;
    //this.fmsInputObj= null;
  }

  /**
   * Comments please!
   */
  public WLAdjustmentIO(/*@NotNull*/ final WLAdjustment.Type adjType,
                        /*@NotNull*/ final Map<String,String> argsMap) {

    final String mmi= "WLAdjustmentIO main constructor : ";

    this.adjType= adjType;

    slog.info(mmi+"this.adjType="+this.adjType.name());

    this.argsMapKeySet= argsMap.keySet();
  }

  // ---
  final MeasurementCustomBundle getMcbWLOData() {

    final String mmi= "getMcbWLOData: ";

    try {
      this.nearestObsData.size();
    } catch (NullPointerException e) {

      slog.error(mmi+"this.nearestObsData is null !!");
      throw new RuntimeException(mmi+e);
    }

   try {
      this.location.hashCode();
    } catch (NullPointerException e) {

      slog.error(mmi+"this.location is null !!");
      throw new RuntimeException(mmi+e);
    }

    try {
      this.nearestObsData.get(this.location.getIdentity()).size();
      
    } catch (NullPointerException e) {

      slog.error(mmi+"this.nearestObsData.get(this.location.getIdentity()) is null !!");
      throw new RuntimeException(mmi+e);
    }

    final List<MeasurementCustom> locationMCWLO= this.nearestObsData.get(this.location.getIdentity());

    if (locationMCWLO.size() == 0 ) {
      throw new RuntimeException(mmi+"ERROR: Cannot have locationMCWLO.size() == 0 at this point !!");
    }

    // --- Create a local MeasurementCustomBundle object with the WLO data
    //     List<MeasurementCustom> object for this TG location.
    //final MeasurementCustomBundle mcbWLO= new
    //  MeasurementCustomBundle( this.nearestObsData.get(this.location.getIdentity()) );

    return new MeasurementCustomBundle(locationMCWLO); //mcbWLO;
  }

  // // ---
  // final public Instant getFMFLeadTimeInstantCopy() {
  //   final String mmi= "getFMFLeadTimeInstantCopy: ";
  //   try {
  //     this.fmfLeadTimeInstant.toString();
  //   } catch (NullPointerException npe) {
  //     throw new RuntimeException(mmi+npe);
  //   }
  //   return this.fmfLeadTimeInstant.plusSeconds(0L);
  // }

  // ---
  final public String getFMFLeadTimeInstantECCCOperStr() {
      
    final String mmi= "getFMFLeadTimeInstantECCCOperStr: ";
    
    try {
      this.fmfLeadTimeInstant.toString();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }
    
    slog.info(mmi+"this.fmfLeadTimeInstant="+this.fmfLeadTimeInstant.toString());
    
    // --- return <YYYYMMDDhh>0000 string built from the
    //     YYYY-MM-DDThh:mm:ssZ ISO8601 time string
    final String [] YYYYMMDDhhStrArray= this.fmfLeadTimeInstant.toString().
      split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR)[0].split(IWLToolsIO.ISO8601_DATETIME_SEP_CHAR);

    return YYYYMMDDhhStrArray[0].replace(IWLToolsIO.ISO8601_YYYYMMDD_SEP_CHAR, "") + YYYYMMDDhhStrArray[1] + "0000"; 
  }

  // ---
  final protected String getS104DCF2InputData(final String S104DCF2DataFile, final Map<String, HBCoords> nearestsTGCoords,
                                              final JsonObject mainJsonMapObj, final long nbHoursInPastArg, final int fmfTypeIndex ) {
      
    final String mmi= "getS104DCF2Data: ";

    if (nbHoursInPastArg < 0) {
      throw new RuntimeException(mmi+"nbHoursInPastArg must be >= 0 !!");
    }

    final Set<String> nearestsTGCoordsIds= nearestsTGCoords.keySet();

    if (nearestsTGCoordsIds.size() > 1) {
      slog.warn(mmi+" nearestsTGCoordsIds.size() > 1 !! we will only use the 1st item here"); 
    }

    // --- Get the TG id. String
    final String nearestTGIdStr= (String)nearestsTGCoordsIds.toArray()[0];

    slog.info(mmi+"this.location.getIdentity() (CHS tide gauge id. code)="+this.location.getIdentity());

    if (!nearestTGIdStr.equals(this.location.getIdentity())) {
      throw new RuntimeException(mmi+"The nearestTGIdStr must be the same as we have with this.location.getIdentity() !!");
    }

    slog.info(mmi+"start: nearestTGIdStr="+nearestTGIdStr+", fmfTypeIndex="+fmfTypeIndex);  //fmfType="+fmfType.name());

    //slog.info(mmi+"nearestsTGCoords.get(nearestTGIdStr).getLongitude()="+nearestsTGCoords.get(nearestTGIdStr).getLongitude());
    //slog.info(mmi+"nearestsTGCoords.get(nearestTGIdStr).getLatitude()="+nearestsTGCoords.get(nearestTGIdStr).getLatitude());
    //slog.info(mmi+"this.location.getLongitude()="+this.location.getLongitude());
    //slog.info(mmi+"this.location.getLatitude()="+this.location.getLatitude());
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);    

    slog.info(mmi+"S104DCF2DataFile="+S104DCF2DataFile);
    //slog.info(mmi+"this.modelInputDataFiles="+this.modelInputDataFiles);

    //--- Create the this.nearestModelData object to store the FMF WL input (non-adjusted) data
    this.nearestModelData.add( fmfTypeIndex, //fmfType.ordinal(),
                               new HashMap<String, List<MeasurementCustom>>() );

    // --- Create the Map of ArrayList<MeasurementCustom> for this TG
    this.nearestModelData.get(fmfTypeIndex)
      .put(nearestTGIdStr, new ArrayList<MeasurementCustom>() );
    
    // --- Define the synop run lead time that will be used to not consider
    //     the analysis part (which is before the synop run lead time) for the
    //     non-adjusted forecast WL data returned. It depends on the fmfTypeIndex
    //     int value which could be 0 in case the last (i.e. most recent) synop
    //     run FMF data is processed.
    final Instant synopRunLeadTime= this
      .fmfLeadTimeInstant.minus(fmfTypeIndex * nbHoursInPastArg, ChronoUnit.HOURS);

    slog.info(mmi+"this.fmfLeadTimeInstant="+this.fmfLeadTimeInstant.toString());
    slog.info(mmi+"synopRunLeadTime="+synopRunLeadTime.toString());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);   

    // --- Get only the FMF data for the S104 DCF2 pixel that is the nearest to the tide gauge
    //     location and only for the real forecast data which is in the future compared to
    //     the synopRunLeadTime Instant
    // ModelDataExtraction method call here
    final List<MeasurementCustom> tmpMCWLData= ModelDataExtraction.getNearestS104DCF2Data(S104DCF2DataFile, this.location);

    slog.info(mmi+"Nearest FMF S104 DCF2 data has now been extracted for TG -> "+nearestTGIdStr);
    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);  
    
    //--- Build the complete file path for the previous full model forecast ASCII
    //    input data file that could be used later for (WL0-WLF) error stats
    //final String prevFMFS104CDF2InputDataFilePath= inputFileNameFileObj.getParent() +
    //  File.separator + inputFileName.replace(zeroThHourYYYYMMDDhh,prevFMFInputFNamePrfx);

    final String prevFMFS104CDF2InputDataFilePath= "not defined yet!";
    
    slog.info(mmi+"Will return prevFMFS104CDF2InputDataFilePath="+prevFMFS104CDF2InputDataFilePath);

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    slog.info(mmi+"end");
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    return prevFMFS104CDF2InputDataFilePath;   
  }

  /**
   * Comments please!
   */
  final protected String getH2D2ASCIIWLFProbesData( final String H2D2ASCIIWLFProbesDataFile, final Map<String, HBCoords> nearestsTGCoords,
                                                    final JsonObject mainJsonMapObj, final long nbHoursInPastArg, final int fmfTypeIndex ) {

    // ---
    final String mmi= "getH2D2ASCIIWLProbesData: ";

    if (nbHoursInPastArg < 0) {
      throw new RuntimeException(mmi+"ERROR: nbHoursInPastArg must be >= 0 !!");
    }

    final Set<String> nearestsTGCoordsIds= nearestsTGCoords.keySet();

    slog.info(mmi+"start: nearestsTGCoordsIds="+
              nearestsTGCoordsIds.toString()+", fmfTypeIndex="+fmfTypeIndex);  //fmfType="+fmfType.name());

    slog.info(mmi+"H2D2ASCIIWLFProbesDataFile="+H2D2ASCIIWLFProbesDataFile);
    //slog.info(mmi+"this.modelInputDataFiles="+this.modelInputDataFiles);

    //--- Create the this.nearestModelData object to store the H2D2 ASCII WL
    //      forecast data
    this.nearestModelData.add( fmfTypeIndex, //fmfType.ordinal(),
                               new HashMap<String, List<MeasurementCustom>>() );

    final List<String> H2D2ASCIIWLFProbesDataLines=
      ASCIIFileIO.getFileLinesAsArrayList(H2D2ASCIIWLFProbesDataFile); //(this.modelInputDataFiles);

    // --- Extract-split the header line that defines the H2D2 WL probes used
    //    (ECCC_IDS)
    final String [] headerLineSplit=
      H2D2ASCIIWLFProbesDataLines.get(0).split(H2D2_ASCII_FMT_FLINE_SPLIT);

    final List<String> headerLineList= Arrays.asList(headerLineSplit); //stream(headerLineSplit).collect(Collectors.toSet());

    HashMap<String,Integer> tgDataColumnIndices= new HashMap<String,Integer>();

    for (final String chsTGId: nearestsTGCoordsIds) {
      //slog.info(mmi+"chsTGId="+chsTGId+", ecccId="+nearestsTGEcccIds.get(chsTGId));

      // --- Get the corresponding ECCC TG num. string id.
      final String ecccTGId= mainJsonMapObj
	.getJsonObject(chsTGId).getString(ITideGaugeConfig.INFO_ECCC_ID_JSON_KEY); //nearestsTGEcccIds.get(chsTGId);

      tgDataColumnIndices.put(chsTGId, headerLineList.indexOf(ecccTGId));

      slog.info(mmi+"CHS TG:"+chsTGId+", ECCC TG Id:"+ecccTGId+
               " H2D2 data line index is="+tgDataColumnIndices.get(chsTGId));

      // --- Create the Map entry for this CHS TG. in the nearestModelData (FMF data per-se)
      //this.nearestModelData.get(fmfType.ordinal()).
      this.nearestModelData.get(fmfTypeIndex)
        .put(chsTGId, new ArrayList<MeasurementCustom>() );

      // --- Create the Map entry for this CHS TG. in the nearestModelNowcastData (nowcast data only)
      //     but only if the chsTGId key does not already exists.
      if (!this.nearestModelNowcastData.containsKey(chsTGId)) {
	this.nearestModelNowcastData.put(chsTGId, new ArrayList<MeasurementCustom>());
      }
    }

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Get the forecast zero'th hour timestamp to discard the analysis
    //     (a.k.a nowcast) WL data part. Need to use the input file path
    //     to do so.
    final File inputFileNameFileObj= new File(H2D2ASCIIWLFProbesDataFile);

    final String inputFileName= inputFileNameFileObj.getName();

    //final String [] tmpInputFileNameSplit= new
    //  File(H2D2ASCIIWLFProbesDataFile).getName().split(H2D2_ASCII_FMT_FNAME_SPLITSTR)

    final String zeroThHourYYYYMMDDhh=
      inputFileName.split(H2D2_ASCII_FMT_FNAME_SPLITSTR)[0];
   //  File(H2D2ASCIIWLFProbesDataFile).getName().split(H2D2_ASCII_FMT_FNAME_SPLITSTR)[0];

    slog.info(mmi+"zeroThHourYYYYMMDDhh="+zeroThHourYYYYMMDDhh);

    final String zeroThHourISO8601= zeroThHourYYYYMMDDhh.substring(0,4)  + IWLToolsIO.ISO8601_YYYYMMDD_SEP_CHAR +
                                    zeroThHourYYYYMMDDhh.substring(4,6)  + IWLToolsIO.ISO8601_YYYYMMDD_SEP_CHAR +
                                    zeroThHourYYYYMMDDhh.substring(6,8)  + IWLToolsIO.ISO8601_DATETIME_SEP_CHAR +
                                    zeroThHourYYYYMMDDhh.substring(8,10) + ":00:00Z"; //.000Z";

    slog.info(mmi+"zeroThHourISO8601="+zeroThHourISO8601);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Get an Instant object from the zerothHourISO8601 string.
    final Instant zeroThHourInstant= Instant.parse(zeroThHourISO8601);
    
    // --- Define this.fmfLeadTimeInstant with the zerothHourISO8601 string.
    //     BUT only if the fmfTypeIndex is IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal()
    if ( fmfTypeIndex == IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal()) {
      this.fmfLeadTimeInstant= zeroThHourInstant.plusSeconds(0L);
    }
    
    //slog.info(mmi+"this.fmfLeadTimeInstant check="+this.fmfLeadTimeInstant.toString());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Get the column index of the timestamps in the ASCII file.
    final int timeStampColumnIndex=
      headerLineList.indexOf(H2D2_ASCII_FMT_TIMESTAMP_KEY);

    //slog.info(mmi+"timeStampColumnIndex="+timeStampColumnIndex);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Need to get rid of the file lines < H2D2_ASCII_FMT_1ST_DATA_LINE_INDEX
    final List<String> relevantDataLines= H2D2ASCIIWLFProbesDataLines
      .subList(H2D2_ASCII_FMT_1ST_DATA_LINE_INDEX, H2D2ASCIIWLFProbesDataLines.size());

    // ---
    for (final String inputDataLine: relevantDataLines ) {

       //slog.info(mmi+"inputDataLine="+inputDataLine);

       final String [] inputDataLineSplit=
         inputDataLine.split(H2D2_ASCII_FMT_FLINE_SPLIT);

       final Instant timeStampInstant= Instant.
         ofEpochSecond(Long.parseLong(inputDataLineSplit[timeStampColumnIndex]));

       // // --- Discard analysis-nowcast WL data (i.e. for timestamps smaller than zerothHourInstant)
       // if (timeStampInstant.compareTo(zeroThHourInstant) < 0 ) {
       //   //slog.info(mmi+"Skipping nowcast data at timestamp: "+timeStampInstant.toString());
       //   //slog.info(mmi+"Debug System.exit(0)");
       //   //System.exit(0);
       //   continue;
       // }

       // ---
       for (final String chsTGId: nearestsTGCoordsIds) {

         final double tgWLFValue= Double
           .parseDouble(inputDataLineSplit[tgDataColumnIndices.get(chsTGId)]);

         //slog.info(mmi+"timeStampSeconds="+timeStampSeconds+", tgWLValue="+tgWLValue);
         //--- Store the H2D2 WLF value for this CHS TG for this timestamp.
         //this.nearestModelData.get(fmfType.ordinal()).get(chsTGId).

	 final MeasurementCustom mcTmp= new MeasurementCustom(timeStampInstant, tgWLFValue, IWL.MAXIMUM_UNCERTAINTY_METERS);

	 // BEFORE modif. related to nowcast data usage:
	 //  if (timeStampInstant.compareTo(zeroThHourInstant) < 0 ) {
	 if (timeStampInstant.compareTo(zeroThHourInstant) <= 0 ) {

	   // --- the mcTmp object goes in the nowcast data part.
	   this.nearestModelNowcastData.get(chsTGId).add(mcTmp);
	   
	 } else {
	     
	   // --- ithe mcTmp object goes in the FMF futur time part.
	   this.nearestModelData.get(fmfTypeIndex).get(chsTGId).add(mcTmp);
           //   add( new MeasurementCustom(timeStampInstant, tgWLFValue, IWL.MAXIMUM_UNCERTAINTY_METERS) );
	 }
       }

       //slog.info(mmi+"Debug System.exit(0)");
       //System.exit(0);
    }

    // --- Get the the List<MeasurementCustom> objec of the 1st TG
    //     to extract its timestamp info
    final List<MeasurementCustom> tg0McList= this.
      nearestModelData.get(fmfTypeIndex).get(nearestsTGCoordsIds.toArray()[0]);
      //nearestModelData.get(fmfType.ordinal()).get(nearestsTGCoordsIds.toArray()[0]);

    //final List<MeasurementCustom> tg0McList=
    //  fullModelForecastData.get(nearestsTGCoordsIds.toArray()[0]);

    //final int nbTimeStamps= tg0McList.size();
    //slog.info(mmi+"model results nbTimeStamps="+nbTimeStamps);

    //--- Get the time incr. interval of the full model forecast data
    this.fmfDataTimeIntervalSeconds=
      MeasurementCustom.getDataTimeIntervallSeconds(tg0McList);

    final int nbTimeStamps= tg0McList.size();

    slog.info(mmi+"model results nbTimeStamps="+nbTimeStamps);

    // --- Need to use (double) cast to get what we want in terms of hours to go
    //     in past but only if nbHoursInPastArg == 0
    final long nbHoursToGoInPast= (nbHoursInPastArg != 0 ) ? nbHoursInPastArg :
       (long) ( (double) nbTimeStamps * (double) this.fmfDataTimeIntervalSeconds/ITimeMachine.SECONDS_PER_HOUR );

    slog.info(mmi+"this.fmfDataTimeIntervalSeconds="+this.fmfDataTimeIntervalSeconds);
    slog.info(mmi+"nbHoursToGoInPast="+nbHoursToGoInPast);

    //--- Get the Instant object that define the timestamp of the previous
    //    full model forecast data file to use for the (WLO-WLF) error stats.
    final Instant prevFMFInstantInPast= zeroThHourInstant.
      plusSeconds(-nbHoursToGoInPast*ITimeMachine.SECONDS_PER_HOUR);

    slog.info(mmi+"prevFMFInstantInPast="+prevFMFInstantInPast.toString());

    final String tmpPrevFMFInstantInPastStr= prevFMFInstantInPast.
      toString().split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR)[0];

    slog.info(mmi+"tmpPrevFMFInstantInPastStr="+tmpPrevFMFInstantInPastStr);

    // --- Replace the ISO8601 date time string separators by the empty string
    //    for the timestamp of the previous full model forecast data file.
    final String prevFMFInputFNamePrfx= tmpPrevFMFInstantInPastStr.
      replace(IWLToolsIO.ISO8601_YYYYMMDD_SEP_CHAR,"").replace(IWLToolsIO.ISO8601_DATETIME_SEP_CHAR,"");

    //--- Build the complete file path for the previous full model forecast ASCII
    //    input data file that could be used later for (WL0-WLF) error stats
    final String previousFMFASCIIDataFilePath= inputFileNameFileObj.getParent() +
      File.separator + inputFileName.replace(zeroThHourYYYYMMDDhh,prevFMFInputFNamePrfx);

    slog.info(mmi+"Will return previousFMFASCIIDataFilePath="+previousFMFASCIIDataFilePath);
    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    return previousFMFASCIIDataFilePath;
  }

  // ---
  final protected void getTGObsData(final Double wloQCThresholdAbsVal) {

    final String mmi= "getTGObsData: ";

    slog.info(mmi+"start: wloQCThresholdAbsVal="+wloQCThresholdAbsVal);

    //boolean haveWLOData= true;

    // --- check if the WL Obs. (a.k.a. WLO) input dat file exists
    //	   If it does not exists on disk then there is no WLO data
    //     to use 
    if ( WLToolsIO.checkForFileExistence(this.tideGaugeWLODataFile) ) {

      List<MeasurementCustom> tmpWLOMcList= null;

      slog.info(mmi+"this.tideGaugeWLODataFile="+this.tideGaugeWLODataFile);
	
      if (this.obsInputDataFormat == IWLToolsIO.Format.CHS_JSON ) {

        //this.nearestObsData= new HashMap<String,List<MeasurementCustom>>();

	slog.info(mmi+"Reading WLO input datausing the "+IWLToolsIO.Format.CHS_JSON.name()+" format");
	
        // --- Read the WLO data in a temp. List<MeasurementCustom> object
        tmpWLOMcList= WLAdjustmentIO
	  .getWLDataInCHSJsonFmt(this.tideGaugeWLODataFile, this.prdDataTimeIntervalSeconds, this.adjLocationZCVsVDatum, wloQCThresholdAbsVal);

        // --- Read the WLO data in a temp. List<MeasurementCustom> object

        //slog.info(mmi+"tmpWLOMcList.size()="+tmpWLOMcList.size());
        //slog.info(mmi+"tmpWLOMcList.get(0).getValue()="+tmpWLOMcList.get(0).getValue());
        //slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);

      } else if (this.obsInputDataFormat == IWLToolsIO.Format.IWLS_JSON) {

	slog.info(mmi+"Reading WLO input data using the "+IWLToolsIO.Format.IWLS_JSON.name()+" format");

	tmpWLOMcList= WLToolsIO
	  .getWLDataInIWLSJsonFmt(this.tideGaugeWLODataFile, this.prdDataTimeIntervalSeconds, wloQCThresholdAbsVal, WLToolsIO.getOutputDirectory()); //WLToolsIO.getOutputDirectory());	
	
        //slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);    
      }

      //slog.info(mmi+"tmpWLOMcList.size()="+tmpWLOMcList.size());
      //slog.info(mmi+"tmpWLOMcList.get(0).getValue()="+tmpWLOMcList.get(0).getValue());
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);    

      if (tmpWLOMcList.size() >= IWLMeasurement.MIN_NUMBER_OF_WL_HFOSC_RMV) {
	    
        // --- Assign the temp. List<MeasurementCustom> object to the this.nearestObsData object
        //     using the TG location id as key but apply the WLMeasurement.removeHFWLOscillations
        //     method to it before the assignation itself
        this.nearestObsData.put(this.location.getIdentity(),
                                WLMeasurement.removeHFWLOscillations(MAX_TIMEDIFF_FOR_HF_OSCILLATIONS_REMOVAL_SECONDS, tmpWLOMcList)) ;
      } else {

	slog.warn(mmi+"WARNING!!: Not enough WLO data to apply the WLMeasurement.removeHFWLOscillations method for TGat location -> "+this.location.getIdentity());

	// --- Not enough WLO data to apply the WLMeasurement.removeHFWLOscillations
	//     method, just assign the tmpWLOMcList as is
	this.nearestObsData.put(this.location.getIdentity(), tmpWLOMcList);
      }
	
      slog.info(mmi+"Done with reading the TG obs (WLO) at location -> "+this.location.getIdentity());

    } else {	
      this.haveWLOData= false; 
    }

    slog.info(mmi+"this.haveWLOData="+this.haveWLOData);
      
    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    //return haveWLOData;
  }

  /**
   * Comments please!
   */
  final static JsonObject getSpineJsonLocationIdInfo( /*@NotNull*/ final String spineLocationIdInfoFile) {

    final String mmi= "getSpineJsonLocationIdInfo: ";

    Map<String,String> spineLocationIdInfo= new HashMap<String,String>();

    //--- Deal with possible null tcInputfilePath String: if @NotNull not used
    try {
      spineLocationIdInfoFile.length();

    } catch (NullPointerException e) {

      slog.error(mmi+"spineLocationIdInfoFile is null !!");
      throw new RuntimeException(mmi+e);
    }

    slog.debug(mmi+"start: spineLocationIdInfoFile=" + spineLocationIdInfoFile);

    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(spineLocationIdInfoFile);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+"e");
    }

    final JsonObject mainJsonFileInputObj= Json.createReader(jsonFileInputStream).readObject();  //tmpJsonTcDataInputObj;

    // --- TODO: add fool-proof checks on all the Json dict keys.

    final JsonObject spineLocationIdInfoJsonObj=
      mainJsonFileInputObj.getJsonObject(IWLLocation.INFO_JSON_DICT_KEY);

    try {
      jsonFileInputStream.close();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    slog.debug(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    return spineLocationIdInfoJsonObj;
  }

  /**
   * Comments please!
   */
  final public static List<MeasurementCustom> getWLDataInCHSJsonFmt(final String WLDataJsonFile, final long timeIncrToUseSeconds,
								    final double fromZCToOtherDatumConvValue, final Double wlQCThresholdAbsVal) {
    final String mmi= "getWLDataInCHSJsonFmt: ";

    slog.debug(mmi+"start");
    
    //ArrayList<MeasurementCustom> retList= new ArrayList<MeasurementCustom>();

    if ( timeIncrToUseSeconds > 0L &&
         (timeIncrToUseSeconds > MAX_TIMEINCR_DIFF_FOR_NNEIGH_TIMEINTERP_SECONDS) ) {

      throw new RuntimeException(mmi+"Cannot have timeIncrToUse > MAX_TIMEINCR_DIFF_FOR_NNEIGH_TIMEINTERP_SECONDS !!");
    }

    //--- Deal with possible null WLDataJsonFile
    try {
      WLDataJsonFile.length();
    } catch (NullPointerException npe) {
	//slog.error(mmi+"WLDataJsonFile is null !!");
      throw new RuntimeException(mmi+npe);
    }

    if (!WLToolsIO.checkForFileExistence(WLDataJsonFile)) {
      throw new RuntimeException(mmi+"WLDataJsonFile -> "+WLDataJsonFile+" not found !!");
    }   

    slog.debug(mmi+"start: WLDataJsonFile=" + WLDataJsonFile+
               ", fromZCToOtherDatumConvValue="+fromZCToOtherDatumConvValue);

    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(WLDataJsonFile);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+e);
    }

    final JsonArray jsonWLDataArray= Json.createReader(jsonFileInputStream).readArray();  //tmpJsonTcDataInputObj;

    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }
 
    slog.debug(mmi+"done with WLDataJsonFile=" + WLDataJsonFile);

    slog.debug(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Need to use fromZCToOtherDatumConvValue (which can be 0.0) as 4th arg.
    //     to the WLToolsIO.checkWLDataCHSJsonArray
    return WLToolsIO.checkWLDataCHSJsonArray(jsonWLDataArray, timeIncrToUseSeconds, wlQCThresholdAbsVal, fromZCToOtherDatumConvValue );
    
    //return retListMCs;
  }

  // ---
  final protected Map<Long, MeasurementCustom> readTGTimeDepResidualsStats(final String tgIdentity,
									   final String tgResidualsStatsIODirectory) {
      
    final String mmi= "readTGTimeDepResidualsStats: ";

    slog.info(mmi+"start");

    Map<Long, MeasurementCustom> tgTimeDepResidualsStats= null;

    final String tgTimeDepResidualsStatsFile= tgResidualsStatsIODirectory + File.separator +
      PRV_FMF_RESIDUALS_STATS_SUBDIRNAME + File.separator + RESIDUALS_STATS_ATTG_FNAME_PRFX + tgIdentity + IWLToolsIO.JSON_FEXT;

    if (WLToolsIO.checkForFileExistence(tgTimeDepResidualsStatsFile)) {
    
      slog.info(mmi+"Reading tgTimeDepResidualsStatsFile="+tgTimeDepResidualsStatsFile);

      FileInputStream jsonFileInputStream= null;

      try {
        jsonFileInputStream= new FileInputStream(tgTimeDepResidualsStatsFile);
      } catch (FileNotFoundException e) {
	
        throw new RuntimeException(mmi+"ERROR: The tgTimeDepResidualsStatsFile -> "+
	   			   tgTimeDepResidualsStatsFile+" cannot be opened in read mode !!");
      }

      final JsonArray jsonDataArray= Json.
        createReader(jsonFileInputStream).readArray();

      tgTimeDepResidualsStats= new HashMap<Long, MeasurementCustom>();
    
      for (int itemIter= 0; itemIter< jsonDataArray.size(); itemIter++) {
	
        final JsonObject jsonDataObj=
          jsonDataArray.getJsonObject(itemIter);

        final double timeDepResidualAvg= jsonDataObj.
	getJsonNumber(IWLToolsIO.VALUE_JSON_KEY).doubleValue();

        final double timeDepResidualUncertainty= jsonDataObj.
	  getJsonNumber(IWLToolsIO.UNCERTAINTY_JSON_JEY).doubleValue();

        final Long longIdx= jsonDataObj.
	  getJsonNumber(IWLAdjustmentIO.FMF_RESIDUALS_STATS_TDEP_OFST_SECONDS_JSON_KEY).longValue();

        // --- We do not use the Instant object for the time dependent residual stats
        //     so we pass null for it in the MeasurementCustom constructor.
        tgTimeDepResidualsStats.put(longIdx,
                                    new MeasurementCustom(null, timeDepResidualAvg, timeDepResidualUncertainty));
      }
      
    } else {
      slog.warn(mmi+"The tgTimeDepResidualsStatsFile -> "+
		tgTimeDepResidualsStatsFile+" does not exists, returning null !!");
    }
    
    slog.info(mmi+"end");

    return tgTimeDepResidualsStats;
  }

  // --- Using the CHS JSON format.
  final protected void writeTGTimeDepResidualsStats(final String tgIdentity,
                                                    final Map<Long, MeasurementCustom> timeDepResidualsStatsMap,
                                                    final String tgResidualsStatsIODirectory                     ) {

    final String mmi= "writeTGTimeDepResidualsStats: ";

    slog.info(mmi+"start");

    try {
      timeDepResidualsStatsMap.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    final String tgTimeDepResidualsStatsFile= tgResidualsStatsIODirectory + File.separator +
      NEW_FMF_RESIDUALS_STATS_SUBDIRNAME + File.separator + RESIDUALS_STATS_ATTG_FNAME_PRFX + tgIdentity + IWLToolsIO.JSON_FEXT;

    slog.info(mmi+"Writing tgTimeDepResidualsStatsFile="+tgTimeDepResidualsStatsFile);

    FileOutputStream jsonFileOutputStream= null;

    try {
      jsonFileOutputStream= new FileOutputStream(tgTimeDepResidualsStatsFile);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+e);
    }

    JsonArrayBuilder jsonArrayBuilderObj= Json.createArrayBuilder();

    final SortedSet<Long> timeDepResidualsStatsMapSSet= Collections
      .synchronizedSortedSet(new TreeSet<Long>(timeDepResidualsStatsMap.keySet()));

    for (final Long longIter: timeDepResidualsStatsMapSSet) { //timeDepResidualsStatsMap.keySet()) {

      final MeasurementCustom mc= timeDepResidualsStatsMap.get(longIter);

      jsonArrayBuilderObj.
        add( Json.createObjectBuilder().
          add(IWLToolsIO.VALUE_JSON_KEY, mc.getValue() ).
	     add( IWLAdjustmentIO.FMF_RESIDUALS_STATS_TDEP_OFST_SECONDS_JSON_KEY, longIter).
	       add( IWLToolsIO.UNCERTAINTY_JSON_JEY, mc.getUncertainty() )); 
    }

    // --- Now write the Json data bundle in the output file.
    Json.createWriter(jsonFileOutputStream).
      writeArray( jsonArrayBuilderObj.build() );

    // --- We can close the Json file now
    try {
      jsonFileOutputStream.close();
    } catch (IOException ioe) {
      throw new RuntimeException(mmi+ioe);
    }

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);
  }
}
