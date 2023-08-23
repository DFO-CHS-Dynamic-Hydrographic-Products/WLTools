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
import ca.gc.dfo.chs.wltools.util.IHBGeom;
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

  protected String locationId= null;
  protected String locationIdInfo= null;

  protected InputDataType inputDataType= null;
  protected InputDataTypesFormatsDef inputDataFormat= null;

  protected double adjLocationLatitude= 0.0;
  protected double adjLocationLongitude= 0.0;
  protected double adjLocationZCVsVDatum= 0.0;

  protected ArrayList<WLMeasurement> locationOriginalData= null;
  protected ArrayList<MeasurementCustom> locationAdjustedData= null;

  protected Map<String, ArrayList<WLMeasurement>> nearestObsData= null;
  protected Map<String, ArrayList<WLMeasurement>> nearestModelData= null;

  protected List<String> inputDataFilesPaths= null;

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
  final ArrayList<HBCoords> getH2D2NCDFGridPointsCoords(/*@NotNull*/ final String h2d2NCDFInputDataFile) {

    final String mmi= "getH2D2NCDFGridPointsCoords: ";

    slog.info(mmi+"start: h2d2NCDFInputDataFile=" + h2d2NCDFInputDataFile);

    //--- Deal with possible null tcInputfilePath String: if @NotNull not used
    try {
      h2d2NCDFInputDataFile.length();

    } catch (NullPointerException e) {

      slog.error(mmi+"h2d2NCDFInputDataFile is null !!");
      throw new RuntimeException(mmi+e);
    }

    //Map<Integer,HBCoords> h2d2NCDFGridPointsCoords= null;

    final int openStatus= HDFql.
      execute("USE READONLY FILE "+h2d2NCDFInputDataFile);

    //slog.info(mmi+"openStatus="+openStatus);

    if (openStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+" HDFql.execute() open "+h2d2NCDFInputDataFile+
                                 " file error return value="+openStatus);
    }

    //final String showUseFileRet= HDFql.execute("SHOW USE FILE");
    //slog.info(mmi+"showUseFileRet="+showUseFileRet);

    final String [] nodesLLCoordsDSetIds= ECCC_H2D2_COORDS_DSETS_NAMES.
      get(ECCC_H2D2_WLF_NAMES.SURFACEN).split(INPUT_DATA_FMT_SPLIT_CHAR);

    final String nodesLonCoordsDSetId= nodesLLCoordsDSetIds[IHBGeom.LON_IDX];
    final String nodesLatCoordsDSetId= nodesLLCoordsDSetIds[IHBGeom.LAT_IDX];

    final String [] edgesLLCoordsDSetIds= ECCC_H2D2_COORDS_DSETS_NAMES.
      get(ECCC_H2D2_WLF_NAMES.SURFACEE).split(INPUT_DATA_FMT_SPLIT_CHAR);

    final String edgesLonCoordsDSetId= edgesLLCoordsDSetIds[IHBGeom.LON_IDX];
    final String edgesLatCoordsDSetId= edgesLLCoordsDSetIds[IHBGeom.LAT_IDX];

    final String [] nodesCoordsDSetIds= { nodesLonCoordsDSetId, nodesLatCoordsDSetId };
    final String [] edgesCoordsDSetIds= { edgesLonCoordsDSetId, edgesLatCoordsDSetId };

    final int nodesLatSelectStatus= HDFql.execute("SELECT FROM "+nodesCoordsDSetIds[IHBGeom.LAT_IDX]);

    if (nodesLatSelectStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+" HDFql.execute() select for"+
                                 nodesCoordsDSetIds[IHBGeom.LAT_IDX]+" error return value="+nodesLatSelectStatus);
    }

    final int nodesLonSelectStatus= HDFql.execute("SELECT FROM "+nodesCoordsDSetIds[IHBGeom.LON_IDX]);

    if (nodesLonSelectStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+" HDFql.execute() select for"+
                                 nodesCoordsDSetIds[IHBGeom.LON_IDX]+" error return value="+nodesLonSelectStatus);
    }

    final int howManyNodes= HDFql.cursorGetCount();

    final int edgesLatSelectStatus= HDFql.execute("SELECT FROM "+edgesCoordsDSetIds[IHBGeom.LAT_IDX]);

    if (edgesLatSelectStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+" HDFql.execute() select for"+
                                 edgesCoordsDSetIds[IHBGeom.LAT_IDX]+" error return value="+edgesLatSelectStatus);
    }

    final int edgesLonSelectStatus= HDFql.execute("SELECT FROM "+edgesCoordsDSetIds[IHBGeom.LON_IDX]);

    if (edgesLonSelectStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+" HDFql.execute() select for"+
                                 edgesCoordsDSetIds[IHBGeom.LON_IDX]+" error return value="+edgesLonSelectStatus);
    }

    final int howManyEdges= HDFql.cursorGetCount();

    slog.info(mmi+"howManyNodes="+howManyNodes);
    slog.info(mmi+"howManyEdges="+howManyEdges);

    final int howManyGridPoints= howManyNodes+howManyEdges;

    slog.info(mmi+"howManyGridPoints="+howManyGridPoints);

    ArrayList<HBCoords> h2d2NodesNCDFGridPointsCoords= new ArrayList<HBCoords>();
    ArrayList<HBCoords> h2d2EdgesNCDFGridPointsCoords= new ArrayList<HBCoords>();

    // --- Initialize with undefined HBCoords objects
    for (int gridPointIdx= 0; gridPointIdx< howManyNodes; gridPointIdx++) {
      h2d2NodesNCDFGridPointsCoords.add(gridPointIdx, new HBCoords() );
    }

    // --- Initialize with undefined HBCoords objects
    for (int gridPointIdx= 0; gridPointIdx< howManyEdges; gridPointIdx++) {
      h2d2EdgesNCDFGridPointsCoords.add(gridPointIdx, new HBCoords() );
    }

    // --- Process nodes first
    for(int llIdx=0; llIdx <= IHBGeom.LAT_IDX; llIdx++) {

      slog.info(mmi+"Processing H2D2 nodes NetCDF coordinates dataset -> "+nodesCoordsDSetIds[llIdx]);

      HDFql.execute("SELECT FROM "+nodesCoordsDSetIds[llIdx]);

      for (int dSetValueIdx= 0; dSetValueIdx < howManyNodes; dSetValueIdx++) {

        HDFql.cursorNext();
        //final double llValue= HDFql.cursorGetDouble();
        //slog.info(mmi+"llValue 0="+llValue);

        h2d2NodesNCDFGridPointsCoords.
          get(dSetValueIdx).setLonOrLat(llIdx,HDFql.cursorGetDouble());

        //slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);

      }
    }

    // --- Process edges now
    for(int llIdx=0; llIdx <= IHBGeom.LAT_IDX; llIdx++) {

      slog.info(mmi+"Processing H2D2 edges NetCDF coordinates dataset -> "+edgesCoordsDSetIds[llIdx]);

      HDFql.execute("SELECT FROM "+edgesCoordsDSetIds[llIdx]);

      for (int dSetValueIdx= 0; dSetValueIdx < howManyEdges; dSetValueIdx++) {

        HDFql.cursorNext();
        //final double llValue= HDFql.cursorGetDouble();
        //slog.info(mmi+"llValue 0="+llValue);

        h2d2EdgesNCDFGridPointsCoords.
          get(dSetValueIdx).setLonOrLat(llIdx,HDFql.cursorGetDouble());

        //slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);

      }
    }


    final int closeStatus= HDFql.execute("CLOSE FILE "+h2d2NCDFInputDataFile);

    //slog.info(mmi+"closeStatus="+closeStatus);

    if (closeStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+" HDFql.execute() close file error return value="+closeStatus);
    }

    ArrayList<HBCoords> h2d2NCDFGridPointsCoords= new
      ArrayList<HBCoords>(h2d2NodesNCDFGridPointsCoords);

    h2d2NCDFGridPointsCoords.addAll(h2d2EdgesNCDFGridPointsCoords);

    //final HBCoords checkAllN0= h2d2NCDFGridPointsCoords.get(0);
   // slog.info(mmi+"checkAllN0 lon="+checkAllN0.getLongitude());
   // slog.info(mmi+"checkAllN0 lat="+checkAllN0.getLatitude());

    //final HBCoords checkAllE0= h2d2NCDFGridPointsCoords.get(howManyNodes);
    //slog.info(mmi+"checkAllE0 lon="+checkAllE0.getLongitude());
    //slog.info(mmi+"checkAllE0 lat="+checkAllE0.getLatitude());

    //final HBCoords checkAllEE= h2d2NCDFGridPointsCoords.get(howManyGridPoints-1);
    //slog.info(mmi+"checkAllEE lon="+checkAllEE.getLongitude());
    //slog.info(mmi+"checkAllEE lat="+checkAllEE.getLatitude());

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

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

