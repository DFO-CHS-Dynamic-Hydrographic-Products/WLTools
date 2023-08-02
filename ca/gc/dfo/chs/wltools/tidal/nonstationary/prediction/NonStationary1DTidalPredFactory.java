//package ca.gc.dfo.iwls.fmservice.modeling.tides;
package ca.gc.dfo.chs.wltools.tidal.nonstationary.prediction;

/**
 * Created on 2018-01-19.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-20, Gilles Mercier
 */

//---
import ca.gc.dfo.chs.wltools.nontidal.stage.Stage;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageInputData;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageCoefficient;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1D;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1DData;
import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.Stationary1DTidalPredFactory;
//import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.TidalPredictionsFactory;

//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1D;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1DData;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
//---
//---

/**
 * River-discharge and-or atmospheric influenced (a.k.a. non-stationary) tidal prediction object.
 * This implements the prediction-reconstruction part of the NS_TIDE theory
 *  (Matte et al., Journal of Atmospheric and Oceanic Technology, 2013)
 */
final public class NonStationary1DTidalPredFactory
   extends Stationary1DTidalPredFactory implements IStageIO, INonStationaryIO {

   private final static String whoAmI=
      "ca.gc.dfo.chs.wltools.tidal.nonstationary.prediction.NonStationary1DTidalPredFactory";

  /**
   * log utility.
   */
  private final Logger slog = LoggerFactory.getLogger(whoAmI);

  /**
   * List of HashMap objects of tidal constituents information for the non-stationary(river discharge and-or atmospheric)
   * tidal constituents with polynomial order >=1 for a specific location(station) coming from a file or a DB.
   */
  private HashMap<String, HashMap<String, Constituent1D>> hoTcDataMaps= null;

  /**
   * List of Constituent1DData objects which will be used by the non-stationary tidal prediction method.
   * MUST have the number of Constituent1DData objects in the constituent1DDataItems being the same as we
   * have Map items in one Map item of tcDataMaps list.
   */
  //private Map<String, Map<String,Constituent1DData>> constituent1DDataItems= null;
  private HashMap<String, Constituent1DData> constituent1DDataItems= null;

  /**
   * The stage equation (polynomial) object.
   */
  private Stage stagePart= null;

  ///**
  // * To store the stage input data (river discharges and-or atmos. data) with their related time stamps
  // */
  //private Map<String,List<MeasurementCustom>> staticStageInputData= null;
  //private List<List<Map<Long,MeasurementCustom>>> staticStageInputData= null;

  /**
   * Default constructor.
   */
  public NonStationary1DTidalPredFactory() {

    super();

    this.stagePart= null;
    this.hoTcDataMaps= null;
    this.constituent1DDataItems= null;
  }


  public NonStationary1DTidalPredFactory(/*NotNull*/final String stationId,
                                         /*NotNull*/final IStage.Type type,
                                         /*NotNull*/final Long timeStartSeconds,
                                         /*NotNull*/final Long timeEndSeconds,
                                         /*NotNull*/final Long timeIncrSeconds,
                                         final String stageInputDataFile,
                                         final IStageIO.FileFormat stageInputDataFileFormat) {
     this();

     this.stagePart= new Stage(stationId,type,
                               timeStartSeconds,timeEndSeconds,timeIncrSeconds,
                               stageInputDataFile,stageInputDataFileFormat);

  }
  /**
   * @param timeStampSeconds : A time-stamp in seconds since the epoch where we want a single tidal prediction.
   * @return The newly computed single tidal prediction in double precision.
   */
  @Override
  final public double computeTidalPrediction(final long timeStampSeconds) {

     //final Map<String,StageInputData> stageInputDataMap= this.stagePart.getInputDataMap();
     //final HashMap<Long,StageInputData> stageInputDataMap= this.stagePart.getInputDataMap();
     //slog.info("computeTidalPrediction: start, timeStampSeconds="+timeStampSeconds);

     final HashMap<String,StageCoefficient> stageCoefficientMap= this.stagePart.getCoeffcientsMap();

     //slog.info("computeTidalPrediction: stageCoefficientMap="+stageCoefficientMap.toString());
     //slog.info("computeTidalPrediction: debug System.exit(0)");
     //System.exit(0);

     //final HashMap<String,MeasurementCustom> stageInputDataMap=
     final HashMap<Long,StageInputData> stageInputTimeStampedData= this.stagePart.getTimeStampedInputData();

     //slog.info("computeTidalPrediction: aft. getting stage objects.");

       //getInputDataAtTimeStamp(timeStampSeconds - stageCoefficient.getTimeLagSeconds());

     // --- Get the zero'th order non-stationary WL pred. part taking the stage zero'th
     //    order coefficient as the Z0 (WL average).
     double tidalPredValue= super.
       computeTidalPrediction(timeStampSeconds) +
          stageCoefficientMap.get(STAGE_JSON_ZEROTH_ORDER_KEY).getValue();

     //slog.info("computeTidalPrediction: aft. getting stationary tidalPredValue="+tidalPredValue);

     // --- Add the higher order(s) contribution to the WL tidal pred. signal.
     for (final String stageCoeffId: this.constituent1DDataItems.keySet()) { // stageInputDataMap.keySet()) {

        // --- Get the non-stationary WL tidal pred. contribution for this
        //     higher order >=1/
        final double hoTidalValue= super.astroInfosFactory.
           computeTidalPrediction(timeStampSeconds,this.constituent1DDataItems.get(stageCoeffId));
        
        //slog.info("computeTidalPrediction: aft. getting non-stationary hoTidalValue="+hoTidalValue);

        //final StageInputData stageInputData= stageInputDataMap.get(stInputDataId);

        final StageCoefficient stageCoefficient= stageCoefficientMap.get(stageCoeffId);
        final double stageCoefficientValue= stageCoefficient.getValue();

        //slog.info("computeTidalPrediction: aft. getting stageCoefficientValue="+stageCoefficientValue);
        //slog.info("computeTidalPrediction: aft. getting stageCoefficientValue, stageCoefficient.getTimeLagSeconds()="+stageCoefficient.getTimeLagSeconds());

        // --- Get the stage value for this stage coefficient using the time lag
        //     as determined by the non-stationary tidal analysis.
        //final double stageInputDataValue= stageInputDataMap.get(stageCoeffId).
        //    getAtTimeStamp(timeStampSeconds - stageCoefficient.getTimeLagSeconds());
        final StageInputData stageInputData=
          stageInputTimeStampedData.get(timeStampSeconds - stageCoefficient.getTimeLagSeconds());

        //slog.info("computeTidalPrediction: aft. getting stageInputData="+stageInputData.toString());
        //slog.info("computeTidalPrediction: aft. getting stageInputData, stageCoeffId="+stageCoeffId);

        final double stageInputDataValue= stageInputData.getValueForCoeff(stageCoeffId); //.getDataUnitValue();

        //slog.info("computeTidalPrediction: aft. getting stageInputDataValue="+stageInputDataValue);

        // ---- Apply the non-stationary calculation with the related stage value part and the
        //      the hoTidalValue for this higher order.
        tidalPredValue += (stageCoefficientValue + hoTidalValue) * stageInputDataValue ; /// stageInputDataValue;
     }

     //slog.info("computeTidalPrediction: end");

     // ---
     return tidalPredValue;
  }

  /**
   * Comments please!
   */
  final public NonStationary1DTidalPredFactory getNSJSONFileData(/*@NotNull*/ final String tcInputfilePath) {

    //System.out.println("NonStationary1DTidalPredFactory getNSJSONFileData: start");

    //--- Deal with possible null tcInputfilePath String:
    try {
      tcInputfilePath.length();

    } catch (NullPointerException e) {

      slog.error("getNSJSONFileData: tcInputfilePath is null !!");
      throw new RuntimeException(e);
    }

    slog.info("getNSJSONFileData Start: tcInputfilePath=" + tcInputfilePath);

    //--- Get the TCF format ASCII lines in a List of Strings:
    //final List<String> jsonFileLines = ASCIIFileIO.getFileLinesAsArrayList(tcInputfilePath);
    //final JsonParser tcJsonInput= Json.createParser(new FileInputStream(tcInputfilePath));

    //JsonObject tmpJsonTcDataInputObj= null;
    FileInputStream jsonFileInputStream= null;

    try {
        //final FileInputStream tcInputfilePathRdr= new FileInputStream(tcInputfilePath);
       //final JsonArray jsonTcDataInputArray= Json.createReader(new FileInputStream(tcInputfilePath)).readArray();
       //tmpJsonTcDataInputObj= Json.createReader(new FileInputStream(tcInputfilePath)).readObject();

       jsonFileInputStream= new FileInputStream(tcInputfilePath);

    } catch (FileNotFoundException e) {

       //this.log.error("tcInputfilePath"+tcInputfilePath+" not found !!");
       throw new RuntimeException(e);
    }

    final JsonObject mainJsonTcDataInputObj=
       Json.createReader(jsonFileInputStream).readObject();  //tmpJsonTcDataInputObj;

    // --- TODO: add fool-proof checks on all the Json dict keys.

    final JsonObject channelGridPointJsonObj=
       mainJsonTcDataInputObj.getJsonObject(STATION_INFO_JSON_DICT_KEY);

    //this.log.info("channelGridPointInfo="+channelGridPointInfo.toString());

    final double stationLat= channelGridPointJsonObj.
       getJsonNumber(STATION_INFO_JSON_LATCOORD_KEY).doubleValue();

    slog.info("getNSJSONFileData: stationLat="+stationLat);

    // --- Populate the this.stagePart object.
    final JsonObject stageJsonObj=
       mainJsonTcDataInputObj.getJsonObject(STAGE_JSON_DICT_KEY);

    //this.stagePart= new Stage();
    if (this.stagePart == null) {
       throw new RuntimeException("getNSJSONFileData: this.stagePart cannot be null at this point!! ");
    }

    // --- Populate the this.stagePart with the stage equation coefficients.
    this.stagePart.setCoeffcientsMap(stageJsonObj);

    //final HashMap<String,StageCoefficient>
    final Set<String> stageCoefficientsIds=
       this.stagePart.getCoeffcientsMap().keySet();

    slog.info("getNSJSONFileData: station stageCoefficientsIds="+stageCoefficientsIds); //.keySet().toString());

    // --- Populate the non-stationary tidal constituents data with the Json
    //     formatted file content.
    final JsonObject jsonTcDataInputObj=
       mainJsonTcDataInputObj.getJsonObject(FLUVIAL_TIDAL_CONSTS_JSON_DICT_KEY);

    // --- Allocate the super.tcDataMap HashMap object to store
    //     the zero'th order non-stationary tidal constituents
    //     data before doing the related astronomic tidal calculations
    //      for them.
    super.tcDataMap= new HashMap<>();

    // ---  Allocate this.tcDataMaps to store the order>=1 non-stationary
    //      tidal constituents before doing the related astronomic tidal
    //      calculations for them.
    this.hoTcDataMaps= new HashMap<>();

    // --- Set the String keys of the this.hoTcDataMaps HashMap object,
    for (final String stageCoefficientsId: stageCoefficientsIds) {

       // --- Must not consider the zero'th order key for the this.hoTcDataMaps object,
       if (! stageCoefficientsId.equals(STAGE_JSON_ZEROTH_ORDER_KEY) ) {
          this.hoTcDataMaps.put(stageCoefficientsId, new HashMap<>() );
       }
    }

    final String jsonAmplitudeKey=
       STAGE_JSON_KEYS_SPLIT + TIDAL_CONSTS_JSON_AMP_KEY;

    final String jsonGrwPhaseLagKey=
       STAGE_JSON_KEYS_SPLIT + TIDAL_CONSTS_JSON_PHA_KEY;

    // --- Build the zero'th order amplitude key for the Jsom
    //     formatted input.
    final String zeroThOrderAmpKey= STAGE_JSON_ZEROTH_ORDER_KEY + jsonAmplitudeKey;
                 //STAGE_JSON_KEYS_SPLIT + TIDAL_CONSTS_JSON_AMP_KEY;

    // --- Build the zero'th order Greenwich phase lag key for the Jsom
    //     formatted input.
    final String zeroThOrderPhaKey= STAGE_JSON_ZEROTH_ORDER_KEY + jsonGrwPhaseLagKey;
                // STAGE_JSON_KEYS_SPLIT + TIDAL_CONSTS_JSON_PHA_KEY;

    // --- Now populate the super.tcDataMap and the this.tcDataMaps HashMap objects
    //     with the related non-stationary tidal constituents data.
    for (final String jsonTcConstName: jsonTcDataInputObj.keySet()) {

       // --- Be sure to remove any leading and-or trailing blank
       //     (and annoying) characters.
       //final String tcConstName= jsonTcConstName.strip();
       //this.log.info("Processing tidal const. "+tcConstName);

       //this.tcDataMaps.put(tcConstName, new HashMap<>());

       // --- Get the JsonObject for this non-stationary tidal const.
       final JsonObject jsonTcDataObj=
          jsonTcDataInputObj.getJsonObject(jsonTcConstName);
 
       // --- Amplitude for the zero'th order of this
       //     tidal const.
       final double zeroThOrderAmplitude=
          jsonTcDataObj.getJsonNumber(zeroThOrderAmpKey).doubleValue();

       // ---Assuming that the Greenwich phase lag is in radians here.
       final double zeroThOrderGrwPhaseLag=
          jsonTcDataObj.getJsonNumber(zeroThOrderPhaKey).doubleValue();

       // --- Be sure to remove any leading and-or trailing blank
       //     (and annoying) characters.
       final String tcConstName= jsonTcConstName.strip();
       slog.info("getNSJSONFileData: Processing tidal const. \""+tcConstName+"\"");

       // --- Populate the super.tcDataMap object with the
       //     zero'th order for this non-stationary tidal const.
       super.tcDataMap.put( tcConstName,
                            new Constituent1D(zeroThOrderAmplitude,zeroThOrderGrwPhaseLag)) ;

       // --- Loop on the order >=1 stage(s) id(s) and set the inner
       //     HashMap with the non-staionary tidal constituents info
       //     for this location.
       for (final String hoStageId: this.hoTcDataMaps.keySet()) {

          final String hoOrderAmpKey= hoStageId + jsonAmplitudeKey;
          final String hoOrderPhaKey= hoStageId + jsonGrwPhaseLagKey;

          final double hoTcAmplitude=
            jsonTcDataObj.getJsonNumber(hoOrderAmpKey).doubleValue();

          final double hoTcGrwPhaseLag=
            jsonTcDataObj.getJsonNumber(hoOrderPhaKey).doubleValue();

          this.hoTcDataMaps.get(hoStageId).put( tcConstName,
                                                new Constituent1D(hoTcAmplitude,hoTcGrwPhaseLag) );
       }

       slog.info("getNSJSONFileData: Done with Processing tidal const. \""+tcConstName+"\"");
    }

    //this.log.info("super.tcDataMap.keySet="+super.tcDataMap.keySet().toString());
    //this.log.info("\n\nsuper.tcDataMap.get(M2)="+super.tcDataMap.get("M2").toString());
    //this.log.info("\n\nthis.hoTcDataMaps.get(CS1).get(M2)="+this.hoTcDataMaps.get("CS1").get("M2").toString());
    //this.log.info("\n\nthis.hoTcDataMaps.get(CS2).get(M2)="+this.hoTcDataMaps.get("CS2").get("M2").toString());

    //this.log.debug("NonStationary1DTidalPredFactory getNSJSONFileData: done with Json.createParser");
    //this.log.info("Debug System.exit(0)");
    //System.exit(0);

    try {
       jsonFileInputStream.close();
    } catch (IOException e) {
       throw new RuntimeException(e);
    }

    slog.info("getNSJSONFileData: End");

    return this;
  }

  /**
   * @param method           : Tidal prediction method to use.
   * @param latitudeRadians  : Latitude of the 2D point where we want 1D tidal predictions
   * @param startTimeSeconds : Time-stamp in seconds since the epoch for the time reference used for astronomic
   *                         arguments computations.
   * @param constNames       : A Set of tidal constituents names to use for the tidal predictions.
   * @return The current  object.
   */
  @Override
  final public NonStationary1DTidalPredFactory setAstroInfos(final Method method,
                                                             final double latitudeRadians,
                                                             final long startTimeSeconds,
                                                             /*@NotNull @Size(min = 1)*/ final Set<String> constNames) {
    try {
      constNames.size();
      
    } catch (NullPointerException e) {
      
      slog.error("setAstroInfos: constNames==null !!");
      throw new RuntimeException(e);
    }
    
    slog.info("setAstroInfos: start");

    // --- Compute the stationary astronomic information and set the stationary
    //     tidal constituents amplitudes and phases.
    //super.setAstroInfos(STATIONARY_FOREMAN, latitudeRadians, startTimeSeconds, constNames);
    super.setAstroInfos(method, latitudeRadians, startTimeSeconds, constNames);

    slog.info("setAstroInfos: after super.setAstroInfos()");
    //slog.info("setAstroInfos : Debug exit(0)");
    //System.exit(0);

    slog.info("setAstroInfos: this.constituent1DDataItems.keySet()="+this.hoTcDataMaps.keySet().toString());
    //slog.info("setAstroInfos : Debug exit(0)");
    //System.exit(0);

    this.constituent1DDataItems= new HashMap<>();

    // --- Set the non-stationary tidal constituents amplitudes and phases for
    //     the higher order(s) stage(s)
    for (final String tcMapId: this.hoTcDataMaps.keySet()) { //this.constituent1DDataItems.keySet()) {

       //slog.info("setAstroInfos: tcMapId="+tcMapId);
       //slog.info("setAstroInfos:this.hoTcDataMaps.get(tcMapId)="+this.hoTcDataMaps.get(tcMapId).toString());

       this.constituent1DDataItems.put(tcMapId,
          new Constituent1DData(this.hoTcDataMaps.get(tcMapId),this.astroInfosFactory));
    }

    slog.info("setAstroInfos: end");
    //slog.info("setAstroInfos : Debug exit(0)");
    //System.exit(0);

    return this;
  }
}
