package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
//import java.sql;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
//import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// ---
//import javax.sql;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import as.hdfql.HDFql;
import as.hdfql.HDFqlCursor;
import as.hdfql.HDFqlConstants;

// ---
import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.wl.fms.FMS;
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.wl.fms.FMSInput;
import ca.gc.dfo.chs.wltools.util.TimeMachine;
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPred;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
//import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpine;

/**
 * Comments please!
 */
abstract public class WLAdjustmentIO implements IWLAdjustmentIO, IWLAdjustment { //extends <>

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //protected IWLAdjustmentIO.LocationType locationType= null;

  protected Set<String> argsMapKeySet= null; //argsMap.keySet();

  protected IWLAdjustment.Type adjType= null;

  protected WLLocation location= null;

  //protected String locationId= null;
  protected String locationIdInfo= null;

  protected String fullForecastModelName= "UNKNOWN";

  //protected DataType inputDataType= null;

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

  protected List< Map<String, List<MeasurementCustom>> > nearestModelData=
    new ArrayList< Map<String, List<MeasurementCustom>> > (FullModelForecastType.values().length);

  protected String modelForecastInputDataInfo= null;
  protected List<String> modelForecastInputDataFiles= null;
  //protected String= modelForecastInputDataInfo= null;

  //protected long obsDataTimeIntervalSeconds= IWLStationPred.TIME_NOT_DEFINED;
  protected long prdDataTimeIntervalSeconds= IWLStationPred.TIME_NOT_DEFINED;
  protected long fmfDataTimeIntervalSeconds= IWLStationPred.TIME_NOT_DEFINED;

  protected FMS fmsObj= null;
  protected FMSInput fmsInputObj= null;
  //protected FMSFactory fmsFactoryObj= null;

