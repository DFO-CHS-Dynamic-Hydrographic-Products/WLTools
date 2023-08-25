package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

// ---
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// ---
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

  protected List<String> modelInputDataFiles= null;

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

    this.modelInputDataFiles= null;
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


    // --- Create the ArrayList<HBCoords> that will contain all the
    //     model coordinates from FEM grid (triangles) nodes and edges
    //     with the h2d2NodesNCDFGridPointsCoords
    ArrayList<HBCoords> h2d2NCDFGridPointsCoords= new
      ArrayList<HBCoords>(h2d2NodesNCDFGridPointsCoords);

    // --- Add the h2d2EdgesNCDFGridPointsCoords ArrayList<HBCoords> to
    //     the h2d2NCDFGridPointsCoords (at its end)
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
  final void getH2D2NearestGPNCDFWLData(/*@NotNull@*/ final Map<String, HBCoords> nearestsTGCoords ) {

    final String mmi= "getH2D2NearestGPNCDFWLData: ";

    slog.info(mmi+"start");

    if (this.modelInputDataFiles == null) {
      throw new RuntimeException(mmi+"this.modelInputDataFiles cannot be null at this point!!");
    }

    if (this.locationId == null) {
      throw new RuntimeException(mmi+"this.locationId cannot be null at this point!!");
    }

    // --- Now get the coordinates of:
    //     1). The nearest H2D2 model input data grid point from the WDS location
    //     2). The nearest H2D2 model input data grid point from the three nearest TG locations.
    final String firstInputDataFile= this.modelInputDataFiles.get(0);

    slog.info(mmi+"firstInputDataFile="+firstInputDataFile);

    // ---
    final ArrayList<HBCoords> mdlGrdPtsCoordinates=
      this.getH2D2NCDFGridPointsCoords(firstInputDataFile);

    // --- Now locate the nearest H2D2 model grid point from the WDS location.
    int nearestMdlGpIndex= -1;
    double minDist= Double.MAX_VALUE;

    for (int modelGridPointIdx= 0;
             modelGridPointIdx < mdlGrdPtsCoordinates.size(); modelGridPointIdx++) {

      final HBCoords mdlGridPointHBCoords= mdlGrdPtsCoordinates.get(modelGridPointIdx);

      final double checkDist= Trigonometry.
        getDistanceInRadians(this.adjLocationLongitude, this.adjLocationLatitude,
                             mdlGridPointHBCoords.getLongitude(),mdlGridPointHBCoords.getLatitude());

       if (checkDist < minDist) {
         minDist= checkDist;
         nearestMdlGpIndex= modelGridPointIdx;
       }
    }

    slog.info(mmi+"nearestMdlGpIndex="+nearestMdlGpIndex+
              " for the WDS location="+ this.locationId);

    //final HBCoords mdlGridPointHBCoordsCheck= mdlGrdPtsCoordinates.get(nearestMdlGpIndex);
    //slog.info(mmi+"this.adjLocationLongitude="+this.adjLocationLongitude);
    //slog.info(mmi+"this.adjLocationLatitude="+this.adjLocationLatitude);
    //slog.info(mmi+"mdlGridPointHBCoordsCheck lon="+mdlGridPointHBCoordsCheck.getLongitude());
    //slog.info(mmi+"mdlGridPointHBCoordsCheck lat="+mdlGridPointHBCoordsCheck.getLatitude());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    //this.nearestModelData= new HashMap<String, ArrayList<WLMeasurement>>();
    //this.nearestModelData.put(this.locationId,
    //                          this.getH2D2WLForecastData(nearestMdlGpIndex));

    Map<String,Integer> nearestModelDataInfo= new HashMap<String,Integer>();

    nearestModelDataInfo.put(this.locationId,nearestMdlGpIndex);

    // --- Now locate the 3 nearest H2D2 model grid points from the
    //     3 nearest tide gauges (one tide gauge => one model grid point).
    //     and get the related H2D2 WL data.
    for (final String tgCoordId: nearestsTGCoords.keySet()) {

      slog.info(mmi+"Searching for the nearest H2D2 model grid point from the TG:"+tgCoordId);

      final HBCoords tgHBCoords= nearestsTGCoords.get(tgCoordId);

      final double tgLat= tgHBCoords.getLatitude();
      final double tgLon= tgHBCoords.getLongitude();

      //slog.info(mmi+"tgLon="+tgLon);
      //slog.info(mmi+"tgLat="+tgLat);

      int nearestModelGridPointIndex= -1;
      double mdlGpMinDist= Double.MAX_VALUE;

      for (int modelGridPointIdx= 0;
               modelGridPointIdx < mdlGrdPtsCoordinates.size(); modelGridPointIdx++) {

        final HBCoords mdlGridPointHBCoords= mdlGrdPtsCoordinates.get(modelGridPointIdx);

        final double checkDist= Trigonometry.getDistanceInRadians( tgLon, tgLat,
          mdlGridPointHBCoords.getLongitude(), mdlGridPointHBCoords.getLatitude() );

        //slog.info(mmi+"mdlGridPointHBCoords.getLongitude()="+mdlGridPointHBCoords.getLongitude());
        //slog.info(mmi+"mdlGridPointHBCoords.getLatitude()="+mdlGridPointHBCoords.getLatitude());
        //slog.info(mmi+"checkDist="+checkDist);
        //slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);

        if (checkDist < mdlGpMinDist) {
          mdlGpMinDist= checkDist;
          nearestModelGridPointIndex= modelGridPointIdx;
        }
      }

      slog.info(mmi+"nearestModelGridPointIndex="+
                nearestModelGridPointIndex+" for tide gauge -> "+tgCoordId);

      //final HBCoords mdlGridPointHBCoordsCheck= mdlGrdPtsCoordinates.get(nearestModelGridPointIndex);
      //slog.info(mmi+"tgLon="+tgLon);
      //slog.info(mmi+"tgLat="+tgLat);
      //slog.info(mmi+"mdlGridPointHBCoordsCheck.getLongitude()="+mdlGridPointHBCoordsCheck.getLongitude());
      //slog.info(mmi+"mdlGridPointHBCoordsCheck.getLatitude()="+mdlGridPointHBCoordsCheck.getLatitude());
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);

      //this.nearestModelData.put(tgCoordId,
      //                          this.getH2D2WLForecastData(nearestModelGridPointIndex));

      nearestModelDataInfo.put(tgCoordId,nearestModelGridPointIndex);
    }

    this.getH2D2WLForecastData(nearestModelDataInfo);

    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

  }

  /**
   * Comments please!
   */
  //final ArrayList<WLMeasurement>
  final void getH2D2WLForecastData(/*@NotNull*/ final Map<String,Integer> nearestModelDataInfo) {

    final String mmi= "getH2D2WLForecastData: ";

    slog.info(mmi+"start");

    if (this.modelInputDataFiles == null) {
      throw new RuntimeException(mmi+"this.modelInputDataFiles cannot be null at this point!!");
    }

    //--- Create the this.nearestModelData HashMap
    this.nearestModelData= new HashMap<String,ArrayList<MeasurementCustom>>();

    // --- Intialize each interpolation location wanted with empty
    //     ArrayList<MeasurementCustom> objects .
    for (final String locationStrId: nearestModelDataInfo.keySet()) {
      //this.nearestModelData.put(locationStrId, new ArrayList<WLMeasurement>() );
      this.nearestModelData.put(locationStrId, new ArrayList<MeasurementCustom>() );
    }

    Map<Instant,String> relevantInputFiles= new HashMap<Instant,String>();

    //--- Keep only the files that are more recent for a given timestamp
    for (final String h2d2NCDFInputDataFile: this.modelInputDataFiles) {

      slog.info(mmi+" Processing h2d2NCDFInputDataFile -> "+h2d2NCDFInputDataFile);

      final String [] fNameTmp= ((new File(h2d2NCDFInputDataFile).getName()).split("\\.")[0]).split("_");

      //slog.info(mmi+"fNameTmp 0="+fNameTmp[0]);
      //slog.info(mmi+"fNameTmp="+fNameTmp.toString());
      //slog.info(mmi+" new File(h2d2NCDFInputDataFile).getName()="+ new File(h2d2NCDFInputDataFile).getName());

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);

      final String YYYYMMDDhh= fNameTmp[5];
      final String hhmmFut= fNameTmp[6];

      //final int openStatus= HDFql.
      //  execute("USE READONLY FILE "+h2d2NCDFInputDataFile);

      //if (openStatus != HDFqlConstants.SUCCESS) {
      //  throw new RuntimeException(mmi+" HDFql.execute() open file -> "+
      //                             h2d2NCDFInputDataFile+" error return value="+openStatus);
      //}

      //HDFql.execute("SELECT FROM "+ECCC_H2D2_TIME_ATTR_NAME);

      //final int checkTimeIntType = HDFql.cursorGetDataType();

      //slog.info(mmi+"checkTimeIntType="+checkTimeIntType);

      long timeStampSeconds= 0;

      //HDFql.cursorNext();

      //if (checkTimeIntType == HDFqlConstants.INT) {
      //
      //  //slog.warn(mmi+"checkTimeIntType == HDFqlConstants.INT !! converting time integer to long integer");
      //
      //  timeStampSeconds= (long)HDFql.cursorGetInt();
      //
      //} else if (checkTimeIntType == HDFqlConstants.BIGINT) {
      //
      //  //slog.info(mmi+"checkTimeIntType == HDFqlConstants.BIGINT, we are happy!!");
      //
      //  timeStampSeconds= HDFql.cursorGetBigint();
      //}

      //slog.info(mmi+"timeStampSeconds="+timeStampSeconds);

      //final Instant timeStampInstant= Instant.ofEpochSecond(timeStampSeconds);

      //relevantInputFiles.put(timeStampInstant,h2d2NCDFInputDataFile);

      //final int closeStatus= HDFql.
      //  execute("CLOSE FILE "+h2d2NCDFInputDataFile);

      //if (closeStatus != HDFqlConstants.SUCCESS) {
      //  throw new RuntimeException(mmi+" HDFql.execute() close file -> "+
      //                             h2d2NCDFInputDataFile+" error return value="+closeStatus);
      //}
    }

    slog.info(mmi+"relevantInputFiles.size()="+relevantInputFiles.size());

    //final Set<String> checkFiles= (Set<String>)relevantInputFiles.values();
    //slog.info(mmi+"checkFiles="+checkFiles.toString());

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    for (final Instant timeStampInstant: relevantInputFiles.keySet()) {

      final String h2d2NCDFInputDataFile= relevantInputFiles.get(timeStampInstant);

      //slog.info(mmi+"h2d2NCDFInputDataFile="+h2d2NCDFInputDataFile);

      final int openStatus= HDFql.
        execute("USE READONLY FILE "+h2d2NCDFInputDataFile);

      if (openStatus != HDFqlConstants.SUCCESS) {
        throw new RuntimeException(mmi+" HDFql.execute() open file -> "+
                                   h2d2NCDFInputDataFile+" error return value="+openStatus);
      }

      // --- Recall that the H2D2 WL data is separated in two different datasets
      //      SURFACEN(nodes) and SURFACEE (edges)

      // --- MUST process nodes first as for the coordinates
      ArrayList<Double> h2d2WLNodesData= new ArrayList<Double>();

      // --- MUST process nodes first as for the coordinates
      HDFql.execute("SELECT FROM "+ECCC_H2D2_WLF_NAMES.SURFACEN.name());

      final int howManyNodes= HDFql.cursorGetCount();

      // --- Loop on the nodes WL data
      for (int dSetValueIdx= 0; dSetValueIdx < howManyNodes; dSetValueIdx++) {

        HDFql.cursorNext();
        h2d2WLNodesData.add(HDFql.cursorGetDouble());

        //slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);
      }

      ArrayList<Double> h2d2WLEdgesData= new ArrayList<Double>();

      HDFql.execute("SELECT FROM "+ECCC_H2D2_WLF_NAMES.SURFACEE.name());

      final int howManyEdges= HDFql.cursorGetCount();

      // --- Loop on the edges WL data
      //     TODO: Add a generic method to do that
      for (int dSetValueIdx= 0; dSetValueIdx < howManyEdges; dSetValueIdx++) {

        HDFql.cursorNext();
        h2d2WLEdgesData.add(HDFql.cursorGetDouble());
      }

      // --- Put nodes and edges WL data in just one ArrayList<Double> object
      ArrayList<Double> h2d2WLData= new ArrayList<Double>(h2d2WLNodesData);

      h2d2WLData.addAll(h2d2WLEdgesData);

      //final MeasurementCustom measCstm= new
      //   MeasurementCustom(Instant.ofEpochSecond(timeStampSeconds), wlPrediction, 0.0)

      for (final String locationStrId: nearestModelDataInfo.keySet()) {

        final int nearestModelGridPoint= nearestModelDataInfo.get(locationStrId);

        //HDFql.cursor_absolute(nearestModelGridPoint);

        final MeasurementCustom measCstm= new
          MeasurementCustom(timeStampInstant, h2d2WLData.get(nearestModelGridPoint), 0.0);

        this.nearestModelData.get(locationStrId).add( measCstm );//new WLMeasurement(measCstm) );
      }

      final int closeStatus= HDFql.
        execute("CLOSE FILE "+h2d2NCDFInputDataFile);

      if (closeStatus != HDFqlConstants.SUCCESS) {
        throw new RuntimeException(mmi+" HDFql.execute() close file -> "+
                                   h2d2NCDFInputDataFile+" error return value="+closeStatus);
      }

      //slog.info(mmi+" Done with h2d2NCDFInputDataFile -> "+h2d2NCDFInputDataFile);
    }

    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    //return retList;
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

