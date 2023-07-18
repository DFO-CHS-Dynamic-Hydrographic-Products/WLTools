//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---

import ca.gc.dfo.chs.wltools.util.ITrigonometry;
//import ca.gc.dfo.iwls.fmservice.modeling.util.ITrigonometry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
import java.util.Map;

//import javax.validation.constraints.Min;
//---
//---

/**
 * Class Constituent1D: contains basic tidal constituent data attibutes.
 */
final public class Constituent1D implements ITrigonometry {
  
  /**
   * static log utility
   */
  private final static Logger staticLog = LoggerFactory.getLogger("Constituent1D");
  /**
   * log utility
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  // tidal amplitude computations)
  /**
   * Tidal constituent ammplitude (meters for water levels or meters/seconds for 2D currents components)
   */
  protected double amplitude = 0.0;
  /**
   * Greenwich phase lag (must always be in radians for tidal computations)
   */
  protected double grnwchPhLag = 0.0; //--- MUST BE IN RADIANS(To be used without degrees to radians conversion in
  /**
   * Normally just a reference to an already existing IConstituentAstro object.
   */
  protected IConstituentAstro iConstituentAstro = null;
  
  /**
   * @param constituent1D : A Constituent1D object used for initialization of the new Constituent1D.
   */
  public Constituent1D(/*@NotNull*/ final Constituent1D constituent1D) {

    this(constituent1D.amplitude, constituent1D.grnwchPhLag, false, constituent1D.iConstituentAstro);
  }
  
  /**
   * @param amplitude         : Tidal constituent ammplitude (meters for water levels or meters/seconds for 2D
   *                          currents components)
   * @param grnwchPhLag       : Greenwich phase lag.
   * @param deg2rad           : boolean to signal that a degrees to radians is needed for the Greenwich phase lag.
   * @param iConstituentAstro : IConstituentAstro object.
   */
  public Constituent1D(final double amplitude, final double grnwchPhLag,
                       final boolean deg2rad, final IConstituentAstro iConstituentAstro) {

    this.set(amplitude, grnwchPhLag, deg2rad, iConstituentAstro);
  }
  
  /**
   * @param amplitude         : An amplitude to be set in this object
   * @param grnwchPhLag       A Greenwich phase lag to be set in this object.
   * @param deg2rad           : boolean to signal that a degrees to radians is needed for the Greenwich phase lag.
   * @param iConstituentAstro : IConstituentAstro object to be set in this object(could be null)
   * @return this.
   */
  protected final Constituent1D set(final double amplitude, final double grnwchPhLag,
                                    final boolean deg2rad, final IConstituentAstro iConstituentAstro) {
    
    this.amplitude = amplitude;
    this.grnwchPhLag = (deg2rad == true) ? DEGREES_2_RADIANS * grnwchPhLag : grnwchPhLag;
    
    this.iConstituentAstro = iConstituentAstro;
    
    return this;
  }
  
  /**
   * @param amplitude   : Tidal constituent ammplitude (meters for water levels or meters/seconds for 2D currents
   *                    components)
   * @param grnwchPhLag : Greenwich phase lag.
   */
  public Constituent1D(final double amplitude, final double grnwchPhLag) {
    this(amplitude, grnwchPhLag, false, null);
  }
  
  /**
   * @param amplitude   : Tidal constituent ammplitude (meters for water levels or meters/seconds for 2D currents
   *                    components)
   * @param grnwchPhLag : Greenwich phase lag.
   * @param deg2rad     : boolean to signal that a degrees to radians is needed for the Greenwich phase lag.
   */
  public Constituent1D(final double amplitude, final double grnwchPhLag, final boolean deg2rad) {
    this(amplitude, grnwchPhLag, deg2rad, null);
  }
  
  /**
   * @param amplitude         : Tidal constituent ammplitude (meters for water levels or meters/seconds for 2D
   *                          currents components)
   * @param grnwchPhLag       : Greenwich phase lag.
   * @param iConstituentAstro : IConstituentAstro object.
   */
  public Constituent1D(final double amplitude, final double grnwchPhLag, final IConstituentAstro iConstituentAstro) {
    this(amplitude, grnwchPhLag, false, iConstituentAstro);
  }
  
