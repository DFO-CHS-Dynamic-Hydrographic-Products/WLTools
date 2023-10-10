//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.wl.WLZE;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.wl.fms.FMSWLMeasurement;
import ca.gc.dfo.chs.wltools.wl.fms.FMSResidualConfig;
import ca.gc.dfo.chs.wltools.numbercrunching.ScalarOps;
import ca.gc.dfo.chs.wltools.wl.fms.FMSStationCovarianceConfig;

/**
 *
 */

//---
//---
//---

/**
 * Class used to compute and estimate the WLP residuals errors for a given WL station with the(slightly modified)
 * legacy DVFM algorithm.
 * (NOTE: this class is inherited by the TidalRemnantResidual class).
 */
public class LegacyResidual extends LegacyFMSResidualFactory implements ILegacyFMSResidual, IWL {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.fms.legacy.LegacyResidual: ";

  /**
   * log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Normally only used as a reference when we have to consider a tidal remnant component. It is null for WL stations
   * having no significant tidal influence.
   */
  protected WLZE tidalRemnantRf= null;

  /**
   * To store the WL residual component related data structures(a.k.a objects).
   */
  private LegacyResidualData resData= null;

  /**
   * Estimated WL surge WITHOUT tidal remnant.
   */
  private WLZE estimatedSurge= null;

  /**
   * The time stamp(in the past) in seconds since the epoch used for the beginning of the residuals errors computations
   */
  private long sseStart= 0L;

  /**
   * @param residualCfg : FMSResidualConfig object
   * @param stationId : The CHS TG station string id.
   */
  public LegacyResidual(/*@NotNull*/ final FMSResidualConfig residualCfg, /*@NotNull*/ final String stationId) {

    this(residualCfg, stationId, residualCfg.getDeltaTMinutes()) ; //.doubleValue());
  }

  /**
   * @param residualCfg     : FMSResidualConfig object obtained from a WL station ForecastingContext object.
   * @param stationId       : The CHS TG station string id
   * @param timeIncrDtMinutes :
   */
  public LegacyResidual(/*@NotNull*/ final FMSResidualConfig residualCfg,
                        /*@NotNull*/ final String stationId, /*@Min(1)*/ final double timeIncrDtMinutes) {
    super(stationId);

    final String mmi= "LegacyResidual constructor: ";

    slog.info(mmi+"start for station: " + stationId);

    if (residualCfg == null) {

      slog.error(mmi+"residualCfg==null ! for station:" + stationId);
      throw new RuntimeException(mmi);
    }

    final List<FMSStationCovarianceConfig>
      stationsCovarianceCfgList= residualCfg.getFMSStationCovarianceConfigList();

    if (stationsCovarianceCfgList == null) {

      slog.error(mmi+"stationsCovarianceCfgList == null ! for station: " + stationId);
      throw new RuntimeException(mmi);
    }

    if (stationsCovarianceCfgList.size() == 0) {

      slog.error(mmi+"stationsCovarianceList.size() <= 0 ! for station: " + stationId);
      throw new RuntimeException(mmi);
    }

    //if (residualCfg.getTauHours() == null) {
    //  slog.error("LegacyResidual constructor: residualCfg.getTauHours()==null ! for station:" + stationCode);
    //  throw new RuntimeException("LegacyResidual constructor");
    //}

//        if (residualCfg.getTauHours()!=RetrieveWlObservationsHandler.NB_HOURS_OF_OBSERVATIONS) {
//        }

    this.tidalRemnantRf= null;
    this.estimatedSurge= new WLZE(0.0, 0.0);

    slog.info(mmi+"residualCfg=" + residualCfg.toString());

    this.covData= new
      LegacyFMSCov(residualCfg.getFallBackError(), stationId, stationsCovarianceCfgList);

    final int nbAuxCov= this.covData.auxCovSize();

    this.resData= new
      LegacyResidualData(nbAuxCov, residualCfg.getTauHours(), timeIncrDtMinutes);

    slog.info(mmi+"end for station: " + stationId);
  }

