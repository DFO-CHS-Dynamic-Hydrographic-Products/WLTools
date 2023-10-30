//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

/**
 *
 */

//---
import java.lang.Math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.util.IGeo;
import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.wl.WLTimeNode;
import ca.gc.dfo.chs.wltools.wl.fms.FMSInput;
import ca.gc.dfo.chs.wltools.wl.fms.FMSConfig;
//import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.wl.fms.IFMSResidual;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.wl.fms.FMSWLStationData;
import ca.gc.dfo.chs.wltools.wl.fms.FMSWLMeasurement;

//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.IWL;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLTimeNode;
//import ca.gc.dfo.iwls.modeling.fms.Forecast;
//import ca.gc.dfo.iwls.modeling.fms.Residual;
//import ca.gc.dfo.iwls.modeling.fms.StationCovariance;
//import ca.gc.dfo.iwls.modeling.fms.TidalRemnant;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;

//---
//---

/**
 * Class for one WL station FM Service objects.
 */
public final class FMSWLStation extends FMSWLStationData implements IFMS, IWL {

  final private static String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.FMSWLStation";

  /**
   * log utility.
   */
  final private static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Array of FMWLMeasurement objects references.
   */
  private final FMSWLMeasurement []
    dataReferences= new FMSWLMeasurement[WLType.values().length];

  /**
   * The generic IFMResidual used for WL errors residuals computations.
   */
  private IFMSResidual fmsResidual= null;

  /**
   * Index of the WL station in the List of FMWLStation objects of the FMWLData class.
   */
  private int stationNodeIndex= 0;

  /**
   * The time-stamp(seconds since the epoch) used to mark the time of the initial merge of the default WLF-QC
   * (made with prediction data without external storm surge and-or fresh water discharge.debugrmation) with the
   *  full model forecast data coming from a full-fledged model that includes atmos. forcings and-or
   *  fresh-water discharge effects. This merge is done not too far in time after the last valid obs. data
   *  has been used to adjust the prediction for the QC forecast.
   */
  private long fmfMergeCompleteSse= 0L;

  /**
   * Time weight for the initial merge the default QC forecast data
   * with the full model forecast data (if any).
   */
  private double tiFMFMergeWeight= 0.0;

  /**
   * Time weight for merging the full model forecast data (if any)
   * with the long term (~30 days) prediction data. This is the final
   * merge that begins after the last timestamp of the full model forecast
   * data.
   */
  private double tiLongTermPredMergeWeight= 0.0;

  /**
   * Need to have the WL difference between the last full model forecast WL
   * and the long term prediction WL data at the same timestamp to merge the
   * full model forecast data with the long term NS_TIDE or climato prediction data
   */
  private double lastQCFVsFMFDiff= 0.0;

  /**
   * @param stationNodeIndex   : The WL station index in the allStationsData List attribute of class FMWLData.
   * @param forecastingContext : The ForecastingContext object of a given WL station.
   */
  //FMSWLStation(/*@Min(0)*/ final int stationNodeIndex, /*@NotNull*/ final ForecastingContext forecastingContext) {
  //  this(stationNodeIndex, forecastingContext, GlobalVerticalDatum.NAVD88, 0.0);
  //}

  FMSWLStation(/*@Min(0)*/ final int stationNodeIndex, /*@NotNull*/ final FMSInput fmsInput) {

    this(stationNodeIndex, fmsInput, GlobalVerticalDatum.NAVD88, 0.0);
  }

  /**
   * @param stationNodeIndex     : The WL station index in the allStationsData List attribute of class FMWLData.
   * @param forecastingContext   : The ForecastingContext object of a given WL station.
   * @param globalVerticalDatum  : The GlobalVerticalDatum object for the global vertical reference.
   * @param globalVerticalOffset : The vertical Z elevation offset relative to the GlobalVerticalDatum of the WL
   *                             station.
   */
  //private FMSWLStation(@Min(0) final int stationNodeIndex, @NotNull final ForecastingContext forecastingContext,
  //                     @NotNull final GlobalVerticalDatum globalVerticalDatum, final double globalVerticalOffset) {

