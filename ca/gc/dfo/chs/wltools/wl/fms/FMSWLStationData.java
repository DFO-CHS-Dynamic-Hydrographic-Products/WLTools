//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.util.IGeo;
import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.wl.fms.FMSInput;
import ca.gc.dfo.chs.wltools.util.GlobalRefPoint;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.wl.WLMeasurementFinder;

//---
//import com.vividsolutions.jts.geom.Point;
//---
//---
//import ca.gc.dfo.iwls.fmservice.modeling.geo.IGeo;

/**
 * Class for a specific WL station database WLType objects(PREDICTION, OBSERVATION, MODEL_FORECAST, QC_FORECAST )
 * references.
 */
//@Slf4j
abstract public class FMSWLStationData extends GlobalRefPoint implements IFMS, IWL {

  final static private String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.FMSWLStationData";

  /**
   * static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * updatedForecastData:
   * To store the newly updated full (i.e. with storm surge and-or river discharge effects)
   * WL forecast data in the future for the processed CHS TG.
   */
  final List<MeasurementCustom>
    updatedForecastData= new ArrayList<MeasurementCustom>();

  /**
   * One WLMeasurementFinder object for each PREDICTION, OBSERVATION, MODEL_FORECAST, QC_FORECAST WLType.
   */
  private final List<WLMeasurementFinder>
    wlMeasurementFinderList= new ArrayList<WLMeasurementFinder>(WLType.values().length);

  /**
   * To keep track of the last valid WLO time-stamp(seconds since the epcoh)available.
   */
  protected long lastWLOSse= 0L;

  /**
   * boolean flag to signal that we have some WL full model forecast to use for this WL station.
   */
  //boolean useSsf= false;
  protected boolean useFullModelForecast= false;

  /**
   * The time-increment(in seconds) between successive WL(O,P,F,SSF) data.
   */
  protected long secondsIncr= FORECASTS_TIME_INCR_SECONDS_MAX;

  /**
   * WL storm surge type to use.
   */
  protected StormSurgeWLType ssfType= StormSurgeWLType.WLSSF_FULL;

  /**
   * The String id. of the stationIt MUST be the String returned by the FMSConfig.getStationId() method.
   */
  protected String stationId= null;

