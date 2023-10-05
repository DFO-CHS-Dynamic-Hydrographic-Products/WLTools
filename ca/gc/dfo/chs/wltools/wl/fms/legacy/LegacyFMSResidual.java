//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

/**
 *
 */

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.FMSInput;
import ca.gc.dfo.chs.wltools.wl.fms.FMSConfig;
import ca.gc.dfo.chs.wltools.wl.fms.IFMSResidual;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.fms.FMSWLMeasurement;
import ca.gc.dfo.chs.wltools.wl.fms.FMSResidualConfig;
import ca.gc.dfo.chs.wltools.wl.fms.FMSResidualFactory;
import ca.gc.dfo.chs.wltools.wl.fms.FMSTidalRemnantConfig;
import ca.gc.dfo.chs.wltools.wl.fms.legacy.TidalRemnantResidual;
import ca.gc.dfo.chs.wltools.wl.fms.legacy.LegacyFMSResidualFactory;

//--
//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSResidualFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSWLMeasurement;
//import ca.gc.dfo.iwls.fmservice.modeling.fms.IFMSResidual;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.modeling.fms.FmsParameters;
//import ca.gc.dfo.iwls.modeling.fms.Forecast;
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
 * External entry point for Legacy errors residuals computations. It encapsulate the two types of Legacy methods
 * i.e. with or without a WL tidal remnant component.
 */
final public class LegacyFMSResidual implements IFMSResidual, ILegacyFMS {

  private static final String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.fms.legacy.LegacyFMSResidual";

  /**
   * static log utility.
   */
  private static final Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Generic Legacy errors residuals object(could be a LegacyResidual or a TidalRemnantResidual object)
   */
  protected LegacyFMSResidualFactory residual= null;

  //--- For possible future usage.
  //    public LegacyFMResidual() {
  //        super();
  //    }

  /**
   * Validate all the ForecastingContext configuration objects in the List forecastingContextsList.
   *
   * @param fmsConfigList : A List of FMSConfig objects.
   * @return true if all the FMSConfig objects are ok, false otherwise.
   */
  //public final static boolean validateFMSConfig(@NotNull @Size(min = 1) final List<ForecastingContext> forecastingContextsList) {
  public final static boolean validateFMSConfig(/*@NotNull @Size(min = 1)*/ final List<FMSInput> fmsInputList) {

    final String mmi= "validateFMSConfig; ";

    slog.info(mmi+"start");

    boolean ret= true;

    //final ForecastingContext fc0 = forecastingContextsList.get(0);
    final FMSConfig fc0= (FMSConfig)fmsInputList.get(0);

    final String referenceStationId= fc0.getStationId();

    //final FmsParameters fm0 = fc0.getFmsParameters();
    //final Forecast fs0 = fm0.getForecast();

    final FMSResidualConfig rs0= fc0.getFMSResidualConfig();

    FMSTidalRemnantConfig tr0= fc0.getFMSTidalRemnantConfig();

    //--- begin at item 1 to avoid comparing reference station with itself.
    //for (final ForecastingContext fc: forecastingContextsList.subList(1, forecastingContextsList.size())) {
    for (final FMSConfig fc: fmsInputList.subList(1, fmsInputList.size())) {

      final String stationId= fc.getStationId();

      //final FmsParameters fm = fc.getFmsParameters();
      //final Forecast fs = fm.getForecast();
      final FMSResidualConfig rs= fc.getFMSResidualConfig();

      //--- Check if we have a duplicate ForecastingContext
      if (stationId.equals(referenceStationId)) {

        slog.error(mmi+"Found a ForecastingContext duplicate for station: " + stationId);
        ret= false;
        break;
      }

      if (fc.getDeltaTMinutes() != fc0.getDeltaTMinutes()) {

        slog.error(mmi+"Must have the same deltaT minutes for all FMSCnnfig objects, station in error: " + stationId);
        ret= false;
        break;
      }

      //if (fc.getDurationHours() != fc0.getDurationHours()) {
      //  slog.error(mmi+"Must have the same Forecast duration hours for all FMSCnnfig objects, station in error: " + stationId);
      //  ret= false;
      //  break;
      //}

//            if (!rs.getMethod().equals(rs0.getMethod())) {
//                this.log.error("Must have the same Residual method for all ForecastingContexts, station in error:
//                "+stId);
//                ret= false;
//                break;
//            }

      if (rs.getTauHours() != rs0.getTauHours()) {

        slog.error(mmi+"Must have the same Residual tau hours for all FMSCnnfig objects, station in error: " + stationId);
        ret= false;
        break;
      }

      if (rs.getDeltaTMinutes() != rs0.getDeltaTMinutes()) {

        slog.error(mmi+"Must have the same Residual deltaT minutes for all ForecastingContexts, station in error: " + stationId);
        ret= false;
        break;
      }
    }

    //--- Validate TidalRemnant parameters(if any)
    //     (tr0 could be null at this point)
    if (tr0 == null) {

      //--- Try to find a non-null TidalRemnant Object:
      //for (final ForecastingContext fc : forecastingContextsList) {
      for (final FMSConfig fc : fmsInputList) {

        if ((tr0= fc.getFMSTidalRemnantConfig()) != null) {
          break;
        }
      }
    }

    if (tr0 != null) {

      slog.info(mmi+"Found a non-null FMSTidalRemnantConfig object, validate it with other FMSTidalRemnantConfig objects (if any)");

      //for (final ForecastingContext fc : forecastingContextsList) {
      for (final FMSConfig fc: fmsInputList) {

        final FMSTidalRemnantConfig tr= fc.getFMSTidalRemnantConfig();

        if (tr != null) {

          final String stationId= fc.getStationId();

          if (tr.getTauHours() != tr0.getTauHours()) {

            slog.error(mmi+"Must have the same FMSTidalRemnantConfig tau hours for all FMSConfig objects, station in error: " + stationId);
            ret= false;
            break;
          }

          if (tr.getDeltaTMinutes() != tr0.getDeltaTMinutes()) {

            slog.error(mmi+"Must have the same FMSTidalRemnantConfig deltaT minutes for all FMSConfig objects, station in error: " + stationId);
            ret= false;
            break;
          }
        }
      }
    }

    return ret;
  }

