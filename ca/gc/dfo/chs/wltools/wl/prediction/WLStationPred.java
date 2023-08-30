//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created by Gilles Mercier on 2018-01-10.
 */

//---
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
//import javax.validation.constraints.NotNull;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPred;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory;

/**
 * Class for the computation of water level predictions
 */
final public class WLStationPred extends WLStationPredFactory {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred";

  /**
   * Usual class instance log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //private String outputDirectory= null;
  //private List<MeasurementCustom> predictionData= null;

  /**
   * Default constuctor:
   */
  public WLStationPred() {
    super();

    this.outputDirectory= null;
    this.predictionData= null;
  }

 /**
   * Parse the main program arguments:
   */
  public WLStationPred(/*NotNull*/ final HashMap<String,String> argsMap) {

    final String mmi=
      "WLStationPred(final HasMap<String,String> mainProgramOptions) constructor: ";

    slog.info(mmi+"start");

    final Set<String> argsMapKeySet= argsMap.keySet();

    if (!argsMapKeySet.contains("--startTimeISOFormat")) {
      throw new RuntimeException(mmi+"Must have the mandatory prediction location info option: --startTimeISOFormat=<YYYY-MM-DDThh:mm:ss> defined");
    }

    // --- Fool-proof check for the startTimeISOFormat?
    //     Assuming that the machine time is in UTC
    final String startTimeISOFormat= argsMap.get("--startTimeISOFormat");

    slog.info(mmi+"startTimeISOFormat="+startTimeISOFormat);

    final Instant startTimeInstant= Instant.parse(startTimeISOFormat); // ("2014-01-01T00:00:00Z");

    final long startTimeSeconds= startTimeInstant.toEpochMilli() / ITimeMachine.SEC_TO_MILLISEC;

    if (argsMapKeySet.contains("--outputDirectory")) {

      this.outputDirectory= argsMap.get("--outputDirectory");

      slog.info(mmi+"Will use this.outputDirectory="+
                this.outputDirectory+" to write prediction results");
    }

    long predDurationInDays= IWLStationPred.DEFAULT_DAYS_DURATION_IN_FUTURE;

    if (argsMapKeySet.contains("--predDurationInDays")) {
      predDurationInDays= Long.parseLong(argsMap.get("--predDurationInDays"));
    }

    if (predDurationInDays > IWLStationPred.MAX_DAYS_DURATION) {

      throw new RuntimeException(mmi+"predDurationInDays -> "+predDurationInDays+
                                 " is larger than IWLStationPred.MAX_DAYS_DURATION_IN_FUTURE -> "+
                                 IWLStationPred.MAX_DAYS_DURATION);
    }

    final long endTimeSeconds= startTimeSeconds +
      predDurationInDays * (long)ITimeMachine.SECONDS_PER_DAY; // + 40L*24L*3600L;

    long timeIncrInSeconds= IWLStationPred.DEFAULT_TIME_INCR_SECONDS;

    if (argsMapKeySet.contains("--timeIncrInSeconds")) {
      timeIncrInSeconds= Long.parseLong(argsMap.get("--timeIncrInSeconds"));
    }

    if (timeIncrInSeconds > IWLStationPred.MAX_TIME_INCR_SECONDS) {
      throw new RuntimeException(mmi+"timeIncrInSeconds ->"+timeIncrInSeconds+
                                 "is larger than IWLStationPred.MAX_TIME_INCR_SECONDS -> "+
                                 IWLStationPred.MAX_TIME_INCR_SECONDS);
    }

    if (timeIncrInSeconds < IWLStationPred.MIN_TIME_INCR_SECONDS) {
      throw new RuntimeException(mmi+"timeIncrInSeconds ->"+timeIncrInSeconds+
                                 "is smaller than IWLStationPred.MIN_TIME_INCR_SECONDS -> "+
                                 IWLStationPred.MIN_TIME_INCR_SECONDS);
    }

    slog.info(mmi+"startTimeInstant="+startTimeInstant.toString());
    slog.info(mmi+"startTimeSeconds="+startTimeSeconds);
    slog.info(mmi+"endTimeSeconds="+endTimeSeconds);
    slog.info(mmi+"timeIncrInSeconds="+timeIncrInSeconds);

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    if (!argsMapKeySet.contains("--stationPredType")) {
      throw new RuntimeException(mmi+"Must have the mandatory prediction location info option: --stationPredType" );
    }

    final String stationPredType= argsMap.get("--stationPredType" );

    if (!stationPredType.equals(IWLStationPred.Type.TIDAL.name()+":"+ITidal.Method.NON_STATIONARY_FOREMAN.name())) {

      throw new RuntimeException(mmi+"Only "+IWLStationPred.Type.TIDAL.name()+":"+
                                 ITidal.Method.NON_STATIONARY_FOREMAN.name()+" prediction method allowed for now!!");
    }

    if (!argsMapKeySet.contains("--stationIdInfo")) {
      throw new RuntimeException(mmi+"Must have the mandatory prediction location info option: --stationIdInfo" );
    }

    final String stationIdInfo= argsMap.get("--stationIdInfo" );

    System.out.println(mmi+"stationPredType="+stationPredType);
    System.out.println(mmi+"stationIdInfo="+stationIdInfo);

    //WLStationPredFactory wlStnPrdFct= null;

    final String [] stationPredTypeSplit=
      stationPredType.split(IStageIO.LOCATION_ID_SPLIT_CHAR);

    final String mainPredType= stationPredTypeSplit[0];

    if ( mainPredType.equals(IWLStationPred.Type.TIDAL.name())) { //  ("TIDAL") ) {

      // --- Tidal pred. method is ITidal.Method.NON_STATIONARY_FOREMAN by default.
      ITidal.Method tidalMethod= ITidal.Method.NON_STATIONARY_FOREMAN;

      final String specTidalMethod= stationPredTypeSplit[1];

      if (specTidalMethod.equals(ITidal.Method.STATIONARY_FOREMAN.name())) {

        //tidalMethod= ITidal.Method.STATIONARY_FOREMAN;

        throw new RuntimeException(mmi+"The"+
                                   ITidal.Method.STATIONARY_FOREMAN.name()+
                                   " tidal prediction method is not allowed for now!!");
      }

      IStage.Type stageType= IStage.Type.DISCHARGE_CFG_STATIC;

      if (tidalMethod == ITidal.Method.NON_STATIONARY_FOREMAN) {

      if (!argsMapKeySet.contains("--stageType")) {

        throw new RuntimeException(mmi+
                                   "Must have the --stageType option defined if tidal method is"+
                                   ITidal.Method.NON_STATIONARY_FOREMAN.name()+ " !!");
      }

      final String stageTypeCheck= argsMap.get("--stageType");

      if (stageTypeCheck.equals(IStage.Type.DISCHARGE_FROM_MODEL.name())) {
        stageType= IStage.Type.DISCHARGE_FROM_MODEL;
      }

      if (stageType != IStage.Type.DISCHARGE_CFG_STATIC) {

        throw new RuntimeException(mmi+"Only "+
                                   IStage.Type.DISCHARGE_CFG_STATIC.name()+
                                   " stage type allowed for now !!");
        }
      }

      if (!argsMapKeySet.contains("--tidalConstsInputFileFormat")) {

        throw new RuntimeException(mmi+
                                   "Must have the --tidalConstsInputFileFormat option defined if mainPredType == TIDAL !!");
      }

      final String tidalConstsInputFileFormat= argsMap.get("--tidalConstsInputFileFormat");

      if (!tidalConstsInputFileFormat.
            equals(ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name())) {

        throw new RuntimeException(mmi+"Only the:"+
                                   ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name()+
                                   " tidal prediction input file format allowed for now!!");
      }

      final ITidalIO.WLConstituentsInputFileFormat
        tidalConstsInputFileFmt= ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON;

      // --- Specific configuration for a tidal prediction.
      super.configureTidalPred(stationIdInfo,
                               startTimeSeconds, //testStartTime, //unixTimeNow,
                               endTimeSeconds,//endPredTime,
                               timeIncrInSeconds,//180L,//180L, //900L, //3600L,//900L,
                               tidalMethod, //ITidal.Method.NON_STATIONARY_FOREMAN,
                               null, //nsTCInputFile,
                               tidalConstsInputFileFmt,//ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON,                                        >
                               stageType, // --- IStage.Type.DISCHARGE_CFG_STATIC: Stage data taken from inner config DB
                               null, // --- Stage input data file
                               null  // --- IStage.Type.DISCHARGE_CFG_STATIC IStageIO.FileFormat is JSON by default.
                              );

    }

    slog.info(mmi+"end");

    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);
  }

  /**
   * comments please!
   */
  final public List<MeasurementCustom> getPredictionData() {
    return super.getPredictionData(); //= super.getAllPredictions();
  }

  /**
   * comments please!
   */
  final public IWLStationPred getAllPredictions() {
    return super.getAllPredictions();
  }

  /**
   * comments please!
   */
  final public void writeIfNeeded(/*@NotNull*/ IWLStationPredIO.OutputFormats outputFormat) {

    final String mmi= "writeIfNeeded: ";

    slog.info(mmi+"start: this.outputDirectory="+this.outputDirectory);

    if (this.outputDirectory != null) {

      if (!IWLStationPredIO.allowedOutputFormats.contains(outputFormat.name())) {
        throw new RuntimeException(mmi+"Invalid output file format -> "+outputFormat.name());
      }

      //final String outputFile= this.outputDirectory + File.separator + ;

      slog.info(mmi+"Writing prediction results in "+this.outputDirectory+
                " using "+ outputFormat.name()+" output file format");

      if ( outputFormat.name().
              equals(IWLStationPredIO.OutputFormats.JSON.name()) ) {

         this.writeJSONOutputFile(this.stationId);
      }
    }

    slog.info(mmi+"end");

    slog.info(mmi+"debug System.exit(0)");
    System.exit(0);
  }

