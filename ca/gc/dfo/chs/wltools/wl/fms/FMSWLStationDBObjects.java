//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.wl.fms.FMSInput;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.wl.WLMeasurementFinder;

//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.fmservice.modeling.geo.GlobalRefPoint;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.IWL;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLMeasurementFinder;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.util.ArrayList;
//import java.util.List;

//---
//import com.vividsolutions.jts.geom.Point;
//---
//---
//import ca.gc.dfo.iwls.fmservice.modeling.geo.IGeo;

/**
 * Class for a specific WL station database WLType objects(PREDICTION, OBSERVATION, FORECAST, EXT_STORM_SURGE)
 * references.
 */
//@Slf4j
abstract public class FMSWLStationDBObjects extends GlobalRefPoint implements IFMS, IWL {

  final static private whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.FMSWLStationDBObjects";

  /**
   * static log utility.
   */
  private final static Logger sLog= LoggerFactory.getLogger(whoAmI);

  /**
   * To store the newly updated full (i.e. with storm surge and-or river discharge effects)
   * WL forecast data in the future for the processed CHS TG.
   */
  final List<MeasurementCustom> updatedForecastData= new ArrayList<MeasurementCustom>();

  /**
   * One WLMeasurementFinder object for each PREDICTION, OBSERVATION, FORECAST, EXT_STORM_SURGE WLType.
   */
  private final List<WLMeasurementFinder>
    wlMeasurementFinderList= new ArrayList<WLMeasurementFinder>(WLType.values().length);

  /**
   * To keep track of the last valid WLO time-stamp(seconds since the epcoh)available.
   */
  long lastWLOSse= 0L;

  /**
   * boolean flag to signal that we have some WL storm surge to use for the forecast at this WL station.
   */
  boolean useSsf= false;

  /**
   * The time-increment(in seconds) between successive WL(O,P,F,SSF) data.
   */
  long secondsIncr= FORECASTS_TIME_INCR_SECONDS_MAX;

  /**
   * WL storm surge type to use.
   */
  private StormSurgeWLType ssfType= StormSurgeWLType.WLSSF_FULL;

  /**
   * The String id. of the stationIt MUST be the String returned by the FMSConfig.getStationId() method.
   */
  private String stationId= null;

