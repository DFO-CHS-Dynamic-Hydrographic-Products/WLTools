//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---
//import java.time.Instant;
//import javax.validation.constraints.Min;

import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITides;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITidesIO;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---

/**
 * Generic class for astronomic informations data.
 */
abstract public class AstroInfosFactory implements ITidal, ITidalIO {

  private final static Logger staticLog = LoggerFactory.getLogger("AstroInfosFactory");
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Method to use.
   */
  protected Method method = Method.FOREMAN; //--- Foreman tidal method as default.
  /**
   * Latitude in radians of the location where the astronomic informations are defined.
   */
  protected double latitudeRad = 0.0;
  /**
   * To keep track of the last update of the time reference of the astronomic informations. It is a time-stamp
   * since the epoch.
   */
  protected SecondsSinceEpoch sse = null;
  /**
   * Array of IConstituentAstro objects(one for the astronomic informations of each tidal constituents used by the
   * tidal method.
   */
  protected IConstituentAstro[] infos = null;
  
  /**
   * @param latitudeRad : Latitude in radians of the location where the astronomic informations are defined.
   */
  public AstroInfosFactory(final double latitudeRad) {
    this(Method.FOREMAN, latitudeRad, null);
  }
  
  /**
   * @param method            : Tidal method to use.
   * @param latitudeRad       : Latitude in radians of the location where the astronomic informations are defined.
   * @param secondsSinceEpoch : A SecondsSinceEpoch object which time-stamp will be the initial time-stamp of the
   *                          time reference of the astronomic informations.
   */
  public AstroInfosFactory(/*@NotNull*/ final Method method, final double latitudeRad,
                           final SecondsSinceEpoch secondsSinceEpoch) {
    
    this();
    
    this.log.debug("AstroInfosFactory constructor : Start: method=" + method + ", latitudeRad=" + latitudeRad +
        ", sse dt=" + ((secondsSinceEpoch != null) ? secondsSinceEpoch.dateTimeString(true) : "null"));
    
    if (!AstroInfosFactory.validateAstroMethod(method)) {
      
      this.log.error("AstroInfosFactory constructor : Invalid input method -> " + method);
      throw new RuntimeException("AstroInfosFactory constructor");
    }
    
    this.method = method;
    
    this.sse = secondsSinceEpoch;
    this.latitudeRad = latitudeRad;
    
    this.log.debug("AstroInfosFactory constr. : Done with input method -> " + method);
  }
  
  /**
   * Default constructor.
   */
  public AstroInfosFactory() {
    this.clear();
  }
  
  /**
   * @param aTidalMethod : A tidal astronomic method ID to validate.
   * @return true if the tidal astronomicmethod ID is a vlid one, false otherwise.
   */
  private final static boolean validateAstroMethod(/*@NotNull*/ final Method aTidalMethod) {
    
    boolean found = false;
    
    for (final Method method : Method.values()) {
      
      if (aTidalMethod == method) {
        found = true;
        break;
      }
    }
    
    return found;
  }
  
  /**
   * Clear this.infos
   */
  protected final void clear() {
    
    this.sse = null;
    this.latitudeRad = 0.0;
    
    if (this.infos != null) {
      
      for (final IConstituentAstro iConstituentAstro : this.infos) {
        iConstituentAstro.init();
      }
    }
    
    this.infos = null;
    
  }
  
  /**
   * @param method           : Tidal method to use.
   * @param latitudeRad      : Latitude in radians of the location where the astronomic informations are defined.
   * @param startTimeSeconds : The initial time-stamp of the time reference of the astronomic informations. It is a
   *                         time-stamp since the epoch.
   */
  public AstroInfosFactory(/*@NotNull*/ final Method method, final double latitudeRad, final long startTimeSeconds) {

    this(method, latitudeRad, new SecondsSinceEpoch(startTimeSeconds));
  }
  
