//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created on 2018-01-12.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-21, Gilles Mercier
 */

import java.io.File;
import java.lang.Math;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

//---
import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
//import ca.gc.dfo.chs.wltools.util.ITrigonometry;
//import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
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
abstract public class WLStationPredFactory extends WLStationPredIO implements IWLStationPred { //, ITidal, ITidalIO, IStage, INonStationaryIO {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory: ";//"ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory";

  /**
   * Usual log utility.
   */

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

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

  // ---  TODO: change stationId to locationId since we can also produce tidal
  //            predicitions at locations that are not TG per se.
  /**
   *  comments please!
   */
  protected String stationId= null;

  protected long startTimeSeconds= IWLStationPred.TIME_NOT_DEFINED;
  protected long endTimeSeconds= IWLStationPred.TIME_NOT_DEFINED;

  protected long timeIncrSeconds= IWLStationPred.DEFAULT_TIME_INCR_SECONDS;

  protected Double latitudeInDecDegrees= null;

  protected boolean predictionReady= false;

  //protected List<MeasurementCustom> predictionData= null;
  //protected String outputDirectory= false;

  /**
   * Default constructor.
   */
  public WLStationPredFactory() {
    this.predictionReady= false;
    this.predictionData= null;
    //this.unfortunateUTCOffsetSeconds = 0L;
  }

  /**
   * comments please!
   */
  public List<MeasurementCustom> getPredictionData() {
    return this.predictionData; //= super.getAllPredictions();
  }