  /**
   * @param wlStationTimeNode : A WLStationTimeNode object.
   * @param apply             : boolean to signal that the estimated WL residual errors are to be applied to get a
   *                          new forecast
   *                          (i.e. the time stamp of the WLStationTimeNode is in the future).
   * @return The WLStationTimeNode object.
   */
  //@NotNull
  @Override
  public WLStationTimeNode estimate(/*@NotNull*/ final WLStationTimeNode wlStationTimeNode, final boolean apply) {

    final String mmi= "estimate: ";

    slog.info(mmi+"start, wlstn="+wlStationTimeNode+
              ", apply=" + apply + ", stationId" +"=" + this.stationId);

    //--- Uncomment the following block for debugging purposes.
//        if (!wlstn.checkTimeSync()) {
//            this.log.debug("LegacyFMCov getAxSurgesErrsInZX: wltsn.checkTimeSync()== false");
//        }
    //--- NOTE: This method is similarly used for both past(when we have
    //          a missing WLO) and future time-stamps

    final LegacyResidualData resData= this.resData;
    final LegacyFMSCov covData= (LegacyFMSCov) this.covData;

//        this.log.debug("LegacyResidual estimate: rd.tau="+rd.tau);
//        this.log.debug("LegacyResidual estimate: rd.dt="+rd.dt);

    final long nodeTimeStamp= wlStationTimeNode.seconds();

    //--- WARNING: The 3 following debugging logs calls can produce null objects exceptions
    slog.info(mmi+"wlstn.sse dt=" + wlStationTimeNode.getSse().dateTimeString(true) +
              ", this.lastUpdateSse dt=" + this.lastUpdateSse.dateTimeString(true));

    slog.info(mmi+"wlstn.get(WLType.PREDICTION).zDValue()=" +
              wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue());

    slog.info(mmi+"wlstn.get(WLType.PREDICTION).seconds dt=" +
        SecondsSinceEpoch.dtFmtString(wlStationTimeNode.get(WLType.PREDICTION).seconds(), true));

    if (wlStationTimeNode.getPstr() != null) {
      slog.info(mmi+"wlstn.pstr()=" + wlStationTimeNode.getPstr());
      slog.info(mmi+"wlstn.pstr().sse dt=" + wlStationTimeNode.getPstr().getSse().dateTimeString(true));
    }

    //--- Retreive previously computed direct surge(s) and the associated error(s) from the auxiliary temporal errors
    // covariances data:
    covData.getAxSurgesErrsInZX(nodeTimeStamp, resData.zX, resData.beta);

    //--- Quite unlikely to happen but we never know.
    if (nodeTimeStamp < this.lastUpdateSse.seconds()) {
      slog.info(mmi+"nodeTimeStamp < this.lastUpdateSse.seconds() !!, nodeTimeStamp must be larger or equal to this.lastUpdateSse.seconds()");
      throw new RuntimeException(mmi);
    }

    //--- Get the time offset compared with the last time stamp of the last residual object update:
    //    NOTE: Could be removed by putting nodeTimeStamp - this.lastUpdateSse.seconds() directly
    //          in timeFactor computation but it is kept ther to report its value in the info logs.
    final long timeOffset= nodeTimeStamp - this.lastUpdateSse.seconds();

    slog.info(mmi+"this.longTermOffsetFactor=" + this.longTermOffsetFactor);

    //---
    final double timeFactor = (double) (timeOffset) * resData.tauInv;

    //final double timeFactor= (double) (nts - this.lastUpdateSse.seconds()) * rd.tauInv;
    //TODO: Test if we can use the forecast duration here as the denominator for the future time ratio
    //final double timeFactor= (double)(nts - this.lastUpdateSse.seconds())/forecastDuration;

    final double surgeWeight= resData.getValueWeight(timeFactor);
    final double errorWeight= resData.getErrorWeight(timeFactor);

    //--- Keep the last surge computed at the previous time-stamp in a local variable
    //    for a possible local usage:
    final double lastEstimatedSurge = resData.zX.at(0);

    slog.info(mmi+"rd.alpha=" + resData.alpha);
    slog.info(mmi+"rd.tauInv=" + resData.tauInv);
    slog.info(mmi+"timeOffset=" + timeOffset);
    slog.info(mmi+"timeFactor=" + timeFactor);

    //this.log.debug("LegacyResidual estimate: timeFactor=" + (double)(nts - this.lastUpdateSse.seconds())
    // /forecastDuration);
    slog.info(mmi+"surgeWeight=" + surgeWeight);
    slog.info(mmi+"errorWeight=" + errorWeight);

    //--- Compute the WL estimated surge and its associated error:
    //    NOTE: computeEstimatedSurge returns the "short term" surge(a.k.a. storm and-or outflow surge)
    covData.computeEstimatedSurge(surgeWeight, errorWeight, resData.eps,
                                  resData.zX, resData.beta, this.estimatedSurge);

    final double estimatedShortTermSurgeZw= this.estimatedSurge.getZw();
    final double estimatedSurgeError= this.estimatedSurge.getError();

    //--- G. Mercier FMS 2018 modification:
    //    Add the WL "long term" offset to the "short term" surge.
    double estimatedSurge=
      estimatedShortTermSurgeZw + this.longTermOffset; //this.longTermSurge;

    double remnantValue= 0.0;
    double remnantError= 0.0;

    //--- Check if we have a tidal remnant to use:
    if (this.tidalRemnantRf != null) {

      remnantValue= this.tidalRemnantRf.getZw();
      remnantError= this.tidalRemnantRf.getError();

      //--- G. Mercier FMS 2018 modification:
      //    Now let the "long term" surge to decay more slowly than the "short term" surge
      //    in cases where we have a tidal remnant signal to use:
      this.longTermOffset *= Math.exp(-timeFactor * this.longTermOffsetFactor);
    }

    slog.info(mmi+"this.longTermOffset=" + this.longTermOffset);
    slog.info(mmi+"estimatedShortTermSurgeZw=" + estimatedShortTermSurgeZw);
    slog.info(mmi+"1st estimatedSurge=" + estimatedSurge);
    slog.info(mmi+"lastEstimatedSurge=" + lastEstimatedSurge);
    slog.info(mmi+"estimatedSurgeError=" + estimatedSurgeError);
    slog.info(mmi+"remnantValue=" + remnantValue);
    slog.info(mmi+"remnantError=" + remnantError);

    //--- G. Mercier FMS 2018 modification:
    //    Now take care of the "short-term surge" (a.k.a. storm and-or outflow surge) effect:
    final double estimatedShortTermSurgeZwAbsVal= Math.abs(estimatedShortTermSurgeZw);

    //--- G. Mercier FMS 2018 modification:
    //    Last check on estimatedSurge: reduce surge values decay if estimatedShortTermSurgeZw
    //    is less(in terms of absolute values) than this.longTermSurge. Use the last estimated
    //    surge computed to deal with the slowly evolving total surge in both cases.
    if (estimatedShortTermSurgeZwAbsVal < Math.abs(this.longTermOffset)) {

      slog.info(mmi+"Using last estimated surge as the newly estimated surge.");

      estimatedSurge = lastEstimatedSurge;

    } else {

      slog.info(mmi+"Setting the newly estimated surge as the mean between it and the last estimated surge.");

      //--- Estimated short term surge >= long term surge: keep the last estimated surge information by
      //    setting the newly estimated surge as the mean betweem its newly computed value and the last estimated surge:
      estimatedSurge = (estimatedSurge + lastEstimatedSurge) / 2.0;
    }

    slog.info(mmi+"final estimatedSurge=" + estimatedSurge);

    //--- Total estimated surge for this time-stamp:
    final double estimatedTotalSurgeZw= estimatedSurge + remnantValue;

    //--- G. Mercier FMS 2018 modification:
    //    The legacy DVFM code is applying another square operation to the estimatedSurgeError here even if all its sum items
    //    have been already subjected to a square operation. It gives more realistic errors if we take the estimatedSurgeError
    //    as it is.
    //    NOTE: estimatedSurgeError is always >= 0.0 see computeEstimatedSurge method in source file LegacyFMCov for
    //    details:
    final double estimatedTotalSurgeError=
      Math.sqrt(estimatedSurgeError + ScalarOps.square(remnantError));

    slog.info(mmi+"estimatedTotalSurgeZw=" + estimatedTotalSurgeZw);
    slog.info(mmi+"estimatedTotalSurgeError=" + estimatedTotalSurgeError);

    //--- Apply estimated WL surge to get the full forecasted WL:
    if (apply) {

      slog.info(mmi+"Applying estimated surge to get the new forecasted water level at: "+
                SecondsSinceEpoch.dtFmtString(nodeTimeStamp, true) + ",  for station: " + this.stationId);

      //--- Set the new computed forecast by applying the estimated surge:
      final double updatedForecast= wlStationTimeNode.
        get(WLType.PREDICTION).getDoubleZValue() + estimatedTotalSurgeZw;

      wlStationTimeNode.
        setUpdatedforecast(wlStationTimeNode.get(WLType.PREDICTION).getInstant(), updatedForecast, estimatedTotalSurgeError);

      slog.info(mmi+"wlstn.get(WLType.PREDICTION).zDValue()=" + wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue());
      slog.info(mmi+"wlstn.getUpdatedForecast().getValue()=" + wlStationTimeNode.getUpdatedForecast().getValue());

      slog.info(mmi+"Debug exit 0");
      System.exit(0);
    }

    //--- Need to store the estimated surge( estimatedShortTermSurgeZw + this.longTermSurge WITHOUT tidal remnant)
    //    in the time node for possible subsequent usage:
    wlStationTimeNode.setSurge(estimatedSurge, estimatedSurgeError);

    slog.info(mmi+"end");

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
  //@NotNull
  @Override
  public WLStationTimeNode getFMSTimeNode(final WLStationTimeNode pstrWLStationTimeNode,
                                          /*@NotNull*/ final SecondsSinceEpoch secondsSinceEpoch,
                                          /*@NotNull @Size(min = 4*/ final FMSWLMeasurement[] data) {
    final String mmi= "getFMSTimeNode: ";

    //final WLMeasurement [] data= new FMWLMeasurement[dbData.length] {new FMWLMeasurement(dbData[PREDICTION]), } ;
    //final FMWLMeasurement t = new FMWLMeasurement(dbData[PREDICTION].measurement());

    slog.info(mmi+"sse dt=" + secondsSinceEpoch.dateTimeString(true));
    slog.info(mmi+"pstr=" + pstrWLStationTimeNode);

    return new WLStationTimeNode(pstrWLStationTimeNode, secondsSinceEpoch, data);
  }

  /**
   * @return this sseStart.
   */
  //@Min(0)
  public long getSseStart() {
    return this.sseStart;
  }

  /**
   * Validate this LegacyResidual setup before beginning computations.
   *
   * @return this LegacyResidual object.
   */
  //@NotNull
  @Override
  public final LegacyResidual setupCheck() {

    final String mmi= "setupCheck: ";

    slog.info(mmi+"start !");

    this.resData.checkNumberCrunchingSize(this.covData.auxCovSize());

    slog.info(mmi+"end");

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
  public long setup(/*@Min(0)*/ final long lastWLOSse,
                    /*@NotNull @Size(min = 1)*/ final List<MeasurementCustom> predictionsMeasurementsList) {

    final String mmi= "setup: ";

    slog.info(mmi+"start: this.covData.auxCovSize()=" + this.covData.auxCovSize());

    this.initStats(predictionsMeasurementsList);

    //this.resData.resize(this.covData.auxCovSize());

    slog.info(mmi+"end");

    return this.setSseStart(lastWLOSse);
  }

  /**
   * Initialize residual errors statistics data structures.
   *
   * @param predictionMeasurementsList : A list of WL predictions(tidal or climatology or a mix of both) Measurement
   *                                   objects.
   * @return this LegacyResidual object.
   */
  protected LegacyResidual initStats(/*@NotNull @Size(min = 1)*/
                                     final List<MeasurementCustom> predictionMeasurementsList) {

    final String mmi= "initStats: ";

    slog.info(mmi+"start");

    //--- Init this.resData.invXpX
    this.resData.invXpX.init(0.0);

    //--- In case wlpa.get(0).getUncertainty()== null;
    double errDenom= PREDICTIONS_ERROR_ESTIMATE_METERS;

    if (predictionMeasurementsList.get(0).getUncertainty() >= IWL.MINIMUM_UNCERTAINTY_METERS) {  //!= null) {
      errDenom= predictionMeasurementsList.get(0).getUncertainty(); // .doubleValue();
    }

    slog.info(mmi+"errDenom="+errDenom);

    //--- Check for an error value near 0.0 with an epsilon here ?
    final double squWlpErrInv= 1.0 / ScalarOps.square(errDenom);

    //--- Need to put 1.0/(wlp.error()*wlp.error()) in the diagonal for this.resdata.invXpX
    for (int d= 0; d< this.resData.invXpX.ncols(); d++) {
      this.resData.invXpX.put(d, d, squWlpErrInv);
    }

    slog.info(mmi+"this.resData.invXpX=" + this.resData.invXpX.toString());

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    return this;
  }

  /**
   * Set the time stamp since the epoch for the beginning of residual errors statistics computations.
   *
   * @param lastWLOSse : The last valid WLO of the WL station related to this LegacyResidual object.
   * @return Newly set this.sseStart.
   */
  protected long setSseStart(/*@Min(0)*/ final long lastWLOSse) {
    return (this.sseStart = lastWLOSse - (long) this.resData.tau);
  }

  /**
   * Update the cumulative residual errors statistics with new WLO information(if available)
   * at a new time stamp for a given WL station.
   *
   * @param wlStationTimeNode : A WLStationTimeNode object.
   * @return The WLStationTimeNode object.
   */
  //@NotNull
  @Override
  public WLStationTimeNode update(/*@NotNull*/ final WLStationTimeNode wlStationTimeNode) {

    final String mmi= "update: ";

    final LegacyResidualData resData= this.resData;
    final LegacyFMSCov covData= (LegacyFMSCov) this.covData;

    slog.info(mmi+"wlstn=" + wlStationTimeNode);
    slog.info(mmi+"wlstn dt=" + wlStationTimeNode.getSse().dateTimeString(true));
    slog.info(mmi+"rd.tau=" + resData.tau);
    slog.info(mmi+"rd.dt=" + resData.dt);
    slog.info(mmi+"longTermOffset=" + this.longTermOffset);

    //--- Always need to do time scaling:
    double e2 = resData.dataTimeScaling();

    slog.info(mmi+"e2=" + e2);

    //--- Method cd.getAxSurgesInZX(node.seconds(),rd.zX) returns false if a covariance surge data is missing.
    //    The retreived covariance surge data(if any) is returned in vector rd.zX
    if (!covData.getValidAxSurgesInZX(wlStationTimeNode.seconds(), resData.zX)) {

      slog.info(mmi+"!cd.getValidAxSurgesInZX(node.seconds(),rd.zX) at"+
                wlStationTimeNode.getSse().dateTimeString(true) + ", need to use WLF surge.");

      slog.info(mmi+"wlstn.getQCFSurge()=" + wlStationTimeNode.getQCFSurge());
      slog.info(mmi+"wlstn.get(WLType.QC_FORECAST)=" + wlStationTimeNode.get(WLType.QC_FORECAST));

      //--- The direct surge is then computed with the FORECAST and its error is also the FORECAST error:
      wlStationTimeNode.
        setSurge(wlStationTimeNode.getQCFSurge(),wlStationTimeNode.get(WLType.QC_FORECAST).getDoubleZError());

    } else {

      slog.info(mmi+"wlstn.get(WLType.OBSERVATION).getDoubleZValue()="+
                wlStationTimeNode.get(WLType.OBSERVATION).getDoubleZValue());

      slog.info(mmi+"wlstn.get(WLType.PREDICTION).getDoubleZValue()="+
                wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue());

      //--- We can update estimated surge statistics:
      //--- Direct WL surge computed with the OBSERVATION and the PREDICTION:
      final double rawSurge= wlStationTimeNode.
        get(WLType.OBSERVATION).getDoubleZValue() - wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue();

      final double remnantValue=
        (this.tidalRemnantRf == null ? 0.0 : this.tidalRemnantRf.getZw());

      slog.info(mmi+"rawSurge=" + rawSurge);
      slog.info(mmi+"remnantValue=" + remnantValue);

      //--- Remove tidal remnant(which could be zero for station without significant tidal influence)
      //    from the raw surge before computing stats:
      final double zY= rawSurge - remnantValue;

      slog.info(mmi+"zY=" + zY);

      //--- Update OLS regression parameters(cd.beta vector)
      resData.OLSRegression(zY, covData.beta);

      slog.info(mmi+"cd.beta=" + covData.beta.toString());
      slog.info(mmi+"rd.zX=" + resData.zX);
      slog.info(mmi+"estimated surge=" + covData.beta.dotProd(resData.zX));

      //double errorEstimate= cd.beta.dotProd(rd.zX);

      //--- Update local weighted sum of unaccounted WL surge variance with new error.
      //    NOTE: cd.beta.dotProd(rd.zX) is the surge "modelled_value"(a.k.a forecast-in-the-past))
      //          variable in the update_model function of the legacy 1990 DVFM C source code kit.
      e2 += ScalarOps.square(zY - covData.beta.dotProd(resData.zX));

      slog.info(mmi+"e2=" + e2);

      //--- Update sum of weights and sum of squares of weights.
      resData.omega += 1.0;
      resData.omega2 += 1.0;

      //--- New error epsilon
      resData.eps = Math.sqrt(e2 / resData.omega2);

      slog.info(mmi+"rd.eps=" + resData.eps);

      //--- Store the newly computed direct surge component(zY) and its error==0.0(for now) in the current node for
      // possible subsequent usage:
      wlStationTimeNode.setSurge(zY, 0.0);
    }

    //--- Change this.lastUpdateSse to the newly updated node SecondsSinceEpoch Object:
    this.lastUpdateSse= wlStationTimeNode.getSse();

    //this.cnt++;

    slog.info(mmi+"end"); //cnt="+cnt);

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
