package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;

/**
 *
 */

//---

import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSWLMeasurement;
import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.ScalarOps;
import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
import ca.gc.dfo.iwls.fmservice.modeling.wl.IWL;
import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
import ca.gc.dfo.iwls.fmservice.modeling.wl.WLZE;
import ca.gc.dfo.iwls.modeling.fms.Residual;
import ca.gc.dfo.iwls.modeling.fms.StationCovariance;
import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

//---
//---
//---
//---

/**
 * Class used to compute and estimate the WLP residuals errors for a given WL station with the(slightly modified)
 * legacy DVFM algorithm.
 * (NOTE: this class is inherited by the TidalRemnantResidual class).
 */
public class LegacyResidual extends LegacyFMSResidualFactory implements ILegacyFMSResidual, IWL {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Normally only used as a reference when we have to consider a tidal remnant component. It is null for WL stations
   * having no significant tidal influence.
   */
  protected WLZE tidalRemnantRf = null;
  /**
   * To store the WL residual component related data structures(a.k.a objects).
   */
  private LegacyResidualData resData = null;
  /**
   * Estimated WL surge WITHOUT tidal remnant.
   */
  private WLZE estimatedSurge = null;
  /**
   * The time stamp(in the past) in seconds since the epoch used for the beginning of the residuals errors computations
   */
  private long sseStart = 0L;
  
  /**
   * @param residualCfg : Residual config. object obtained from a WL station ForecastingContext object.
   * @param stationCode : The WL station usual SINECO string ID.
   */
  public LegacyResidual(@NotNull final Residual residualCfg, @NotNull final String stationCode) {
    this(residualCfg, stationCode, residualCfg.getDeltaTMinutes().doubleValue());
  }
  
  /**
   * @param residualCfg       : Residual config. object obtained from a WL station ForecastingContext object.
   * @param stationCode       : The WL station usual SINECO string ID.
   * @param timeIncrDtMinutes :
   */
  public LegacyResidual(@NotNull final Residual residualCfg, @NotNull final String stationCode,
                        @Min(1) final double timeIncrDtMinutes) {
    
    super(stationCode);
    
    this.log.debug("LegacyResidual constructor start for station:" + stationCode);
    
    if (residualCfg == null) {
      
      this.log.error("LegacyResidual constructor: residualCfg==null ! for station:" + stationCode);
      throw new RuntimeException("LegacyResidual constructor");
    }
    
    final List<StationCovariance> stationsCovarianceList = residualCfg.getCovariance();
    
    if (stationsCovarianceList == null) {
      
      this.log.error("LegacyResidual constructor:  stationsCovarianceList == null ! for station:" + stationCode);
      throw new RuntimeException("LegacyResidual constructor");
    }
    
    if (stationsCovarianceList.size() == 0) {
      
      this.log.error("LegacyResidual constructor: stationsCovarianceList.size() <= 0 ! for station:" + stationCode);
      throw new RuntimeException("LegacyResidual constructor");
    }
    
    if (residualCfg.getTauHours() == null) {
      
      this.log.error("LegacyResidual constructor: residualCfg.getTauHours()==null ! for station:" + stationCode);
      throw new RuntimeException("LegacyResidual constructor");
    }

//        if (residualCfg.getTauHours()!=RetrieveWlObservationsHandler.NB_HOURS_OF_OBSERVATIONS) {
//        }
    
    this.tidalRemnantRf = null;
    this.estimatedSurge = new WLZE(0.0, 0.0);
    
    this.log.debug("LegacyResidual constr.: residualCfg=" + residualCfg.toString());
    
    this.covData = new LegacyFMSCov(residualCfg.getFallBackError(), stationCode, stationsCovarianceList);
    
    final int nbAuxCov = this.covData.auxCovSize();
    
    this.resData = new LegacyResidualData(nbAuxCov, residualCfg.getTauHours().doubleValue(), timeIncrDtMinutes);
    
    this.log.debug("LegacyResidual constr.: end for station:" + stationCode);
  }
  