   // --- TOOO: Update the javadoc comments.
  /**
   * Specific WLStationPredFactory constructor for the tidal predictions (stationary or non-stationary).
   * @param method:                 The prediction method to use as defined in the Method object.
   * @param stationId               The WL station id. where we want to produce predictions.
   * @param tcInputFile             A tidal consts. input data file for the station.
   * @param tcInputFileFormat:      Tidal consts. input file format to use.
   * @param startTimeSeconds:       The start time(in seconds since the epoch) for the astronomic arguments computations.
   * @param endTimeSeconds:         The end time(in seconds since the epoch) for the prediction data produced.
   * @param timeIncrSeconds:        The time increment to use between the successive WL prediction data produced (must be a multiple of 60 seconds,default 900)
   * @param latitudeDecimalDegrees: The latitude(in decimal degrees) of the WL station (null if the station lat is defined in the tcInputFile)
   */
  //public WLStationPredFactory(/*@NotNull*/final String stationId,
  protected WLStationPredFactory configureTidalPred(/*@NotNull*/final String stationId,
                                                    /*@NotNull*/final Long startTimeSeconds,
                                                    /*@NotNull*/final Long endTimeSeconds,
                                                    final Long timeIncrSeconds,
                                                    final ITidal.Method method,
                                                    final String stationTcInputFile,
                                                    final ITidalIO.WLConstituentsInputFileFormat tcInputFileFormat,
                                                    final IStage.Type stageType,
                                                    final String stageInputDataFile,
                                                    final IStageIO.FileFormat stageInputDataFileFormat) {

    final String mmi="configure: ";

    slog.info(mmi+"start, stationId="+stationId);

    this.stationId= stationId;

    String stationTcInputFileLocal= stationTcInputFile; // Could be null at this point.

    // --- Validate the start and end times
    if (endTimeSeconds <= startTimeSeconds) {
      throw new RuntimeException(mmi+"Invalid startTimeSeconds="+startTimeSeconds.toString()+
                                " and-or endTimeSeconds="+endTimeSeconds.toString()+"time stamps !");
    }

    this.startTimeSeconds= startTimeSeconds;
    this.endTimeSeconds= endTimeSeconds;

    // ---
    if (timeIncrSeconds != null) {

      if ( timeIncrSeconds < IWLStationPred.MIN_TIME_INCR_SECONDS ) {
        throw new RuntimeException(mmi+"Invalid (too small) timeIncrSeconds="+timeIncrSeconds.toString());
      }

      if( timeIncrSeconds > IWLStationPred.MAX_TIME_INCR_SECONDS ) {
        throw new RuntimeException(mmi+"Invalid (too large) timeIncrSeconds="+timeIncrSeconds.toString());
      }

      this.timeIncrSeconds= timeIncrSeconds;
    }

    try {
      stationId.length();
    } catch (NullPointerException e) {

      slog.error(mmi+"stationId==null !!");
      throw new RuntimeException(e);
    }

    //--- To avoid SNAFU mix-up between WL stations data:
    this.stationId = stationId;

    // ---
    if ( method == ITidal.Method.STATIONARY_FOREMAN || method == ITidal.Method.NON_STATIONARY_FOREMAN) {

       if ( method == ITidal.Method.STATIONARY_FOREMAN ) {

          if ( tcInputFileFormat != ITidalIO.WLConstituentsInputFileFormat.STATIONARY_TCF) {

              slog.error(mmi+"STATIONARY_FOREMAN prediction method -> Must have STATONARY_TCF for the tc input file format!");
              throw new RuntimeException(mmi);
          }

          this.tidalPred1D= new Stationary1DTidalPredFactory();
       }

       if ( method == ITidal.Method.NON_STATIONARY_FOREMAN) {

          if ( tcInputFileFormat != ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON) {

              slog.error(mmi+"NON_STATIONARY_JSON prediction method -> Must have NON_STATONARY_JSON for the tc input file format!");
              throw new RuntimeException(mmi);
          }

          if (stationTcInputFileLocal == null) {

            final String [] stationIdSplit=
              this.stationId.split(IStageIO.LOCATION_ID_SPLIT_CHAR);

            if (stationIdSplit.length != 3) {
              throw new RuntimeException(mmi+"ERROR: stationIdSplit.length != 3 !!");
            }

            final String regionIdInfo= stationIdSplit[0];
            final String subRegionIdInfo= stationIdSplit[1];
            final String stationIdSpec= stationIdSplit[2];

            // --- Build the path of the non-stationaty tidel consts. file inside the
            //     inner cfg DB.
            stationTcInputFileLocal= WLToolsIO.getMainCfgDir() + "/tidal/nonStationary/" + regionIdInfo +
              "/dischargeClusters/" + subRegionIdInfo + File.separator + INonStationaryIO.CLUSTER_TFHA_MAIN_SUBDIR_NAME + //"/dischargeClimatoTFHA/"+
              File.separator + stationIdSpec + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IStageIO.LOCATION_INFO_JSON_FNAME_EXT;
          }

          //slog.info(mmi+"stationTcInputFileLocal="+stationTcInputFileLocal);
          //slog.info(mmi+"Debug System.exit(0)");
          //System.exit(0);

          this.tidalPred1D= new
             NonStationary1DTidalPredFactory(this.stationId, stageType,
                                             this.startTimeSeconds, this.endTimeSeconds, this.timeIncrSeconds,
                                             stageInputDataFile, stageInputDataFileFormat);

          slog.info(mmi+"done with new NonStationary1DTidalPredFactory()");
          //slog.info(mmi+"debug System.exit(0)");
          //System.exit(0);
       }

       slog.info(mmi+"stationTcInputFileLocal="+stationTcInputFileLocal);

       //--- Retreive WL station tidal constituents from a local disk file.
       //    It MUST be used before the following this.1DTidalPred.setAstroInfos
       //    method call
       this.getStationConstituentsData(stationId, stationTcInputFileLocal, tcInputFileFormat);

       slog.info(mmi+"done with this.getStationConstituentsData");
       //this.log.info("WLStationPredFactory constructor: Debug System.exit(0)");
       //System.exit(0);

       //--- MUST convert decimal degrees latitude to radians here:
       ///   MUST be used after this.getStationConstituentsData method call.
       this.tidalPred1D.setAstroInfos(method,
                                      Math.toRadians(this.latitudeInDecDegrees),
                                      startTimeSeconds,
                                      this.tidalPred1D.getTcNames());
       //this.predictionReady= true;

    } // --- ITidal method prediction typw

    this.predictionReady= true;

    slog.info(mmi+"end");
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    return this;
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
    final String mmi="getStationConstituentsData: ";

    slog.info(mmi+"start");

    try {
      stationId.length();
    } catch (NullPointerException e) {

      slog.error(mmi+"stationId==null !!");
      throw new RuntimeException(e);
    }

    try {
      inputFileFormat.ordinal();

    } catch (NullPointerException e) {

      slog.error(mmi+"inputFileFormat==null !!");
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

        slog.info(mmi+"reading TCF format file: "+tcfFilePath);

        //--- Extract tidal constituents data from a classic legacy DFO TCF ASCII file
        //    getTCFFileData also returns the station latitude in decinaul degrees
        this.latitudeInDecDegrees=
          this.tidalPred1D.getTCFFileData(tcfFilePath);

        // --- Get the unfortunateUTCOffsetSeconds (if any)
        this.unfortunateUTCOffsetSeconds=
          this.tidalPred1D.getUnfortunateUTCOffsetSeconds();

        slog.debug(mmi+"Done with reading TCF format file: "+tcfFilePath);
        slog.debug(mmi+"Debug System.exit(0)");
        System.exit(0);
        break;

      case NON_STATIONARY_JSON:

        if (tcInputfilePath == null) {

          slog.error(mmi+"NON_STATIONARY_JSON input file format: tcInputfilePath cannot be null !!");
          throw new RuntimeException(mmi);
        }

        slog.debug(mmi+"Reading NON_STATIONARY_JSON tc input file: "+tcInputfilePath);

        this.latitudeInDecDegrees=
          this.tidalPred1D.getNSJSONFileData(tcInputfilePath);

        slog.info(mmi+"Done with reading tcInputfilePath");
        //this.log.info("Debug System.exit(0)");
        //System.exit(0);
        break;

      default:

        //this.log.error("Invalid file format ->" + inputFileFormat);
        throw new RuntimeException(mmi+"Invalid file format ->" + inputFileFormat);
        //break;
    } // ---- end switch block

    slog.info(mmi+"this.latitudeInDecDegrees="+this.latitudeInDecDegrees);

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    return this;
  }

