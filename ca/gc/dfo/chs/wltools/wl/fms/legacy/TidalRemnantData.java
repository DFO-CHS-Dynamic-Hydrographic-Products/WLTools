//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.numbercrunching.ScalarOps;
import ca.gc.dfo.chs.wltools.wl.fms.FMSTidalRemnantConfig;

/**
 *
 */

////---
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.ScalarOps;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.modeling.fms.TidalRemnant;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---
//---
//---

/**
 * To store the tidal remnant component data.
 */
final public class TidalRemnantData extends LegacyResidualData implements ILegacyFMS {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.legacy";

  /**
   * static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Mean compensation (Using the same variable name as Legacy DVFM nomenclature).
   */
  protected double k1= 0.0; //--- Mean compensation

  /**
   * Local mean of the observations (Using the same variable name as Legacy DVFM nomenclature).
   * NOTE(2018): It is used as an accumulator so it MUST be in double precision!
   */
  protected double k2= 0.0;

  /**
   * Phase correction (Using the same variable name as Legacy DVFM nomenclature).
   */
  protected double eps1= 0.0;

  /**
   * Amplitude(or gain) correction (Using the same variable name as Legacy DVFM nomenclature).
   */
  protected double eps2= 0.0;

  /**
   * Phase correction maximum (Using the same variable name as Legacy DVFM nomenclature).
   */
  protected double eps1Max= 0.0;

  /**
   * Amplitude correction maximum (Using the same variable name as Legacy DVFM nomenclature).
   */
  protected double eps2Max= 0.0; //--- Amplitude correction maximum.

  /**
   * 1.0/Phase correction maximum (Using a multiplication instead of a division by this.eps1Max).
   */
  protected double eps1MaxInv= 0.0;

  /**
   * 1.0/Amplitude correction maximum. (Using a multiplication instead of a division by this.eps2Max).
   */
  protected double eps2MaxInv= 0.0; //--- 1.0/Amplitude correction maximum.

  /**
   * Time factor to compute the WLP(predictions) time derivatives. it is 1.0/(forecast time increment in seconds)
   * instead of the hard-coded value 60 in the Legacy DVFM code.
   */
  private double timeDerivativeFactor= 0.0;

//    //--- For possible future usage
//    public TidalRemnantData() {
//        super();
//        this.init(0.0,0.0);
//    }

  /**
   * @param tidalRemnantCfg : FMSTidalRemnantConfig object
   * @param timeIncrMinutes : The time increment in minutes used for the tidal remnant computations.
   */
  public TidalRemnantData(final FMSTidalRemnantConfig tidalRemnantCfg, final double timeIncrMinutes) {

    this(tidalRemnantCfg.getTauHours(), tidalRemnantCfg.getDeltaTMinutes(),
         tidalRemnantCfg.getMaxEps1(), tidalRemnantCfg.getMaxEps2(), timeIncrMinutes);
  }

  /**
   * @param tauHours        : The number of hours to go back in the past.
   * @param dtMinutes       : The time increment in minutes used for the tidal remnant computations.
   * @param eps1Max         : The eps1Max to use for the tidal remnant computations.
   * @param eps2Max         : The eps2Max to use for the tidal remnant computations.
   * @param timeIncrMinutes : The time increment in minutes used for the tidal remnant computations.
   */
  public TidalRemnantData(final double tauHours, final double dtMinutes,
                          final double eps1Max, final double eps2Max, final double timeIncrMinutes) {

    super(0, tauHours, dtMinutes);

    final String mmi= "TidalRemnantData constructor: ";

    slog.info(mmi+"tauHours=" + tauHours);
    slog.info(mmi+"dtMinutes=" + dtMinutes);
    slog.info(mmi+"eps1Max=" + eps1Max);
    slog.info(mmi+"eps2Max=" + eps2Max);
    slog.info(mmi+"timeIncrMinutes=" + timeIncrMinutes);

    this.init(eps1Max, eps2Max, timeIncrMinutes);
  }

  /**
   * @param eps1Max         : The eps1Max to use.
   * @param eps2Max         : The eps2Max to use.
   * @param timeIncrMinutes : The time increment in minutes used for the tidal remnant computations.
   */
  private void init(final double eps1Max, final double eps2Max, final double timeIncrMinutes) {

    final String mmi= "init: ";

    this.k1= 0.0;
    this.k2= 0.0;

    this.eps1= 0.0;
    this.eps2= 0.0;

    if (eps1Max <= 0.0) {
      slog.error(mmi+"eps1Max <= 0.0 !");
      throw new RuntimeException(mmi);
    }

    if (eps2Max <= 0.0) {
      slog.error(mmi+"eps2Max <= 0.0 !");
      throw new RuntimeException(mmi);
    }

    this.eps1Max= eps1Max;
    this.eps2Max= eps2Max;

    this.eps1MaxInv= 1.0 / this.eps1Max;
    this.eps2MaxInv= 1.0 / this.eps2Max;

    //--- NOTE: The time-derivative factor must be in meters/minutes units according to
    //          the legacy 1990 DVFM source code comments.

    //--- To get WL time derivative in meters/MINUTES
    // this.timeDerivativeFactor= 1.0/timeIncrMinutes;

    //--- To get WL time derivative in meters/seconds to get the expected values by the
    //    the orignal WL forecast theory developed by Smith&Thompson.
    this.timeDerivativeFactor= 1.0 / (timeIncrMinutes * SECONDS_PER_MINUTE);

    this.allocInit(TIDAL_REMNANT_DATA_DIM);
  }