  /**
   * @param wlStationTimeNode : A WLStationTimeNode object.
   * @param apply             : boolean to signal that the estimated WL residual errors are to be applied to get a
   *                          new forecast
   *                          (i.e. the time stamp of the WLStationTimeNode is in the future).
   * @return The WLStationTimeNode object.
   */
  @NotNull
  @Override
  public WLStationTimeNode estimate(@NotNull final WLStationTimeNode wlStationTimeNode, final boolean apply) {
    
    this.log.debug("LegacyResidual estimate: start, wlstn=" + wlStationTimeNode + ", apply=" + apply + ", stationCode" +
        "=" + this.stationCode);
    
    //--- Uncomment the following block for debugging purposes.
//        if (!wlstn.checkTimeSync()) {
//            this.log.debug("LegacyFMCov getAxSurgesErrsInZX: wltsn.checkTimeSync()== false");
//        }
    
    //--- NOTE: This method is similarly used for both past(when we have
    //          a missing WLO) and future time-stamps
    
    final LegacyResidualData resData = this.resData;
    final LegacyFMSCov covData = (LegacyFMSCov) this.covData;

//        this.log.debug("LegacyResidual estimate: rd.tau="+rd.tau);
//        this.log.debug("LegacyResidual estimate: rd.dt="+rd.dt);
    
    final long nodeTimeStamp = wlStationTimeNode.seconds();
    
    //--- WARNING: The 3 following debugging logs calls can produce null objects exceptions
    this.log.debug("LegacyResidual estimate: wlstn.sse dt=" + wlStationTimeNode.getSse().dateTimeString(true) +
        ", this.lastUpdateSse dt=" + this.lastUpdateSse.dateTimeString(true));
    this.log.debug("LegacyResidual estimate: wlstn.get(WLType.PREDICTION).zDValue()=" +
        wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue());
    this.log.debug("LegacyResidual estimate: wlstn.get(WLType.PREDICTION).seconds dt=" +
        SecondsSinceEpoch.dtFmtString(wlStationTimeNode.get(WLType.PREDICTION).seconds(), true));
    
    if (wlStationTimeNode.getPstr() != null) {
      this.log.debug("LegacyResidual estimate: wlstn.pstr()=" + wlStationTimeNode.getPstr());
      this.log.debug("LegacyResidual estimate: wlstn.pstr().sse dt=" + wlStationTimeNode.getPstr().getSse().dateTimeString(true));
    }
    
    //--- Retreive previously computed direct surge(s) and the associated error(s) from the auxiliary temporal errors
    // covariances data:
    covData.getAxSurgesErrsInZX(nodeTimeStamp, resData.zX, resData.beta);
    
    //--- Quite unlikely to happen but we never know.
    if (nodeTimeStamp < this.lastUpdateSse.seconds()) {
      
      this.log.error("LegacyResidual estimate: nts < this.lastUpdateSse.seconds() ! nts must be larger or equal to " +
          "this.lastUpdateSse.seconds()");
      throw new RuntimeException("LegacyResidual estimate");
    }
    
    //--- Get the time offset compared with the last time stamp of the last residual object update:
    //    NOTE: Could be removed by putting nodeTimeStamp - this.lastUpdateSse.seconds() directly
    //          in timeFactor computation but it is kept ther to report its value in the info logs.
    final long timeOffset = nodeTimeStamp - this.lastUpdateSse.seconds();
    
    this.log.debug("LegacyResidual estimate: this.longTermSurgeFactor=" + this.longTermSurgeFactor);
    
    //---
    final double timeFactor = (double) (timeOffset) * resData.tauInv;
    
    //final double timeFactor= (double) (nts - this.lastUpdateSse.seconds()) * rd.tauInv;
    //TODO: Test if we can use the forecast duration here as the denominator for the future time ratio
    //final double timeFactor= (double)(nts - this.lastUpdateSse.seconds())/forecastDuration;
    
    final double surgeWeight = resData.getValueWeight(timeFactor);
    final double errorWeight = resData.getErrorWeight(timeFactor);
    
    //--- Keep the last surge computed at the previous time-stamp in a local variable
    //    for a possible local usage:
    final double lastEstimatedSurge = resData.zX.at(0);
    
    this.log.debug("LegacyResidual estimate:  rd.alpha=" + resData.alpha);
    this.log.debug("LegacyResidual estimate:  rd.tauInv=" + resData.tauInv);
    this.log.debug("LegacyResidual estimate: timeOffset=" + timeOffset);
    this.log.debug("LegacyResidual estimate: timeFactor=" + timeFactor);
    //this.log.debug("LegacyResidual estimate: timeFactor=" + (double)(nts - this.lastUpdateSse.seconds())
    // /forecastDuration);
    this.log.debug("LegacyResidual estimate: surgeWeight=" + surgeWeight);
    this.log.debug("LegacyResidual estimate: errorWeight=" + errorWeight);
    
    //--- Compute the WL estimated surge and its associated error:
    //    NOTE: computeEstimatedSurge returns the "short term" surge(a.k.a. storm and-or outflow surge)
    covData.computeEstimatedSurge(surgeWeight, errorWeight, resData.eps, resData.zX, resData.beta, this.estimatedSurge);
    
    final double estimatedShortTermSurgeZw = this.estimatedSurge.getZw();
    final double estimatedSurgeError = this.estimatedSurge.getError();
    
    //--- G. Mercier FMS 2018 modification:
    //    Add the WL "long term" surge to the "short term" surge.
    double estimatedSurge = estimatedShortTermSurgeZw + this.longTermSurge;
    
    double remnantValue = 0.0;
    double remnantError = 0.0;
    
    //--- Check if we have a tidal remnant to use:
    if (this.tidalRemnantRf != null) {
      
      remnantValue = this.tidalRemnantRf.getZw();
      remnantError = this.tidalRemnantRf.getError();
      
      //--- G. Mercier FMS 2018 modification:
      //    Now let the "long term" surge to decay more slowly than the "short term" surge
      //    in cases where we have a tidal remnant signal to use:
      this.longTermSurge *= Math.exp(-timeFactor * this.longTermSurgeFactor);
    }
    
    this.log.debug("LegacyResidual estimate: this.longTermSurge=" + this.longTermSurge);
    this.log.debug("LegacyResidual estimate: estimatedShortTermSurgeZw=" + estimatedShortTermSurgeZw);
    this.log.debug("LegacyResidual estimate: 1st estimatedSurge=" + estimatedSurge);
    this.log.debug("LegacyResidual estimate: lastEstimatedSurge=" + lastEstimatedSurge);
    this.log.debug("LegacyResidual estimate: estimatedSurgeError=" + estimatedSurgeError);
    this.log.debug("LegacyResidual estimate: remnantValue=" + remnantValue);
    this.log.debug("LegacyResidual estimate: remnantError=" + remnantError);
    
    //--- G. Mercier FMS 2018 modification:
    //    Now take care of the "short-term surge" (a.k.a. storm and-or outflow surge) effect:
    final double estimatedShortTermSurgeZwAbsVal = Math.abs(estimatedShortTermSurgeZw);
    
    //--- G. Mercier FMS 2018 modification:
    //    Last check on estimatedSurge: reduce surge values decay if estimatedShortTermSurgeZw
    //    is less(in terms of absolute values) than this.longTermSurge. Use the last estimated
    //    surge computed to deal with the slowly evolving total surge in both cases.
    if (estimatedShortTermSurgeZwAbsVal < Math.abs(this.longTermSurge)) {
      
      this.log.debug("LegacyResidual estimate: Using last estimated surge as the newly estimated surge.");
      
      estimatedSurge = lastEstimatedSurge;
      
    } else {
      
      this.log.debug("LegacyResidual estimate: Setting the newly estimated surge as the mean between it and the last " +
          "estimated surge.");
      
      //--- Estimated short term surge >= long term surge: keep the last estimated surge information by
      //    setting the newly estimated surge as the mean betweem its newly computed value and the last estimated surge:
      estimatedSurge = (estimatedSurge + lastEstimatedSurge) / 2.0;
    }
    
    this.log.debug("LegacyResidual estimate: final estimatedSurge=" + estimatedSurge);
    
    //--- Total estimated surge for this time-stamp:
    final double estimatedTotalSurgeZw = estimatedSurge + remnantValue;
    
    //--- G. Mercier FMS 2018 modification:
    //    The legacy DVFM code is applying another square to the estimatedSurgeError here even if all its sum items
    //    have been
    //    subjected to a square operation. It gives more realistic errors if we take the estimatedSurgeError as it is.
    //    NOTE: estimatedSurgeError is always >= 0.0 see computeEstimatedSurge method in source file LegacyFMCov for
    //    details:
    final double estimatedTotalSurgeError = Math.sqrt(estimatedSurgeError + ScalarOps.square(remnantError));
    
    this.log.debug("LegacyResidual estimate: estimatedTotalSurgeZw=" + estimatedTotalSurgeZw);
    this.log.debug("LegacyResidual estimate: estimatedTotalSurgeError=" + estimatedTotalSurgeError);
    
    //--- Apply estimated WL surge to get the full forecasted WL:
    if (apply) {
      
      this.log.debug("LegacyResidual estimate: Applying estimated surge to get the new forecasted water level at: "
          + SecondsSinceEpoch.dtFmtString(nodeTimeStamp, true) + ",  for station: " + this.stationCode);
      
      //--- Set the new computed forecast by applying the estimated surge:
      final double updatedForecast = wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue() + estimatedTotalSurgeZw;
      
      wlStationTimeNode.setUpdatedforecast(wlStationTimeNode.get(WLType.PREDICTION).getInstant(), updatedForecast,
          estimatedTotalSurgeError);
      
      this.log.debug("LegacyResidual estimate: wlstn.get(WLType.PREDICTION).zDValue()=" + wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue());
      this.log.debug("LegacyResidual estimate: wlstn.getUpdatedForecast().getValue()=" + wlStationTimeNode.getUpdatedForecast().getValue());
    }
    
    //--- Need to store the estimated surge( estimatedShortTermSurgeZw + this.longTermSurge WITHOUT tidal remnant)
    //    in the time node for possible subsequent usage:
    wlStationTimeNode.setSurge(estimatedSurge, estimatedSurgeError);
    
    this.log.debug("LegacyResidual estimate: end");
    
    return wlStationTimeNode;
  }
  