  /**
   * @param forecastingContext   : A ForecastingContext object coming from a SQL request on the operational DB.
   * @param globalVerticalDatum  : A GlobalVerticalDatum object.
   * @param globalVerticalOffset : The global vertical Z elevation offset referred to the GlobalVerticalDatum object.
   *                             It is zero if WL location is directly referred to the GlobalVerticalDatum.
   */
  //public FMSWLStationDBObjects(/*@NotNull*/ final ForecastingContext forecastingContext,
  public FMSWLStationDBObjects(/*@NotNull*/ final FMSInput fmsInput,
                               /*@NotNull*/ final GlobalVerticalDatum globalVerticalDatum, final double globalVerticalOffset) {

    //--- Re-activate the following super invocation if the ca.gc.dfo.iwls.station class re-activate its own com
    // .vividsolutions.jts.geom.Point as an attribute.
    //super(fc.getStationInfo().getLocation(), globalVerticalDatum, globalVerticalOffset);

    //--- super invocation dealing with the absence of the com.vividsolutions.jts.geom.Point as an attribute to the
    // ca.gc.dfo.iwls.Station class
    //super(forecastingContext.getStationInfo().getLongitude(), forecastingContext.getStationInfo().getLatitude(), 0.0,

    super(fmsInput.getStationHBCoords().getLongitude(),
          fmsInput.getStationHBCoords().getLatitude(),
          0.0, globalVerticalDatum, globalVerticalOffset);

    this.stationId= fmsInput.getStationId();

    //--- Set the ForecastingContext.newGeneratedForecasts object to point to the WL station udpatedForecastData
    // object to be able to udpate the DB with the new WLF data.
    //forecastingContext.setNewGeneratedForecasts(this.udpatedWLFData);

    //--- Predictions:
    final List<MeasurementCustom> dbPrdList= fmsInput.getPredictions();

    slog.info(mmi+"Got " + dbPrdList.size() + " predictions data.");

    final long frstDt= dbPrdList.get(0).getEventDate().getEpochSecond();

    slog.info(mmi+"Predictions 1st time-stamp Instant Object:" + dbPrdList.get(0).getEventDate().toString());
    slog.info(mmi+"Predictions 1st time-stamp SecondsSinceEpoch UTC: " + SecondsSinceEpoch.dtFmtString(frstDt, true));

    //--- Set this.measurementsFinderList references:

    //--- Populate this.wlMeasurementFinderList for PREDICTION type:
    this.wlMeasurementFinderList.add(PREDICTION, new WLMeasurementFinder(dbPrdList));

    final long lastDt = dbPrdList.get(dbPrdList.size() - 1).getEventDate().getEpochSecond();

    slog.info(mmi+"Predictions last time-stamp: " + SecondsSinceEpoch.dtFmtString(lastDt, true));

    //--- WL OBSERVATION type
    final List<MeasurementCustom> dbObsList= fmsInput.getObservations();

    if ((dbObsList != null) && (dbObsList.size() > 0)) {

      slog.info(mmi+"Got "+dbObsList.size()+
                " WL observations from the DB for station: " + this.stationId);

      this.wlMeasurementFinderList.add(OBSERVATION, new WLMeasurementFinder(dbObsList));

      this.lastWLOSse= this.wlMeasurementFinderList.get(OBSERVATION).getLastSse();

    } else {

      this.wlMeasurementFinderList.add(OBSERVATION, null);

      slog.info(mmi+"No observations retreived(null or size()==0) from the DB for station:"+
                this.stationID + ", setting this last.ObsSse at the FMSInput reference time.");

      this.lastWLOSse = fmsInput.getReferenceTime().getEpochSecond();
    }

    slog.info(mmi+"this.lastWLOSse time-stamp="+
              SecondsSinceEpoch.dtFmtString(this.lastWLOSse, true));

    final int wlpSize = this.wlMeasurementFinderList.get(PREDICTION).size();

    final List<MeasurementCustom> dbFcsList = fmsInput.getForecasts();

    if ((dbFcsList != null) && (dbFcsList.size() > 0)) {

      slog.info(mmi+"Got " + dbFcsList.size() + " WL forecasts data for station: " + this.stationId);

      final int wlfSize= dbFcsList.size();

      if (wlfSize != wlpSize) {

        slog.warn(mmi+"wlfSize=" + wlfSize + ", wlpSize=" + wlpSize);
        slog.warn(mmi+"wlfSize != wlpSize for station: " + this.stationId);
        slog.warn(mmi+"WLP and WLF are not synchronized ! WLF will be replaced by WLP !");

        this.wlMeasurementFinderList.add(FORECAST, this.wlMeasurementFinderList.get(PREDICTION));

        throw new RuntimeException(mmi+"Debug exit here !!");

      } else {

        slog.debug(mmi+"Got the same number of WLP and WLF data, WLF data will be used");

        this.wlMeasurementFinderList.add(FORECAST, new WLMeasurementFinder(dbFcsList));

        throw new RuntimeException(mmi+"Debug exit here !!");
      }

    } else {

      slog.warn(mmi+"No WL forecasts for station:"+
                this.stationId + ", replacing it with its prediction-climatology");

      this.wlMeasurementFinderList.add(FORECAST, this.wlMeasurementFinderList.get(PREDICTION));

      throw new RuntimeException(mmi+"Debug exit here !!");
    }

    //TODO : un-comment the following if-else block when the EXT_STORM_SURGE will be available in the DB
//        final List<Measurement> dbSsfList= fc.getStormSurge();
//        if ((dbSsfList != null) && (dbSsfList.size() > 0) ) {
//
//            this.log.debug("Got "+checkSsf.size()+" WL storm surges from the DB for station:"+this.stationCode);
//            this.WLMeasurementFinderList.add(EXT_STORM_SURGE, new WLMeasurementFinder(dbSsfList));
//        } else {
    
    this.log.warn("FMSWLStationDBObjects constructor: No WL storm surges retreived from the DB for station:" +
        this.stationCode + ", replacing it with WLP");
    
    this.wlMeasurementFinderList.add(EXT_STORM_SURGE, this.wlMeasurementFinderList.get(PREDICTION));
//        }
    
    //--- this.wlMeasurementFinderList.get(PREDICTION).getSecondsIncrement() returns an absolute value:
    this.secondsIncr = this.wlMeasurementFinderList.get(PREDICTION).getSecondsIncrement();
    
    if (this.secondsIncr == 0L) {
      
      this.log.error("FMSWLStationDBObjects constructor: this.secondsIncr <= 0L for station: " + this.stationCode);
      throw new RuntimeException("FMSWLStationDBObjects constructor");
    }
    
    if (this.secondsIncr < FORECASTS_TIME_INCR_SECONDS_MIN) {
      
      this.log.error("FMSWLStationDBObjects constructor: this.secondsIncr=" + this.secondsIncr + " < " +
          "FORECASTS_TIME_INCR_SECONDS_MIN=" +
          FORECASTS_TIME_INCR_SECONDS_MIN + " for station: " + this.stationCode);
      
      throw new RuntimeException("FMSWLStationDBObjects constructor");
    }
    
    if (this.secondsIncr > FORECASTS_TIME_INCR_SECONDS_MAX) {
      
      this.log.error("FMSWLStationDBObjects constructor: this.secondsIncr=" + this.secondsIncr + " > " +
          "FORECASTS_TIME_INCR_SECONDS_MAX=" +
          FORECASTS_TIME_INCR_SECONDS_MAX + " for station: " + this.stationCode);
      
      throw new RuntimeException("FMSWLStationDBObjects constructor: Cannot update forecast !");
    }
    
    boolean validTi = false;
    
    //--- Validate the time increment returned for the time increment allowed.
    for (final int check : FORECASTS_TIME_INCR_MINUTES_ALLOWED) {
      
      if (this.secondsIncr == (long) SECONDS_PER_MINUTE * check) {
        validTi = true;
        break;
      }
    }
    
    if (!validTi) {
      
      this.log.error("FMSWLStationDBObjects constructor: Invalid time increment in seconds=" + this.secondsIncr + " " +
          "for station: " + this.stationCode);
      throw new RuntimeException("FMSWLStationDBObjects constructor");
    }
  
    this.log.debug("FMSWLStationDBObjects constructor: Will use " + this.secondsIncr + " seconds " +
        "as time increment.");
  
    //this.log.debug("No WLSSF for now then set this.wlssfDb to the predictions to use it as a
    // fake storm-surge
    // forecast.");
    
    //--- TODO: un-comment the following line when we will be ready to use forecasts merge.
    //final String mergeWith= fc.getFmsParameters().getForecast().getMerge();
    final String mergeWith = null;
    
    if (mergeWith != null) {
  
      this.log.debug("FMSWLStationDBObjects constructor: Using " + mergeWith + " as forecasts " +
          "merge target for " +
          "station:" + this.stationCode);
      this.useSsf = true;

//            switch (mergeWith) {
//
//                case WLSSF_FULL.toString() :
//
//            }
    
    }
    
    if (this.useSsf) {
      this.log.debug("FMSWLStationDBObjects constructor: WLSSF data is: " + mergeWith + " and " +
          "merge type will be: " + this.ssfType + " for station:" + this.stationCode);
    }

//        this.log.debug("this.wlMeasurementFinderList.get(PREDICTION)="+this.wlMeasurementFinderList.get(PREDICTION));
//        this.log.debug("this.wlMeasurementFinderList.get(OBSERVATION)="+this.wlMeasurementFinderList.get
//        (OBSERVATION));
//        this.log.debug("this.wlMeasurementFinderList.get(FORECAST)="+this.wlMeasurementFinderList.get(FORECAST));
//        this.log.debug("this.wlMeasurementFinderList.get(EXT_STORM_SURGE)="+this.wlMeasurementFinderList.get
//        (EXT_STORM_SURGE));
  }
  
