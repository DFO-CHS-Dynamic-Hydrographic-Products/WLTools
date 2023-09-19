package ca.gc.dfo.chs.wltools.wl.fms;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

//---
//---
//---

/**
 * FM Service master class.
 */
final public class NeighborStationCovInfo {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.NeighborStationCovInfo";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  private String neighborStationId= null;

  private Float timeLagMinutes= null;

  private Float fallBackCoeff = null;

  /**
   *
   */
  public NeighborStationCovInfo(final String neighborStationId,
                                final Float timeLagMinutes, final Float fallBackCoeff) {

    this.fallBackCoeff= fallBackCoeff;
    this.timeLagMinutes= timeLagMinutes;
    this.neighborStationId= neighborStationId;
  }

  final public Float getTimeLagMinutes() {
    return this.timeLagMinutes;
  }

  final public Float getFallBackCoeff() {
    return this.fallBackCoeff;
  }

  final public void setTimeLagMinutes(final Float timeLagMinutes) {
    this.timeLagMinutes= timeLagMinutes;
  }

  final public void getFallBackCoeff(final Float fallBackCoeff) {
    this.fallBackCoeff= fallBackCoeff;
  }

  @Override
  public String toString() {
    return whoAmi+"{" +
        "stationId='" + stationId + '\'' +
        ", timeLagMinutes=" + timeLagMinutes +
        ", fallBackCoeff=" + fallBackCoeff +
        "}";
  }
}
