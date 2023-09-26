package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;

/**
 *
 */

//--

import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSResidualFactory;
import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSWLMeasurement;
import ca.gc.dfo.iwls.fmservice.modeling.fms.IFMSResidual;
import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
import ca.gc.dfo.iwls.modeling.fms.FmsParameters;
import ca.gc.dfo.iwls.modeling.fms.Forecast;
import ca.gc.dfo.iwls.modeling.fms.Residual;
import ca.gc.dfo.iwls.modeling.fms.TidalRemnant;
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
 * External entry point for Legacy errors residuals computations. It encapsulate the two types of Legacy methods
 * i.e. with or without a WL tidal remnant component.
 */
final public class LegacyFMSResidual implements IFMSResidual, ILegacyFMS {
  
  /**
   * static log utility.
   */
  private static final Logger staticLog = LoggerFactory.getLogger("LegacyFMResidual");
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Generic Legacy errors residuals object(could be a LegacyResidual or a TidalRemnantResidual object)
   */
  protected LegacyFMSResidualFactory residual = null;
  
  //--- For possible future usage.
//    public LegacyFMResidual() {
//        super();
//    }
  
  /**
   * Validate all the ForecastingContext configuration objects in the List forecastingContextsList.
   *
   * @param forecastingContextsList : A List of ForecastingContext configuration objects.
   * @return true if all the ForecastingContext configuration objects are ok, false otherwise.
   */
  public final static boolean validateFMConfigParameters(@NotNull @Size(min = 1) final List<ForecastingContext> forecastingContextsList) {
    
    boolean ret = true;
    
    final ForecastingContext fc0 = forecastingContextsList.get(0);
    
    final String referenceStationCode = fc0.getStationCode();
    
    final FmsParameters fm0 = fc0.getFmsParameters();
    final Forecast fs0 = fm0.getForecast();
    
    final Residual rs0 = fm0.getResidual();
    
    TidalRemnant tr0 = fm0.getTidalRemnant();
    
    //--- begin at item 1 to avoid comparing reference station with itself.
    for (final ForecastingContext fc : forecastingContextsList.subList(1, forecastingContextsList.size())) {
      
      final String stationCode = fc.getStationCode();
      
      final FmsParameters fm = fc.getFmsParameters();
      final Forecast fs = fm.getForecast();
      final Residual rs = fm.getResidual();
      
      //--- Check if we have a duplicate ForecastingContext
      if (stationCode.equals(referenceStationCode)) {
        
        staticLog.error("LegacyFMResidual validateFMConfigParameters: Found a ForecastingContext duplicate for " +
            "station: " + stationCode);
        ret = false;
        break;
      }
      
      if (fs.getDeltaTMinutes() != fs0.getDeltaTMinutes()) {
        
        staticLog.error("LegacyFMResidual validateFMConfigParameters: Must have the same Forecast deltaT minutes " +
            "for all ForecastingContexts, station in error: " + stationCode);
        ret = false;
        break;
      }
      
      if (fs.getDurationHours() != fs0.getDurationHours()) {
        
        staticLog.error("LegacyFMResidual validateFMConfigParameters:  Must have the same Forecast duration hours " +
            "for all ForecastingContexts, station in error: " + stationCode);
        ret = false;
        break;
      }

//            if (!rs.getMethod().equals(rs0.getMethod())) {
//                this.log.error("Must have the same Residual method for all ForecastingContexts, station in error:
//                "+stId);
//                ret= false;
//                break;
//            }
      
      if (rs.getTauHours() != rs0.getTauHours()) {
        
        staticLog.error("LegacyFMResidual validateFMConfigParameters: Must have the same Residual tau hours for " +
            "all ForecastingContexts, station in error: " + stationCode);
        ret = false;
        break;
      }
      
      if (rs.getDeltaTMinutes() != rs0.getDeltaTMinutes()) {
        
        staticLog.error("LegacyFMResidual validateFMConfigParameters: Must have the same Residual deltaT minutes for " +
            "all ForecastingContexts, station in error: " + stationCode);
        ret = false;
        break;
      }
    }
    
    //--- Validate TidalRemnant parameters(if any)
    //     (tr0 could be null at this point)
    if (tr0 == null) {
      
      //--- Try to find a non-null TidalRemnant Object:
      for (final ForecastingContext fc : forecastingContextsList) {
        
        if ((tr0 = fc.getFmsParameters().getTidalRemnant()) != null) {
          break;
        }
      }
    }
    
    if (tr0 != null) {
      
      staticLog.debug("LegacyFMResidual validateFMConfigParameters: Found a non-null TidalRemnant, validate it with " +
          "other TidalRemnant configs. if any");
      
      for (final ForecastingContext fc : forecastingContextsList) {
        
        final TidalRemnant tr = fc.getFmsParameters().getTidalRemnant();
        
        if (tr != null) {
          
          final String stationCode = fc.getStationCode();
          
          if (tr.getTauHours() != tr0.getTauHours()) {
            
            staticLog.error("LegacyFMResidual validateFMConfigParameters: Must have the same TidalRemnant tau hours " +
                "for " +
                "all ForecastingContexts, station in error: " + stationCode);
            ret = false;
            break;
          }
          
          if (tr.getDeltaTMinutes() != tr0.getDeltaTMinutes()) {
            
            staticLog.error("LegacyFMResidual validateFMConfigParameters: Must have the same TidalRemnant deltaT " +
                "minutes for all ForecastingContexts, station in error: " + stationCode);
            ret = false;
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
  @NotNull
  @Override
  public final FMSResidualFactory getFMSResidualFactory() {
    return this.residual;
  }
  
  /**
   * @param forecastingContext : A ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext object.
   * @param lastWLOSse         : The time-stamp(seconds since the epoch) of the last valid WLO available for the
   *                           station.
   * @return this as a generic IFMSResidual type object.
   */
  @NotNull
  @Override
  public final IFMSResidual getIFMSResidual(@NotNull final ForecastingContext forecastingContext,
                                            @Min(0) final long lastWLOSse) {
    
    this.log.debug("LegacyFMResidual getIFMSResidual: Start");
    
    //this.init();
    
    final String stationId = forecastingContext.getStationCode();
    
    final FmsParameters fmsParameters = forecastingContext.getFmsParameters();
    final Forecast forecastCfg = fmsParameters.getForecast();
    
    final double forecastDurationSeconds = SECONDS_PER_HOUR * forecastCfg.getDurationHours();
    
    if (fmsParameters.getTidalRemnant() != null) {
      
      this.log.debug("LegacyFMResidual getIFMSResidual: Will use residual computation with tidal remnant component " +
          "for station:" + stationId);
      this.residual = new TidalRemnantResidual(fmsParameters.getResidual(), fmsParameters.getTidalRemnant(),
          forecastCfg.getDeltaTMinutes(), stationId);
      
    } else {
      
      this.log.debug("LegacyFMResidual getIFMSResidual: Will use residual computation without tidal remnant component" +
          " for station:" + stationId);
      this.residual = new LegacyResidual(fmsParameters.getResidual(), stationId);
    }
  
    final List<MeasurementCustom> wlpl = forecastingContext.getPredictions();
    
    if (wlpl.get(0).getUncertainty() == null) {
      
      this.log.warn("LegacyFMResidual getIFMSResidual: wlpl.get(0).getUncertainty()==null for station:" + stationId + " !");
      this.log.warn("LegacyFMResidual getIFMSResidual: the default predictions error of " + PREDICTIONS_ERROR_ESTIMATE_METERS + " will be used for the residual setup.");
    }
    
    this.residual.setup(lastWLOSse, wlpl);
    
    this.log.debug("LegacyFMResidual getIFMSResidual: End");
    
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
  @NotNull
  @Override
  public final WLStationTimeNode newFMSTimeNode(final WLStationTimeNode pstrWLStationTimeNode,
                                                @NotNull final SecondsSinceEpoch secondsSinceEpoch,
                                                @NotNull @Size(min = 4) final FMSWLMeasurement[] data) {
    
    this.log.debug("LegacyFMResidual newFMSTimeNode Start: pstr=" + pstrWLStationTimeNode + ", sse.dt=" + secondsSinceEpoch.dateTimeString(true));
    
    return this.residual.getFMSTimeNode(pstrWLStationTimeNode, secondsSinceEpoch, data);
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
  @NotNull
  @Override
  public final WLStationTimeNode processWLStationTimeNode(@NotNull final String stationCode,
                                                          final boolean stillGotWLOs,
                                                          @NotNull final WLStationTimeNode wlStationTimeNode) {
    
    final String dts = wlStationTimeNode.getSse().dateTimeString(true);
    
    this.log.debug("LegacyFMResidual processWLStationTimeNode: Computing Legacy residual at at time stamp: " + dts +
        " for station: " + stationCode + ", stillGotWLO=" + stillGotWLOs);
    
    if (wlStationTimeNode.get(WLType.OBSERVATION) != null) {
      
      this.log.debug("LegacyFMResidual processWLStationTimeNode: Valid OBSERVATION DB data at time stamp: "
          + dts + " for station: " + stationCode + ", updating residuals errors statistics and long term surge");
      
      //--- Update the long term surge if we have a valid WLO.
      this.residual.updateFMSLongTermSurge(wlStationTimeNode);
      
      //--- Update residual temporal errors covariances stats:
      this.residual.update(wlStationTimeNode);
      
    } else {
      
      if (stillGotWLOs) {
        
        //--- We are in the past(compared with the more recent WLO data available) and we have a missing WLO DB data:
        this.log.debug("LegacyFMResidual processWLStationTimeNode :Missing WLO for station: " +
            stationCode + " at time-stamp: " + dts + ", the surge will be estimated and applied");
        
        //--- Just need to update residual correction parameters.
        this.residual.updateAlphaParameters();
        
        this.residual.incrNbMissingWLO();
        
        //this.log.debug("LegacyFMResidual processWLStationTimeNode: nb. missing WLO at this point="+this.residual
        // .incrMissingObs());
        
      } else {
        
        //--- We are in the future and the surge is estimated with the statistics computed with the valid WLO data.
        this.log.debug("LegacyFMResidual processWLStationTimeNode: Estimating WL residual for : " + stationCode + " " +
            "at future time-stamp: " + dts);
      }
      
      //--- Use this.residual.estimate both for future forecasts AND missing WLO DB data:
      //    The 2nd arg. is a flag to signal that we have(or not have) to produce an updated forecast.
      this.residual.estimate(wlStationTimeNode, !stillGotWLOs);
    }
    
    //--- Add the newly processed station time node to the list of lag nodes of this.residual:
    this.residual.udpateLagNode(wlStationTimeNode);
    
    this.log.debug("LegacyFMResidual processWLStationTimeNode end: nb. missing WLO at this point=" + this.residual.getNbMissingWLO());
    
    return wlStationTimeNode;
  }
}