  private FMSWLStation(/*@Min(0)*/ final int stationNodeIndex, /*@NotNull*/ final FMSInput fmsInput,
                       /*@NotNull*/ final IGeo.GlobalVerticalDatum globalVerticalDatum, final double globalVerticalOffset) {

    //super(forecastingContext, globalVerticalDatum, globalVerticalOffset);
    super(fmsInput, globalVerticalDatum, globalVerticalOffset);

    final String mmi= "FMSWLStation main constructor: ";

    slog.debug(mmi+"Start: stationId:" + this.getStationId());

    if (fmsInput == null) {

      slog.error(mmi+"fmsInput==null");
      throw new RuntimeException(mmi+"Cannot update forecast !");
    }

    //if (forecastingContext.getFmsParameters() == null) {
    //  this.log.error("FMWLStation constr.: forecastingContext.getFmsParameters()==null");
    //  throw new RuntimeException("FMSWLStation constructor: Cannot update forecast !");
    //}

    //final Forecast forecastParameters = forecastingContext.getFmsParameters().getForecast();
    //if (forecastParameters == null) {
    //  this.log.error("FMWLStation constr.: forecastParameters==null");
    //  throw new RuntimeException("FMSWLStation constructor: Cannot update forecast !");
    //}

    //if (forecastParameters.getMergeHours() == null) {
    //  this.log.error("FMWLStation constr.: forecastParameters.getMergeHours()==null");
    //  throw new RuntimeException("FMSWLStation constructor: Cannot update forecast !");
    //}

    //--- Duration in seconds after which the merge to the storm surge forecast(if any) is complete:
    //final long mergeDurationSeconds = SECONDS_PER_HOUR * forecastParameters.getMergeHours().longValue();
    final long mergeDurationSeconds= (long) SECONDS_PER_HOUR * fmsInput.getFMFMergeDurationHours();

    if (mergeDurationSeconds < 0L) {

      slog.error(mmi+"mergeDurationSeconds<0L");
      throw new RuntimeException(mmi+"Cannot update forecast !");
    }

    //if (forecastingContext.getReferenceTime() == null) {
    //  this.log.error("FMWLStation constr.: forecastingContext.getReferenceTime()==null");
    //  throw new RuntimeException("FMSWLStation constructor: Cannot update forecast !");
    //}

    //--- The time stamp in seconds after which the merge to the storm surge forecast(if any) is complete:
    //this.mergeCompleteSse = forecastingContext.getReferenceTime().getEpochSecond() + mergeDurationSeconds;
    this.fmfMergeCompleteSse= fmsInput.
      getReferenceTimeInSeconds() + mergeDurationSeconds;

    //--- time interpolation weight factor for merging QC forecast data
    //    with full model forecast data (if any).
    this.tiFMFMergeWeight= 1.0 / mergeDurationSeconds;

    // --- time interpolation weight factor for merging the full model forecast data (if any)
    //     with the long term prediction data. This merge is 3 * slower than for the
    //     merge with the QC forecast data.
    this.tiLongTermPredMergeWeight= this.tiFMFMergeWeight/3.0;

    //this.log.debug("this.lastWLOSse time-stamp="+SecondsSinceEpoch.dtFmtString(this.lastWLOSse,true));

    if (stationNodeIndex < 0) {

      slog.error(mmi+"stationNodeIndex<0");
      throw new RuntimeException(mmi+"Cannot update forecast !");
    }

    this.stationNodeIndex= stationNodeIndex;

    final double predsTimeIncrMinutes = (this.secondsIncr / SECONDS_PER_MINUTE);

    if (fmsInput.getDeltaTMinutes() != predsTimeIncrMinutes) {

      slog.warn(mmi+"Must change station: " + this.getStationId() + " forecast parameters dt="
          + fmsInput.getDeltaTMinutes() + " minutes to the predictions interval of " + predsTimeIncrMinutes + " minutes get the best results.");

      fmsInput.setDeltaTMinutes(predsTimeIncrMinutes);
    }

    //final Residual residualParameters = forecastingContext.getFmsParameters().getResidual();
    final FMSResidualConfig fmsResidualConfig= fmsInput.getFMSResidualConfig();

    //if (residualParameters.getDeltaTMinutes() != predsTimeIncrMinutes) {
    if (fmsResidualConfig.getDeltaTMinutes() != predsTimeIncrMinutes) {

      slog.warn(mmi+"Must change station: " + this.getStationId() + " FMS residual parameters dt="
          + fmsResidualConfig.getDeltaTMinutes() + " minutes to the predictions interval of " + predsTimeIncrMinutes + " minutes get the best results.");

      //residualParameters.setDeltaTMinutes(predsTimeIncrMinutes);
      fmsResidualConfig.setDeltaTMinutes(predsTimeIncrMinutes);
    }

    //--- Change the station time lag with itself if different from prediction time increment:
    //    NOTE: The lags of the auxiliary stations(if any) are not changed here.
    final FMSStationCovarianceConfig thisStationCovarianceCfg=
      fmsResidualConfig.getFMSStationCovarianceConfigList().get(0); //residualParameters.getCovariance().get(0);

    if (thisStationCovarianceCfg.getTimeLagMinutes() != predsTimeIncrMinutes) {

      slog.warn(mmi+"Must change this station: " + this.getStationId() + " covariance config time lag="
          + thisStationCovarianceCfg.getTimeLagMinutes() + " minutes to the predictions interval of " + predsTimeIncrMinutes + " minutes get the best results.");

      thisStationCovarianceCfg.setTimeLagMinutes(predsTimeIncrMinutes);
    }

    final FMSTidalRemnantConfig fmsTidalRemnantConfig= fmsInput.getFMSTidalRemnantConfig();  //forecastingContext.getFmsParameters().getTidalRemnant();

    if (fmsTidalRemnantConfig != null) {

      if (fmsTidalRemnantConfig.getDeltaTMinutes() != predsTimeIncrMinutes) {

       slog.warn(mmi+"Must change station: " + this.getStationId() + " tidal remnant config delta time minutes" +
           + fmsTidalRemnantConfig.getDeltaTMinutes() + " minutes to the predictions interval of " + predsTimeIncrMinutes + " minutes get the best results.");

       fmsTidalRemnantConfig.setDeltaTMinutes(predsTimeIncrMinutes);
      }
    }

    //--- Set this.residual IFMResidual object according to the ForecastingContext configuration Object of the station
    this.fmsResidual= FMSResidualFactory.
      getIFMSResidual(fmsInput, this.lastWLOSse); //getIFMSResidual(forecastingContext, this.lastWLOSse);

    //--- Create the FMWLMeasurement objects contained in this.dataReferences array:
    for (final WLType wlt : WLType.values()) {
      this.dataReferences[wlt.ordinal()]= new FMSWLMeasurement(null);
    }

    slog.debug(mmi+"End for station:" + this.getStationId());
  }

