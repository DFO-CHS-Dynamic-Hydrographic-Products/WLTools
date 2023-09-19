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
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.wl.fms.FMSLegacyContext;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpine;


/**
 * Comments please!
 */
abstract public class WLAdjustmentIO implements IWLAdjustmentIO { //extends <>

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //protected IWLAdjustmentIO.LocationType locationType= null;

  protected Set<String> argsMapKeySet= null; //argsMap.keySet();

  protected IWLAdjustment.Type adjType= null;

  protected String locationId= null;
  protected String locationIdInfo= null;

  //protected DataType inputDataType= null;

  protected IWLStationPredIO.Format obsInputDataFormat= null;
  protected IWLStationPredIO.Format predictInputDataFormat= null;

  protected DataTypesFormatsDef modelForecastInputDataFormat= null;

  protected double adjLocationLatitude= 0.0;
  protected double adjLocationLongitude= 0.0;
  protected double adjLocationZCVsVDatum= 0.0;

  //protected ArrayList<WLMeasurement> locationOriginalData= null;
  protected ArrayList<MeasurementCustom> locationOriginalData= null;
  protected ArrayList<MeasurementCustom> locationAdjustedData= null;

  //protected Map<String, ArrayList<WLMeasurement>> nearestObsData= null;
  //protected Map<String, ArrayList<WLMeasurement>> nearestModelData= null;

  protected Map<String, ArrayList<MeasurementCustom>> nearestObsData= null;
  protected Map<String, ArrayList<MeasurementCustom>> nearestModelData= null;

  protected String modelForecastInputDataInfo= null;
  protected List<String> modelForecastInputDataFiles= null;
  //protected String= modelForecastInputDataInfo= null;

  protected FMSLegacyContext fmsLegacyContextObj= null;

  /**
   * Comments please!
   */
  public WLAdjustmentIO() {

    this.argsMapKeySet= null;

    this.locationId=
      this.locationIdInfo= null;

    //this.inputDataType= null;

    this.obsInputDataFormat=
      this.predictInputDataFormat= null;

    this.modelForecastInputDataFormat = null;

    this.adjLocationZCVsVDatum=
      this.adjLocationLatitude=
        this.adjLocationLongitude= 0.0;

    this.locationOriginalData= null;
    this.locationAdjustedData= null;

    this.nearestObsData=
      this.nearestModelData= null;

    this.modelForecastInputDataInfo= null;
    this.modelForecastInputDataFiles= null;
    //this.modelInputDataFiles= null;
  }

  /**
   * Comments please!
   */
  public WLAdjustmentIO(/*@NotNull*/ final WLAdjustment.Type adjType,
                        /*@NotNull*/ final Map<String,String> argsMap) {
    final String mmi=
      "WLAdjustmentIO(final WLAdjustment.Type adjType,final Map<String,String> argsMap) construtor : ";

    this.adjType= adjType;

    slog.info(mmi+"this.adjType="+this.adjType.name());

    this.argsMapKeySet= argsMap.keySet();
  }

  /**
   * Comments please!
   */
  final void getH2D2ASCIIWLFProbesData( /*@NotNull*/ final String H2D2ASCIIWLFProbesDataFile,
                                        /*@NotNull*/ Map<String, HBCoords>  nearestsTGCoords,
                                        /*@NotNull*/ final JsonObject       mainJsonMapObj     ) {

                                       ///*@NotNull*/ Map<String,String> nearestsTGEcccIds ) {

    // --- TODO: Use a Set<String> object instead of a Map<String, HBCoords> object
    //     because the HBCoords object is useless for this method.

    final String mmi= "getH2D2ASCIIWLProbeData: ";

    final Set<String> nearestsTGCoordsIds= nearestsTGCoords.keySet();

    slog.info(mmi+"start: nearestsTGCoordsIds="+nearestsTGCoordsIds.toString());

    slog.info(mmi+"H2D2ASCIIWLFProbesDataFile="+H2D2ASCIIWLFProbesDataFile);
    //slog.info(mmi+"this.modelInputDataFiles="+this.modelInputDataFiles);

    //--- Create the this.nearestModelData object to store the H2D2 ASCII WL
    //      forecast data
    this.nearestModelData= new HashMap<String, ArrayList<MeasurementCustom>>();

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
        getJsonObject(chsTGId).getString(TIDE_GAUGES_INFO_ECCC_IDS_KEY); //nearestsTGEcccIds.get(chsTGId);

      tgDataColumnIndices.put(chsTGId,headerLineList.indexOf(ecccTGId));

      slog.info(mmi+"CHS TG:"+chsTGId+", ECCC TG Id:"+ecccTGId+
               " H2D2 data line index is="+tgDataColumnIndices.get(chsTGId));

      // --- Create the Map entry for this CHS TG.
      this.nearestModelData.put(chsTGId, new ArrayList<MeasurementCustom>() );
    }

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Get the forecast zero'th hour timestamp to discard the analysis
    //     (a.k.a nowcast) WL data part. Need to use the input file name
    //      to do so.
    final String zerothHourYYYYMMDDhh= new
      File(H2D2ASCIIWLFProbesDataFile).getName().split(H2D2_ASCII_FMT_FNAME_SPLITSTR)[0];

