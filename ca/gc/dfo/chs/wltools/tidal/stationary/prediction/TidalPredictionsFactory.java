//package ca.gc.dfo.iwls.fmservice.modeling.tides;
package ca.gc.dfo.chs.wltools.tidal.stationary.prediction;

/**
 * Created by Gilles Mercier on 2018-01-10.
 */

//---
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.AstroInfosFactory;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman.ForemanAstroInfos;

//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.AstroInfosFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman.ForemanAstroInfos;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;

//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
//---
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1D;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1DData;

/**
 * Generic class for tidal predictions.
 */
abstract public class TidalPredictionsFactory implements ITidal, ITidalIO {

  /**
   * log utility
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Generic AstroInfosFactory object for astronomic arguments computations.
   */
  protected AstroInfosFactory astroInfosFactory = null;
  
  /**
   * Default constructor:
   */
  public TidalPredictionsFactory() {
    this.astroInfosFactory = null;
  }
  
  /**
   * @param method           : Tidal prediction method to use.
   * @param latitudeRadians  : Latitude of the point(or average latitude of a region) where we want tidal predictions
   * @param startTimeSeconds : Time-stamp in seconds since the epoch for the time reference used for astronomic
   *                         arguments computations.
   * @param constNames       : A Set of tidal constituents names to use for the tidal predictions.
   * @return The current TidalPredictionsFactory object.
   */
  protected TidalPredictionsFactory setAstroInfos(final Method method,
                                                  final double latitudeRadians,
                                                  final long startTimeSeconds,
                                                  /*@NotNull @Size(min = 1)*/ final Set constNames) {
    
    try {
      constNames.size();
      
    } catch (NullPointerException e) {
      this.log.error("TidalPredictionsFactory setAstroInfos: constNames==null !!");
      throw new RuntimeException(e);
    }
    
    this.log.debug("TidalPredictionsFactory setAstroInfos : start");
    this.log.debug("TidalPredictionsFactory setAstroInfos : method=" + method + ", latitudeRadians=" + latitudeRadians);
    this.log.debug("TidalPredictionsFactory setAstroInfos : startTimeSeconds dt=" + SecondsSinceEpoch.dtFmtString(startTimeSeconds, true));
    this.log.debug("TidalPredictionsFactory setAstroInfos : constNames=" + constNames.toString());
    
    switch (method) {
      
      case FOREMAN:
        
        this.astroInfosFactory = new ForemanAstroInfos(latitudeRadians, startTimeSeconds, constNames);
        break;

//--- In case we want to test XTIDE method.
//            case XTIDE:
//
//                this.log.error("TidalPredictionsFactory setAstroInfos : XTIDE tidal prediction method not yet
//                implemented !");
//                throw new RuntimeException("TidalPredictionsFactory setAstroInfos");
//                //break;
      
      default:
        
        this.log.error("TidalPredictionsFactory setAstroInfos : Invalid tidal prediction method-> " + method);
        throw new RuntimeException("TidalPredictionsFactory setAstroInfos");
        //break;
    }
    
    this.log.debug("TidalPredictionsFactory setAstroInfos : end");
    
    return this;
  }
  
  /**
   * @param timeStampSeconds : A time-stamp in seconds since the epoch where we want a single tidal prediction.
   * @return The newly computed single tidal prediction in double precision.
   */
  abstract public double computeTidalPrediction(final long timeStampSeconds);
}
