//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created by Gilles Mercier on 2018-01-10.
 */

//---
//import java.time.Instant;
import java.util.HashMap;
//import java.util.List;
//import javax.validation.constraints.Min;

//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.station.Station;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;

//---
//---
//---
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;

/**
 * Class for the computation of water level predictions
 */
final public class WLStationPred extends WLStationPredFactory {

  private final static String whoAmI="ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred";

  /**
   * Usual class instance log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Default constuctor:
   */
  public WLStationPred() {
    super();
  }
  
  /**
   * @param stationId:              String id. of the WL station.
   * @param startTimeSeconds:       Starting time in seconds.
   * @param enTimeSeconds:          Ending time in seconds.
   * @param timeIncrSeconds:        Time increments between sucessive WLs (in seconds)
   * @param latitudeDecimalDegrees: (null for stage-only and climatology methods) Need latitude of the target location for astronomic ephemerides computations.
   * @param method:                 (null for stage-only and climatology methods) The Tidal prediction method name id.
   * @param wlTContstfileFormat:    
   */
  public WLStationPred(/*@NotNull*/ final String stationId,
                       final String stationTcInputFile,
                       /*@NotNull*/ final long startTimeSeconds,
                       /*@NotNull*/ final long endTimeSeconds,
                       final long timeIncrSeconds,
                       final double latitudeDecimalDegrees,
                       final ITidalIO.WLConstituentsInputFileFormat wlTContstfileFormat,
                       final ITidal.Method method,
                       final HashMap<String, String> stageTimeVaryingData) {

     super(method, stationId, stationTcInputFile, wlTContstfileFormat,
           startTimeSeconds, endTimeSeconds, timeIncrSeconds, latitudeDecimalDegrees); //, stageTimeVaryingData);
    //super(Method.FOREMAN, stationId, inputfileFormat, latitudeDecimalDegrees, startTimeSeconds);
  }
  
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
