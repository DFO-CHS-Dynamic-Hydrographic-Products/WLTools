//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman;


/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//---

/**
 * Specific class for one main constituent static satellite data.
 */
final public class MainConstituentSatellite implements IForemanConstituentAstro {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Satellite amplitude ratio flag used for the computation of the MainConstituent nodal modulation adjustment
   * factor for amplitude
   * and also for astronomical argument computations
   */
  protected int amplitudeRatioFlag = -1;
  /**
   * Satellite Doodson numbers changes used for the computation of the MainConstituent nodal modulation adjustment
   * factor for amplitude.
   * and also for astronomical argument computations.
   */
  protected int[] doodsonNumChanges = null; //--- Must have Frmn.MC_DOODSON_NUM_CHG_LEN dimension !
  /**
   * Satellite phase correction used in the computation of the MainConstituent nodal modulation adjustment factor
   * for amplitude
   * and also for astronomical argument computations.
   */
  protected double phaseCorrection = 0.0;
  /**
   * Satellite amplitudeRatio used in the computation of the MainConstituent nodal modulation adjustment factor for
   * amplitude
   * and also for astronomical argument computations.
   */
  protected double amplitudeRatio = 0.0;
  
  /**
   * Default contructor.
   */
  public MainConstituentSatellite() {
    this.init();
  }
  
  /**
   * MainConstituentSatellite initialization.
   */
  protected final void init() {
    
    this.amplitudeRatioFlag = 0;
    this.doodsonNumChanges = null;
    
    this.phaseCorrection = 0.0;
    this.amplitudeRatio = 0.0;
  }
  
  /**
   * @param amplitudeRatioFlag : The amplitudeRatioFlag to set this.amplitudeRatioFlag with.
   * @param doodsonNumChanges  : The doodsonNumChanges to set this.doodsonNumChanges with.
   * @param amplitudeRatio     : The amplitudeRatio to set this.amplitudeRatio with.
   * @param phaseCorrection    : The phaseCorrection to set this.phaseCorrection.
   */
  public MainConstituentSatellite(final int amplitudeRatioFlag,
                                  @NotNull @Size(min = MC_DOODSON_NUM_CHG_LEN) final int[] doodsonNumChanges,
                                  final double amplitudeRatio, final double phaseCorrection) {
    
    try {
      doodsonNumChanges.hashCode();
      
    } catch (NullPointerException e) {
      
      this.log.error("MainConstituentSatellite constructor: doodsonNumChanges==null!");
      throw new RuntimeException(e);
    }
    
    this.amplitudeRatioFlag = amplitudeRatioFlag;
    this.doodsonNumChanges = new int[MC_DOODSON_NUM_CHG_LEN];
    
    for (int k = 0; k < MC_DOODSON_NUM_CHG_LEN; k++) {
      
      this.doodsonNumChanges[k] = doodsonNumChanges[k];
    }
    
    this.phaseCorrection = phaseCorrection;
    this.amplitudeRatio = amplitudeRatio;
  }
  
  /**
   * @return A String representing the contents of the MainConstituentSatellite object.
   */
  final public String toString() {
    
    try {
      doodsonNumChanges.hashCode();
      
    } catch (NullPointerException e) {
      
      this.log.error("MainConstituentSatellite toString: doodsonNumChanges==null!");
      throw new RuntimeException(e);
    }
    
    String doodNums = "Doodson Numbers changes = [";
    
    for (int k = 0; k < MC_DOODSON_NUM_CHG_LEN - 1; k++) {
      doodNums = doodNums + this.doodsonNumChanges[k] + ",";
    }
    
    doodNums = doodNums + this.doodsonNumChanges[MC_DOODSON_NUM_CHG_LEN - 1] + "]";
    
    return this.getClass() + ", AmpRatioFlag=" + this.amplitudeRatioFlag +
        ", PhaseCorrection=" + this.phaseCorrection + ", AmplitudeRatio=" + this.amplitudeRatio + ", " + doodNums;
  }
  
  /**
   * @return this.amplitudeRatioFlag
   */
  final public int amplitudeRatioFlag() {
    return this.amplitudeRatioFlag;
  }
  
  /**
   * @param index : Index of the Doodson number wanted.
   * @return The Doodson number wanted.
   */
  final public int getDoodsonNumAt(@Min(0) final int index) {
    
    int ret = -1;
    
    try {
      
      ret = this.doodsonNumChanges[index];
      
    } catch (ArrayIndexOutOfBoundsException e) {
      
      this.log.error("MainConstituentSatellite getDoodsonNumAt: Invalid index for array this.doodsonNumChanges ->" + index);
      throw new RuntimeException(e);
    }
    
    return ret;
  }
  
  /**
   * @return this.phaseCorrection
   */
  final public double getPhaseCorrection() {
    return this.phaseCorrection;
  }
  
  /**
   * @return this.amplitudeRatio
   */
  final public double getAmplitudeRatio() {
    return this.amplitudeRatio;
  }
}