  /**
   * @return this.residual as a generice FMSResidualFactory object regardless of its sub-type.
   */
  //@NotNull
  //@Override
  public final FMSResidualFactory getFMSResidualFactory() {
    return this.residual;
  }

  /**
   * @param fmsConfig  : A FMConfig object.
   * @param lastWLOSse : The time-stamp(seconds since the epoch) of the last valid WLO available for the
   *                     CHS TG station.
   * @return this as a generic IFMSResidual type object.
   */
  //@NotNull
  //@Override
  public final IFMSResidual getIFMSResidual(final FMSInput fmsInput, ///*@NotNull*/ final ForecastingContext forecastingContext,
                                            /*@Min(0)*/ final long lastWLOSse) {

    final String mmi= "getIFMSResidual: ";

    slog.info(mmi+"start");

    //this.init();

    final String stationId= fmsInput.getStationId();

    //final FmsParameters fmsParameters = forecastingContext.getFmsParameters();
    //final Forecast forecastCfg = fmsParameters.getForecast();

    final double forecastDurationSeconds=
      SECONDS_PER_HOUR * fmsInput.getDurationHoursInFuture();

    if (fmsInput.getFMSTidalRemnantConfig() != null) {

      slog.info(mmi+"Will use residual computation with tidal remnant component for station: " + stationId);

      this.residual= new TidalRemnantResidual(fmsInput.getFMSResidualConfig(),
                                              fmsInput.getFMSTidalRemnantConfig(),
                                              fmsInput.getDeltaTMinutes(), stationId);

    } else {

      slog.info(mmi+"Will use residual computation without tidal remnant component for station: " + stationId);

      this.residual= new
        LegacyResidual(fmsInput.getFMSResidualConfig(), stationId);
    }

    final List<MeasurementCustom> wlpl= fmsInput.getPredictions();

    if (wlpl.get(0).getUncertainty() == null) {

      slog.info(mmi+"wlpl.get(0).getUncertainty()==null for station:" + stationId + " !");

      slog.info(mmi+"the default predictions error of "+
                PREDICTIONS_ERROR_ESTIMATE_METERS + " will be used for the residual setup.");
    }

    this.residual.setup(lastWLOSse, wlpl);

    slog.info(mmi+"end");

    return this;
  }

  //--- For possible future usage.
//    public final long getSseStart() {
//        return this.residual.getSseStart();
//    }

