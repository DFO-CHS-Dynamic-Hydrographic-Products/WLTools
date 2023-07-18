//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---
//import javax.validation.constraints.Min;
//import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;

//---

/**
 * Class for one main constituent data.
 */
final public class MainConstituent extends ForemanConstituentAstro {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Only a reference to already existing MainConstituentStatic object:
   */
  protected MainConstituentStatic mainConstituentStatic = null;
  /**
   * double array for intermediate computations storage.
   */
  private double[] mcCosSinAcc = new double[]{0.0, 0.0};
  
  //--- constructor kept for possible future usage:
  // public MainFrmnConstituent() {
  //     this(null);
  // }

//---  constructor kept for possible future usage:
//    public MainConstituent(@NotNull final String name) {
//        super(name);
//        this.mainConstituentStatic= null;
//    }
  
  /**
   * @param mainConstituentStatic : Reference used for this.mainConstituentStatic
   */
  public MainConstituent(final MainConstituentStatic mainConstituentStatic) {
    
    //SNAFU.log(LogLevel.EXTRA,"mainConstituentStatic="+mainConstituentStatic);
    
    super(mainConstituentStatic.getName());
    
    this.log.debug("MainConstituent constr.  : Start");
    
    this.mainConstituentStatic = mainConstituentStatic;
    
    this.log.debug("MainConstituent constr.  : end");
  }

//    //--- copy constructor kept for possible future usage:
//    public MainConstituent(@NotNull final MainConstituent mfc) {
//
//        //--- Deep copy of super class:
//        super(mfc.name,mfc.tidalFrequency,mfc.fNodalModAdj,mfc.astroArgument);
//
//        //--- this.mfcsd just a reference:
//        this.mainConstituentStatic= mfc.mainConstituentStatic;
//    }
  
  /**
   * @return A String representing the contents of the MainConstituent object.
   */
  @Override
  public final String toString() {
    return super.toString() + ", " + this.mainConstituentStatic.toString();
  }
  
  /**
   * Update MainConstituent astronomic informations.
   *
   * @param latPosRadians      : The latitude of the location(WL station OR some grid point)where the tidal
   *                           constituents are defined
   * @param sunMoonEphemerides : The SunMoonEphemerides object to usr for the computation.
   * @return The MainConstituent itself.
   */
  @Override
  final protected MainConstituent update(final double latPosRadians,
                                         /*@NotNull*/ final SunMoonEphemerides sunMoonEphemerides) {
    
    //--- NOTE: This method could be used repetively in many loops so no check for potentially null object
    // sunMoonEphemerides argument
    //          nor on this.mainConstituentStatic.satellites potentially null object.
    
    //--- Need to init each time update is used
    super.init();
    
    //---- Need to convert result from method ForemanAstroInfosFactory.getMainConstTidalFrequency
    //     from cycles per hours to radians per second here.
    this.tidalFrequency = CPH_2_RPS * ForemanAstroInfosFactory.
       getMainConstTidalFrequency(sunMoonEphemerides,this.mainConstituentStatic.doodsonNumbers);
    
    //this.log.debug("update: this.name="+this.getName()+", this.tidalFrequency="+this.tidalFrequency+",this
    // .mainConstituentStatic="+this.mainConstituentStatic);
    
    //---
    this.astroArgument = ForemanAstroInfosFactory.
       getMainConstAstroArgument(this.mainConstituentStatic.phaseCorrection,
                                 sunMoonEphemerides, this.mainConstituentStatic.doodsonNumbers);
    
    if ((this.mainConstituentStatic.satellites != null) && (this.mainConstituentStatic.satellites.length > 0)) {
      
      //this.log.debug("update: this.name="+this.getName()+
      //        ", num sats="+this.mainConstituentStatic.satellites.length+", this.mainConstituentStatic
      //        .phaseCorrection="+this.mainConstituentStatic.phaseCorrection);
      
      this.mcCosSinAcc = ForemanAstroInfosFactory.
         getMainConstCosSinAcc(latPosRadians, sunMoonEphemerides,
                               this.mainConstituentStatic.satellites, this.mcCosSinAcc);
      
      this.fNodalModAdj = Math.sqrt(this.mcCosSinAcc[COS_INDEX] * this.mcCosSinAcc[COS_INDEX] +
                                    this.mcCosSinAcc[SIN_INDEX] * this.mcCosSinAcc[SIN_INDEX]);
      
      //--- NOTE: We have an accumulation here for TmpInfo.VPhaUNodalModAdj:
      this.astroArgument += Math.atan2(this.mcCosSinAcc[SIN_INDEX], this.mcCosSinAcc[COS_INDEX]) / TWO_PI;
    }
    
    //this.log.debug("Done for main const.: "+this.name+"\n");
    
    return this;
  }
}