  // ---
  public final IFMSResidual getIFMSResidual() {
    return this.fmsResidual;
  }

  /**
   * @param timeStampSeconds : The time-stamp where we want the WL PREDICTION, OBSERVATION, FORECAST(if any) and
   *                         EXT_STORM_SURGE(if any).
   * @return this.dataReferences array.
   */
  //@NotNull
  protected FMSWLMeasurement[] getDataReferences(/*@Min(0)*/ final long timeStampSeconds) {

    final String mmi= "getDataReferences: ";

    FMSWLMeasurement.setMeasurementsRefs(this.getMeasurementType(WLType.PREDICTION, timeStampSeconds),
                                         this.getMeasurementType(WLType.OBSERVATION, timeStampSeconds),
                                         this.getMeasurementType(WLType.QC_FORECAST, timeStampSeconds),
                                         this.getMeasurementType(WLType.MODEL_FORECAST, timeStampSeconds), this.dataReferences);

    //--- this.dataReferences[PREDICTION] object reference should not be null at this point(it is allocated by
    // FMSWLMeasurement.setMeasurementsRefs())
    //    Uncomment the following block for debugging purposes.
//        if (this.dataReferences[PREDICTION]==null) {
//            this.log.error("this.dataReferences[PREDICTION]==null at time-stamp: "+ SecondsSinceEpoch.dtFmtString
//            (seconds,true) );
//            throw new RuntimeException("FMSWLStation constructor: Cannot update forecast !");
//        }

    //--- But if this.dataReferences[PREDICTION].measurement() is null then this is absolutely not normal if this is
    // the case.
    if (this.dataReferences[PREDICTION].measurement() == null) {

      slog.error(mmi+"this.dataReferences[PREDICTION].measurement()==null at time-stamp: "+
                 SecondsSinceEpoch.dtFmtString(timeStampSeconds, true));

      throw new RuntimeException(mmi+"Cannot update forecast !");
    }

//        //--- In case(quite unlikely) that a FORECAST is missing:
//        if (this.dataReferences[FORECAST].measurement()==null) {
//            this.log.warn("this.dataReferences[FORECAST].measurement()==null for station: "+ this.stationCode +" at
//            time stamp: "+SecondsSinceEpoch.dtFmtString(seconds,true)+ ", replacing this FORECAST by the PREDICTION");
//            this.dataReferences[FORECAST]= dataReferences[PREDICTION];
//        }

    return this.dataReferences;
  }

