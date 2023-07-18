package ca.gc.dfo.iwls.fmservice.modeling.tides.astro;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---
//import javax.validation.constraints.Min;
//import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

//---

/**
 * Class for one specific 2D tidal constituent data.
 */
final public class Constituent2D {
  
  /**
   * log utility
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * The i(a.k.a West-East) component of the 2D tidal constituent(a.k.a. U in physical oceanography and
   * meteorological jargon)
   */
  protected Constituent1D i = null;
  /**
   * The j(a.k.a South-North) component of the 2D tidal constituent(a.k.a. V in physical oceanography and
   * meteorological jargon)
   */
  protected Constituent1D j = null;
  
  /**
   * Default constructor
   */
  public Constituent2D() {
    this(null, null);
  }

//    /**
//     * @param constituent2D : A Constituent2D object to use to initialize the new one.
//     */
//    public Constituent2D(@NotNull final Constituent2D constituent2D) {
//        this(constituent2D.i,constituent2D.j);
//    }
  
  /**
   * @param constituent1DI :  A Constituent1D object to use to initialize the i component of the new one.
   * @param constituent1DJ :  A Constituent1D object to use to initialize the j component of the new one.
   */
  public Constituent2D(@NotNull final Constituent1D constituent1DI, @NotNull final Constituent1D constituent1DJ) {
    this(constituent1DI.amplitude, constituent1DI.grnwchPhLag, constituent1DJ.amplitude, constituent1DJ.grnwchPhLag);
  }
  
  /**
   * @param iAmplitude   : Amplitude of the i component of a 2D tidal constituent.
   * @param iGrnwchPhLag Greenwich phase lag(radians) of the i component of a 2D tidal constituent.
   * @param jAmplitude   : Amplitude of the j component of a 2D tidal constituent.
   * @param jGrnwchPhLag Greenwich phase lag(radians) of the j component of a 2D tidal constituent.
   */
  public Constituent2D(final double iAmplitude, final double iGrnwchPhLag,
                       final double jAmplitude, final double jGrnwchPhLag) {
    
    //--- NOTE:The Greenwich phase lags are assumed to be in radians here:
    this(iAmplitude, iGrnwchPhLag, jAmplitude, jGrnwchPhLag, false);
    
    //this.i= new Constituent1D(iAmplitude, iGrnwchPhLag);
    //this.j= new Constituent1D(jAmplitude, jGrnwchPhLag);
  }
  
  /**
   * @param iAmplitude   : Amplitude of the i component of a 2D tidal constituent.
   * @param iGrnwchPhLag Greenwich phase lag of the i component of a 2D tidal constituent.
   * @param jAmplitude   : Amplitude of the j component of a 2D tidal constituent.
   * @param jGrnwchPhLag Greenwich phase lag(radians) of the j component of a 2D tidal constituent.
   * @param deg2rad      : boolean to signal that a degrees to radians is needed for the Greenwich phase lags
   */
  public Constituent2D(final double iAmplitude, final double iGrnwchPhLag,
                       final double jAmplitude, final double jGrnwchPhLag, final boolean deg2rad) {
    
    this(iAmplitude, iGrnwchPhLag, jAmplitude, jGrnwchPhLag, deg2rad, null);
  }
  
  /**
   * @param iAmplitude        : Amplitude of the i component of a 2D tidal constituent.
   * @param iGrnwchPhLag      Greenwich phase lag of the i component of a 2D tidal constituent.
   * @param jAmplitude        : Amplitude of the j component of a 2D tidal constituent.
   * @param jGrnwchPhLag      Greenwich phase lag(radians) of the j component of a 2D tidal constituent.
   * @param deg2rad           : boolean to signal that a degrees to radians is needed for the Greenwich phase lags.
   * @param iConstituentAstro : A IConstituentAstro object(could be null).
   */
  public Constituent2D(final double iAmplitude, final double iGrnwchPhLag,
                       final double jAmplitude, final double jGrnwchPhLag, final boolean deg2rad,
                       final IConstituentAstro iConstituentAstro) {
    
    try {
      iConstituentAstro.getName();
      
    } catch (NullPointerException e) {
      
      this.log.error("Constituent2D constructor: iConstituentAstro==null !!");
      throw new RuntimeException(e);
    }
    
    this.i = new Constituent1D(iAmplitude, iGrnwchPhLag, deg2rad, iConstituentAstro);
    this.j = new Constituent1D(jAmplitude, jGrnwchPhLag, deg2rad, iConstituentAstro);
  }
  
