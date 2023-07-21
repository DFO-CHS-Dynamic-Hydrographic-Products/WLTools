//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created on 2018-01-12.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-21, Gilles Mercier
 */

//---
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.utilASCIIFileIO;
import ca.gc.dfo.chs.wltools.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.nontidal.stage.Stage;
//import ca.gc.dfo.chs.wltools.nontidal.climatology.Climatology;
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
abstract public class WLStationPredFactory
   implements IWL, ITidal, ITidalIO, IStage, INonStationaryIO {
  
  /**
   * Usual log utility.
   */

  private final Logger log = LoggerFactory.getLogger(this.getClass());

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
  protected Stationary1DTidalPredFactory stationaryTidalPred= null;

  /**
   * River-discharge and-or atmospheric influenced (a.k.a. non-stationary) tidal prediction object.
   * This implements the prediction-reconstruction part of the NS_TIDE theory
   *  (Matte et al., Journal of Atmospheric and Oceanic Technology, 2013)
   */
  protected NonStationary1DTidalPredFactory nonStationaryTidalPred= null;

  /**
   * Keep the station ID to avoid WL tidal predictions data mix-up between two WL stations data:
   */
  private String stationId = null;

  /**
   * Default constructor.
   */
  public WLStationPredFactory() {
    this.unfortunateUTCOffsetSeconds = 0L;
  }
  
  /**
   * @param method:                 The prediction method to use as defined in the Method object.
   * @param stationId               : The WL station id.
   * @param inputFileFormat:        WL Tidal constituents input file format(When the WL tidal constituents are not
   *                                retreived from the DB).
   * @param latitudeDecimalDegrees: The latitude(in decimal degrees) of the WL station.
   * @param startTimeSeconds:       The start time(in seconds since the epoch) for the astronomic arguments
   *                                computations.
   */
  public WLStationPredFactory(final Method method, final String stationId,
                              final WLConstituentsInputFileFormat inputFileFormat,
                              final double latitudeDecimalDegrees, final long startTimeSeconds) {
    try {
      stationId.length();
      
    } catch (NullPointerException e) {
      
      this.log.error("WLStationPredFactory constructor: stationId==null !!");
      throw new RuntimeException(e);
    }
    
    //--- To avoid SNAFU mix-up between WL stations data:
    this.stationId = stationId;
    
    //--- Retreive WL station tidal constituents from a local disk file.
    //    TODO: Write a WLStationTidalPredictionsFactory constructor that take the input file as argument.
    this.getStationConstituentsData(stationId, inputFileFormat);
    
    //--- MUST convert decimal degrees latitude to radians here:
    super.setAstroInfos(method,
                        DEGREES_2_RADIANS * latitudeDecimalDegrees,
                        startTimeSeconds,
                        this.tcDataMap.keySet());
  }
  
  /**
   * Extract tidal constituents data for a SINECO station from a local disk file.
   *
   * @param stationId        : WL Station String ID
   * @param inputFileFormat: WL Tidal constituents input file format(When the WL tidal constituents are not
   *                         retreived from the DB).
   * @return The current WLStationTidalPredictionsFactory object with its WL tidal constituents data ready to use.
   */
  @NotNull
  final public WLStationPredFactory getStationConstituentsData(/*@NotNull*/ final String stationId,
                                                               /*@NotNull*/ final ITidesIO.WLConstituentsInputFileFormat inputFileFormat) {
    
    this.log.debug("WLStationPredFactory getStationConstituentsData: : start");
    
    try {
      stationId.length();
      
    } catch (NullPointerException e) {
      
      this.log.error("WLStationTidalPredFactory getStationConstituentsData: stationId==null !!");
      throw new RuntimeException(e);
    }
    
    try {
      inputFileFormat.ordinal();
      
    } catch (NullPointerException e) {
      
      this.log.error("WLStationTidalPredFactory getStationConstituentsData: inputFileFormat==null !!");
      throw new RuntimeException(e);
    }
    
    switch (inputFileFormat) {
      
      case TCF:
        
        //--- Extract tidal constituents data from a classic legacy DFO TCF ASCII file.
        this.getTCFFileData(ITidesIO.TCF_DATA_DIR + stationId + ITidesIO.TCF_DATA_FILE_EXT);
        break;
      
      default:
        
        this.log.error("WLStationTidalPredFactory getStationConstituentsData: Invalid file format ->" + inputFileFormat);
        throw new RuntimeException("WLStationTidalPredFactory getStationConstituentsData");
        //break;
    }
    
    this.log.debug("WLStationTidalPredFactory getStationConstituentsData: : end");
    
    return this;
  }
  
  /**
   * Extract tidal constituents data from a classic legacy DFO TCF ASCII file.
   *
   * @param aTCFFilePath : Complete WL tidal constituents TCF format input file path on a local disk.
   * @return The current WLStationTidalPredictionsFactory object (this).
   */
  @NotNull
  final public WLStationTidalPredFactory getTCFFileData(@NotNull final String aTCFFilePath) {
    
    //--- Deal with possible null aTCFFilePath String:
    try {
      aTCFFilePath.length();
      
    } catch (NullPointerException e) {
      
      this.log.error("WLStationTidalPredFactory getTCFFileData: aTCFFilePath==null!!");
      throw new RuntimeException("WLStationTidalPredictionsFactory getTCFFileData");
    }
    
    this.log.debug("WLStationTidalPredictionsFactory getTCFFileData: Start, aTCFFilePath=" + aTCFFilePath);
    
    //--- Get the TCF format ASCII lines in a List of Strings:
    final List<String> tcfFileLines = ASCIIFileIO.getFileLinesAsArrayList(aTCFFilePath);
    
    //--- Assume no UTC time offset.
    this.unfortunateUTCOffsetSeconds = 0L;
    
    if (this.tcDataMap != null) {
      
      this.log.warn("WLStationTidalPredictionsFactory getTCFFileData: this.tcDataMap!=null, need to clear it first !");
      this.tcDataMap.clear();
      
    } else {
      
      this.log.debug("WLStationTidalPredictionsFactory getTCFFileData: Creating this.tcDataMap.");
      this.tcDataMap = new HashMap<String, Constituent1D>();
    }
    
    //--- Process the TCF format lines
    for (final String line : tcfFileLines) {
      
      this.log.debug("WLStationTidalPredictionsFactory getTCFFileData: processing line: " + line);
      
      //--- Split( blank spaces as delimiters) line in an array of Strings:
      final String[] lineSplit = line.trim().split("\\s+");
      
      final String last2chars = line.substring(line.length() - 2);
      
      //--- The comment String marker is at the end of the line for the TCF format.
      if (last2chars.equals(TCF_COMMENT_FLAG)) {
        
        final String firstString = lineSplit[0];
        
        if (firstString.equals(TCF_UTC_OFFSET_FLAG)) {
          
          final String offsetString = lineSplit[TCF_UTC_OFFSET_LINE_INDEX];
          
          final String offsetSignString = offsetString.substring(0, 1);
          
          final long offset = Long.parseLong(offsetString.substring(1));
          
          this.unfortunateUTCOffsetSeconds = SECONDS_PER_HOUR * (offsetSignString.equals("+") ? offset : -offset);
          
          //this.log.debug("WLStationTidalPredictionsFactory getTCFFileData: getTCFData:
          // unfortunateUTCOffset="+unfortunateUTCOffset);
          
        } else {
          
          this.log.debug("WLStationTidalPredictionsFactory getTCFFileData: Skipping TCF file header line: " + line);
        }
        
        continue;
      }
      
      if (lineSplit.length != TCF_LINE_NB_ITEMS) {
        
        this.log.error("WLStationTidalPredictionsFactory getTCFFileData: lineSplit.length=" + lineSplit.length + " !=" +
            " TCF_LINE_NB_ITEMS=" + TCF_LINE_NB_ITEMS);
        throw new RuntimeException("WLStationTidalPredictionsFactory getTCFFileData");
      }
      
      final String tcName = lineSplit[0];
      
      final double phaseLagDegrees = Double.valueOf(lineSplit[TCF_PHASE_LAG_LINE_INDEX]);

//            final double phaseLagDegrees= Double.valueOf(lineSplit[TCF_PHASE_LAG_LINE_INDEX]) +
//                    (!tcName.equals("Z0") ? this.unfortunateUTCOffsetSeconds/SECONDS_PER_HOUR : 0.0);
      
      //--- NOTE: The Greenwich phase lag must always be expressed in radians in a Constituent1D Object:
      final Constituent1D c1d = new Constituent1D(Double.valueOf(lineSplit[TCF_AMPLITUDE_LINE_INDEX]),
          DEGREES_2_RADIANS * phaseLagDegrees);
      
      this.log.debug("WLStationTidalPredictionsFactory getTCFFileData:  Setting this.tcDataMap with tidal " +
          "constitutent ->" +
          tcName + ", phaseLagDegrees=" + phaseLagDegrees);
      
      this.tcDataMap.put(tcName, c1d);
    }
    
    this.log.debug("WLStationTidalPredictionsFactory getTCFFileData: end,  unfortunateUTCOffset in hours=" + this.unfortunateUTCOffsetSeconds / SECONDS_PER_HOUR);
    
    return this;
  }
  
  /**
   * @param startTimeSeconds:   The start time(in seconds since the epoch) for the tidal predictions wanted.
   * @param endTimeSeconds:     The end time(in seconds since the epoch) for the tidal predictions wanted.
   * @param timeIncrSeconds:    The time increment between the successive tidal predictions data.
   * @param forecastingContext: The ForecastingContext object of the station where we want set the new WL tidal
   *                            predictions.
   * @return The ForecastingContext object taken as argument with its Prediction attribute set to the new WL tidal
   * predictions wanted.
   */
  @NotNull
  final public ForecastingContext computeForecastingContextPreds(final long startTimeSeconds, final long endTimeSeconds,
                                                                 @Min(1L) final long timeIncrSeconds,
                                                                 @NotNull final ForecastingContext forecastingContext) {
    
    if (timeIncrSeconds <= 0L) {
      
      this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds: timeIncrSeconds<=0 !");
      throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
    }
    
    try {
      forecastingContext.toString();
      
    } catch (NullPointerException e) {
      
      this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds: forecastingContext==null !!");
      throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
    }
    
    //--- Avoid awkward WL tidal predictions data mix-up between two WL stations data:
    final String checkStationId = forecastingContext.getStationCode();
    
    try {
      checkStationId.length();
      
    } catch (NullPointerException e) {
      
      this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds: checkStationId==null !!");
      throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
    }
    
    if (!checkStationId.equals(this.stationId)) {
      
      this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds: invalid WL station " +
          "ForecastingContext -> " + checkStationId + " Should be -> " + this.stationId);
      throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
    }
    
    final long tDiff = endTimeSeconds - startTimeSeconds;
    
    if (tDiff == 0L) {
      this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds:  " +
          "(endTimeSeconds-startTimeSeconds)==0 !");
      throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
    }
    
    //--- Check for time ordering:
    final ArrowOfTime arrowOfTime = ((endTimeSeconds - startTimeSeconds) < 0L ? ArrowOfTime.BACKWARD :
        ArrowOfTime.FORWARD);
  
    final List<MeasurementCustom> predictions = forecastingContext.getPredictions();
    
    try {
      predictions.size();
      
    } catch (NullPointerException e) {
      this.log.error("WLStationTidalPredictionsFactory computeForecastingContextPreds: predictions==null !!");
      throw new RuntimeException("WLStationTidalPredictionsFactory computeForecastingContextPreds");
    }
    
    if (predictions.size() != 0) {
      this.log.warn("WLStationTidalPredictionsFactory computeForecastingContextPreds:  predictions.size()!=0 , Need " +
          "to get rid of its oontents here !");
      predictions.clear();
    }
    
    this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: start: station id.= " + forecastingContext.getStationCode());
    this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: this" +
        ".unfortunateUTCOffsetSeconds=" + this.unfortunateUTCOffsetSeconds + " for this station");
    this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: startTimeSeconds dt=" + SecondsSinceEpoch.dtFmtString(startTimeSeconds, true));
    this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: endTimeSeconds dt=" + SecondsSinceEpoch.dtFmtString(endTimeSeconds, true));
    
    if (arrowOfTime == ArrowOfTime.FORWARD) {
      
      this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: computing FORWARD in time " +
          "tidal predictions for station -> " +
          forecastingContext.getStationCode() + " ...");
      
      for (long ti = startTimeSeconds; ti <= endTimeSeconds; ti += timeIncrSeconds) {
        
        predictions.add(this.getMeasurementPredictionAt(ti));
      }
      
    } else {
      
      this.log.debug("WLStationTidalPredictionsFactory computeForecastingContextPreds: computing BACKWARD in time " +
          "tidal predictions for station -> " +
          forecastingContext.getStationCode() + " ...");
      
      for (long ti = startTimeSeconds; ti >= endTimeSeconds; ti -= timeIncrSeconds) {
        
        predictions.add(this.getMeasurementPredictionAt(ti));
      }
    }
    
    this.log.debug("computeForecastingContextPreds: end");
    
    return forecastingContext;
  }
  
  /**
   * @param timeStampSeconds: The time-stamp in seconds since the epoch where we want a WL tidal prediction.
   * @return A new Measurement object which contains the WL tidal prediction wanted.
   */
  @NotNull
  public final MeasurementCustom getMeasurementPredictionAt(@Min(0L) final long timeStampSeconds) {
    
    //--- TODO: Add another getMeasurementPredictionAt method like:
    //          Measurement getMeasurementPredictionAt(@Min(0L) final long timeStampSeconds,@NotNull final
    //          Measurement measurement)
    //          to deal an already existing Measurement object that we want to set its value with the tidal
    //          prediction wanted.
  
    final MeasurementCustom ret = new MeasurementCustom();
    
    ret.setValue(this.computeTidalPrediction(timeStampSeconds));
    
    //--- Need to get the time with the possible unfortunate UTC offset of the predictions:
    ret.setEventDate(Instant.ofEpochSecond(timeStampSeconds + this.unfortunateUTCOffsetSeconds));
    
    //--- The Measurement() constructor does not instantiate its uncertainty attribute as of
    //    2018-03-21 so we need to create-set it here to the default to be sure that this new
    //    Measurement object have something reasonable to use later.
    if (ret.getUncertainty() == null) {
      
      this.log.warn("ret.getUncertainty()==null ! need to set it to default: " + PREDICTIONS_ERROR_ESTIMATE_METERS);
      
      ret.setUncertainty(PREDICTIONS_ERROR_ESTIMATE_METERS);
    }
    
    return ret;
  }
}
