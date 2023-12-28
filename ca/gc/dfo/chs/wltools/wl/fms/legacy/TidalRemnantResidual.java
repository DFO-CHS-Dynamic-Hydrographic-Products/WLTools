//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.fms.FMSWLMeasurement;
import ca.gc.dfo.chs.wltools.wl.fms.FMSResidualConfig;
import ca.gc.dfo.chs.wltools.numbercrunching.ScalarOps;
import ca.gc.dfo.chs.wltools.wl.fms.legacy.LegacyResidual;
import ca.gc.dfo.chs.wltools.wl.fms.FMSTidalRemnantConfig;

//import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSWLMeasurement;
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.ScalarOps;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLMeasurement;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.modeling.fms.Residual;
//import ca.gc.dfo.iwls.modeling.fms.TidalRemnant;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---
//---
//---
//---

/**
 * class TidalRemnantResidual used for tidal remnant WL component.
 */
final public class TidalRemnantResidual extends LegacyResidual implements ILegacyFMSResidual {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.fms.legacy.TidalRemnantResidual; ";

  /**
   * log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * The time stamp used to start the tidal remnant computations.
   */
  private long trSseStart= 0L;

  /**
   * Tidal remnant data storage.
   */
  private TidalRemnantData trData;

  /**
   * @param residualCfg      : Residual object obtained from a WL station ForecastingContext object.
   * @param tidalRemnantCfg  : TidalRemnant object obtained from the same(its a MUST) WL station ForecastingContext
   *                         object.
   * @param timIncrDtMinutes : Time increment in minutes used for the tidal remnant computations.
   * @param stationId        : The WL station usual SINECO string ID.
   */
  public TidalRemnantResidual(/*@NotNull*/ final FMSResidualConfig residualCfg,
                              /*@NotNull*/ final FMSTidalRemnantConfig tidalRemnantCfg,
                              /*@Min(1)*/  final Double timIncrDtMinutes, /*@NotNull*/ final String stationId) {

    super(residualCfg, stationId);

    final String mmi= "TidalRemnantResidual: ";

    slog.debug(mmi+"start");

    this.trData= new TidalRemnantData(tidalRemnantCfg, timIncrDtMinutes.doubleValue());

    slog.debug(mmi+"end");
  }

  /**
   * Estimate(in fact compute, estimate is used for the polymorphic OO aspect) tidal remnant component at
   * a new time stamp for a given WL station.
   *
   * @param wlStationTimeNode : A WLStationTimeNode object.
   * @param apply             : Dummy(not used) boolean to respect the inheritance scheme.
   * @return The WLStationTimeNode object.
   */
  //@NotNull
  //@Override
  public final WLStationTimeNode estimate(/*@NotNull*/ final WLStationTimeNode wlStationTimeNode, final boolean apply) {

    final String mmi= "estimate: ";

    slog.debug(mmi+"start, apply=" + apply);

    //--- Cast WLStationTimeNode in a TidalRemnantDVFMNode Object reference
    //    to be able to access specific TidalRemnantNode attributes
    final TidalRemnantNode trn = (TidalRemnantNode) wlStationTimeNode;

    final long nts= trn.seconds(); //- 900;

    if (nts < this.lastUpdateSse.seconds()) {

      slog.error(mmi+"nts<this.lastUpdateSse.seconds() ! nts must be larger or equal to this.lastUpdateSse.seconds()");
      throw new RuntimeException(mmi);
    }

    final double timeOffset= (double) (nts - this.lastUpdateSse.seconds());

    slog.debug(mmi+"timeOffset=" + timeOffset);

    final TidalRemnantData trd= this.trData;

//        this.log.debug("TidalRemnantResidual estimate: trd.tau="+ trd.tau);
//        this.log.debug("TidalRemnantResidual estimate: trd.dt="+ trd.dt);
    //logger.debug("trNode.seconds() > this.lastUpdateSse.seconds() : Applying estimated tidal
    // remnant correction.");

    //--- Populate trd.zX for tidal remnant:
    trd.zXSetup(wlStationTimeNode);

    //--- Get the estimated tidal remnant for this node
    //    G. Mercier FMS 2018 modification:
    //    No time decay factor for remnant itself but we apply an increasing time factor for its error.
    trn.getRemnant().
      set(trd.computeRemnant(), Math.sqrt(1.0 - trd.getErrorWeight(timeOffset * trd.tauInv)) * trd.eps);

    slog.debug(mmi+"trn.remnant.getZw()=" + trn.remnant.getZw());
    slog.debug(mmi+"trn.remnant.getError()=" + trn.remnant.getError());

    //--- Set this.tidalRemnantRf reference for super class usage:
    this.tidalRemnantRf= trn.remnant;

    //--- MUST use super class estimate !
    super.estimate(wlStationTimeNode, apply);

    slog.debug(mmi+"end");

    return wlStationTimeNode;
  }

