//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created on 2018-01-12.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-21, Gilles Mercier
 */

//---
import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
//import ca.gc.dfo.chs.wltools.util.ITrigonometry;
//import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.Stage;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
//import ca.gc.dfo.chs.wltools.nontidal.climatology.Climatology;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1D;
import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.Stationary1DTidalPredFactory;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.prediction.NonStationary1DTidalPredFactory;

//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITides;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITidesIO;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.TidalPredictions1DFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1D;
//import ca.gc.dfo.iwls.fmservice.modeling.util.ASCIIFileIO;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;

import java.lang.Math;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Min;
//---
//---
//import ca.gc.dfo.iwls.station.Station;
//---

/**
 * Class WLTidalPredictionsFactory acts as abstract base class for water levels tidal predictions:
 */
//abstract
public class WLStationPredFactory implements IWL, IWLStationPred { //, ITidal, ITidalIO, IStage, INonStationaryIO {

   private final static String whoAmI= "ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory";

  /**
   * Usual log utility.
   */

  private final static Logger slog = LoggerFactory.getLogger(whoAmI);

  /**
   * unfortunateUTCOffsetSeconds: The WL tidal constituents could(unfortunately) be defined for local time zones
   * so we have to apply a time zone offset to the results in order to have the
   * tidal predictions defined in UTC(a.k.a. ZULU).
   */
  protected long unfortunateUTCOffsetSeconds = 0L;

  /**
   *  Simple climatology (yearly average normally) prediction object.
   */
  //protected Climatology climatoPred= null;

  /**
   * Stage (river discharge and-or atmospheric) only prediction object.
   */
  protected Stage stagePred= null;

  /**
   * Classic astronomic-harmonic (a.k.a. stationary) tidal prediction object.
   * This implements the prediction-reconstruction part of M. Foreman's tidal
   * package theory and related code.
   */ 
  //protected Stationary1DTidalPredFactory stationaryTidalPred= null;
   protected Stationary1DTidalPredFactory tidalPred1D= null;

  /**
   * River-discharge and-or atmospheric influenced (a.k.a. non-stationary) tidal prediction object.
   * This implements the prediction-reconstruction part of the NS_TIDE theory
   *  (Matte et al., Journal of Atmospheric and Oceanic Technology, 2013)
   */
  //protected NonStationary1DTidalPredFactory nonStationaryTidalPred= null;

  /**
   * Keep the station ID to avoid WL tidal predictions data mix-up between two WL stations data:
   */
  private String stationId = null;

  protected long timeIncrSeconds= IWLStationPred.DEFAULT_TIME_INCR;

  /**
   * Default constructor.
   */
  public WLStationPredFactory() {
    this.unfortunateUTCOffsetSeconds = 0L;
  }
  