  /**
    * comments please
    */
  public IWLStationPred getAllPredictions() {

    final String mmi= "getAllPredictions: ";

    if (this.tidalPred1D != null) {
      this.getAllTidalPredictions();

    } else {
       throw new RuntimeException(mmi+"Sorry! Only the tidal predictions type is available for now!");
    }

    return this;
  }

  /**
    * comments please
    */
  //public List<MeasurementCustom> getAllPredictions() { //getTidalPredictionsForStation() {
  public IWLStationPred getAllTidalPredictions() {

    final String mmi="getAllTidalPredictions: ";

    slog.info(mmi+"start, getting tidal predictions for station -> "+this.stationId);

    if (!predictionReady) {
      throw new RuntimeException(mmi+"not ready for predictions calculations!");
    }

    //ArrayList<MeasurementCustom> retList= new ArrayList<MeasurementCustom>();
    this.predictionData= new ArrayList<MeasurementCustom>();

    // --- Check nbTimeStamps value here >> Must be at least 1
    //     and this.endTimeSeconds - this.startTimeSeconds > this.timeIncrSeconds
    final int nbTimeStamps=
      (int)((this.endTimeSeconds - this.startTimeSeconds)/this.timeIncrSeconds);

    slog.info(mmi+"this.timeIncrSeconds="+this.timeIncrSeconds);

    for (long tsIter= 0; tsIter< nbTimeStamps; tsIter++) {

      final long timeStampSeconds=
        this.startTimeSeconds + tsIter*this.timeIncrSeconds;

      //slog.info(mmi+"timeStampSeconds="+timeStampSeconds);

      final double wlPrediction=
        this.tidalPred1D.computeTidalPrediction(timeStampSeconds);

      //slog.info(mmi+"wlPrediction="+wlPrediction+", timeStampSeconds="+timeStampSeconds);

      //final Instant instant= Instant.ofEpochSecond(timeStampSeconds);

      //slog.info(mmi+"instant.toString()="+instant.toString());
      //slog.info(mmi+"debug System.exit(0)");
      //System.exit(0);

      //final MeasurementCustom tmpMC= new
      //  MeasurementCustom( Instant.ofEpochSecond(timeStampSeconds), wlPrediction, 0.0 );

      //retList.
      this.predictionData.add(new MeasurementCustom(Instant.ofEpochSecond(timeStampSeconds), wlPrediction, 0.0));

      //if (tsIter<=47){
      //  slog.info(mmi+"wlPrediction="+wlPrediction+", timeStampSeconds="+timeStampSeconds);
      //  slog.info(mmi+"debug System.exit(0)");
      //  System.exit(0);
      //}
    }

    slog.info(mmi+"done with tidal predictions for station -> "+this.stationId);
    slog.info(mmi+"end");

     return this;
    //return retList;
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