  /**
   * @param pstrWLStationTimeNode : A WLStationTimeNode just before in time(null for the 1st time iteration)
   * @param secondsSinceEpoch     : A SecondsSinceEpoch with the next(in the future) time stamp to use.
   * @param data                  : An array of 4 FMSWLMeasurement(PREDICTION, OBSERVATION, FORECAST, EXT_STORM_SURGE
   *                              (which could be
   *                              NULL)) objects.
   * @return A new WLStationTimeNode object ready to be used.
   */
  //@NotNull
  //@Override
  public final WLStationTimeNode getFMSTimeNode(final WLStationTimeNode pstrWLStationTimeNode,
                                                /*@NotNull*/ final SecondsSinceEpoch secondsSinceEpoch,
                                                /*@NotNull @Size(min = 4)*/ final FMSWLMeasurement[] data) {
    final String mmi= "getFMSTimeNode: ";

    slog.debug(mmi+"sse dt=" + secondsSinceEpoch.dateTimeString(true));
    slog.debug(mmi+"pstr=" + pstrWLStationTimeNode);

    return new TidalRemnantNode((TidalRemnantNode) pstrWLStationTimeNode, secondsSinceEpoch, data);
  }

  /**
   * @return this.trSseStart.
   */
  //@Min(0)
  //@Override
  public final long getSseStart() {
    return this.trSseStart;
  }

  /**
   * @param lastWLOSse                  : Last valid WLO time stamp in seconds since the epoch .
   * @param predictionsMeasurementsList : : A list a WL predictions(tidal or climato OR a mix of both) data.
   * @return this.trSseStart after being set.
   */
  @Override
  public final long setup(/*@Min(0)*/ final long lastWLOSse,
                          /*@NotNull @Size(min = 1)*/ final List<MeasurementCustom> predictionsMeasurementsList) {

    final String mmi= "setup: ";

    final long superSseStart= super.setup(lastWLOSse, predictionsMeasurementsList);

    slog.debug(mmi+"superSseStart dt=" + SecondsSinceEpoch.dtFmtString(superSseStart));

    this.initStats(predictionsMeasurementsList);

    slog.debug(mmi+"end");

    return this.setSseStart(superSseStart);
  }

  /**
   * @param predictionMeasurementsList : A list a WL predictions(tidal or climato OR a mix of both) data.
   * @return this TidalRemnantResidual object(as a LegacyResidual)
   */
  //@NotNull
  @Override
  protected final LegacyResidual initStats(/*@NotNull*/ final List<MeasurementCustom> predictionMeasurementsList) {

    super.initStats(predictionMeasurementsList);

    final String mmi= "initStats: ";

    slog.debug(mmi+"start");

    this.trData.initInvXpx(predictionMeasurementsList);

    slog.debug(mmi+"end");

    return this;
  }

  /**
   * @param superSseStart : A time stamp in seconds since the epoch coming from the super class LegacyResidual.
   * @return this.trSseStart after being set.
   */
  @Override
  protected final long setSseStart(/*@Min(0)*/ final long superSseStart) {

    //--- Subtract this.trData.tau from this.sseStart which have just been computed in super.setSseStart();
    return (this.trSseStart = (superSseStart - (long) this.trData.tau));
  }

