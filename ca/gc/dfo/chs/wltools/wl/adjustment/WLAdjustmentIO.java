package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import as.hdfql.HDFql;
import as.hdfql.HDFqlCursor;
import as.hdfql.HDFqlConstants;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
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

  protected String locationIdInfo= null;

  protected InputDataType inputDataType= null;
  protected InputDataTypesFormatsDef inputDataFormat= null;

  protected double adjLocationLatitude= 0.0;
  protected double adjLocationLongitude= 0.0;
  protected double adjLocationZCVsVDatum= 0.0;

  protected ArrayList<WLMeasurement> locationOriginalData= null;
  protected ArrayList<MeasurementCustom> locationAdjustedData= null;

  protected Map<String, ArrayList<WLMeasurement>> nearestObsData= null;

  protected List<String> inputDataFilesPaths= null;

  /**
   * Comments please!
   */
  public WLAdjustmentIO() {

    this.argsMapKeySet= null;

    this.locationIdInfo= null;

    this.inputDataType= null;
    this.inputDataFormat= null;

    this.adjLocationZCVsVDatum=
      this.adjLocationLatitude=
        this.adjLocationLongitude= 0.0;

    this.locationOriginalData= null;
    this.locationAdjustedData= null;

    this.nearestObsData= null;
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
  final Map<Integer,HBCoords> getH2D2NCDFGridPointsCoords(/*@NotNull*/ final String h2d2NCDFInputDataFile) {

    final String mmi= "getH2D2NCDFGridPointsCoords: ";

    slog.info(mmi+"start: h2d2NCDFInputDataFile=" + h2d2NCDFInputDataFile);

    //--- Deal with possible null tcInputfilePath String: if @NotNull not used
    try {
      h2d2NCDFInputDataFile.length();

    } catch (NullPointerException e) {

      slog.error(mmi+"h2d2NCDFInputDataFile is null !!");
      throw new RuntimeException(mmi+e);
    }

    Map<Integer,HBCoords> h2d2NCDFGridPointsCoords= null;

    final int openStatus= HDFql.execute("USE READONLY FILE " + h2d2NCDFInputDataFile +"; SHOW USE FILE");

    //slog.info(mmi+"openStatus="+openStatus);

    if (openStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+" HDFql.execute() open file error return value="+openStatus);
    }

    //final String showUseFileRet= HDFql.execute("SHOW USE FILE");
    //slog.info(mmi+"showUseFileRet="+showUseFileRet);

    final String [] nodesLLCoordsDsetIds= ECCC_H2D2_COORDS_DSETS_NAMES.
      get(ECCC_H2D2_WLF_NAMES.SURFACEN).split(INPUT_DATA_FMT_SPLIT_CHAR);

    final String nodesLonCoordsDsetId= nodesLLCoordsDsetIds[0];
    final String nodesLatCoordsDsetId= nodesLLCoordsDsetIds[1];

    final String [] edgesLLCoordsDsetIds= ECCC_H2D2_COORDS_DSETS_NAMES.
      get(ECCC_H2D2_WLF_NAMES.SURFACEE).split(INPUT_DATA_FMT_SPLIT_CHAR);

    final String edgesLonCoordsDsetId= edgesLLCoordsDsetIds[0];
    final String edgesLatCoordsDsetId= edgesLLCoordsDsetIds[1];

     //HDFql.execute("SELECT FROM "+ECCC_H2D2_COORDS_DSETS_NAMES.ECCC_H2D2_WLF_NAMES.SURFACEN

    final int closeStatus= HDFql.execute("CLOSE FILE "+h2d2NCDFInputDataFile);

    //slog.info(mmi+"closeStatus="+closeStatus);

    if (closeStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+" HDFql.execute() close file error return value="+closeStatus);
    }

    slog.info(mmi+"end");
    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    return h2d2NCDFGridPointsCoords;
  }

  /**
   * Comments please!
   */
  final static JsonObject getWDSLocationIdInfo( /*@NotNull*/ final String wdsLocationIdInfoFile) {

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