  /**
   * Report the number of missing WLO items from this.residual
   *
   * @return this.residual.getNbMissingWLO()
   */
  public final int getNbMissingWLO() {
    return this.residual.getNbMissingWLO();
  }

  /**
   * @param pstrWLStationTimeNode : A WLStationTimeNode object which is just before in time compared to the
   *                              SecondsSinceEpoch sse object time-stamp.
   * @param secondsSinceEpoch     :  A SecondsSinceEpoch with the next(in the future) time stamp to use.
   * @param data                  : An array of 4 FMSWLMeasurement(PREDICTION, OBSERVATION, FORECAST, EXT_STORM_SURGE
   *                              (which could be
   *                              NULL)) objects.
   * @return A new WLStationTimeNode object ready to be used.
   */
  //@NotNull
  //@Override
  public final WLStationTimeNode newFMSTimeNode(final WLStationTimeNode pstrWLStationTimeNode,
                                                /*@NotNull*/ final SecondsSinceEpoch secondsSinceEpoch,
                                                /*@NotNull @Size(min = 4)*/ final FMSWLMeasurement[] data) {

    final String mmi= "newFMSTimeNode: ";

    slog.info(mmi+"pstr=" + pstrWLStationTimeNode +
              ", sse.dt=" + secondsSinceEpoch.dateTimeString(true));

    return this.residual.
      getFMSTimeNode(pstrWLStationTimeNode, secondsSinceEpoch, data);
  }

  /**
   * Main computation method for errors residuals statistics computations.
   *
   * @param stationCode       : The WL station usual SINECO ID String
   * @param stillGotWLOs      : A boolean to signal that the WLStationTimeNode object is in the past compared to the
   *                          last
   *                          valid WLO available for the station.
   * @param wlStationTimeNode : WLStationTimeNode object.
   * @return The processed WLStationTimeNode taken as last argument.
   */
  //@NotNull
  @Override
  public final WLStationTimeNode processWLStationTimeNode(/*@NotNull*/ final String stationId,
                                                          final boolean stillGotWLOs,
                                                          /*@NotNull*/ final WLStationTimeNode wlStationTimeNode) {
    final String mmi= "processWLStationTimeNode: ";

    final String dts= wlStationTimeNode.getSse().dateTimeString(true);

    slog.info(mmi+"Computing Legacy residual at at time stamp: " + dts +
              " for station: " + stationId + ", stillGotWLO=" + stillGotWLOs);

    if (wlStationTimeNode.get(WLType.OBSERVATION) != null) {

      slog.info(mmi+"Valid OBSERVATION DB data at time stamp: "+dts+" for station: "+
                stationId + ", updating residuals errors statistics and long term surge");

      //--- Update the long term surge if we have a valid WLO.
      this.residual.updateFMSLongTermWLOffset(wlStationTimeNode);

      //--- Update residual temporal errors covariances stats:
      this.residual.update(wlStationTimeNode);

    } else {

      if (stillGotWLOs) {

        //--- We are in the past(compared with the more recent WLO data available) and we have a missing WLO DB data:
        slog.info(mmi+"Missing WLO for station: "+stationId+
                  " at time-stamp: "+dts+", the surge will be estimated and applied");

        //--- Just need to update residual correction parameters.
        this.residual.updateAlphaParameters();

        this.residual.incrNbMissingWLO();

        //this.log.debug("LegacyFMResidual processWLStationTimeNode: nb. missing WLO at this point="+this.residual
        // .incrMissingObs());

      } else {

        //--- We are in the future and the surge is estimated with the statistics computed with the valid WLO data.
        slog.info(mmi+"Estimating WL residual for : "+stationId+" at future time-stamp: " + dts);
      }

      //--- Use this.residual.estimate both for future forecasts AND missing WLO DB data:
      //    The 2nd arg. is a flag to signal that we have(or not have) to produce an updated forecast.
      this.residual.estimate(wlStationTimeNode, !stillGotWLOs);
    }

    //--- Add the newly processed station time node to the list of lag nodes of this.residual:
    this.residual.udpateLagNode(wlStationTimeNode);

    slog.info(mmi+"nb. missing WLO up-to-now="+
              this.residual.getNbMissingWLO()+ "at TG atation: "+stationId);

    return wlStationTimeNode;
  }
}
