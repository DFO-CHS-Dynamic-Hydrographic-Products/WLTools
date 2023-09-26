//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.FMSAuxCov;
import ca.gc.dfo.chs.wltools.wl.fms.FMSResidualFactory;

//import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSAuxCov;
//import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSResidualFactory;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;

//---

/**
 * For auxiliary temporal residual errors covariances computations of the Legacy forecast method.
 */
final public class LegacyFMSAuxCov extends FMSAuxCov implements ILegacyFMS {

  private final static whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.legacy.LegacyFMSAuxCov";

  /**
   * static log utility.
   */
  private final static Logger log = LoggerFactory.getLogger(whoAmI);

  /**
   * The Legacy forecast method fall back coefficient in case the time-weighted WL surge (or offset) is small.
   */
  protected double fallBack= 0.0; //--- fall-back coefficient (named "b" in model.h from the legacy ODIN-DVFM 1990 kit)

  /**
   * @param timeLagMinutes      : The time lag in minutes of the master FMResidualFactory object of the WL station for
   *                            temporeal errors covariance computations
   * @param fallBack            : The Legacy forecast method fall back coefficient usually coming from a
   *                            ForecastingContext object.
   * @param stationCovarianceId : The WL station SINECO Id related to this Legacy temporal errors covariances
   *                            computations data.
   */
  public LegacyFMSAuxCov(/*@Min(0)*/  final long timeLagMinutes,
                                      final double fallBack,
                         /*@NotNull*/ final String stationCovarianceId) {

    this(timeLagMinutes, fallBack, stationCovarianceId, null);
  }

  /**
   * @param timeLagMinutes      : The time lag in minutes of the master FMResidualFactory object of the WL station for
   *                            temporeal errors covariance computations
   * @param fallBack            : The Legacy forecast method fall back coefficient usually coming from a
   *                            ForecastingContext object.
   * @param stationCovarianceId : The WL station SINECO Id related to this Legacy temporal errors covariances
   *                            computations data.
   * @param fmsResidualFactory  : A FMSResidualFactory type object.
   */
  public LegacyFMSAuxCov(/*@Min(0)*/  final long timeLagMinutes,
                                      final double fallBack,
                         /*@NotNull*/ final String stationCovarianceId,
                         /*@NotNull*/ final FMSResidualFactory fmsResidualFactory) {

    super(timeLagMinutes, stationCovarianceId, fmsResidualFactory);
    this.fallBack= fallBack;
  }

  /**
   * Print this FMAuxCov object contents on the log info stream.
   */
  //@Override
  public final void showMe() {

    final String mmi= "showMe: ":

    super.showMe();

    slog.info(mmi+"this.fallBack=" + this.fallBack);
  }
}
