package ca.gc.dfo.chs.wltools.wl.fms;
//package ca.gc.dfo.iwls.fmservice.modeling.fms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.wl.fms.FMSResidualFactory;

//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;


/**
 * Generic class for one temporal errors covariances computations data.
 */
abstract public class FMSAuxCov implements IFMS {

  final private static String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.FMSAuxCov";

  /**
   * static log utility.
   */
  final private static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * The FMResidualFactory related to this temporal errors covariances computations data.
   */
  protected FMSResidualFactory residual;

  /**
   * The WL station SINECO Id related to this temporal errors covariances computations data.
   */
  protected String stationCovarianceId;

  /**
   * The time lag in seconds of the master FMResidualFactory object of the WL station for temporeal errors covariance
   * computations:
   */
  private long timeLagSeconds= SECONDS_PER_MINUTE * DEFAULT_AUX_COV_TIME_LAG_MINUTES;

  /**
   * @param timeLagMinutes      : The time lag in minutes of the master FMResidualFactory object of the WL station for
   *                            temporeal errors covariance computations.
   * @param stationCovarianceId : The WL station SINECO Id related to this temporal errors covariances computations
   *                            data.
   * @param fmResidualFactory   : The FMResidualFactory of the WL station SINECO Id related to this temporal errors
   *                            covariances computations data.
   */
  public FMSAuxCov(/*@Min(0)*/ final long timeLagMinutes,
                   /*@NotNull*/ final String stationCovarianceId,
                   /*@NotNull*/ final FMSResidualFactory fmResidualFactory) {

    final String mmi= "FMSAuxCov main constructor: ";

    //this.stationIndex= stationIndex;

    slog.info(mmi+"timeLagMinutes=" + timeLagMinutes + ", stationCovarianceId=" + stationCovarianceId);

    if (timeLagMinutes <= 0L) {

      slog.error(mmi+"lagMinutes <= 0L !");

      throw new RuntimeException(mmi+"Cannot update forecast !!");
    }

    slog.info(mmi+"timeLagMinutes=" + timeLagMinutes + ", stationCovarianceId=" + stationCovarianceId);

    this.residual= fmResidualFactory;
    this.timeLagSeconds= SECONDS_PER_MINUTE * timeLagMinutes;
    this.stationCovarianceId= stationCovarianceId;

    slog.info(mmi+"end");
  }

  /**
   * @param seconds : The time-stamp lag in seconds of the past at which we want a stored WLStationTimeNode.
   * @return The wanted WLStationTimeNode object if found, null object otherwise. NOTE: The client method must then
   * check for a null return.
   */
  public final WLStationTimeNode getLagFMSWLStationTimeNode(/*@Min(0L)*/ final long seconds) {

    final String mmi= "getLagFMSWLStationTimeNode: ";

    slog.info(mmi+"this.residual.getStationId()="+this.residual.getStationId()+", seconds dt="+
              SecondsSinceEpoch.dtFmtString(seconds, true) + ", this.timeLagSeconds=" + this.timeLagSeconds);

    return this.residual.getLagFMSWLStationTimeNode(seconds - this.timeLagSeconds);
  }

  public final String getStationCovarianceId() {
    return this.stationCovarianceId;
  }

  /**
   * @return this.residual.longTermOffset
   */
  public final double getFMSLongTermWLOffset() {
    return this.residual.longTermOffset;
  }

  /**
   * Print this FMAuxCov object contents on the log info stream.
   */
  public void showMe() {
    final String mmi= "ShowMe: ";

    slog.info(mmi+"this.residual.stationId= "+this.residual.stationId+
              ": this.timeLagSeconds=" + this.timeLagSeconds);
  }
}
