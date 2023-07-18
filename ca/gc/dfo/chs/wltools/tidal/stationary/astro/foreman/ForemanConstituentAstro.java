//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---
//import javax.validation.constraints.Min;

import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1D;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.ConstituentFactory;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.IConstituentAstro;

//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1D;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.ConstituentFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.IConstituentAstro;
//import ca.gc.dfo.iwls.fmservice.modeling.util.Trigonometry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---
//---

/**
 * Generic class for common attributes of child classes MainConstituent and ShallowWaterConstituent.
 */
abstract public class ForemanConstituentAstro
   extends ConstituentFactory implements IForemanConstituentAstro, IConstituentAstro {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  
  //--- fNodalModAdj : DIMENSIONLESS
  //
  /**
   * constituent tidalFrequency : IN RADIANS/SECONDS
   */
  protected double tidalFrequency = 0.0;
  
  //--- astroArgument : IN RADIANS
  //
  /**
   * It is the dimensionless nodal modulation adjustment factor for amplitude ( named "f" in Foreman's Fortran src
   * code, named fj in his documents )
   */
  protected double fNodalModAdj = F_NODAl_MOD_ADJ_INIT;
  /**
   * It is the astronomical argument (named "v" in Foreman's Fortran src code, named "Vj" in his documents) adjustment
   * for phase PLUS the nodal modulation adjustment factor for phase (named "u" in Foreman's Fortran src code,
   * named "uj"
   * in his documents). Need to be in radians.
   */
  protected double astroArgument = 0.0;
  
  /**
   * Default constructor.
   */
  public ForemanConstituentAstro() {
    super();
  }
  
  /**
   * @param name : Constituent name.
   */
  public ForemanConstituentAstro(/*@NotNull*/ final String name) {
    super(name);
    
    this.log.debug("name=" + name);
  }
  
  /**
   * @param name           : Constituent name.
   * @param tidalFrequency : Tidal frequency to set this.tidalFrequency with.
   * @param fNodalModAdj   :  Nodal modulation adjustment factor for amplitude to set this.fNodalModAdj with.
   * @param astroArgument  : Astronomical argument to set this.astroArgument
   */
  public ForemanConstituentAstro(/*@NotNull*/ final String name,
                                 final double tidalFrequency,
                                 final double fNodalModAdj,
                                 final double astroArgument) {
    
    super(name);
    
    this.set(tidalFrequency, fNodalModAdj, astroArgument);
  }
  
  /**
   * @param tidalFrequency : Tidal frequency to set this.tidalFrequency with.
   * @param fNodalModAdj   :   Nodal modulation adjustment factor for amplitude to set this.fNodalModAdj with.
   * @param astroArgument  :  Astronomical argument to set this.astroArgument with.
   * @return A generic ForemanConstituentAstro object.
   */
  public final ForemanConstituentAstro set(final double tidalFrequency,
                                           final double fNodalModAdj,
                                           final double astroArgument) {
    
    this.tidalFrequency = tidalFrequency;
    this.fNodalModAdj = fNodalModAdj;
    this.astroArgument = astroArgument;
    
    return this;
  }
  
  /**
   * Apply the Trigonometry.getZero2PISandwich method to the astronomical arguments attributes of each
   * ForemanConstituentAstro
   * objects contained in an array.
   *
   * @param foremanConstituentAstroArray : Array of ForemanConstituentAstro objects.
   * @return The array of ForemanConstituentAstro objects.
   */
  protected final static ForemanConstituentAstro[]
    applyZero2PISandwich(/*@NotNull @Size(min = 1)*/ final ForemanConstituentAstro[] foremanConstituentAstroArray) {
    
    //--- The Trigonometry.getZero2PISandwich method must always be applied after the individual FrmnConstituent
    // update method calls.
    //    TODO: Implement parallelization for this loop on ForemanConstituentAstro objects:
    for (final ForemanConstituentAstro fc : foremanConstituentAstroArray) {
      fc.astroArgument = Trigonometry.getZero2PISandwich(fc.astroArgument, true);
    }
    
    return foremanConstituentAstroArray;
  }
  
  /**
   * @param constituent1D : A Constituent1D(with constituent amplitude and Greenwich phase lag attributes) object
   *                      of a specific constituent.
   * @return The amplitude modulated with the cosinus of the difference between this.astroArgument and the
   * constituent1D Greenwich phase lag
   * without use of a time-offset from this.astroArgument(otherwise said: with a time-offset of 0.0)
   */
  @Override
  public final double computeTidalAmplitude(/*@NotNull*/ final Constituent1D constituent1D) {
    
    //--- GrwnchPhLag MUST be in radians here.
    return this.fNodalModAdj * constituent1D.getAmplitude() *
       Math.cos(this.astroArgument - constituent1D.getGrnwchPhLag());
  }
  
  /**
   * @param dTimePos      : The seconds(in double precision) since the last update of the astronomic informations
   *                      related to a specific constituent.
   * @param constituent1D : A Constituent1D(constituent amplitude and Greenwich phase lag) object of a specific
   *                      constituent.
   * @return The amplitude modulated with the cosinus of the difference between this.astroArgument plus a timeoffset
   * and the constituent1D
   * Greenwich phase lag.
   */
  @Override
  public final double computeTidalAmplitude(final double dTimePos,
                                            /*@NotNull*/ final Constituent1D constituent1D) {
    
    //--- NOTE 1: GrwnchPhLag MUST be in radians and dTimePos in seconds here.
    //    NOTE 2: dTimePos could be < 0
    return this.fNodalModAdj * constituent1D.getAmplitude() *
      Math.cos((this.astroArgument + dTimePos * this.tidalFrequency) - constituent1D.getGrnwchPhLag());
  }
  
  /**
   * @param amplitude         : Amplitude of a specific tidal constituent.
   * @param greenwichPhaseLag : Greenwich phase lag of a specific tidal constituent.
   * @return The amplitude modulated with the cosinus of the difference between this.astroArgument and the Greenwich
   * phase lag
   * without use of a time-offset from this.astroArgument(otherwise said: with a time-offset of 0.0)
   */
  @Override
  public final double computeTidalAmplitude(final double amplitude, final double greenwichPhaseLag) {
    
    //--- GrwnchPhLag MUST be in radians here.
    return this.fNodalModAdj * amplitude * Math.cos(this.astroArgument - greenwichPhaseLag);
  }
  
  //--- Greenwich phase lag in degrees:
  // @Override
  // public final double computeTidalAmplitude(final double dTimePos,final Constituent1D c1d) {
  
  //    //--- GrwnchPhLag MUST be in degrees and DTimePos in seconds here.
  //    return this.fNodalModAdj*c1d.getAmplitude()*
  // 	         Math.cos((this.astroArgument + dTimePos*this.tidalFrequency) - Trigo.DEGREES_2_RADIANS*c1d
  // 	         .getGrnwchPhLag());
  // }
  
  //--- Greenwich phase lag in degrees:
  // @Override
  // public final double computeTidalAmplitude(final double dTimePos, final double amplitude, final double
  // grnwchPhLag) {
  
  //    //--- GrwnchPhLag MUST be in degrees and DTimePos in seconds here.
  //    return this.fNodalModAdj*amplitude*
  // 	         Math.cos((this.astroArgument + dTimePos*this.tidalFrequency) - Trigo.DEGREES_2_RADIANS * grnwchPhLag);
  // }
  
  /**
   * @param dTimePos          : The seconds(in double precision) since the last update of the astronomic
   *                          informations related to a specific constituent.
   * @param amplitude         : Amplitude of a specific tidal constituent.
   * @param greenwichPhaseLag : Greenwich phase lag of a specific tidal constituent.
   * @return The amplitude modulated with the cosinus of the difference between this.astroArgument and the Greenwich
   * phase lag
   */
  @Override
  public final double computeTidalAmplitude(final double dTimePos,
                                            final double amplitude,
                                            final double greenwichPhaseLag) {
    
    //--- NOTE 1: GrwnchPhLag MUST be in radians and DTimePos in seconds here.
    //    NOTE 2: dTimePos could be < 0
    return this.fNodalModAdj * amplitude *
       Math.cos((this.astroArgument + dTimePos * this.tidalFrequency) - greenwichPhaseLag);
  }
  
  /**
   * Initialize a ForemanConstituentAstro object.
   *
   * @return The initialized ForemanConstituentAstro object.
   */
  @Override
  public final ForemanConstituentAstro init() {
    
    this.tidalFrequency = 0.0;
    this.fNodalModAdj = F_NODAl_MOD_ADJ_INIT; //--- MUST BE INITIALIZED AT FrmnItf.F_NODAl_MOD_ADJ_INIT==1.0 ->
    // DIMENSIONLESS
    this.astroArgument = 0.0;
    
    return this;
  }
  
  /**
   * @param name           : Constituent name.
   * @param tidalFrequency : Tidal frequency to set this.tidalFrequency with.
   * @param fNodalModAdj   :  Nodal modulation adjustment factor for amplitude to set this.fNodalModAdj with.
   * @param astroArgument  : Astronomical argument to set this.astroArgument.
   * @return A generic ForemanConstituentAstro object.
   */
  public final ForemanConstituentAstro set(/*@NotNull*/ final String name,
                                           final double tidalFrequency,
                                           final double fNodalModAdj,
                                           final double astroArgument) {
    
    super.setName(name);
    //super(Name);
    //this.Sca(Name);
    
    return this.set(tidalFrequency, fNodalModAdj, astroArgument);
  }
  
  /**
   * @return A String representing the contents of the ForemanConstituentAstro object.
   */
  @Override
  public String toString() {
    
    return this.getClass() + ", " + super.toString() + ", Tidal Freq.= " + this.tidalFrequency +
        ", FNodal Mod. Adj.=" + this.fNodalModAdj + ", VPha UNodal Mod. Adj.=" + this.astroArgument;
    
  }
  
  /**
   * Generic ForemanConstituentAstro update method to be implemented by child classes.
   *
   * @param latPosRadians      : Latitude of a location(WL station OR grid point) where tidal constituents are defined.
   * @param sunMoonEphemerides : A SunMoonEphemerides object to update.
   * @return A generic ForemanConstituentAstro object.
   */
  abstract protected ForemanConstituentAstro update(final double latPosRadians,
                                                    /*@NotNull*/ final SunMoonEphemerides sunMoonEphemerides);
  
  //--- For possible future usage.
//    public final double tidalFrequency() {
//        return this.tidalFrequency;
//    }
//
//    public final double fNodalModAdj() {
//        return this.fNodalModAdj;
//    }
//
//    public final double astroArgument() {
//        return this.astroArgument;
//    }

}