  /**
   * @param betaIndex : The index of the double item to retreive from this.beta.
   * @return The double item retreived from this.beta.
   */
  public final double beta(final int betaIndex) {

    double ret= 0.0;

    try {
      ret= this.beta.at(betaIndex);

    } catch (ArrayIndexOutOfBoundsException e) {

      e.printStackTrace();
      throw new RuntimeException(e);
    }

    return ret;
  }

  //--- getters: no use for now
  //public final double eps1() {
  //    return this.eps1;
  //}
  //public final double eps2() {
  //    return this.eps2;
  //}

  /**
   * @return A new tidal remnant component.
   */
  protected final double computeRemnant() {
    return this.eps1 * this.zX.at(0) + this.eps2 * this.zX.at(1) + this.k1 * this.zX.at(2);
  }

  /**
   * @param predictionMeasurementsList: A List of WLP data.
   * @return this TidalRemnantData object with its invXpX matrix initialized.
   */
  protected final TidalRemnantData initInvXpx(/*@NotNull @Size(min = 1)*/
                                              final List<MeasurementCustom> predictionMeasurementsList) {

    final String mmi= "initInvXpx: ";

    //---  NOTE: This method is the equivalent of the function initialize_remnant
    //           from source file remnant.c of legacy DVFM C source code.
    //           This method MUST be used only once before the start of the remnant
    //           computations time increments loop.

    double squZwAvg= 0.0;
    double squZwTmDrvAvg= 0.0;

    double pastZ= predictionMeasurementsList.get(0).getValue();

    //--- use this.tau(seconds) and time derivative factor(1.0/minutes) to compute the
    //    optimal number of WLP data needed to properly init this.invXpX matrix:
    int nbWlpNeeded= (int) (this.tau * this.timeDerivativeFactor) / SECONDS_PER_MINUTE;

    //int nbWlpNeeded= wlpa.size();

    if (nbWlpNeeded > predictionMeasurementsList.size()) {
      slog.info(mmi+"nbWlpNeeded > wlpa.size() !");
      nbWlpNeeded = predictionMeasurementsList.size();
    }

    for (final MeasurementCustom wlp : predictionMeasurementsList.subList(1, nbWlpNeeded)) {
      //for (final Measurement wlp : wlpa.subList(wlpa.size()-nbWlpNeeded,wlpa.size())) {

      final double zw= wlp.getValue();

      squZwAvg += ScalarOps.square(zw);

      //this.log.debug("initInvXpx: zw-pastZ="+(zw-pastZ));
      //--- Predictions time derivative:
      squZwTmDrvAvg += ScalarOps.square(this.timeDerivativeFactor * (zw - pastZ));

      pastZ = zw;
    }

    //--- Zeroing this.invXpX matrix:
    this.invXpX.init(0.0);

//        this.log.debug("TidalRemnantData initInvXpx: nbWlpNeeded="+nbWlpNeeded);
//        this.log.debug("TidalRemnantData initInvXpx: nbWlpNeeded/squZwAvg="+nbWlpNeeded/squZwAvg);
//        this.log.debug("TidalRemnantData initInvXpx: nbWlpNeeded/squZwTmDrvAvg="+nbWlpNeeded/squZwTmDrvAvg);

    //--- Initialize the diagonal of invXpX matrix.
    this.invXpX.put(0, 0, nbWlpNeeded / squZwTmDrvAvg);
    this.invXpX.put(1, 1, nbWlpNeeded / squZwAvg);
    this.invXpX.put(2, 2, 1.0);

    return this;
  }

  /**
   * Do the setup of this.zX vector.
   *
   * @param wlStationTimeNode : A WLStationTimeNode object.
   * @return this TidalRemnantData object.
   */
  protected final TidalRemnantData zXSetup(/*@NotNull*/ final WLStationTimeNode wlStationTimeNode) {

    //--- Shortcut to the previous-in-time WLStationNode:
    final WLStationTimeNode pstr= (WLStationTimeNode) wlStationTimeNode.getPstr();

    //--- Water level prediction of the WLStationTimeNode:
    final double zw= wlStationTimeNode.get(WLType.PREDICTION).getDoubleZValue();

    //--- NOTE: If pstr is null then we set the time derivative to 0.0
    //          by using the zw of the WLStationTimeNode as the past value for the WL prediction.
    final double pstZw= (pstr != null) ? pstr.get(WLType.PREDICTION).getDoubleZValue() : zw;

    //this.log.debug("wlstn dts="+wlstn.getSse().dateTimeString(true));
    //this.log.debug("zw="+zw);
    //this.log.debug("pstZw="+pstZw);
    //this.log.debug("this.timeDerivativeFactor="+this.timeDerivativeFactor);

    //--- Water level prediction backward time derivative for this.zX 1st(index==0) component:
    this.zX.put(this.timeDerivativeFactor * (zw - pstZw), 0);

    //--- Populate the two other data items of this.zX
    this.zX.put(zw, 1);
    this.zX.put(1.0, 2);

    //this.log.debug("this.zX="+this.zX.toString());

    return this;
  }
}