  /**
   * @param aConstName   : A tidal constituent name to validate.
   * @param validTcNames : Array of valid tidal constituents names.
   * @return true if aConstName is found in tcNames
   */
  public final static boolean validateConstName(final String aConstName, final String[] validTcNames) {
    
    int it = 0;
    boolean found = false;
    
    try {
      aConstName.length();
      
    } catch (NullPointerException e) {
      
      staticLog.error("AstroInfosFactory validateConstName: aConstName==null !!");
      throw new RuntimeException(e);
    }
    
    try {
      validTcNames.hashCode();
      
    } catch (NullPointerException e) {
      
      staticLog.error("AstroInfosFactory validateConstName: validTcNames==null !!");
      throw new RuntimeException(e);
    }
    
    while (it < validTcNames.length && !found) {
      found = aConstName.equals(validTcNames[it++]);
    }
    
    return found;
  }
  
  public final double computeTidalPrediction(final long timeStampSeconds,
                                             /*@NotNull @Size(min = 1)*/ final Constituent1DData constituent1DData) {
    
    //--- NOTE 1: Need to pass the time stamp difference to this.updateTimeReference.
    //    NOTE 2: This difference could be < 0.
    //    NOTE 3: The method updateTimeReference is inherited by sub-classes.
    //    NOTE 4: No checks for null objects. This methid is used in many loops.
    
    //--- Get the time offset since last astronomic infos. update:
    long updateTimeDiff = timeStampSeconds - this.sse.seconds();
    
    //--- Update astronomic informations only if the absolute value of updateTimeDiff is equal to
    // ASTRO_UDPATE_OFFSET_SECONDS
    if (Math.abs(updateTimeDiff) == ASTRO_UDPATE_OFFSET_SECONDS) {
      
      //--- Udpate time ref:
      this.updateTimeReference(updateTimeDiff);
      
      //--- MUST set updateTimeDiff at 0 after each astronomic informations update.
      updateTimeDiff = 0L;
    }
    
    //--- Local accumulator for the computed tidal amplitudes of every tidal constituents:
    double tidalAmplitudeAcc = 0.0;
    
    for (final Constituent1D constituent1D : constituent1DData.data) {
      
      //--- Accumulate all tidal constituents computed amplitudes.
      tidalAmplitudeAcc += constituent1D.getTidalAmplitude(updateTimeDiff);
    }
    
    return tidalAmplitudeAcc;
  }
  
  //--- For possible future usage:
//    public final double computeTidalPrediction(final long timeStampSeconds, @NotNull @Size(min=1) final Map<String,
//    Constituent1D> tcMap) {
//
//        //--- NOTE 1: Need to pass the time stamp difference to this.updateTimeReference.
//        //    NOTE 2: This difference could be < 0.
//        this.updateTimeReference(timeStampSeconds-this.sse.seconds());
//
//        double tidalAmplitudeAcc= 0.0;
//
//        for (final IConstituentAstro ic : this.infos) {
//            tidalAmplitudeAcc += ic.computeTidalAmplitude(tcMap.get(ic.getName()));
//        }
//
//        return tidalAmplitudeAcc;
//    }
  
  /**
   * @param timePosLIncr : The time increment to add to this.sse to define the new time reference for the astronomic
   *                     informations.
   * @return this AstroInfosFactory object.
   */
  //@NotNull
  public AstroInfosFactory updateTimeReference(final long timePosLIncr) {
    
    if (this.sse == null) {
      
      this.log.error("AstroInfosFactory updateTimeReference: this.sse == null !");
      throw new RuntimeException("AstroInfosFactory updateTimeReference");
    }
    
    //--- NOTE: We have to consider that there is the possibility that
    //          timePosLIncr could be < 0 if the time is going backwards.
    this.sse.incr(timePosLIncr);
    
    return this;
  }
  
  /**
   * @return The size of this.infos array.
   */
  public final int size() {
    return this.infos.length;
  }
}