  /**
   * @param pstrWLStationTimeNode : A WLStationTimeNode object which is just before in time compared to the
   *                              SecondsSinceEpoch sse object time-stamp.
   * @param secondsSinceEpoch     : A SecondsSinceEpoch with the next(in the future) time stamp to use.
   * @param data                  : An array of 4 FMSWLMeasurement(PREDICTION, OBSERVATION, FORECAST, EXT_STORM_SURGE
   *                              (which could be
   *                              NULL)) objects.
   * @return A new WLStationTimeNode object ready to be used.
   */
  @NotNull
  @Override
  public WLStationTimeNode getFMSTimeNode(final WLStationTimeNode pstrWLStationTimeNode,
                                          @NotNull final SecondsSinceEpoch secondsSinceEpoch,
                                          @NotNull @Size(min = 4) final FMSWLMeasurement[] data) {
    
    //final WLMeasurement [] data= new FMWLMeasurement[dbData.length] {new FMWLMeasurement(dbData[PREDICTION]), } ;
    //final FMWLMeasurement t = new FMWLMeasurement(dbData[PREDICTION].measurement());
    
    this.log.debug("LegacyResidual getFMSTimeNode: sse dt=" + secondsSinceEpoch.dateTimeString(true));
    this.log.debug("LegacyResidual getFMSTimeNode: pstr=" + pstrWLStationTimeNode);
    
    return new WLStationTimeNode(pstrWLStationTimeNode, secondsSinceEpoch, data);
  }
  