  /**
   * @param pstrWLTimeNode     : The WLTimeNode object just before in time compared to the SecondsSinceEpoch sse object
   *                           time-stamp.
   * @param sse                : A SecondsSinceEpoch object having the time-stamp where we want a new
   *                           WLStationTimeNode object ready
   *                           to use.
   * @param sseFutureThreshold : The seconds since the epoch which is the first time-stamp of the future(compared
   *                           with actual real-time not with the last valid WLO).
   * @return A WLStationTimeNode which have been processed by the FMResidualFactory.processFMSWLStation method.
   */
  //@NotNull
  public final WLStationTimeNode getNewWLStationFMTimeNode(/*@NotNull*/ final WLTimeNode pstrWLTimeNode,
                                                           /*@NotNull*/ final SecondsSinceEpoch sse,
                                                           /*(@Min(0)*/ final long sseFutureThreshold) {

    final String mmi= "getNewWLStationFMTimeNode: ";
    //final String dts= sse.dateTimeString(true);
    //final String stationId= this.stationId;

    //final String mmi= "getNewWLStationFMTimeNode: ";
    //slog.debug(mmi+"stationId="+ this.getStationId() + ", sse dts=" + sse.dateTimeString(true) + ", pstr=" + pstrWLTimeNode);

    //--- Get the previous WLStationTimeNode reference if any
    final WLStationTimeNode psr= (pstrWLTimeNode != null) ?
      pstrWLTimeNode.getStationNode(this.stationNodeIndex) : null;

    //if (pstrWLTimeNode != null) {
    //  slog.debug(mmi+"pstr dt= " + pstrWLTimeNode.getSse().dateTimeString(true));
    //}

    //slog.debug(mmi+"psr=" + psr);

    //if (psr != null) {
    //  slog.debug(mmi+"psr dt= " + psr.getSse().dateTimeString(true));
    //}

    slog.debug(mmi+"this.fmfMergeCompleteSse dt=" + SecondsSinceEpoch.dtFmtString(this.fmfMergeCompleteSse, true));
    slog.debug(mmi+"this.tiFMFMergeWeight=" + this.tiFMFMergeWeight);

    return FMSResidualFactory.processFMSWLStation(psr, sse, sseFutureThreshold, this);
  }

  //--- For possible future usage.
//    protected final boolean fullTimeMerged(final long seconds) {
//        return seconds>=this.mergeCompleteSse;
//    }