  /**
   * Specific WLStationPredFactory constructor for the tidal predictions (stationary or non-stationary).
   * @param method:                 The prediction method to use as defined in the Method object.
   * @param stationId               The WL station id. where we want to produce predictions.
   * @param tcInputFile             A tidal consts. input data file for the station.
   * @param tcInputFileFormat:      Tidal consts. input file format to use.
   * @param latitudeDecimalDegrees: The latitude(in decimal degrees) of the WL station.
   * @param startTimeSeconds:       The start time(in seconds since the epoch) for the astronomic arguments computations.
   * @param endTimeSeconds:         The end time(in seconds since the epoch) for the prediction data produced.
   * @param timeIncrSeconds:        The time increment to use between the successive WL prediction data produced (must be a multiple of 60 seconds,default 900)
   * @param latitudeDecimalDegrees: The latitude(in decimal degrees) of the WL station (null if the station lat is defined in the tcInputFile)
   */
  public WLStationPredFactory(/*@NotNull*/final String stationId,
                              /*@NotNull*/final long startTimeSeconds,
                              /*@NotNull*/final long endTimeSeconds,
                              final Long timeIncrSeconds,
                              final Double stationLatitudeDecimalDegrees,
                              final ITidal.Method method,
                              final String stationTcInputFile,
                              final ITidalIO.WLConstituentsInputFileFormat tcInputFileFormat,
                              final IStage.Type stageType,
                              final String stageInputDataFile,
                              final IStageIO.FileFormat stageInputDataFileFormat) {

   slog.info("constructor: start");

    // ---
    if (timeIncrSeconds != null) {
       this.timeIncrSeconds= timeIncrSeconds;
    }

    try {
      stationId.length();
      
    } catch (NullPointerException e) {
      
      slog.error("constructor: stationId==null !!");
      throw new RuntimeException(e);
    }

    //--- To avoid SNAFU mix-up between WL stations data:
    this.stationId = stationId;

    // --- 
    if ( method == ITidal.Method.STATIONARY_FOREMAN || method == ITidal.Method.NON_STATIONARY_FOREMAN) {

       if ( method == ITidal.Method.STATIONARY_FOREMAN ) {

          if ( tcInputFileFormat != ITidalIO.WLConstituentsInputFileFormat.STATIONARY_TCF) {

              slog.error("constructor: STATIONARY_FOREMAN prediction method -> Must have STATONARY_TCF for the tc input file format!");
              throw new RuntimeException("constructor");
          }

          this.tidalPred1D= new Stationary1DTidalPredFactory();
       }

       if ( method == ITidal.Method.NON_STATIONARY_FOREMAN) {

          if ( tcInputFileFormat != ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON) {

              slog.error("constructor: NON_STATIONARY_JSON prediction method -> Must have NON_STATONARY_JSON for the tc input file format!");
              throw new RuntimeException("constructor");
          }

          this.tidalPred1D= new NonStationary1DTidalPredFactory();
       }

       //--- Retreive WL station tidal constituents from a local disk file.
       //    It MUST be used before the following this.1DTidalPred.setAstroInfos
       //    method call
       this.getStationConstituentsData(stationId, stationTcInputFile, tcInputFileFormat);

       slog.info("constructor: done with this.getStationConstituentsData");
       //this.log.info("WLStationPredFactory constructor: Debug System.exit(0)");
       //System.exit(0);

       //--- MUST convert decimal degrees latitude to radians here:
       ///   MUST be used after this.getStationConstituentsData method call.
       this.tidalPred1D.setAstroInfos(method,
                                      Math.toRadians(stationLatitudeDecimalDegrees),
                                      startTimeSeconds,
                                      this.tidalPred1D.getTcNames());

       slog.info("constructor: end");
       //slog.info("constructor: Debug System.exit(0)");
       //System.exit(0);

    } // ---
  }
  