  /**
   * @return this sseStart.
   */
  @Min(0)
  public long getSseStart() {
    return this.sseStart;
  }
  
  /**
   * Validate this LegacyResidual setup before beginning computations.
   *
   * @return this LegacyResidual object.
   */
  @NotNull
  @Override
  public final LegacyResidual setupCheck() {
    
    this.log.debug("LegacyResidual setupCheck start !");
    
    this.resData.checkNumberCrunchingSize(this.covData.auxCovSize());
    
    this.log.debug("LegacyResidual setupCheck end !");
    
    return this;
  }
  
  /**
   * Do the setup of a LegacyResidual object.
   *
   * @param lastWLOSse                  : The time-stamp seconds of the last available valid WLO data for a given WL
   *                                    station
   * @param predictionsMeasurementsList : A list of WL predictions(tidal or climatology or a mix of both) Measurement
   *                                    objects.
   * @return Newly set this.sseStart.
   */
  @Override
  public long setup(@Min(0) final long lastWLOSse,
                    @NotNull @Size(min = 1) final List<MeasurementCustom> predictionsMeasurementsList) {
    
    this.log.debug("LegacyResidual setup Start: this.covData.auxCovSize()=" + this.covData.auxCovSize());
    
    this.initStats(predictionsMeasurementsList);
    
    //this.resData.resize(this.covData.auxCovSize());
    
    this.log.debug("LegacyResidual setup End");
    
    return this.setSseStart(lastWLOSse);
  }
  