  // ----
  final List<MeasurementCustom> getUpdatedForecastData() {
    return this.updatedForecastData;
  }

  /**
   * @param measurementsList : A List of Measurement objects.
   * @param tauHours         : The number of hours to go back in time for WL predictions errors statistics.
   * @return true if the Measurement List have a larger time span than tauHours, false otherwise.
   */
  protected final static boolean validateDBObjects(@NotNull @Size(min = 1) final List<MeasurementCustom> measurementsList,
                                                   @Min(1) final int tauHours) {
    
    staticLog.debug("Start: wlList=" + measurementsList + ", wList.size()=" + measurementsList.size());
    
    //--- We must have at least tauHours between the 1st and the last WL DB data
    final long sseBeg = measurementsList.get(0).getEventDate().getEpochSecond();
    final long sseEnd = measurementsList.get(measurementsList.size() - 1).getEventDate().getEpochSecond();
    
    final int durationHours = ((int) (sseEnd - sseBeg)) / SECONDS_PER_HOUR;
    
    return (durationHours >= tauHours);
  }
  
  /**
   * @param type : The WLType to find.
   * @return The right WLType Measurement List.
   * WARNING: The verification on the existence of the returned Measurement List is deferred to the client method.
   */
  final List<MeasurementCustom> getMeasurementList(@NotNull final WLType type) {
    
    //--- this.getWLMeasurementFinder(type) could return null.
    final WLMeasurementFinder whichOne = this.getWLMeasurementFinder(type);
    
    return (whichOne != null) ? whichOne.getDbData() : null;
  }
  
  /**
   * @param type : The WLType to find.
   * @return The right WLType WLMeasurementFinder object(which could be possibly null)
   */
  private final WLMeasurementFinder getWLMeasurementFinder(@NotNull final WLType type) {
    
    return this.wlMeasurementFinderList.get(type.ordinal());
  }
  
  /**
   * @param type             : The WLType wanted.
   * @param timeStampSeconds : The time-stamp in seconds since the epoch wanted.
   * @return The Measurement object of WLType type at the time-stamp in seconds timeStampSeconds(if any).
   * WARNING : The verification on the existence of the returned Measurement is deferred to the client method.
   */
  final MeasurementCustom getMeasurementType(@NotNull final WLType type,
                                             final long timeStampSeconds) {
    
    final WLMeasurementFinder wlTypeFinder = this.getWLMeasurementFinder(type);
    
    //this.log.debug("type="+type+", this.wlMeasurementFinderList.get(intType)="+this.wlMeasurementFinderList.get
    // (intType));
    
    return (wlTypeFinder != null ? wlTypeFinder.find(timeStampSeconds) : null);
  }
  
  /**
   * @return this.stationCode.
   */
  @NotNull
  public final String getStationCode() {
    return this.stationCode;
  }
  
  /**
   * @return The length of the PREDICTION type WL Measurement data.
   */
  final int predictionsSize() {
    return this.wlMeasurementFinderList.get(PREDICTION).size();
  }
}