  /**
   * Populate a Map of String,Constituent1D with relevant AstroInfosFactory informations.
   *
   * @param astroInfosFactory : AstroInfosFactory
   * @param tcMap             : A Map of String,Constituent1D to populate with relevant AstroInfosFactory informations.
   * @return The populated tcMap object.
   */
  public final static Map<String, Constituent1D> setAstroInfosReferences(/*@NotNull*/ final AstroInfosFactory astroInfosFactory,
                                                                         /*@NotNull @Size(min = 1)*/ final Map<String, Constituent1D> tcMap) {
    
    try {
      astroInfosFactory.size();
      
    } catch (NullPointerException e) {
      
      staticLog.error("Constituent1D setAstroInfosReferences: astroInfosFactory==null !!");
      throw new RuntimeException(e);
    }
    
    try {
      astroInfosFactory.infos.hashCode();
      
    } catch (NullPointerException e) {
      
      staticLog.error("Constituent1D setAstroInfosReferences: astroInfosFactory.infos==null !!");
      throw new RuntimeException(e);
    }
    
    try {
      tcMap.size();
      
    } catch (NullPointerException e) {
      
      staticLog.error("Constituent1D setAstroInfosReferences: tcMap==null !!");
      throw new RuntimeException(e);
    }
    
    if (astroInfosFactory.size() != tcMap.size()) {
      
      staticLog.error("Constituent1D setAstroInfosReferences: astroInfosFactory.size() != tcMap.size()");
      throw new RuntimeException("Constituent1D setAstroInfosReferences");
    }
    
    staticLog.debug("Constituent1D setAstroInfosReferences: Start");
    
    for (final IConstituentAstro iConstituentAstro : astroInfosFactory.infos) {
      
      if (iConstituentAstro == null) {
        
        staticLog.error("Constituent1D setAstroInfosReferences: iConstituentAstro == null !");
        throw new RuntimeException("Constituent1D setAstroInfosReferences");
      }
      
      final String constName = iConstituentAstro.getName();
      
      final Constituent1D constituent1D = tcMap.get(constName);
      
      if (constituent1D == null) {
        
        staticLog.error("Constituent1D setAstroInfosReferences: Constituent1D -> " + constName + " not found in " +
            "Map<String,Constituent1D> tcMap !");
        throw new RuntimeException("Constituent1D setAstroInfosReferences");
        
      } else {
        staticLog.debug("setAstroInfosReferences: Found Constituent1D -> " + constName + " in Map<String," +
            "Constituent1D> tcMap");
      }
      
      constituent1D.iConstituentAstro = iConstituentAstro;
    }
    
    staticLog.debug("Constituent1D setAstroInfosReferences: end");
    
    return tcMap;
  }
  
  /**
   * @param clearVal : Value to use to clear this contents.
   */
  protected final void clear(final double clearVal) {
    this.set(clearVal, clearVal, null);
  }
  
  //--- For possible future usage.
//    /**
//     * @return The tidal amplitude computed with this Constituent1D object.
//     */
//    public final double getTidalAmplitude() {
//
//        try {
//            this.iConstituentAstro.getName();
//
//        } catch (NullPointerException e) {

//            this.log.error("Constituent1D getGrnwchPhLag: this.iConstituentAstro==null !!");
//            throw new RuntimeException(e);
//        }
//
////        if (this.iConstituentAstro ==null) {
////            this.log.error("Constituent1D getGrnwchPhLag: this.iConstituentAstro==null" );
////            throw new RuntimeException("Constituent1D getGrnwchPhLag");
////        }
//
//        // if (!this.iConstituentAstro.getName().equals(this.getName()) ) {
//        // 	  this.log.error("Constituent1D getGrnwchPhLag: Constituent inconsistency between this: "this.name" and
//        this.iConstituentAstro: "+this.iConstituentAstro.getName() );
//        //      throw new RuntimeException("Constituent1D getGrnwchPhLag");
//        // }
//
//        return this.iConstituentAstro.computeTidalAmplitude( this );
//    }
  