  /**
   * Extract tidal constituents data for a SINECO station from a local disk file.
   *
   * @param tcInputfilePath  : Path of the tidal constituents input file for a given station (or grid point). Could be null.
   * @param stationId        : WL Station String ID
   * @param inputFileFormat: WL Tidal constituents input file format(When the WL tidal constituents are not
   *                         retreived from the DB).
   * @return The current WLStationTidalPredictionsFactory object with its WL tidal constituents data ready to use.
   */
  //@NotNull
  final public WLStationPredFactory getStationConstituentsData(/*@NotNull*/ final String stationId,
                                                                            final String tcInputfilePath,
                                                               /*@NotNull*/ final ITidalIO.WLConstituentsInputFileFormat inputFileFormat ) {
    
    slog.info("getStationConstituentsData: start");
    
    try {
      stationId.length();
      
    } catch (NullPointerException e) {
      
      slog.error("getStationConstituentsData: stationId==null !!");
      throw new RuntimeException(e);
    }
    
    try {
      inputFileFormat.ordinal();
      
    } catch (NullPointerException e) {
      
      slog.error("getStationConstituentsData: inputFileFormat==null !!");
      throw new RuntimeException(e);
    }
    
    switch (inputFileFormat) {
      
      case STATIONARY_TCF:

        String tcfFilePath= ITidalIO.TCF_DATA_DIR + stationId + ITidalIO.TCF_DATA_FILE_EXT;

        if (tcInputfilePath != null) {
           tcfFilePath= tcInputfilePath;
        } //else {
          // final String tcfFilePath= ITidalIO.TCF_DATA_DIR + stationId + ITidalIO.TCF_DATA_FILE_EXT;
          //}

        slog.info("getStationConstituentsData: reading TCF format file: "+tcfFilePath);

        //--- Extract tidal constituents data from a classic legacy DFO TCF ASCII file.
        //    We use chained method call to be able to set this.unfortunateUTCOffsetSeconds (if any)
        this.unfortunateUTCOffsetSeconds= this.tidalPred1D
                .getTCFFileData(tcfFilePath).getUnfortunateUTCOffsetSeconds();

        slog.debug("getStationConstituentsData: Done with reading TCF format file: "+tcfFilePath);
        slog.debug("getStationConstituentsData: Debug System.exit(0)");
        System.exit(0);
        break;

      case NON_STATIONARY_JSON:

        if (tcInputfilePath == null) {
           slog.error("getStationConstituentsData: NON_STATIONARY_JSON input file format: tcInputfilePath cannot be null !!");
           throw new RuntimeException("WLStationPredFactory getStationConstituentsData");
        }

        slog.debug("getStationConstituentsData: reading NON_STATIONARY_JSON tc input file: "+tcInputfilePath);

        this.tidalPred1D.getNSJSONFileData(tcInputfilePath);

        slog.info("NON_STATIONARY_JSON format: Done with reading tcInputfilePath");
        //this.log.info("Debug System.exit(0)");
        //System.exit(0);
        break;

      default:

        //this.log.error("Invalid file format ->" + inputFileFormat);
        throw new RuntimeException("Invalid file format ->" + inputFileFormat);
        //break;
    } // ---- end switch block
    
    slog.info("getStationConstituentsData: end");
    
    return this;
  }

  /**
   * @param timeStampSeconds: The time-stamp in seconds since the epoch where we want a WL tidal prediction.
   * @return A new Measurement object which contains the WL tidal prediction wanted.
   */
  //@NotNull
  public final MeasurementCustom getMeasurementPredictionAt(/*@Min(0L)*/ final long timeStampSeconds) {

    //--- TODO: Add another getMeasurementPredictionAt method like:
    //          Measurement getMeasurementPredictionAt(@Min(0L) final long timeStampSeconds,@NotNull final
    //          Measurement measurement)
    //          to deal an already existing Measurement object that we want to set its value with the tidal
    //          prediction wanted.

    final MeasurementCustom ret = new MeasurementCustom();

    ret.setValue(this.tidalPred1D.computeTidalPrediction(timeStampSeconds));

    //--- Need to get the time with the possible unfortunate UTC offset of the predictions:
    ret.setEventDate(Instant.ofEpochSecond(timeStampSeconds + this.unfortunateUTCOffsetSeconds));

    //--- The Measurement() constructor does not instantiate its uncertainty attribute as of
    //    2018-03-21 so we need to create-set it here to the default to be sure that this new
    //    Measurement object have something reasonable to use later.
    if (ret.getUncertainty() == null) {

      slog.warn("getMeasurementPredictionAt: ret.getUncertainty()==null ! need to set it to default: " + PREDICTIONS_ERROR_ESTIMATE_METERS);

      ret.setUncertainty(PREDICTIONS_ERROR_ESTIMATE_METERS);
    }

    return ret;
  }

