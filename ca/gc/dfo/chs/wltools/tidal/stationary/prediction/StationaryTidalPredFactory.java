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
 * Generic class for classic astronomic-harmonic (a.k.a stationary) tidal predictions.
 */
abstract public class StationaryTidalPredFactory implements ITidal, ITidalIO {

  /**
   * log utility
   */
   private final Logger log = LoggerFactory.getLogger(this.getClass());
  //private final static Logger log = LoggerFactory.getLogger(ClassName.class);

  /**
   * Generic AstroInfosFactory object for astronomic arguments computations.
   */
  protected AstroInfosFactory astroInfosFactory = null;
  
  /**
   * Default constructor:
   */
  public StationaryTidalPredFactory() {
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
  protected StationaryTidalPredFactory setAstroInfos(final Method method,
                                                     final double latitudeRadians,
                                                     final long startTimeSeconds,
                                                      /*@NotNull @Size(min = 1)*/ final Set constNames) {
    
    try {
      constNames.size();
      
    } catch (NullPointerException e) {
      log.error("setAstroInfos: constNames==null !!");
      throw new RuntimeException(e);
    }
    
    log.info("setAstroInfos : start");
    log.info("setAstroInfos : method=" + method + ", latitudeRadians=" + latitudeRadians);
    log.info("setAstroInfos : startTimeSeconds dt=" + SecondsSinceEpoch.dtFmtString(startTimeSeconds, true));
    log.info("setAstroInfos : constNames=" + constNames.toString());

    switch (method) {

      // --- NON_STATIONARY_FOREMAN == STATIONARY_FOREMAN at this point.
      case NON_STATIONARY_FOREMAN:
      case STATIONARY_FOREMAN:

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
        
        //this.log.error("setAstroInfos : Invalid tidal prediction method-> " + method);
        throw new RuntimeException("setAstroInfos : Invalid tidal prediction method-> " + method);
        //break;
    }
    
    log.info("end");
    
    return this;
  }
  
  /**
   * @param timeStampSeconds : A time-stamp in seconds since the epoch where we want a single tidal prediction.
   * @return The newly computed single tidal prediction in double precision.
   */
  abstract public double computeTidalPrediction(final long timeStampSeconds);

  /**
   * Comments please!
   */
  abstract public StationaryTidalPredFactory getTCFFileData(/*@NotNull*/ final String tcfFilePath);

  /**
   * Comments please!
   */
  abstract public StationaryTidalPredFactory getNSJSONFileData(/*@NotNull*/ final String jsonFilePath);

}