  //---
  // @NotNull
  protected final WLStationTimeNode mergeWithFullModelForecast(/*@Min(0)*/  final long seconds,
                                                               /*@Min(0)*/  final long fmfThreshold,
                                                               /*@NotNull*/ final WLStationTimeNode wlstn) {
    final String mmi= "mergeWithFullModelForecast: ";

    slog.debug(mmi+"Merging QC forecast with full model forecast, dt=" + SecondsSinceEpoch.dtFmtString(seconds, true));

    final long lastFullModelForecastDataSse= this.
      lastFullModelForecastMcObj.getEventDate().getEpochSecond();

    //double lastQCFVsFMFDiff= 0.0;

    if (fmfThreshold > this.fmfMergeCompleteSse ) {
      throw new RuntimeException(mmi+"Cannot have fmfThreshold > this.fmfMergeCompleteSse !!");
    }

    if (seconds < this.fmfMergeCompleteSse) {

      // --- Ensure that fmfWeight is between 0.0 and 1.0 (inclusive).
      final double fmfWeight=
        Math.max(0.0, Math.min(1.0, this.tiFMFMergeWeight * (seconds - fmfThreshold) ));

      slog.debug(mmi+"merge dt="+SecondsSinceEpoch.dtFmtString (seconds, true)+
                ", fmfThreshold=" + fmfThreshold + ", fmfWeight=" + fmfWeight +", fmfMergeCompleteSse="+fmfMergeCompleteSse);

      //slog.debug(mmi+"Debug exit 0");
      //System.exit(0);
      slog.debug(mmi+"bef. merge: wlstn.getUpdatedForecast().getValue()="+wlstn.getUpdatedForecast().getValue());

      wlstn.mergeWithFullModelForecast(this.surgeOffsetType, fmfWeight);

      slog.debug(mmi+"aft. merge: wlstn.getUpdatedForecast().getValue()="+wlstn.getUpdatedForecast().getValue());
      //slog.debug(mmi+"Debug exit 0");
      //System.exit(0);

    } else if (seconds <= lastFullModelForecastDataSse ) {

      // --- Need to subtract the wlstn.getUpdatedForecast().getValue()
      //     from the his.lastFullModelForecastMcObj.getValue() to get the
      //    correct merge value.
      this.lastQCFVsFMFDiff=
        this.lastFullModelForecastMcObj.getValue() - wlstn.getUpdatedForecast().getValue();

      slog.debug(mmi+"bef. merge: wlstn.getUpdatedForecast().getValue()="+wlstn.getUpdatedForecast().getValue());

      // --- At this point we use %100 of the full model forecast data for the updated forecast data.
      wlstn.mergeWithFullModelForecast(this.surgeOffsetType, 1.0);

      slog.debug(mmi+"aft. merge: wlstn.getUpdatedForecast().getValue()="+wlstn.getUpdatedForecast().getValue());
      //slog.debug(mmi+"Debug exit 0");
      //System.exit(0);

    } else {

      // --- Here at seconds > lastFullModelForecastDataSse we need to merge the full
      //     model forecast with the 32 days NS_TIDE prediction data but the merge is
      //     done in the opposite sense compared to when seconds < this.fmfMergeCompleteSse
      //     so we need to define the fmfWeightInv as being calculated as follows:
      //     1.0 - this.tiPredMergeWeight * (seconds - lastFullModelForecastDataSse)
      final double fmfWeightInv=
        Math.max(0.0, Math.min(1.0, 1.0 - this.tiLongTermPredMergeWeight * (seconds - lastFullModelForecastDataSse)));

      slog.debug(mmi+"fmfWeightInv="+fmfWeightInv);
      slog.debug(mmi+"tiLongTermPredMergeWeight="+this.tiLongTermPredMergeWeight);
      //slog.debug(mmi+"Debug exit 0");
      //System.exit(0);

      //slog.debug(mmi+"bef. merge: wlstn.getUpdatedForecast().getValue()="+wlstn.getUpdatedForecast().getValue());
      //slog.debug(mmi+"lastFullModelForecastMcObj.getValue()="+ this.lastFullModelForecastMcObj.getValue());
      //slog.debug(mmi+"this.lastQCFVsFMFDiff=="+ this.lastQCFVsFMFDiff);

     // --- NOTE: We have to use the WLSO_DE_TIDED surge offet type because we only need
     //     to apply the time-decaying this.lastQCFVsFMFDiff WL difference in the future
     //     to the QC forecast WL data in order to smoothly merge its signal with the full
     //     forecast model WL data.
      wlstn.mergeWithFullModelForecastZValue(SurgeOffsetWLType.WLSO_DE_TIDED,
                                             fmfWeightInv, this.lastQCFVsFMFDiff);

     //--- Now set this.lastFullModelForecastMcObj WL value to the value of the
     //    newly merged updated forecast for the next loop iteration on future timestamps for
     //    the next merge calculation
     //this.lastFullModelForecastMcObj.
     //   setValue(wlstn.getUpdatedForecast().getValue());

      slog.debug(mmi+"aft. merge: wlstn.getUpdatedForecast().getValue()="+wlstn.getUpdatedForecast().getValue());
      //slog.debug(mmi+"Debug exit 0");
      //System.exit(0);

    }

    slog.debug(mmi+"end");
    //slog.debug(mmi+"Debug exit 0");
    //System.exit(0);

    return wlstn;
  }
}