  ///**
  // * @param startTimeSeconds:   The start time(in seconds since the epoch) for the tidal predictions wanted.
  // * @param endTimeSeconds:     The end time(in seconds since the epoch) for the tidal predictions wanted.
  // * @param timeIncrSeconds:    The time increment between the successive tidal predictions data.
  // * @param forecastingContext: The ForecastingContext object of the station where we want set the new WL tidal
  // *                            predictions.
  // * @return The ForecastingContext object taken as argument with its Prediction attribute set to the new WL tidal
  // * predictions wanted.
  // */
  //@NotNull
  //final public ForecastingContext computeForecastingContextPreds(final long startTimeSeconds, final long endTimeSeconds,
  //                                                               @Min(1L) final long timeIncrSeconds,
  //                                                               @NotNull final ForecastingContext forecastingContext) {
  //  
  //  if (timeIncrSeconds <= 0L) {
  //    
  //    this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds: timeIncrSeconds<=0 !");
  //    throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
  //  }
  //
  //  try {
  //    forecastingContext.toString();
  //
  //  } catch (NullPointerException e) {
  //
  //    this.log.error("WLStationPredFactory computeForecastingContextPreds: forecastingContext==null !!");
  //    throw new RuntimeException("WLStationPredFactory computeForecastingContextPreds");
  //  }
  //
  //  //--- Avoid awkward WL tidal predictions data mix-up between two WL stations data:
  //  final String checkStationId = forecastingContext.getStationCode();
  //
  //  try {
  //    checkStationId.length();
  //    
  //  } catch (NullPointerException e) {
 // 
 //     this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds: checkStationId==null !!");
 //     throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
  //  }
  //
  //  if (!checkStationId.equals(this.stationId)) {
  //  
  //    this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds: invalid WL station " +
  //        "ForecastingContext -> " + checkStationId + " Should be -> " + this.stationId);
  //    throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
  //  }
    
  //final long tDiff = endTimeSeconds - startTimeSeconds;
  //
  //  if (tDiff == 0L) {
  //    this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds:  " +
  //        "(endTimeSeconds-startTimeSeconds)==0 !");
  //    throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
 //   }
    
  //  //--- Check for time ordering:
  //  final ArrowOfTime arrowOfTime = ((endTimeSeconds - startTimeSeconds) < 0L ? ArrowOfTime.BACKWARD :
  //      ArrowOfTime.FORWARD);
  //
  //  final List<MeasurementCustom> predictions = forecastingContext.getPredictions();
  //
  //  try {
  //    predictions.size();
  //    
  //  } catch (NullPointerException e) {
  //    this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds: predictions==null !!");
  //    throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
  //  }
  //
  //  if (predictions.size() != 0) {
  //    this.log.warn("WLStationTidalPredictionsFactory computeForecastingContextPreds:  predictions.size()!=0 , Need " +
  //        "to get rid of its oontents here !");
  //    predictions.clear();
  //  }
  //
  //  this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: start: station id.= " + forecastingContext.getStationCode());
  //  this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: this" +
  //      ".unfortunateUTCOffsetSeconds=" + this.unfortunateUTCOffsetSeconds + " for this station");
  //  this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: startTimeSeconds dt=" + SecondsSinceEpoch.dtFmtString(startTimeSeconds, true));
  //  this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: endTimeSeconds dt=" + SecondsSinceEpoch.dtFmtString(endTimeSeconds, true));
  //
  //  if (arrowOfTime == ArrowOfTime.FORWARD) {
  //  
  //    this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: computing FORWARD in time " +
  //        "tidal predictions for station -> " +
  //        forecastingContext.getStationCode() + " ...");
  //    
  //    for (long ti = startTimeSeconds; ti <= endTimeSeconds; ti += timeIncrSeconds) {
  //      
  //      predictions.add(this.getMeasurementPredictionAt(ti));
  //    }
 //     
  //  } else {
  //    
  //    this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: computing BACKWARD in time " +
  //        "tidal predictions for station -> " +
  //        forecastingContext.getStationCode() + " ...");
  //    
  //    for (long ti = startTimeSeconds; ti >= endTimeSeconds; ti -= timeIncrSeconds) {
  //      
  //      predictions.add(this.getMeasurementPredictionAt(ti));
  //    }
  //  }
    
  //this.log.debug("computeForecastingContextPreds: end");
  //
  //  return forecastingContext;
  //}

}