  /**
   * Initialize residual errors statistics data structures.
   *
   * @param predictionMeasurementsList : A list of WL predictions(tidal or climatology or a mix of both) Measurement
   *                                   objects.
   * @return this LegacyResidual object.
   */
  protected LegacyResidual initStats(@NotNull @Size(min = 1) final List<MeasurementCustom> predictionMeasurementsList) {
    
    this.log.debug("LegacyResidual initStats Start");
    
    //--- Init this.resData.invXpX
    this.resData.invXpX.init(0.0);
    
    //--- In case wlpa.get(0).getUncertainty()== null;
    double errDenom = PREDICTIONS_ERROR_ESTIMATE_METERS;
    
    if (predictionMeasurementsList.get(0).getUncertainty() != null) {
      errDenom = predictionMeasurementsList.get(0).getUncertainty().doubleValue();
    }
    
    //--- Check for an error value near 0.0 with an epsilon here ?
    final double squWlpErrInv = 1.0 / ScalarOps.square(errDenom);
    
    //--- Need to put 1.0/(wlp.error()*wlp.error()) in the diagonal for this.resdata.invXpX
    for (int d = 0; d < this.resData.invXpX.ncols(); d++) {
      this.resData.invXpX.put(d, d, squWlpErrInv);
    }
    
    this.log.debug("LegacyResidual initStats End:  this.resData.invXpX=" + this.resData.invXpX.toString());
    
    return this;
  }
  
  /**
   * Set the time stamp since the epoch for the beginning of residual errors statistics computations.
   *
   * @param lastWLOSse : The last valid WLO of the WL station related to this LegacyResidual object.
   * @return Newly set this.sseStart.
   */
  protected long setSseStart(@Min(0) final long lastWLOSse) {
    return (this.sseStart = lastWLOSse - (long) this.resData.tau);
  }
  
