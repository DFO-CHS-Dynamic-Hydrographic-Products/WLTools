//package ca.gc.dfo.iwls.fmservice.modeling.tides;
package ca.gc.dfo.chs.wltools.tidal.stationary.prediction;

/**
 * Created on 2018-01-19.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-20: Gilles Mercier
 */

// ---
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent2D;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent2DData;
import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.StationaryTidalPredFactory;

//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent2D;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent2DData;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
//---

/**
 * eneric class for producing 2D(i.e. two spatial components usually U,V tidal currents) tidal predictions
 */
abstract public class Stationary2DTidalPredFactory extends StationaryTidalPredFactory {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Map of a group of 2D tidal constituents informations coming from a file or a DB.
   */
  protected Map<String, Constituent2D> tcDataMap = null;
  /**
   * Constituent2DData object which will be used by the tidal predictions method.
   */
  private Constituent2DData constituent2DData = null;
  
  /**
   * Default constructor.
   */
  public Stationary2DTidalPredFactory() {
    
    super();
    
    this.tcDataMap = null;
    this.constituent2DData = null;
  }
  
  /**
   * @param method           : Tidal prediction method to use.
   * @param latitudeRadians  : Latitude of the 2D grid-point where we want 2D tidal predictions
   * @param startTimeSeconds : Time-stamp in seconds since the epoch for the time reference used for astronomic
   *                         arguments computations.
   * @param constNames       : A Set of tidal constituents names to use for the tidal predictions.
   * @return The current TidalPredictions2DFactory object.
   */
  @Override
  final protected Stationary2DTidalPredFactory setAstroInfos(final Method method,
                                                             final double latitudeRadians,
                                                             final long startTimeSeconds,
                                                            /*@NotNull @Size(min = 1)*/ final Set constNames) {
    
    this.log.debug("Stationary2DTidalPredFactory setAstroInfos : start");
    
    super.setAstroInfos(method, latitudeRadians, startTimeSeconds, constNames);
    
    //--- TODO: implement constructor Constituent2DData(this.tcDataMap, this.astroInfosFactory)
    //this.constituent2DData= new Constituent2DData(this.tcDataMap, this.astroInfosFactory);
    
    this.log.debug("Stationary2DTidalPredFactory setAstroInfos : end");
    
    return this;
  }
}
