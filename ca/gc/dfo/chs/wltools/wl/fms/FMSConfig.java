package ca.gc.dfo.chs.wltools.wl.fms;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSDT;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentType;


//---
//---
//---

/**
 * FM Service master class.
 */
final public class FMSConfig extends LegacyFMSDT {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.FMSConfig";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  private String stationId;

  private String mergeTo;

  private Float stdErrSigma;

  private Float durationHours;

  private Float mergeDurationHours;

  /**
   *
   */
  //public FMSConfig(final Float stdErrSigma) {
  //}

  //public FMSConfig(final Map<String,String> argsMap, final WLAdjustmentType wlAdjObj ) {
  public FMSConfig( final WLAdjustmentType wlAdjObj ) {

    //super();

    this.stationId= wlAdjObj.getLocationId();

  }

  final public String getStationId() {
    return this.stationId;
  }

  final public String getMergeTo() {
    return this.mergeTo;
  }

  final public Float getDurationHours() {
    return this.durationHours;
  }

  final public Float getStdErrSigma() {
    return this.stdErrSigma;
  }

  final public Float getMergeDurationHours() {
    return this.mergeDurationHours;
  }

  final public void setStationId(final String stationId) {
    this.stationId= stationId;
  }

  final public void setMergeTo(final String mergeTo) {
    this.mergeTo= mergeTo;
  }

  final public void setDurationHours(final Float durationHours) {
    this.durationHours= durationHours;
  }

  final public void setStdErrSigma(final Float stdErrSigma) {
    this.stdErrSigma= stdErrSigma;
  }

  final public void setMergeDurationHours(final Float mergeDurationHours) {
    this.mergeDurationHours= mergeDurationHours;
  }

  @Override
  public String toString() {
    return "Forecast{" +
        "deltaTMinutes=" + this.getDeltaTMinutes() + ", " +
        "durationHours=" + this.getDurationHours() + ", " +
        "stdErrSigma=" + this.getStdErrSigma() + ", " +
        "mergeTo=" + this.getMergeTo() + ", " +
        "mergeDurationHours=" + this.getMergeDurationHours() + "}";
  }
}
