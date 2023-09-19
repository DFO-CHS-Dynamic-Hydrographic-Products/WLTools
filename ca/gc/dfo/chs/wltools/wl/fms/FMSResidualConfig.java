package ca.gc.dfo.chs.wltools.wl.fms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSTime;
import ca.gc.dfo.chs.wltools.wl.fms.neighborStationCovInfo;

//---
//---
//---

/**
 * FM Service master class.
 */
final public class FMSResidualConfig extends LegacyFMSTime {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.FMSResidualConfig";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  private String method;

  private Float fallBackError;

  private List<NeighborStationCovInfo> neighborStationCovInfo= new LinkedList<neighborStationCovInfo>();

  /**
   * Default constructor.
   */
  public FMSResidualConfig(final String method,  final Float fallBackError,
                           final Float tauHours, final Float deltaTMinutes) {

    super(tauHours, deltaTMinutes);
    this.method= method;
    this.fallBackError= fallBackError;
  }

  final public String getMethod() {
    return this.method;
  }

  final public getFallBackError() {
    return this.fallBackError;
  }

  final public void setMethod(final String method) {
    this.method= method;
  }

  final public void setFallBackError(final Float fallBackError) {
    this.fallBackError= fallBackError;
  }

  @Override
  public String toString() {
    return whoAmi+"{" +
        "method=" + this.getMethod() + ", " +
        "tauhours=" + this.getTauHours() + ", " +
        "deltatminutes=" + this.getDeltaTMinutes() + ", " +
        "fallbackerror=" + this.getFallBackError() // + ", " +
        //"stationCovariance=" + Arrays.toString(this.covariance.toArray()) +
        "}";
  }
}
