//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman;

/**
 * Created by Gilles Mercier  on 2018-01-03.
 */

//---
//import javax.validation.constraints.NotNull;

/**
 * Specific class for one main constituent data from which a shallow water constituent is derived.
 */
final public class ShallowWaterConstituentDerivation {
  
  /**
   * Multiplication coefficient applied to the main constituent frequency, nodal modulation adjustment factor
   * for amplitude and astronomical argument(see ShallowWaterConstituent update method).
   */
  protected double coefficient = 0.0;
  
  /**
   * Just a reference to an already existing MainFrmnConstituent object (NOTE: This reference is supposed to stay
   * the same after being set).
   */
  protected MainConstituent mainConstituent = null;
  
  /**
   * Just a reference to an already existing MainFrmnConstituent object (NOTE: This reference is supposed to stay
   * the same after being set).
   */
  protected MainConstituentStatic mainConstituentStatic = null;
  
  /**
   * Default constructor.
   */
  public ShallowWaterConstituentDerivation() {
    this.init();
  }
  
  /**
   * Object initialization.
   */
  protected final void init() {
    
    this.coefficient = 0.0;
    
    this.mainConstituent = null;
    this.mainConstituentStatic = null;
  }
  
  /**
   * @param coefficient           : Multiplication coefficient applied to the main constituent frequency, nodal
   *                              modulation adjustment factor
   *                              for amplitude and astronomical argument(see ShallowWaterConstituent update method).
   * @param mainConstituentStatic : A MainConstituentStatic object to use for the this.mainConstituentStatic reference.
   */
  public ShallowWaterConstituentDerivation(final double coefficient,
                                           /*@NotNull*/ final MainConstituentStatic mainConstituentStatic) {
    
    this.coefficient = coefficient;
    this.mainConstituentStatic = mainConstituentStatic;
    
    //--- NOTE: this.mainConstituent is always null at this point.
    //          It will be set later with the usage of method this.setMfc
    this.mainConstituent = null;
  }
  
  /**
   * @return A String representing the contents of the ShallowWaterConstituentDerivation object.
   */
  public final String toString() {

    return "this.coefficient=" + this.coefficient +
        ", this.mainConstituentStatic=" + this.mainConstituentStatic.toString() +
        (this.mainConstituent != null ? ", " + this.mainConstituent.toString() : "");
  }
  
  //--- Kept for possible future usage:
//    protected final ShallowWaterConstituentDerivation setMfc(@NotNull final MainConstituent mainConstituent) {
//
//        this.mainConstituent= mainConstituent;
//        return this;
//    }

}