  public Constituent2D(@NotNull final Constituent1D constituent1DI, @NotNull final Constituent1D constituent1DJ,
                       final boolean deepCopy) {
    
    try {
      constituent1DI.getAmplitude();
      
    } catch (NullPointerException e) {
      
      this.log.error("Constituent2D constructor: constituent1DI==null !!");
      throw new RuntimeException(e);
    }
    
    try {
      constituent1DJ.getAmplitude();
      
    } catch (NullPointerException e) {
      
      this.log.error("Constituent2D constructor: constituent1DJ==null !!");
      throw new RuntimeException(e);
    }
    
    if (deepCopy) {
      
      //this.log.debug("Constituent2D constructor: (deep) copying Constituent arguments." );
      //this.set(c1dI.amplitude,c1dI.grnwchPhLag,c1dJ.amplitude,c1dJ.grnwchPhLag);
      
      //--- Deep copy:
      this.i.set(constituent1DI.amplitude, constituent1DI.grnwchPhLag, constituent1DI.iConstituentAstro);
      this.j.set(constituent1DJ.amplitude, constituent1DJ.grnwchPhLag, constituent1DJ.iConstituentAstro);
      
    } else {
      
      //this.log.debug("Constituent2D constructor: Simply set Constituent references." );
      
      //--- Using references:
      this.i = constituent1DI;
      this.j = constituent1DJ;
    }
  }
  
  /**
   * @param clearVal : double precision value to use for the clear operation.
   */
  protected final void clear(final double clearVal) {
    
    this.i.clear(clearVal);
    this.j.clear(clearVal);
    
    this.i = this.j = null;
  }
  
  /**
   * @return The i component Constituent1D object.
   */
  public final Constituent1D getI() {
    return this.i;
  }
  
  /**
   * @return The amplitude of the i component Constituent1D object.
   */
  public final double getIAmplitude() {
    return this.i.amplitude;
  }
  
  /**
   * @return The Greenwich phase lag of the i component Constituent1D object.
   */
  public final double getIGrnwchPhLag() {
    return this.i.grnwchPhLag;
  }
  
  /**
   * @return The j component Constituent1D object.
   */
  public final Constituent1D getJ() {
    return this.j;
  }
  
  /**
   * @return The amplitude of the j component Constituent1D object.
   */
  public final double getJAmplitude() {
    return this.j.amplitude;
  }
  
  /**
   * @return The Greenwich phase lag of the j component Constituent1D object.
   */
  public final double getJGrnwchPhLag() {
    return this.j.grnwchPhLag;
  }
  
  /**
   * @param iAmplitude   : Amplitude of the i component of a 2D tidal constituent.
   * @param iGrnwchPhLag Greenwich phase lag of the i component of a 2D tidal constituent.
   * @param jAmplitude   : Amplitude of the j component of a 2D tidal constituent.
   * @param jGrnwchPhLag Greenwich phase lag(radians) of the j component of a 2D tidal constituent.
   * @return The current Constituent2D object this.
   */
  protected Constituent2D set(final double iAmplitude, final double iGrnwchPhLag, final double jAmplitude,
                              final double jGrnwchPhLag) {
    
    this.i.set(iAmplitude, iGrnwchPhLag);
    this.j.set(jAmplitude, jGrnwchPhLag);
    
    return this;
  }

//  protected void set(double IAmplitude,double IGrnwchPhLag,double JAmplitude,double JGrnwchPhLag,final
//  CoordinateReferenceSystem Crs) {
//
//       //super(Crs);
//
//       this.I.set(IAmplitude,IGrnwchPhLag);
//       this.J.set(JAmplitude,JGrnwchPhLag);
//
//       //return this;
//   }
}