  /**
   * Comments please!
   */
  public WLAdjustmentIO() {

    this.argsMapKeySet= null;

    this.location= null;

    //this.locationId=
    this.locationIdInfo= null;

    //this.inputDataType= null;

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

    this.fmsObj= null;
    this.fmsInputObj= null;
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

  /**
   * Comments please!
   */
  final String getH2D2ASCIIWLFProbesData( /*@NotNull*/ final String H2D2ASCIIWLFProbesDataFile,
                                          /*@NotNull*/ Map<String, HBCoords>  nearestsTGCoords,
                                          /*@NotNull*/ final JsonObject       mainJsonMapObj,
                                                       final long             nbHoursInPastArg,
                                                       final int              fmfTypeIndex ) {
                                                       //final FullModelForecastType fmfType ) {

                                       ///*@NotNull*/ Map<String,String> nearestsTGEcccIds ) {

    // --- TODO: Use a Set<String> object instead of a Map<String, HBCoords> object
    //     because the HBCoords object is useless for this method.

    final String mmi= "getH2D2ASCIIWLProbeData: ";

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
      final String ecccTGId= mainJsonMapObj.
        getJsonObject(chsTGId).getString(ITideGaugeConfig.INFO_ECCC_ID_JSON_KEY); //nearestsTGEcccIds.get(chsTGId);

      tgDataColumnIndices.put(chsTGId, headerLineList.indexOf(ecccTGId));

      slog.info(mmi+"CHS TG:"+chsTGId+", ECCC TG Id:"+ecccTGId+
               " H2D2 data line index is="+tgDataColumnIndices.get(chsTGId));

      // --- Create the Map entry for this CHS TG.
      //this.nearestModelData.get(fmfType.ordinal()).
      this.nearestModelData.get(fmfTypeIndex).
        put(chsTGId, new ArrayList<MeasurementCustom>() );
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

    final String zerothHourYYYYMMDDhh=
      inputFileName.split(H2D2_ASCII_FMT_FNAME_SPLITSTR)[0];
   //  File(H2D2ASCIIWLFProbesDataFile).getName().split(H2D2_ASCII_FMT_FNAME_SPLITSTR)[0];

    slog.info(mmi+"zerothHourYYYYMMDDhh="+zerothHourYYYYMMDDhh);

    final String zerothHourISO8601= zerothHourYYYYMMDDhh.substring(0,4)  + IWLToolsIO.ISO8601_YYYYMMDD_SEP_CHAR +
                                    zerothHourYYYYMMDDhh.substring(4,6)  + IWLToolsIO.ISO8601_YYYYMMDD_SEP_CHAR +
                                    zerothHourYYYYMMDDhh.substring(6,8)  + IWLToolsIO.ISO8601_DATETIME_SEP_CHAR +
                                    zerothHourYYYYMMDDhh.substring(8,10) + ":00:00Z"; //.000Z";

    slog.info(mmi+"zerothHourISO8601="+zerothHourISO8601);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    final Instant zeroThHourInstant= Instant.parse(zerothHourISO8601);

    //slog.info(mmi+"zerothHourInstant check="+zerothHourInstant.toString());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Get the column index of the timestamps in the ASCII file.
    final int timeStampColumnIndex=
      headerLineList.indexOf(H2D2_ASCII_FMT_TIMESTAMP_KEY);

    //slog.info(mmi+"timeStampColumnIndex="+timeStampColumnIndex);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Need to get rid of the file lines < H2D2_ASCII_FMT_1ST_DATA_LINE_INDEX
    final List<String> relevantDataLines= H2D2ASCIIWLFProbesDataLines.
      subList(H2D2_ASCII_FMT_1ST_DATA_LINE_INDEX, H2D2ASCIIWLFProbesDataLines.size());

    // ---
    for (final String inputDataLine: relevantDataLines ) {

       //slog.info(mmi+"inputDataLine="+inputDataLine);

       final String [] inputDataLineSplit=
         inputDataLine.split(H2D2_ASCII_FMT_FLINE_SPLIT);

       final Instant timeStampInstant= Instant.
         ofEpochSecond(Long.parseLong(inputDataLineSplit[timeStampColumnIndex]));

       // --- Discard analysis-nowcast WL data (i.e. for timestamps smaller than zerothHourInstant)
       if (timeStampInstant.compareTo(zeroThHourInstant) < 0 ) {
         //slog.info(mmi+"Skipping nowcast data at timestamp: "+timeStampInstant.toString());
         //slog.info(mmi+"Debug System.exit(0)");
         //System.exit(0);

         continue;
       }

       for (final String chsTGId: nearestsTGCoordsIds) {

         final double tgWLFValue= Double.
           parseDouble(inputDataLineSplit[tgDataColumnIndices.get(chsTGId)]);

         //slog.info(mmi+"timeStampSeconds="+timeStampSeconds+", tgWLValue="+tgWLValue);
         //--- Store the H2D2 WLF value for this CHS TG for this timestamp.
         //this.nearestModelData.get(fmfType.ordinal()).get(chsTGId).
         this.nearestModelData.get(fmfTypeIndex).get(chsTGId).
           add( new MeasurementCustom(timeStampInstant, tgWLFValue, IWL.MAXIMUM_UNCERTAINTY_METERS) );
       }

       //slog.info(mmi+"Debug System.exit(0)");
       //System.exit(0);
    }

    // --- Get the the List<MeasurementCustom> objec of the 1st TG
    //    to extract its timestamp info
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
      File.separator + inputFileName.replace(zerothHourYYYYMMDDhh,prevFMFInputFNamePrfx);

    slog.info(mmi+"returned previousFMFASCIIDataFilePath="+previousFMFASCIIDataFilePath);
    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    return previousFMFASCIIDataFilePath;
  }

  // --- IMPORTANT: Do not use this method for WL observation data since we could
  //     have missing data so the result of this method could be misleading for 
  //     that kind of WL data. 
 //public static final long getDataTimeIntervallSeconds(final List<MeasurementCustom> dataList) {
 //   //long ret= IWLStationPred.TIME_NOT_DEFINED;
 //   final long firstTimeStampSeconds= dataList.get(0).getEventDate().getEpochSeconds();
 //   final long secondTimeStampSeconds= dataList.get(1).getEventDate().getEpochSeconds();
 //  // --- Do not assume that the secondTimeStampSeconds value is larger than the
 //  //     firstTimeStampSeconds value so usr Math.abs here.
 //   return Math.abs(secondTimeStampSeconds - firstTimeStampSeconds);
  //}

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

    final JsonObject mainJsonFileInputObj= Json.
      createReader(jsonFileInputStream).readObject();  //tmpJsonTcDataInputObj;

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
  final static ArrayList<MeasurementCustom>
    getWLDataInJsonFmt(/*@NotNull*/ final String WLDataJsonFile,
                       final long timeIncrToUseSeconds, final double fromZCToOtherDatumConvValue) {

    final String mmi= "getWLDataInJsonFmt: ";

    //ArrayList<MeasurementCustom> retList= new ArrayList<MeasurementCustom>();

    if ( timeIncrToUseSeconds > 0L &&
         (timeIncrToUseSeconds > MAX_TIMEINCR_DIFF_FOR_NNEIGH_TIMEINTERP_SECONDS) ) {

      throw new RuntimeException(mmi+"Cannot have timeIncrToUse > MAX_TIMEINCR_DIFF_FOR_NNEIGH_TIMEINTERP_SECONDS !!");
    }

    //--- Deal with possible null nsTidePredDataJsonFile String: if @NotNull not used
    try {
      WLDataJsonFile.length();

    } catch (NullPointerException e) {

      slog.error(mmi+"WLDataJsonFile is null !!");
      throw new RuntimeException(mmi+e);
    }

    slog.info(mmi+"start: WLDataJsonFile=" + WLDataJsonFile+
              ", fromZCToOtherDatumConvValue="+fromZCToOtherDatumConvValue);

    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(WLDataJsonFile);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+e);
    }

