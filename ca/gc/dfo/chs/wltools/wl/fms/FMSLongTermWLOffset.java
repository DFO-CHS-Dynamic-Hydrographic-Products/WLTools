//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

/**
 * Created by G. Mercier on 2018-02-06.
 * Modified-adapted for the Spine 2.0 API system by G. Mercier on 2023-09-26
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;

//---
//import ca.gc.dfo.iwls.fmservice.modeling.wl.IWL;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import javax.validation.constraints.NotNull;

//---
//---

/**
 * Utility class dealing with long term WL offet computations.
 * NOTE: This long term surge is simply the difference between the valid WLO data values
 * and the WLP data averages without any time-weighting for the same time span. It is normally
 * almost zero for WL stations having a significant tidal influence but it can be rather large
 * for upstream St-Lawrence river stations.
 */
abstract public class FMSLongTermWLOffset implements IFMS {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.fms.FMSLongTermWLOffset";

  /**
   * static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Number of prediction(WLP) data counter.
   */
  protected int nbWLP= 0;

  /**
   * Number of valid WLO data counter.
   */
  protected int nbValidWLO= 0;

  /**
   * Valid WLO data values accumulator.
   */
  protected double wloAcc= 0.0;

  /**
   * WLP data values accumulator.
   */
  protected double wlpAcc= 0.0;

  protected double wlpVsWloRMSE= 0.0;

  protected double wlpVsWloRMSEAcc= 0.0;

  /**
   * The computed long term WL offset value.
   */
  protected double longTermOffset= 0.0;

  /**
   * The computed long term WL offset compensation factor.
   */
  protected double longTermOffsetFactor= 0.0;

  /**
   * Default constructor.
   */
  public FMSLongTermWLOffset() {
    this.init();
  }

  /**
   * Object initialization.
   *
   * @return This FMLongTermWLOffset object.
   */
  protected final FMSLongTermWLOffset init() {

    //final String mmi= "init: ";
    //slog.debug(mmi+"start");

    this.nbWLP=
       this.nbValidWLO= 0;

    this.wloAcc=
      this.wlpAcc=
          this.longTermOffset=
            this.longTermOffsetFactor= 0.0;

    this.wlpVsWloRMSE=
      this.wlpVsWloRMSEAcc= 0.0;

    //slog.debug(mmi+"end");

    return this;
  }

  /**
   * @param wlStationTimeNode : A WLStationTimeNode object. Its WLO and WLP data will be used for the update.
   * @return The udpated longTermSurge
   */
  final public double updateFMSLongTermWLOffset(/*@NotNull*/ final WLStationTimeNode wlStationTimeNode) {

    final String mmi= "updateFMSLongTermWLOffset: ";

    //slog.debug(mmi+"Need to implement RMSE or MAE calculation to be able to detect that the obs have a tidal-like signa behavior over 48 hours in the past !!");
    //slog.debug(mmi+"Debug exit 0");
    //System.exit(0);

    this.nbWLP++;

    final double wlpValue= wlStationTimeNode.
      get(IWL.WLType.PREDICTION).getDoubleZValue();

    this.wlpAcc += wlpValue;

    //--- Avoid(as much as we can!) null object SNAFUs
    if (wlStationTimeNode.get(IWL.WLType.OBSERVATION) != null) {

      this.nbValidWLO++;

      final double wloValue= wlStationTimeNode.
        get(IWL.WLType.OBSERVATION).getDoubleZValue();

      this.wloAcc += wloValue;

      this.wlpVsWloRMSEAcc += (wlpValue-wloValue)*(wlpValue-wloValue);
    }

    //--- No time-weighting stuff here, just plain average.
    this.longTermOffset= this.getWLOAvg() - this.getWLPAvg();

    // --- this.longTermOffsetFactor is used to calculate the decay in time
    //     of this.longTermOffset
    final double timeDecayFactorDenom= (1.0 + Math.abs(this.longTermOffset));

    this.longTermOffsetFactor= 1.0 / (timeDecayFactorDenom * timeDecayFactorDenom);

    if (this.nbValidWLO > 0) {
      this.wlpVsWloRMSE= Math.sqrt(this.wlpVsWloRMSEAcc/this.nbValidWLO);
    }

    //final double longTermOffsetAbsVal= Math.abs(this.longTermOffset);
    //--- Take this.wlpVsWloRMSE as the long term offset abs value
    //    if it is larger than longTermOffsetAbsVal. This is done
    //    to avoid using a time decay factor that is too small when
    //    the WLO data of a tide gauge that is located far away from
    //    tidal influence shows a tidal like behavior over the last
    //    48 hours (could happen sometimes for TGs like Jetee1). This
    //    avoids having the WLF-QC signal to go back too quickly to the
    //    predictions values.
    //final double timeDecayFactorDenom=
    //  ( this.wlpVsWloRMSE > longTermOffsetAbsVal) ?
    //    (1.0 + this.wlpVsWloRMSE) : (1.0 + longTermOffsetAbsVal);
    //this.longTermOffsetFactor= 1.0 / (timeDecayFactorDenom * timeDecayFactorDenom );

    slog.debug(mmi+"this.getWLOAvg()=" + this.getWLOAvg());
    slog.debug(mmi+"this.getWLPAvg()=" + this.getWLPAvg());
    slog.debug(mmi+"this.longTermOffset=" + this.longTermOffset);
    slog.debug(mmi+"this.wlpVsWloRMSE="+this.wlpVsWloRMSE);
    slog.debug(mmi+"this.longTermOffsetFactor=" + this.longTermOffsetFactor);

    //slog.debug(mmi+"Debug exit 0");
    //System.exit(0);

    return this.longTermOffset;
  }

  /**
   * @return The Valid WLO data average.
   */
  protected final double getWLOAvg() {
    return (this.nbValidWLO > 0) ? this.wloAcc / (double) this.nbValidWLO : 0.0;
  }

  /**
   * @return The WLP data average.
   */
  protected final double getWLPAvg() {
    return (this.nbWLP > 0) ? this.wlpAcc / (double) this.nbWLP : 0.0;
  }
}