  /**
   * @param amplitude         : An amplitude to be set in this object
   * @param grnwchPhLag       A Greenwich phase lag to be set in this object.
   * @param iConstituentAstro : IConstituentAstro object to be set in this object.
   * @return this.
   */
  protected final Constituent1D set(final double amplitude,
                                    final double grnwchPhLag,
                                    final IConstituentAstro iConstituentAstro) {
    
    try {
      iConstituentAstro.getName();
      
    } catch (NullPointerException e) {
      
      this.log.error("Constituent1D set: iConstituentAstro==null !!");
      throw new RuntimeException(e);
    }
    
    //--- NOTE: Assuming that grnwchPhLag is in radians here:
    this.set(amplitude, grnwchPhLag, false, iConstituentAstro);
    
    return this;
  }
  
  /**
   * @return this.amplitude attribute.
   */
  public final double getAmplitude() {
    return this.amplitude;
  }
  
  /**
   * @return this.grnwchPhLag attribute.
   */
  public final double getGrnwchPhLag() {
    return this.grnwchPhLag;
  }
  
  public final double getTidalAmplitude(final long timeIncr) {
    
    //--- WARNING: No check for this.iConstituentAstro here.
    //    We assume that it is not null to speed up things
    //    considering that this method is supposed to be used in loops.

//        try {
//            this.iConstituentAstro.getName();
//
//        } catch (NullPointerException e) {

//            this.log.error("Constituent1D getTidalAmplitude:this.iConstituentAstro==null !!");
//            throw new RuntimeException(e);
//        }
    
    return this.iConstituentAstro.computeTidalAmplitude(timeIncr, this);
  }
  
  /**
   * @param amplitude   : An amplitude to be set in this object
   * @param grnwchPhLag A Greenwich phase lag to be set in this object.
   * @return this.
   */
  protected final Constituent1D set(final double amplitude, final double grnwchPhLag) {
    
    //--- NOTE: Assuming that grnwchPhLag is in radians here:
    this.set(amplitude, grnwchPhLag, false, null);
    
    return this;
  }
  
  /**
   * @param amplitude   : An amplitude to be set in this object
   * @param grnwchPhLag A Greenwich phase lag to be set in this object.
   * @param deg2rad     : boolean to signal that a degrees to radians is needed for the Greenwich phase lag.
   * @return this.
   */
  protected final Constituent1D set(final double amplitude, final double grnwchPhLag, final boolean deg2rad) {
    
    this.set(amplitude, grnwchPhLag, deg2rad, null);
    
    return this;
  }
  
  /**
   * @return A string representing the contents of the current Constituent1D object
   */
  @Override
  public final String toString() {
    
    return this.getClass() + ", " +
        super.toString() + ", " +
        "this.amplitude=" + this.amplitude + ", " +
        "this.grnwchPhLag=" + this.grnwchPhLag + ", " +
        ((this.iConstituentAstro != null) ? this.iConstituentAstro.toString() : "");
  }
  
  //--- For possible future usage.
  //    protected final Constituent1D setAstroInfo(final IConstituent iConstituentAstro) {
//        this.iConstituentAstro= iConstituentAstro;
//        return this;
//    }
  
  //--- For possible future usage.
//    /**
//     * @return The tidal amplitude computed with this Constituent1D object.
//     */
//    public final double getTidalAmplitude() {
//
//        try {
//            this.iConstituentAstro.getName();
//
//        } catch (NullPointerException e) {
//            this.log.error("Constituent1D getTidalAmplitude: this.iConstituentAstro==null !!");
//            throw new RuntimeException("Constituent1D getTidalAmplitude");
//        }
//
////        if (this.iConstituentAstro ==null) {
////            this.log.error("Constituent1D getTidalAmplitude: this.iConstituentAstro==null" );
////            throw new RuntimeException("Constituent1D getTidalAmplitude");
////        }
//
//        // if (!this.iConstituentAstro.getName().equals(this.getName()) ) {
//        // 	 this.log.error("Constituent1D getTidalAmplitude: Constituent inconsistency between this: "this.name"
//        and this.iConstituentAstro: "+this.iConstituentAstro.getName() );
//        //     throw new RuntimeException("Constituent1D getTidalAmplitude");
//        // }
//
//        return this.iConstituentAstro.computeTidalAmplitude( this );
//    }
}
