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
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;

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

  protected InputDataType inputDataType= null;
  protected InputDataTypesFormatsDef inputDataFormat= null;

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

  protected String modelInputDataDef= null;
  //protected List<String> modelInputDataFiles= null;

  /**
   * Comments please!
   */
  public WLAdjustmentIO() {

    this.argsMapKeySet= null;

    this.locationId=
      this.locationIdInfo= null;

    this.inputDataType= null;
    this.inputDataFormat= null;

    this.adjLocationZCVsVDatum=
      this.adjLocationLatitude=
        this.adjLocationLongitude= 0.0;

    this.locationOriginalData= null;
    this.locationAdjustedData= null;

    this.nearestObsData=
      this.nearestModelData= null;

    this.modelInputDataDef= null;
    //this.modelInputDataFiles= null;
  }

  /**
   * Comments please!
   */
  public WLAdjustmentIO(/*@NotNull*/ final WLAdjustment.Type adjType, /*@NotNull*/ final Map<String,String> argsMap) {

    final String mmi= "WLAdjustmentIO(final WLAdjustment.Type adjType,final Map<String,String> argsMap) construtor : ";

    this.adjType= adjType;

    slog.info(mmi+"this.adjType="+this.adjType.name());

    this.argsMapKeySet= argsMap.keySet();
  }

  /**
   * Comments please!
   */
  final void getH2D2ASCIIWLFProbesData(/*@NotNull*/ Map<String, HBCoords> nearestsTGCoords,
                                       /*@NotNull*/ Map<String,String> nearestsTGEcccIds ) {

    final String mmi= "getH2D2ASCIIWLProbesData: ";

    final Set<String> nearestsTGCoordsIds= nearestsTGCoords.keySet();

    slog.info(mmi+"start: nearestsTGCoordsIds="+nearestsTGCoordsIds.toString());

    slog.info(mmi+"this.modelInputDataDef="+this.modelInputDataDef);

    //--- Create the this.nearestModelData object to store the H2D2 ASCII WL
    //      forecast data
    this.nearestModelData= new HashMap<String, ArrayList<MeasurementCustom>>();

    final List<String> H2D2ASCIIWLFProbesData=
      ASCIIFileIO.getFileLinesAsArrayList(this.modelInputDataDef);

    // --- Extract-split the header line that defines the H2D2 WL probes used
    //    (ECCC_IDS)
    final String [] headerLineSplit=
      H2D2ASCIIWLFProbesData.get(0).split(H2D2_ASCII_FMT_FLINE_SPLIT);

    final List<String> headerLineList= Arrays.asList(headerLineSplit); //stream(headerLineSplit).collect(Collectors.toSet());

    HashMap<String,Integer> tgDataColumnIndices= new HashMap<String,Integer>();

    for (final String chsTGId: nearestsTGCoordsIds) {
      //slog.info(mmi+"chsTGId="+chsTGId+", ecccId="+nearestsTGEcccIds.get(chsTGId));

      final String ecccTGId= nearestsTGEcccIds.get(chsTGId);

      tgDataColumnIndices.put(chsTGId,headerLineList.indexOf(ecccTGId));

      slog.info(mmi+"CHS TG:"+chsTGId+", ECCC TG Id:"+ecccTGId+
               " H2D2 data line index is="+tgDataColumnIndices.get(chsTGId));

      // --- Create the Map entry for this CHS TG.
      this.nearestModelData.put(chsTGId, new ArrayList<MeasurementCustom>() );
    }

    // --- Get the forecast zero'th hour timestamp to discard the analysis
    //     (a.k.a nowcast) WL data part. Need to use the input file name
    //      to do so.
    final String zerothHourYYYYMMDDhh= new
      File(this.modelInputDataDef).getName().split(H2D2_ASCII_FMT_FNAME_SPLITSTR)[0];

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
    final List<String> relevantDataLines= H2D2ASCIIWLFProbesData.
      subList(H2D2_ASCII_FMT_1ST_DATA_LINE_INDEX, H2D2ASCIIWLFProbesData.size());

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

    slog.info(mmi+"nbTimeStamps="+nbTimeStamps);

    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);
  }

  /**
   * Comments please!
   */
  final static JsonObject getWDSJsonLocationIdInfo( /*@NotNull*/ final String wdsLocationIdInfoFile) {

    final String mmi= "getWDSLocationIdInfo: ";

    Map<String,String> wdsLocationIdInfo= new HashMap<String,String>();

    //--- Deal with possible null tcInputfilePath String: if @NotNull not used
    try {
      wdsLocationIdInfoFile.length();

    } catch (NullPointerException e) {

      slog.error(mmi+"wdsLocationIdInfoFile is null !!");
      throw new RuntimeException(mmi+e);
    }

    slog.info(mmi+"start: wdsLocationIdInfoFile=" + wdsLocationIdInfoFile);

    FileInputStream jsonFileInputStream= null;

    try {
       jsonFileInputStream= new FileInputStream(wdsLocationIdInfoFile);

    } catch (FileNotFoundException e) {
       throw new RuntimeException(mmi+"e");
    }

    final JsonObject mainJsonTcDataInputObj= Json.
      createReader(jsonFileInputStream).readObject();  //tmpJsonTcDataInputObj;

    // --- TODO: add fool-proof checks on all the Json dict keys.

    final JsonObject wdsLocationIdInfoJsonObj=
      mainJsonTcDataInputObj.getJsonObject(IStageIO.LOCATION_INFO_JSON_DICT_KEY);

    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    return wdsLocationIdInfoJsonObj;
  }
}
