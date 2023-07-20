//package ca.gc.dfo.iwls.fmservice.modeling.tides;
package ca.gc.dfo.chs.wltools.tidal.nonstationary.prediction;

/**
 * Created on 2018-01-19.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-20, Gilles Mercier
 */

//---
import ca.gc.dfo.chs.wltools.stage.Stage;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1D;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1DData;
import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.TidalPredictionsFactory;

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
 * Generic class for producing 1D(i.e. only one spatial component) tidal predictions
 */
final public class NonStationaryTidalPredictionsFactory extends TidalPredictionsFactory {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * List of Map objects of tidal constituents information for a specific location coming from a file or a DB.
   */
  protected List<Map<String, Constituent1D>> tcDataMaps = null;

  /**
   * List of Constituent1DData objects which will be used by the non-stationary tidal prediction method.
   */
  private List<Constituent1DData> constituent1DDataItems = null;

  private Stage stageTerms= null;

  /**
   * Default constructor.
   */
  public NonStationaryTidalPredictionsFactory() {
    
    super();
    
    this.tcDataMaps= null;
    this.stageTerms= null;
    this.constituent1DDataItems= null;
  }
  
  /**
   * @param timeStampSeconds : A time-stamp in seconds since the epoch where we want a single tidal prediction.
   * @return The newly computed single tidal prediction in double precision.
   */
  @Override
  final public double computeTidalPrediction(final long timeStampSeconds) {

     double retAcc= 0.0; 

     //this.stageTerms.evaluate(
     // return this.astroInfosFactory.computeTidalPrediction(timeStampSeconds, this.constituent1DDataItems);

     return retAcc;
  }
  
  /**
   * @param method           : Tidal prediction method to use.
   * @param latitudeRadians  : Latitude of the 2D point where we want 1D tidal predictions
   * @param startTimeSeconds : Time-stamp in seconds since the epoch for the time reference used for astronomic
   *                         arguments computations.
   * @param constNames       : A Set of tidal constituents names to use for the tidal predictions.
   * @return The current TidalPredictions1DFactory object.
   */
  @Override
  final protected NonStationaryTidalPredictionsFactory setAstroInfos(final Method method,
                                                                     final double latitudeRadians,
                                                                     final long startTimeSeconds,
                                                                     /*@NotNull @Size(min = 1)*/ final Set constNames) {
    try {
      constNames.size();
      
    } catch (NullPointerException e) {
      
      this.log.error("NonStationaryTidalPredictionsFactory setAstroInfos: constNames==null !!");
      throw new RuntimeException(e);
    }
    
    this.log.debug("NonStationaryTidalPredictionsFactory setAstroInfos: setAstroInfos : start");
    
    super.setAstroInfos(method, latitudeRadians, startTimeSeconds, constNames);
    
    int dimCount= 0;

    // ---
    for (Constituent1DData c1DD: this.constituent1DDataItems) {
        c1DD= new Constituent1DData(this.tcDataMaps.get(dimCount),this.astroInfosFactory);
    }

    //this.constituent1DData = new Constituent1DData(this.tcDataMap, this.astroInfosFactory);
    
    this.log.debug("NonStationaryTidalPredictionsFactory setAstroInfos: end");
    
    return this;
  }
}