  /**
   * Update the cumulative residual errors statistics with new WLO information(if available)
   * at a new time stamp for a given WL station.
   *
   * @param wlStationTimeNode : A WLStationTimeNode object.
   * @return The WLStationTimeNode object.
   */
  @NotNull
  @Override
  public WLStationTimeNode update(@NotNull final WLStationTimeNode wlStationTimeNode) {
    
    final LegacyResidualData resData = this.resData;
    final LegacyFMSCov covData = (LegacyFMSCov) this.covData;
    
    this.log.debug("LegacyResidual update: wlstn=" + wlStationTimeNode);
    this.log.debug("LegacyResidual update: wlstn dt=" + wlStationTimeNode.getSse().dateTimeString(true));
    this.log.debug("LegacyResidual update: rd.tau=" + resData.tau);
    this.log.debug("LegacyResidual update: rd.dt=" + resData.dt);
    this.log.debug("LegacyResidual update: longTermSurge=" + this.longTermSurge);
    
    //--- Always need to do time scaling:
    double e2 = resData.dataTimeScaling();
    
    this.log.debug("LegacyResidual update: e2=" + e2);
    
    //--- Method cd.getAxSurgesInZX(node.seconds(),rd.zX) returns false if a covariance surge data is missing.
    //    The retreived covariance surge data(if any) is returned in vector rd.zX
    if (!covData.getValidAxSurgesInZX(wlStationTimeNode.seconds(), resData.zX)) {
      
      this.log.warn("!cd.getValidAxSurgesInZX(node.seconds(),rd.zX) at" + wlStationTimeNode.getSse().dateTimeString(true) + ", need to use WLF surge.");
      
      this.log.debug("LegacyResidual update: wlstn.getWlfSurge()=" + wlStationTimeNode.getWlfSurge());
      this.log.debug("LegacyResidual update: wlstn.get(WLType.FORECAST)=" + wlStationTimeNode.get(WLType.FORECAST));
      
      //--- The direct surge is then computed with the FORECAST and its error is also the FORECAST error:
      wlStationTimeNode.setSurge(wlStationTimeNode.getWlfSurge(),
          wlStationTimeNode.get(WLType.FORECAST).getDoubleZError());
      
    } else {
      
      this.log.debug("LegacyResidual update: wlstn.get(WLType.OBSERVATION).getDoubleZValue()=" + wlStationTimeNode.get(WLType.OBSERVATION).getDoubleZValue());
      this.log.debug("LegacyResidual update: wlstn.get(WLType.PREDICTION).getDoubleZValue()=" + wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue());
      
      //--- We can update estimated surge statistics:
      
      //--- Direct WL surge computed with the OBSERVATION and the PREDICTION:
      final double rawSurge =
          wlStationTimeNode.get(WLType.OBSERVATION).getDoubleZValue() - wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue();
      
      final double remnantValue = (this.tidalRemnantRf == null ? 0.0 : this.tidalRemnantRf.getZw());
      
      this.log.debug("LegacyResidual update: rawSurge=" + rawSurge);
      this.log.debug("LegacyResidual update: remnantValue=" + remnantValue);
      
      //--- Remove tidal remnant(which could be zero for station without significant tidal influence)
      //    from the raw surge before computing stats:
      final double zY = rawSurge - remnantValue;
      
      this.log.debug("LegacyResidual update: zY=" + zY);
      
      //--- Update OLS regression parameters(cd.beta vector)
      resData.OLSRegression(zY, covData.beta);
      
      this.log.debug("LegacyResidual update: cd.beta=" + covData.beta.toString());
      this.log.debug("LegacyResidual update: rd.zX=" + resData.zX);
      this.log.debug("LegacyResidual update: estimated surge=" + covData.beta.dotProd(resData.zX));
      
      //double errorEstimate= cd.beta.dotProd(rd.zX);
      
      //--- Update local weighted sum of unaccounted WL surge variance with new error.
      //    NOTE: cd.beta.dotProd(rd.zX) is the surge "modelled_value"(a.k.a forecast-in-the-past))
      //          variable in the update_model function of the legacy 1990 DVFM C source code kit.
      e2 += ScalarOps.square(zY - covData.beta.dotProd(resData.zX));
      
      this.log.debug("LegacyResidual update: e2=" + e2);
      
      //--- Update sum of weights and sum of squares of weights.
      resData.omega += 1.0;
      resData.omega2 += 1.0;
      
      //--- New error epsilon
      resData.eps = Math.sqrt(e2 / resData.omega2);
      
      this.log.debug("LegacyResidual update: rd.eps=" + resData.eps);
      
      //--- Store the newly computed direct surge component(zY) and its error==0.0(for now) in the current node for
      // possible subsequent usage:
      wlStationTimeNode.setSurge(zY, 0.0);
    }
    
    //--- Change this.lastUpdateSse to the newly updated node SecondsSinceEpoch Object:
    this.lastUpdateSse = wlStationTimeNode.getSse();
    
    //this.cnt++;
    
    this.log.debug("LegacyResidual update: end"); //cnt="+cnt);
    
    return wlStationTimeNode;
  }
  
  /**
   * Dummy(a.k.a. "stub" in HPC jargon) method (but it is not dummy for the TidalRemnantResidual child class).
   *
   * @return this LegacyResidual object as a ILegacyFMSResidual.
   */
  @Override
  public ILegacyFMSResidual updateAlphaParameters() {
    
    //--- Nothing to do here, just a placeholder.
    return this;
  }
}