  /**
   * @param forecastingContext   : A ForecastingContext object coming from a SQL request on the operational DB.
   * @param globalVerticalDatum  : A GlobalVerticalDatum object.
   * @param globalVerticalOffset : The global vertical Z elevation offset referred to the GlobalVerticalDatum object.
   *                             It is zero if WL location is directly referred to the GlobalVerticalDatum.
   */
  //public FMSWLStationDBObjects(/*@NotNull*/ final ForecastingContext forecastingContext,
  public FMSWLStationData(/*@NotNull*/ final FMSInput fmsInput,
                          /*@NotNull*/ final IGeo.GlobalVerticalDatum globalVerticalDatum, final double globalVerticalOffset) {

    //--- Re-activate the following super invocation if the ca.gc.dfo.iwls.station class re-activate its own com
    // .vividsolutions.jts.geom.Point as an attribute.
    //super(fc.getStationInfo().getLocation(), globalVerticalDatum, globalVerticalOffset);

    //--- super invocation dealing with the absence of the com.vividsolutions.jts.geom.Point as an attribute to the
    // ca.gc.dfo.iwls.Station class
    //super(forecastingContext.getStationInfo().getLongitude(), forecastingContext.getStationInfo().getLatitude(), 0.0,

    super(fmsInput.getStationHBCoords().getLongitude(),
          fmsInput.getStationHBCoords().getLatitude(),
          0.0, globalVerticalDatum, globalVerticalOffset);

    final String mmi= "FMSWLStationData constructor: ";

    this.stationId= fmsInput.getStationId();

    //--- Set the ForecastingContext.newGeneratedForecasts object to point to the WL station udpatedForecastData
    // object to be able to udpate the DB with the new WLF data.
    //forecastingContext.setNewGeneratedForecasts(this.udpatedWLFData);

    //--- Predictions:
    final List<MeasurementCustom> prdDataList= fmsInput.getPredictions();

    slog.info(mmi+"Got " + prdDataList.size() + " predictions data.");

    final long frstDt= prdDataList.
      get(0).getEventDate().getEpochSecond();

    slog.info(mmi+"Predictions 1st time-stamp Instant Object:" + prdDataList.get(0).getEventDate().toString());
    slog.info(mmi+"Predictions 1st time-stamp SecondsSinceEpoch UTC: " + SecondsSinceEpoch.dtFmtString(frstDt, true));

    //--- Set this.measurementsFinderList references:

    //--- Populate this.wlMeasurementFinderList for PREDICTION type:
    this.wlMeasurementFinderList.
      add(PREDICTION, new WLMeasurementFinder(prdDataList));

    final long lastDt= prdDataList.
      get(prdDataList.size() - 1).getEventDate().getEpochSecond();

    slog.info(mmi+"Predictions last time-stamp: " + SecondsSinceEpoch.dtFmtString(lastDt, true));

    //--- WL OBSERVATION type
    final List<MeasurementCustom> obsDataList= fmsInput.getObservations();

    if ((obsDataList != null) && (obsDataList.size() > 0)) {

      slog.info(mmi+"Got "+obsDataList.size()+
                " WL observations from the DB for station: " + this.stationId);

      this.wlMeasurementFinderList.
        add(OBSERVATION, new WLMeasurementFinder(obsDataList));

      this.lastWLOSse= this.
        wlMeasurementFinderList.get(OBSERVATION).getLastSse();

    } else {

      this.wlMeasurementFinderList.add(OBSERVATION, null);

      slog.info(mmi+"No observations retreived(null or size()==0) from the DB for station:"+
                this.stationId + ", setting this last.ObsSse at the FMSInput reference time.");

      this.lastWLOSse= fmsInput.
        getReferenceTime().getEpochSecond();
    }

    slog.info(mmi+"this.lastWLOSse time-stamp="+
              SecondsSinceEpoch.dtFmtString(this.lastWLOSse, true));

    final int prdDataSize= this.
      wlMeasurementFinderList.get(PREDICTION).size();

    final List<MeasurementCustom> qcfDataList= fmsInput.getQualityControlForecasts();

    if ((qcfDataList != null) && (qcfDataList.size() > 0)) {

      slog.info(mmi+"Got " + qcfDataList.size() + " WL QC forecasts data for station: " + this.stationId);

      final int qcfDataSize= qcfDataList.size();

      if (qcfDataSize != prdDataSize) {

        slog.info(mmi+"qcfDataSize=" + qcfDataSize + ", prdDataSize=" + prdDataSize);
        slog.info(mmi+"qcfDataSize != prdDataSize for station: " + this.stationId);
        slog.info(mmi+"QC forecasts and predictions are not synchronized ! QC forecasts will be replaced by predictions !");

        this.wlMeasurementFinderList.
          add(QC_FORECAST, new WLMeasurementFinder(prdDataList)); //this.wlMeasurementFinderList.get(PREDICTION));

        throw new RuntimeException(mmi+"Debug exit here !!");

      } else {

        slog.debug(mmi+"Got the same number of predictions and QC forecasts data, QC forecasts data will be used");

        this.wlMeasurementFinderList.
          add(QC_FORECAST, new WLMeasurementFinder(qcfDataList));

        slog.info(mmi+"Ingestion of WL QC forecast data Nnt tested yet!!");
        throw new RuntimeException(mmi+"Debug exit here !!");
      }

    } else {

      slog.info(mmi+"No WL QC forecasts for station: "+
                this.stationId + ", replacing it with its prediction-climatology");

      this.wlMeasurementFinderList.
        add(QC_FORECAST, this.wlMeasurementFinderList.get(PREDICTION));

      //slog.info(mmi+"Debug exit 0 here !");
      //System.exit(0);

      //throw new RuntimeException(mmi+"Debug exit here !!");
    }

    // --- Add the model full forecast WL data.
    final List<MeasurementCustom> mfDataList= fmsInput.getModelForecasts();

    if ( (mfDataList != null ) && (mfDataList.size() > 0 ) ) {

      this.wlMeasurementFinderList.
        add( MODEL_FORECAST, new WLMeasurementFinder(mfDataList) );

      slog.info(mmi+"Got "+mfDataList.size()+
                " model WL forecasts data for station: " + this.stationId);

      this.useFullModelForecast= true;

      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);

      //final int mfDataSize= mfDataList.size();
      //if (mfDataSize != prdDataSize) {
      //  slog.info(mmi+"mfDataSize=" + mfDataSize + ", prdDataSize=" + prdDataSize);
      //  slog.info(mmi+"mfDataSize != prdDataSize for station: " + this.stationId);
      //  slog.info(mmi+"Model forecasts and predictions are not synchronized ! model forecasts will be replaced by predictions !");
      //  this.wlMeasurementFinderList.add(MODEL_FORECAST, this.wlMeasurementFinderList.get(PREDICTION));
      //  throw new RuntimeException(mmi+"Debug exit here !!");
      //} else {
      //  slog.debug(mmi+"Got the same number of predictions and model forecasts data, Model forecasts data will be used");
      //  this.wlMeasurementFinderList.add(MODEL_FORECAST, this.wlMeasurementFinderList.get(MODEL_FORECAST)); //new WLMeasurementFinder(dbFcsList));
      //  this.useFullModelForecast= true;
      //  throw new RuntimeException(mmi+"Debug exit here !!");
      //}

    } else {

      slog.info(mmi+"No model WL forecasts for station:"+
                this.stationId + ", replacing it with its prediction-climatology");

      this.wlMeasurementFinderList.
        add(MODEL_FORECAST, this.wlMeasurementFinderList.get(PREDICTION));

      throw new RuntimeException(mmi+"Abnormal situation: Debug exit here !!");
    }