    final JsonArray jsonWLDataArray= Json.
      createReader(jsonFileInputStream).readArray();  //tmpJsonTcDataInputObj;

    //List<String> checkTimeStamps= new ArrayList<String>();
    List<Instant> trackExistingInstants= new ArrayList<Instant>();

    ArrayList<MeasurementCustom> retListMCs= null;

    ArrayList<MeasurementCustom> tmpRetListMCs= new ArrayList<MeasurementCustom>();

    ArrayList<MeasurementCustom> mcsAtNonValidTimeStamps= new ArrayList<MeasurementCustom>();

    //for (final JsonObject jsonObj: jsonPredDataArray.toArray()) {
    for (int itemIter= 0; itemIter< jsonWLDataArray.size(); itemIter++) {

      final JsonObject jsonWLDataObj=
        jsonWLDataArray.getJsonObject(itemIter);

      final Instant wlDataInstant= Instant.
        parse(jsonWLDataObj.getString(IWLToolsIO.INSTANT_JSON_KEY));

      final long checkTimeStampSeconds= wlDataInstant.getEpochSecond();

      //// --- Could have time stamps that are not defined with the "normal" time
     // //     increment difference so just get rid of the related WL data.
     // //     e.g.: When WL obs data have 1mins time incr. intervalls (CHS TGs)
     // //           OR WL obs data have 5mins time incr. intervalls (ECCC TGs)
     // //           it means that for ECCC TGs we only use WL obs data at 15mins
    //  //           time intervals if timeIncrToUse is 3mins (180 seconds)
    //  //     NOTE: a timeIncrToUse < 0 means that we do not need to check
    //  //           the time increments (e.g. for predictions)
    //  if ( (timeIncrToUseSeconds > 0L) && (checkTimeStampSeconds % timeIncrToUseSeconds != 0L)) {
    //    continue;
    //  }

      if (trackExistingInstants.contains(wlDataInstant)) {

        slog.warn(mmi+"Found an Instant timestamp duplicate - >"+
                  wlDataInstant.toString()+" in the WL data, ignoring it !!");
        continue;
        //throw new RuntimeException(mmi+"The time stamp: "+wlDataInstant.toString()+" is duplicated !! ");
      }

      //--- NOTE: converting to the other vertical datum from the ZC by adding
      //    fromZCToOtherDatumConvValue from the WLO value read from the json
      //    input file. Users have just to pass the same value but with the
      //    opposite sign to get the value being converted to the ZC.
      final double wlDataValue= jsonWLDataObj.
        getJsonNumber(IWLToolsIO.VALUE_JSON_KEY).doubleValue() + fromZCToOtherDatumConvValue;

      //slog.info(mmi+"wlPredValue="+wlPredValue);
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);