//  /**
//   * @param stationId:              String id. of the WL station.
//   * @param startTimeSeconds:       Starting time in seconds.
//   * @param enTimeSeconds:          Ending time in seconds.
//   * @param timeIncrSeconds:        Time increments between sucessive WLs (in seconds)
//   * @param latitudeDecimalDegrees: (null for stage-only and climatology methods) Need latitude of the target location for astronomic ephemerides computations.
//   * @param method:                 (null for stage-only and climatology methods) The Tidal prediction method name id.
//   * @param wlTContstfileFormat:    
//   */
////  public WLStationPred(/*@NotNull*/ final String stationId,
//                       final String stationTcInputFile,
//                       /*@NotNull*/ final long startTimeSeconds,
//                       /*@NotNull*/ final long endTimeSeconds,
//                       final long timeIncrSeconds,
//                       final double latitudeDecimalDegrees,
//                       final ITidalIO.WLConstituentsInputFileFormat wlTContstfileFormat,
//                       final ITidal.Method method,
//                       final HashMap<String, String> stageTimeVaryingData) {
//
//     //super(method, stationId, stationTcInputFile, wlTContstfileFormat,
//     //      startTimeSeconds, endTimeSeconds, timeIncrSeconds, latitudeDecimalDegrees); //, stageTimeVaryingData);
//    //super(Method.FOREMAN, stationId, inputfileFormat, latitudeDecimalDegrees, startTimeSeconds);
//  }

  ///**
  // * @param method            : Method object which specify which method to use for the tidal computations.
  // * @param inputFileFormat:  object of class ConstituentsInputFileFormat for the Z tidal constituents input file
  // *                          format.
  // * @param startTimeSeconds: Time stamp in seconds since UNIX epoch time 0 for the astronomic ephemerides
  // *                          computations.
  // * @param stationInfo:      IWLS Station object normally retreived from the DB with a SQL request.
  // */
  //public WLStationPred(final Method method,
  //                     final WLConstituentsInputFileFormat inputFileFormat,
  //                     final long startTimeSeconds, /*@NotNull*/ final Station stationInfo) {
  //
  //  //--- No more com.vividsolutions.jts.geom.Point object in IWLS Station class.
  //  //    But it could eventually make a comeback.
  //  super(method, stationInfo.getCode(), inputFileFormat, stationInfo.getLatitude(), startTimeSeconds);
  //
  //  //super(method, stationInfo.getCode(), inputFileFormat, stationInfo.getLocation().getCoordinates()[0].y,
  //  // startTimeSeconds);
  //}
  
  /**
   * @param method              : Method object which specify which method to use for the tidal computations.
   * @param inputFileFormat:    object of class ConstituentsInputFileFormat for the Z tidal constituents input file
   *                            format.
   * @param startTimeSeconds:   Time stamp in seconds since UNIX epoch for the beginning of the tidal preds. time
   *                            series wanted.
   * @param endTimeSeconds:     Time stamp in seconds since UNIX epoch for the end of the tidal preds. time series
   *                            wanted.
   * @param timeIncrSeconds:    Time increment in seconds between each tidal preds. time series items.
   * @param forecastingContext: IWLS ForecastingContext object normally retreived from the DB with a SQL request.
   * @return The ForecastingContext taken as last argument with its Prediction time series set to the newly computed
   * tidal prediction.
   */
//  //@NotNull
//  final public static ForecastingContext computeForecastingContextPredictions(final Method method,
//                                                                              final WLConstituentsInputFileFormat inputFileFormat,
//                                                                              final long startTimeSeconds,
//                                                                              final long endTimeSeconds,
//                                                                              final long timeIncrSeconds,
//                                                                              /*@NotNull*/ final ForecastingContext forecastingContext) {
//    try {
//      forecastingContext.getReferenceTime();
//
//    } catch (NullPointerException e) {
//
//      staticLogger.error("WLStationTidalPredictions computeForecastingContextPredictions: forecastingContext==null !!");
//      throw new RuntimeException("WLStationTidalPredictions computeForecastingContextPredictions");
//    }
//
//    staticLogger.debug("WLStationTidalPredictions computeForecastingContextPredictions: start: station id.= " + forecastingContext.getStationCode());
//
//    final WLStationTidalPredictionsFactory tmpFactory = new WLStationTidalPredictions(method, inputFileFormat,
//        startTimeSeconds, forecastingContext.getStationInfo());
//
//    return tmpFactory.computeForecastingContextPreds(startTimeSeconds, endTimeSeconds, timeIncrSeconds,
//        forecastingContext);
//  }

}