    //--- this.wlMeasurementFinderList.get(PREDICTION).getSecondsIncrement() returns an absolute value:
    this.secondsIncr= this.
      wlMeasurementFinderList.get(PREDICTION).getSecondsIncrement();

    if (this.secondsIncr == 0L) {

      slog.error(mmi+"this.secondsIncr <= 0L for station: " + this.stationId);
      throw new RuntimeException(mmi+"Cannot update forecast!");
    }

    if (this.secondsIncr < FORECASTS_TIME_INCR_SECONDS_MIN) {

      slog.error(mmi+"this.secondsIncr=" + this.secondsIncr + " < " +
        "FORECASTS_TIME_INCR_SECONDS_MIN=" + FORECASTS_TIME_INCR_SECONDS_MIN + " for station: " + this.stationId);

      throw new RuntimeException(mmi+"Cannot update forecast!");
    }

    if (this.secondsIncr > FORECASTS_TIME_INCR_SECONDS_MAX) {

      slog.error(mmi+"this.secondsIncr=" + this.secondsIncr + " > "+
        "FORECASTS_TIME_INCR_SECONDS_MAX="+FORECASTS_TIME_INCR_SECONDS_MAX + " for station: " + this.stationId);

      throw new RuntimeException(mmi+"Cannot update forecast !");
    }

    boolean validTi= false;

    //--- Validate the time increment returned for the time increment allowed.
    for (final int check : FORECASTS_TIME_INCR_MINUTES_ALLOWED) {

      if (this.secondsIncr == (long) SECONDS_PER_MINUTE * check) {
        validTi= true;
        break;
      }
    }

    if (!validTi) {
      slog.error(mmi+"Invalid time increment in seconds="+this.secondsIncr+" for station: "+this.stationId);
      throw new RuntimeException(mmi+"Cannot update forecast !");
    }

    slog.info(mmi+"Will use " + this.secondsIncr + " seconds as the time increment in seconds");

    slog.info(mmi+"end");
    //slog.info(mmi+"System exit 0");
    //System.exit(0);

    //throw new RuntimeException(mmi+"Debug exit!");
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
  protected final static boolean validate(/*@NotNull @Size(min = 1)*/
                                          final List<MeasurementCustom> measurementsList, /*@Min(1)*/ final int tauHours) {

    final String mmi= "validate: ";

    slog.info(mmi+"start: wList.size()=" + measurementsList.size()); // wlList=" + measurementsList + ", wList.size()=" + measurementsList.size());

    //--- We must have at least tauHours between the 1st and the last WL DB data
    final long sseBeg= measurementsList.get(0).getEventDate().getEpochSecond();

    final long sseEnd= measurementsList.
      get(measurementsList.size() - 1).getEventDate().getEpochSecond();

    final int durationHours= ((int) (sseEnd - sseBeg)) / SECONDS_PER_HOUR;

    slog.info(mmi+"tauHours="+tauHours);
    slog.info(mmi+"durationHours="+durationHours);

    slog.info(mmi+"end");
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    return (durationHours >= tauHours);
  }

  /**
   * @param type : The WLType to find.
   * @return The right WLType Measurement List.
   * WARNING: The verification on the existence of the returned Measurement List is deferred to the client method.
   */
  final List<MeasurementCustom> getMeasurementList(/*@NotNull*/ final WLType type) {

    //--- this.getWLMeasurementFinder(type) could return null.
    final WLMeasurementFinder whichOne= this.getWLMeasurementFinder(type);

    return (whichOne != null) ? whichOne.getMCDataList() : null;
  }

  /**
   * @param type : The WLType to find.
   * @return The right WLType WLMeasurementFinder object(which could be possibly null)
   */
  private final WLMeasurementFinder getWLMeasurementFinder(/*@NotNull*/ final WLType type) {

    return this.wlMeasurementFinderList.get(type.ordinal());
  }

  /**
   * @param type             : The WLType wanted.
   * @param timeStampSeconds : The time-stamp in seconds since the epoch wanted.
   * @return The Measurement object of WLType type at the time-stamp in seconds timeStampSeconds(if any).
   * WARNING : The verification on the existence of the returned Measurement is deferred to the client method.
   */
  final MeasurementCustom getMeasurementType(/*@NotNull*/
                                             final WLType type,
                                             final long timeStampSeconds) {
    final String mmi= "getMeasurementType: ";

    final WLMeasurementFinder wlTypeFinder= this.getWLMeasurementFinder(type);

    slog.info(mmi+"type="+type); //+", this.wlMeasurementFinderList.get(intType)="+this.wlMeasurementFinderList.get(intType));

    return (wlTypeFinder != null ? wlTypeFinder.find(timeStampSeconds) : null);
  }

  /**
   * @return this.stationCode.
   */
  //@NotNull
  public final String getStationId() {
    return this.stationId;
  }

  /**
   * @return The length of the PREDICTION type WL Measurement data.
   */
  final int predictionsSize() {
    return this.wlMeasurementFinderList.get(PREDICTION).size();
  }
}
