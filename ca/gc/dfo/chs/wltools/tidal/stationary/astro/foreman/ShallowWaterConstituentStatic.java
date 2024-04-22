//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-03.
 */

//---

import ca.gc.dfo.chs.wltools.tidal.stationary.astro.ConstituentFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.ConstituentFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
import java.util.List;
import java.util.ArrayList;

//import javax.validation.constraints.Min;
//---
//---

/**
 * Specific class for one shallow water constituent static data(see source code of class ConstituentsStaticData) as
 * defined
 * in the dood_num.txt file of the TCWLTools package.
 */
final public class ShallowWaterConstituentStatic extends ConstituentFactory implements IForemanConstituentAstro {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Data from which a shallow water constituent is derived.
   */
  protected ShallowWaterConstituentDerivation[] data = null;
  
  /**
   * Default constructor.
   */
  public ShallowWaterConstituentStatic() {
    this(null);
  }
  
  /**
   * @param name : The name of the ShallowWaterConstituent.
   */
  public ShallowWaterConstituentStatic(/*@NotNull*/ final String name) {
    
    super(name);
    this.data = null;
  }
  
  /**
   * @param name                       : The name of the ShallowWaterConstituent.
   * @param strDataAL                  : Shallow water constituent String data(coming from a file OR a DB).
   * @param mainConstituentStaticArray : An array of main constituent static data from which a shallow water
   *                                   constituent is derived.
   */
  public ShallowWaterConstituentStatic(/*@NotNull*/ final String name,
                                       /*@NotNull @Size(min = 1)*/ final List<String> strDataAL,
                                       /*@NotNull @Size(min = 1)*/ final MainConstituentStatic[] mainConstituentStaticArray) {
    
    super(name);
    
    this.log.debug("Processing shallow water tidal constituent: " + name);
    
    final int numMainConsts = Integer.parseInt(strDataAL.get(0));
    
    //--- Allocate this.data to contains numMainConsts objects:
    this.data = new ShallowWaterConstituentDerivation[numMainConsts];
    
    //--- Reverse iteration in main constituent infos. data:
    int dit = numMainConsts - 1;
    
    //System.out.println("strDataAL="+strDataAL);
    
    while (dit >= 0) {
      
      final int stringIdx = (dit + 1) * DATA_COMBO_LEN;
      
      final MainConstituentStatic
         mainConstituentStatic= (MainConstituentStatic)
             ConstituentFactory.get(strDataAL.get(stringIdx), mainConstituentStaticArray);
      
      //System.out.println("dit="+dit+", stringIdx="+stringIdx +", strDataAL.get(idx)="+strDataAL.get(stringIdx)+",
      // mfc="+mfc);
      
      this.data[dit--]= new
         ShallowWaterConstituentDerivation(Double.parseDouble(strDataAL.get(stringIdx - 1)), mainConstituentStatic);
    }
    
    this.log.debug("Done with shallow water tidal constituent: " + name + ", this.toString()=" + this.toString());
  }
  
  /**
   * @return A String representing the contents of the ShallowWaterConstituentStatic object.
   */
  @Override
  public final String toString() {
    
    //System.out.println("this.name="+this.name);
    
    String dataStr = "";
    
    for (final ShallowWaterConstituentDerivation dit : this.data) {
      dataStr += " [ Main const. derivation: " + (dit.mainConstituent != null ? dit.mainConstituent.toString() : "") + " ]";
    }
    
    return ", " + this.getClass() + ", " + super.toString() + ", " + dataStr;
  }

  //--- Return a List of the names of the main constituents from which this
  //    shallow water constiuents derives.
  final public List<String> getMainConstituentsNamesList() {

    List<String> mcNamesList= new ArrayList<String>();

    for (final ShallowWaterConstituentDerivation shallowWaterConstituentDerivation : this.data) {
	
      mcNamesList.add( shallowWaterConstituentDerivation.mainConstituentStatic.getName()) ;
    }

    return mcNamesList;
  }
  
  /**
   * @param constituentFactoryArrray : An array of ConstituentFactory objects(references to the MainConstituent(s)
   *                                 object(s) from which the
   *                                 shallow water constituent is derived).
   * @return The ShallowWaterConstituentStatic object.
   */
  final protected ShallowWaterConstituentStatic
    setMainConstituentsReferences(/*@NotNull @Size(min = 1)*/ final ConstituentFactory[] constituentFactoryArrray) {
    
    //--- Set the MainConstituent objects references for each ShallowWaterMainConstituentDerivation in this.data;
    
    this.log.debug("setMainConstituentsReferences: this.name=" + this.name);
    
    for (final ShallowWaterConstituentDerivation shallowWaterConstituentDerivation : this.data) {
      
      final String mainConstName = shallowWaterConstituentDerivation.mainConstituentStatic.getName();
      
      this.log.debug("setMainConstituentsReferences: mainConstName=" + mainConstName);
      
      shallowWaterConstituentDerivation.mainConstituent=
          (MainConstituent) ConstituentFactory.get(mainConstName,constituentFactoryArrray);

      try {
	shallowWaterConstituentDerivation.mainConstituent.hashCode();
      } catch (NullPointerException npe) {
	throw new RuntimeException("setMainConstituentsReferences: shallowWaterConstituentDerivation.mainConstituent cannot be null at this point! mainConstName="+
				   mainConstName+", shallow const. this.name=" + this.name);
      }
    }
    
    this.log.debug("setMainConstituentsReferences: this.toString()=" + this.toString());
    
    return this;
  }
}
