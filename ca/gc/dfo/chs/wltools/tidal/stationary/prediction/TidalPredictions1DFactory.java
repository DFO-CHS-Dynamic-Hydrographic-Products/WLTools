//package ca.gc.dfo.iwls.fmservice.modeling.tides;
package ca.gc.dfo.chs.wltools.tidal.stationary.prediction;

/**
 * Created on 2018-01-19.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-20, Gilles Mercier
 */

//---
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1D;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1DData;
import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.TidalPredictionsFactory;

//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1D;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1DData;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
//---
//---

/**
 * Generic class for producing 1D(i.e. only one spatial component) tidal predictions
 */
abstract public class TidalPredictions1DFactory extends TidalPredictionsFactory {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Map of a group of tidal constituents informations coming from a file or a DB.
   */
  protected Map<String, Constituent1D> tcDataMap = null;
  /**
   * Constituent1DData object which will be used by the tidal predictions method.
   */
  private Constituent1DData constituent1DData = null;
  
  /**
   * Default constructor.
   */
  public TidalPredictions1DFactory() {
    
    super();
    
    this.tcDataMap = null;
    this.constituent1DData = null;
  }
  
  /**
   * @param timeStampSeconds : A time-stamp in seconds since the epoch where we want a single tidal prediction.
   * @return The newly computed single tidal prediction in double precision.
   */
  @Override
  final public double computeTidalPrediction(final long timeStampSeconds) {
    return this.astroInfosFactory.computeTidalPrediction(timeStampSeconds, this.constituent1DData);
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
  final protected TidalPredictions1DFactory setAstroInfos(final Method method,
                                                          final double latitudeRadians,
                                                          final long startTimeSeconds,
                                                          /*@NotNull @Size(min = 1)*/ final Set constNames) {

    try {
      constNames.size();
      
    } catch (NullPointerException e) {
      
      this.log.error("TidalPredictions1DFactory setAstroInfos: constNames==null !!");
      throw new RuntimeException(e);
    }
    
    this.log.debug("TidalPredictions1DFactory setAstroInfos: setAstroInfos : start");
    
    super.setAstroInfos(method, latitudeRadians, startTimeSeconds, constNames);
    
    this.constituent1DData = new Constituent1DData(this.tcDataMap, this.astroInfosFactory);
    
    this.log.debug("TidalPredictions1DFactory setAstroInfos: end");
    
    return this;
  }
}
