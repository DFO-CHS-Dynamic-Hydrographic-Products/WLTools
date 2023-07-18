//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-03.
 */

//---
//import javax.validation.constraints.Min;

import ca.gc.dfo.chs.wltools.tidal.stationary.astro.ConstituentFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.ConstituentFactory;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//---
//---
//import ca.gc.dfo.iwls.fmservice.modeling.util.ITrigonometry;

/**
 * Shallow water constituent data.
 */
final public class ShallowWaterConstituent extends ForemanConstituentAstro {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Just a reference to an already existing ShallowWaterConstituentStatic object.
   */
  private ShallowWaterConstituentStatic shallowWaterConstituentStatic = null;

//    //--- constructor kept for possible future usage
//    public ShallowWaterConstituent() {
//        super();
//        this.shallowWaterConstituentStatic= null;
//    }
  
  /**
   * @param staticData              : A ShallowWaterConstituentStatic object.
   * @param constituentFactoryArray : An array of ConstituentFactory objects(references to the MainConstituent(s)
   *                                object(s) from which the
   *                                shallow water constituent is derived).
   */
  public ShallowWaterConstituent(@NotNull final ShallowWaterConstituentStatic staticData,
                                 @NotNull @Size(min = 1) final ConstituentFactory[] constituentFactoryArray) {
    
    super(staticData.getName());
    
    this.log.debug("this.name=" + name);
    
    this.shallowWaterConstituentStatic = staticData.setMainConstituentsReferences(constituentFactoryArray);
    
    this.log.debug(this.toString());
  }
  
  //--- constructor kept for possible future usage
//    public ShallowWaterConstituent(final String constNameStr, final double staticTidalFrequency, final double
//    fNodalModAdj, final double astroArgument) {
//
//        super( constNameStr, staticTidalFrequency, fNodalModAdj, astroArgument);
//
//        this.shallowWaterConstituentStatic= null;
//    }
  
  /**
   * @return A String representing the contents of the ShallowWaterConstituent object.
   */
  @Override
  public final String toString() {
    
    return "this.getClass()=" + this.getClass() + ", " + super.toString() + ", " +
        (this.shallowWaterConstituentStatic != null ? this.shallowWaterConstituentStatic.toString() : "");
  }
  
  /**
   * @param latPosRadians      : The latitude in radians of the location where the tidal constituents are defined.
   * @param sunMoonEphemerides : Dummy argument to respect the super-class abstract update method declaration.
   * @return The ShallowWaterConstituent object.
   */
  @Override
  final protected ShallowWaterConstituent update(final double latPosRadians,
                                                 @NotNull final SunMoonEphemerides sunMoonEphemerides) {
    
    //this.log.debug("updating shallow water constituent: "+this.name);
    
    //--- Is the tidal frequency read in Foreman static data file an average ???
    //    Then we must re-compute it from the frequencies of the main constituents
    //    from which this shallow water constituent derives.
    //
    //    So the frequency is 0.0 at this point ??
    //    TODO: Check with ECCC Tcl script for this.
    //this.tidalFrequency= 0.0;
    
    //--- Need to init each time update is used
    super.init();
    
    //--- Loop on the main constituents infos. from which this shallow water constituent derives.
    for (final ShallowWaterConstituentDerivation dit : this.shallowWaterConstituentStatic.data) {
      
      final MainConstituent mainConstituent = dit.mainConstituent;

//            if (mainConstituent== null) {
//                this.log.error("ShallowWaterConstituent update: mainConstituent== null");
//                 throw new RuntimeException("ShallowWaterConstituent update");
//            }
      
      final double coefficient = dit.coefficient;
      
      //--- Accumulate addition of (tidal frequencies * coeff.) from related main constituents.
      this.tidalFrequency += (mainConstituent.tidalFrequency * coefficient);
      
      //--- Accumulate multiplication of fNodalModAdj elevated to power coeff. from related main constituents.
      this.fNodalModAdj *= Math.pow(mainConstituent.fNodalModAdj, Math.abs(coefficient));
      
      //--- Accumulate addition of ( astroArgument * coeff.) from related main constituents.
      this.astroArgument += (mainConstituent.astroArgument * coefficient);
    }
    
    //this.log.debug("this.toString()="+this.toString());
    //this.log.debug("Done with shallow water constituent: "+this.name+"\n");
    
    return this;
  }
}