    slog.info(mmi+"zerothHourYYYYMMDDhh="+zerothHourYYYYMMDDhh);

    final String zerothHourISO8601= zerothHourYYYYMMDDhh.substring(0,4)  + "-" +
                                    zerothHourYYYYMMDDhh.substring(4,6)  + "-" +
                                    zerothHourYYYYMMDDhh.substring(6,8)  + "T" +
                                    zerothHourYYYYMMDDhh.substring(8,10) + ":00:00Z"; //.000Z";

    slog.info(mmi+"zerothHourISO8601="+zerothHourISO8601);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    final Instant zerothHourInstant= Instant.parse(zerothHourISO8601);

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
       if (timeStampInstant.compareTo(zerothHourInstant) < 0 ) {
         //slog.info(mmi+"Skipping nowcast data at timestamp: "+timeStampInstant.toString());
         //slog.info(mmi+"Debug System.exit(0)");
         //System.exit(0);

         continue;
       }

       for (final String chsTGId: nearestsTGCoordsIds) {

         final double tgWLFValue=
           Double.parseDouble(inputDataLineSplit[tgDataColumnIndices.get(chsTGId)]);

         //slog.info(mmi+"timeStampSeconds="+timeStampSeconds+", tgWLValue="+tgWLValue);
         //--- Store the H2D2 WLF value for this CHS TG for this timestamp.
         this.nearestModelData.get(chsTGId).
           add( new MeasurementCustom(timeStampInstant,tgWLFValue,0.0) );
       }

       //slog.info(mmi+"Debug System.exit(0)");
       //System.exit(0);
    }

    final int nbTimeStamps= this.nearestModelData.
      get(nearestsTGCoordsIds.toArray()[0]).size();

    slog.info(mmi+"model results nbTimeStamps="+nbTimeStamps);

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
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

    final JsonObject mainJsonFileInputObj= Json.
      createReader(jsonFileInputStream).readObject();  //tmpJsonTcDataInputObj;

    // --- TODO: add fool-proof checks on all the Json dict keys.

    final JsonObject spineLocationIdInfoJsonObj=
      mainJsonFileInputObj.getJsonObject(IStageIO.LOCATION_INFO_JSON_DICT_KEY);

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
  final ArrayList<MeasurementCustom> getWLDataInJsonFmt(/*@NotNull*/ final String WLDataJsonFile) {

    final String mmi= "getWLDataInJsonFmt: ";

    ArrayList<MeasurementCustom> retList= new ArrayList<MeasurementCustom>();

    //--- Deal with possible null nsTidePredDataJsonFile String: if @NotNull not used
    try {
      WLDataJsonFile.length();

    } catch (NullPointerException e) {

      slog.error(mmi+"WLDataJsonFile is null !!");
      throw new RuntimeException(mmi+e);
    }

    slog.info(mmi+"start: WLDataJsonFile=" + WLDataJsonFile);

    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(WLDataJsonFile);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+"e");
    }

    final JsonArray jsonWLDataArray= Json.
      createReader(jsonFileInputStream).readArray();  //tmpJsonTcDataInputObj;

    //for (final JsonObject jsonObj: jsonPredDataArray.toArray()) {
    for (int itemIter= 0; itemIter< jsonWLDataArray.size(); itemIter++) {

      final JsonObject jsonWLDataObj=
        jsonWLDataArray.getJsonObject(itemIter);

      final Instant wlDataTimeStamp= Instant.
        parse(jsonWLDataObj.getString(IWLStationPredIO.INSTANT_JSON_KEY));

      //slog.info(mmi+"wlPredTimeStamp="+wlPredTimeStamp.toString());

      final double wlDataValue= jsonWLDataObj.
        getJsonNumber(IWLStationPredIO.VALUE_JSON_KEY).doubleValue();

      //slog.info(mmi+"wlPredValue="+wlPredValue);
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);

      double uncertainty= 0.0;

      if (jsonWLDataObj.containsKey(IWLStationPredIO.UNCERTAINTY_JSON_JEY)) {

        uncertainty= jsonWLDataObj.
          getJsonNumber(IWLStationPredIO.UNCERTAINTY_JSON_JEY).doubleValue();
      }

      // --- Add this WL pred Instant and value as a MeasurementCustom object
      //    in the returned list
      retList.add(new MeasurementCustom(wlDataTimeStamp, wlDataValue, uncertainty));
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

    return retList;
  }

}