  /**
   * Update(in fact compute, update is used for the polymorphic OO aspect) tidal remnant ocmponent at a
   * new time stamp for a given WL station.
   *
   * @param wlStationTimeNode : A WLStationTimeNode object.
   * @return The WLStationTimeNode object.
   */
  //@NotNull
  @Override
  public final WLStationTimeNode update(/*@NotNull*/ final WLStationTimeNode wlStationTimeNode) {

    final String mmi= "update: ";

    //--- NOTE: this method is the Equivalent of function update_remnant
    //          from source file remnant.c of legacy DVFM source code.

    //--- Handy shorcut reference to this.trData TidalRemnantData Object:
    final TidalRemnantData trd= this.trData;

    slog.debug(mmi+"wlstn=" + wlStationTimeNode);
    slog.debug(mmi+"wlstn dt=" + wlStationTimeNode.getSse().dateTimeString(true));
    slog.debug(mmi+"trd.tau=" + trd.tau);
    slog.debug(mmi+"trd.dt=" + trd.dt);

    //--- WLStationTimeNode prediction WL value:
    final double wlpZw= wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue();

    //--- NOTE: Here the legacy DVFM code is doing a time-derivative with an WLP zw value
    //          interpolated just 1 second before the current WLP time and mulitply the
    //          result by 60 seconds. This gives a time-derivative that is 60 times larger
    //          than is normally expected by the orignal forecast theory developed by Smith&Thompson.
    trd.zXSetup(wlStationTimeNode);

    //--- Shortcut for WLO DB data:
    //    (NOTE: wloDb MUST be not null here)
    final WLMeasurement wloDb= wlStationTimeNode.get(WLType.OBSERVATION);

    //--- Always have to do time scaling:
    double er= trd.dataTimeScaling();

    //---- Get the raw residual(WLO - WLP) for this node:
    //     NOTE: zY is the equivalent of local variable Y in the function update_remnant from
    //     source file remnant.c of legacy DVFM C source code.
    double zY= wloDb.getDoubleZValue() - wlpZw;

    trd.k2 *= trd.alpha;
    trd.k2 += (1.0 - trd.alpha) * zY;

    zY -= trd.k2;

    //--- Update OLS regression parameters in tr.beta
    trd.OLSRegression(zY, trd.beta);

    slog.debug(mmi+"trd.beta=" + trd.beta);

    final double beta0= trd.beta.at(0);
    final double beta1= trd.beta.at(1);

    trd.eps1= trd.eps1Max * Math.signum(beta0) * (1.0 - Math.exp(-Math.abs(beta0) * trd.eps1MaxInv));
    trd.eps2= trd.eps2Max * Math.signum(beta1) * (1.0 - Math.exp(-Math.abs(beta1) * trd.eps2MaxInv));

    trd.k1= trd.eps2 * trd.beta.at(2) / beta1;

    slog.debug(mmi+"trd.eps1=" + trd.eps1);
    slog.debug(mmi+"trd.eps2=" + trd.eps2);
    slog.debug(mmi+"trd.k1=" + trd.k1);

    //--- Cast wlstn in a TidalRemnantNode Object reference
    //    to be able to access specific TidalRemnantNode attributes
    final TidalRemnantNode trn= (TidalRemnantNode) wlStationTimeNode;

    //--- Compute and set the tidal remnant for this TidalRemnantNode Object
    final double newRemnantValue= trd.computeRemnant();

    trn.remnant.setZw(newRemnantValue);

    slog.debug(mmi+"newRemnantValue=" + newRemnantValue);

    //--- Sum of squares of remnants:
    er += ScalarOps.square(newRemnantValue);

    //--- Update sum of weights and sum of squares of weights.
    trd.omega += 1.0;
    trd.omega2 += 1.0;

    //--- New remnant (epsilon) uncertainty
    trd.eps= Math.sqrt(er / trd.omega2);

    //--- Set this.tidalRemnantRf reference before using super.update(node)
    this.tidalRemnantRf= trn.remnant; //.zw();

    //--- Continue with super class(LegacyResidual) update
    //    But only if wlstn.seconds() >= super.sseStart
    if (wlStationTimeNode.seconds() >= super.getSseStart()) {
      super.update(wlStationTimeNode);
      //super.update(wlstn,longTermSurge);
    }

    return wlStationTimeNode;
  }

  /**
   * @return this TidalRemnantResidual object(as a ILegacyFMSResidual).
   */
  @Override
  public final ILegacyFMSResidual updateAlphaParameters() {

    //this.log.debug("updateAlphaParameters start.");

    //--- Handy shorcut reference to this.trData TidalRemnantData Object:
    final TidalRemnantData trd= this.trData;

    //this.log.debug("updateAlphaParameters start: trd.alpha="+trd.alpha);

    trd.eps1 *= trd.alpha;
    trd.eps2 *= trd.alpha;
    trd.k1 *= trd.alpha;

    //this.log.debug("updateAlphaParameters end: trd.eps1="+trd.eps1+", trd.eps2="+trd.eps2+", trd.k1="+trd.k1);

    return this;
  }
}
