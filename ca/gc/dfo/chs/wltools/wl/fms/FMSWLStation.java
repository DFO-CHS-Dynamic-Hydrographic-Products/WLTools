//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

/**
 *
 */

//---
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
   * The time-stamp(seconds since the epoch) used to mark the time of the complete merge of the default WLF-QC
   * (made with prediction data without external storm surge and-or fresh water discharge component) with the
   *  full model forecast component coming from a full-fledged model that includes atmos. forcings and-or
   *  fresh-water discharge effects.
   */
  private long fmfMergeCompleteSse= 0L;

  /**
   * Time weight for  merge of the default forecasts(without external storm surge component)
   * with the external storm surge component(if any).
   */
  private double tiForecastMergeWeight= 0.0;

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

    slog.info(mmi+"Start: stationId:" + this.getStationId());

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
    this.fmfMergeCompleteSse= fmsInput.getReferenceTimeInSeconds() + mergeDurationSeconds;

    //--- time interpolation weight factor for merging verification forecast with a storm surge forecast(if any).
    this.tiForecastMergeWeight = 1.0 / mergeDurationSeconds;

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

    slog.info(mmi+"this.ssfMergeCompleteSse dt=" + SecondsSinceEpoch.dtFmtString(this.fmfMergeCompleteSse, true));
    slog.info(mmi+"this.tiForecastMergeWeight=" + this.tiForecastMergeWeight);

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

    slog.info(mmi+"Merging QC forecast with full model forecast, dt=" + SecondsSinceEpoch.dtFmtString(seconds, true));

    if (seconds < this.fmfMergeCompleteSse) {

      final double fmfWeight= this.tiForecastMergeWeight * (seconds - fmfThreshold);

      slog.info(mmi+"merge dt="+SecondsSinceEpoch.dtFmtString (seconds, true)+", fmfWeight=" + fmfWeight);

      wlstn.mergeWithFullModelForecast(this.ssfType, fmfWeight);

    } else {

      wlstn.mergeWithFullModelForecast(this.ssfType, 1.0);
    }

    return wlstn;
  }
}
