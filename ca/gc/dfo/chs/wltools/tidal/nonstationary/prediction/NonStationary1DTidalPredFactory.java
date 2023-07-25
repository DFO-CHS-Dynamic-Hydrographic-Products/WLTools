//package ca.gc.dfo.iwls.fmservice.modeling.tides;
package ca.gc.dfo.chs.wltools.tidal.nonstationary.prediction;

/**
 * Created on 2018-01-19.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-20, Gilles Mercier
 */

//---
import ca.gc.dfo.chs.wltools.nontidal.stage.Stage;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1D;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1DData;
import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.Stationary1DTidalPredFactory;
//import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.TidalPredictionsFactory;

//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1D;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1DData;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
//---
//---

/**
 * River-discharge and-or atmospheric influenced (a.k.a. non-stationary) tidal prediction object.
 * This implements the prediction-reconstruction part of the NS_TIDE theory
 *  (Matte et al., Journal of Atmospheric and Oceanic Technology, 2013)
 */
final public class NonStationary1DTidalPredFactory extends Stationary1DTidalPredFactory {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * List of Map objects of tidal constituents information for the non-stationary(river discharge and-or atmospheric)
   * for a specific location coming from a file or a DB.
   */
  protected Map<Map<String, Constituent1D>> tcDataMaps= null;

  /**
   * List of Constituent1DData objects which will be used by the non-stationary tidal prediction method.
   * MUST have the number of Constituent1DData objects in the constituent1DDataItems being the same as we
   * have Map items in one Map item of tcDataMaps list.
   */
  private Map<String,Constituent1DData> constituent1DDataItems= null;

  /**
   * The stage equation (polynomial) object.
   */
  private Stage stagePart= null;

  ///**
  // * To store the stage input data (river discharges and-or atmos. data) with their related time stamps
  // */
  //private Map<String,List<MeasurementCustom>> staticStageInputData= null;
  //private List<List<Map<Long,MeasurementCustom>>> staticStageInputData= null;

  /**
   * Default constructor.
   */
  public NonStationary1DTidalPredFactory() {
    
    super();
    
    this.tcDataMaps= null;
    this.stagePart= null;
    //this.staticStageInputData= null;
    this.constituent1DDataItems= null;
  }
  
  /**
   * @param timeStampSeconds : A time-stamp in seconds since the epoch where we want a single tidal prediction.
   * @return The newly computed single tidal prediction in double precision.
   */
  @Override
  final public double computeTidalPrediction(final long timeStampSeconds) {

     // --- Compute the stationary part (NOTE: no Z0 average to use here, it is rather
     //     in the stage part (the CS0 coefficient, added at the end.)
     double retAcc= super.computeTidalPrediction(timeStampSeconds);

     // --- Add the non-stationary parts of the signal
     for (final String stInputDataId: this.stagePart.staticInputData.keySet()) {

        // --- Calculate the non-stationary tidal argument.
        final double nsTidalArg= super.astroInfosFactory
           .computeTidalPrediction(timeStampSeconds,this.constituent1DDataItems.get(stInputDataId));

        // ---- Apply the stage calculation with the related stage part and the non-stationart tidal arg. added together
        //      (multiply  by the time varying stage input data,note the lag time subtraction.)
        retAcc += (stagePart.coefficients.get(stInputDataId) + nsTidalArg) *
                   this.stagePart.staticInputData.get(stInputDataId).get(timeStampSeconds-timeLagSeconds);
     }

     // Add the stage part.
     //this.stageTerms.evaluate(
     // return this.astroInfosFactory.computeTidalPrediction(timeStampSeconds, this.constituent1DDataItems);

     // --- Add the stage zero'th order coefficient (WL signal average) for the returned WL prediction
     //     for this timestamp.
     return retAcc + stagePart.coefficients.get(IStageIO.STAGE_JSON_D0_KEY);
  }
  
  /**
   * @param method           : Tidal prediction method to use.
   * @param latitudeRadians  : Latitude of the 2D point where we want 1D tidal predictions
   * @param startTimeSeconds : Time-stamp in seconds since the epoch for the time reference used for astronomic
   *                         arguments computations.
   * @param constNames       : A Set of tidal constituents names to use for the tidal predictions.
   * @return The current  object.
   */
  @Override
  final protected NonStationary1DTidalPredFactory setAstroInfos(final Method method,
                                                                final double latitudeRadians,
                                                                final long startTimeSeconds,
                                                               /*@NotNull @Size(min = 1)*/ final Set constNames) {
    try {
      constNames.size();
      
    } catch (NullPointerException e) {
      
      this.log.error("NonStationary1DTidalPredFactory setAstroInfos: constNames==null !!");
      throw new RuntimeException(e);
    }
    
    this.log.debug("NonStationary1DTidalPredFactory setAstroInfos: setAstroInfos : start");

    // --- Compute the stationary astronomic information and set the stationary
    //     tidal constituents amplitudes and phases.
    super.setAstroInfos(method, latitudeRadians, startTimeSeconds, constNames);
    
    //int dimCount= 0;

    // --- Set the non-stationary tidal constituents amplitudes and phases.
    //for (Constituent1DData c1DD: this.constituent1DDataItems) {
    for (final String tcMapId: this.constituent1DDataItems.keySet()) {

        this.constituent1DDataItems.get(tcMapId)=
           new Constituent1DData(this.tcDataMaps.get(tcMapId),this.astroInfosFactory);
    }

    //this.constituent1DData = new Constituent1DData(this.tcDataMap, this.astroInfosFactory);
    
    this.log.debug("NonStationary1DTidalPredFactory setAstroInfos: end");
    
    return this;
  }
}