      double uncertainty= MeasurementCustom.UNDEFINED_UNCERTAINTY;

      if (jsonWLDataObj.containsKey(IWLToolsIO.UNCERTAINTY_JSON_JEY)) {

        uncertainty= jsonWLDataObj.
          getJsonNumber(IWLToolsIO.UNCERTAINTY_JSON_JEY).doubleValue();
      }

      uncertainty= (uncertainty > IWL.MINIMUM_UNCERTAINTY_METERS) ? uncertainty: IWL.MAXIMUM_UNCERTAINTY_METERS;

      // --- Could have time stamps that are not defined with the "normal" time
      //     increment difference so just get rid of the related WL data.
      //     e.g.: When WL obs data have 1mins time incr. intervalls (CHS TGs)
      //           OR WL obs data have 5mins time incr. intervalls (ECCC TGs)
      //           it means that for ECCC TGs we only use WL obs data at 15mins
      //           time intervals if timeIncrToUse is 3mins (180 seconds)
      //     NOTE: a timeIncrToUse < 0 means that we do not need to check
      //           the time increments (e.g. for predictions)
      if ( (timeIncrToUseSeconds > 0L) && (checkTimeStampSeconds % timeIncrToUseSeconds != 0L)) {

        // --- Store the data at this non-valid timestamp in the local mcsAtOtherTimeStamps List
        //     to possibly use it later.
        mcsAtNonValidTimeStamps.add(new MeasurementCustom(wlDataInstant, wlDataValue, uncertainty));

      } else {

        // --- Put the data at this valid time stamp in the retListMCs List
        tmpRetListMCs.add(new MeasurementCustom(wlDataInstant, wlDataValue, uncertainty));
      }

    } // --- for (int itemIter= 0; itemIter< jsonWLDataArray.size(); itemIter++) loop block

    slog.info(mmi+"tmpRetListMCs.size()="+tmpRetListMCs.size());
    slog.info(mmi+"mcsAtNonValidTimeStamps.size="+mcsAtNonValidTimeStamps.size());

    // --- Now check if the mssing WL data could be replaced by data that is reasonably close
    //     in terms of timestamps.
    if ( (timeIncrToUseSeconds > 0L) && (mcsAtNonValidTimeStamps.size() > 0 ) ) {

      slog.info(mmi+"Trying to find WL replacements not too far in time for missing timestamps");

      retListMCs= WLMeasurement.findPossibleWLReplacements(timeIncrToUseSeconds,
        mcsAtNonValidTimeStamps,tmpRetListMCs, ITimeMachine.SECONDS_PER_MINUTE);

      slog.info(mmi+"Done with WLMeasurement.findPossibleWLReplacements() method");
      slog.info(mmi+"retListMCs.size() after WLMeasurement.findPossibleWLReplacements()="+retListMCs.size());

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);

    } else {
      retListMCs= tmpRetListMCs;
    }

    try {
      jsonFileInputStream.close();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    slog.info(mmi+"done with WLDataJsonFile=" + WLDataJsonFile);

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    return retListMCs;
  }
}
