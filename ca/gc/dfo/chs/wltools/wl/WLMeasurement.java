//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

import java.util.List;
import java.time.Instant;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;

/**
 * abstract wrapper class for the official IWLS package Measurement class.
 */
abstract public class WLMeasurement implements IWLMeasurement {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.wl.WLMeasurement";

  /**
   * static logger utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * To keep track of where we are in a List of WLMeasurement to speed-up searches based on a time-stamp.
   */
  protected int trackIndex= 0;

  /**
   * The wrapped Measurement object(which is only a reference, no new object creation)
   */
  protected MeasurementCustom measurement= null;

  public WLMeasurement() {

    this.trackIndex= 0;
    this.measurement= null;
  }

  /**
   * @param measurement :  The wrapped Measurement object.
   */
  public WLMeasurement(final MeasurementCustom measurement) {
    this.measurement= measurement;
  }

  /**
   * @param msm : A Measurement object
   * @return String : The date-time-stamp String representation of the time-stamp of the  Measurement object argument.
   */
  public static String dateTimeString(/*@NotNull*/ final MeasurementCustom msm) {

    return SecondsSinceEpoch.dtFmtString(msm.getEventDate().getEpochSecond(), true);
  }

  /**
   * Try to recursively find a Measurement having a specific time-stamp
   * in a List in backward mode and set this.measurement with it if it is found.
   *
   * @param seconds    : The specific time-stamp wanted
   * @param startIndex : The start index in the List
   * @param msma       : The Measurement List.
   * @return WLMeasurement : this
   */
  protected final WLMeasurement findWLBackward(/*@Min(0)*/ final long seconds,
                                               /*@Min(0)*/ final int startIndex,
                                               /*@NotNull @Size(min = 2)*/ final List<MeasurementCustom> msma) {
    final String mmi= "findWLBackward: ";

    slog.info(mmi+" Need to be (re-)tested before using this method ! exit 1 !");
    System.exit(1);

    if (startIndex == 0) {

      final MeasurementCustom ms= msma.get(0);
      final long sse= ms.getEventDate().getEpochSecond();

      this.measurement= (sse == seconds) ? ms : null;
      this.trackIndex= 0;

    } else if (startIndex < msma.size()) {

      //--- Verify 1st that the sse wanted is at startIndex
      final MeasurementCustom ms= msma.get(startIndex);
      final long sse= ms.getEventDate().getEpochSecond();

      if (sse == seconds) {

        this.measurement= ms;
        this.trackIndex= startIndex;

      } else if (startIndex > 0) {

        //--- NOTE: recursive call:
        this.findWLBackward(seconds, startIndex - 1, msma);
      }

    } else {

      slog.info(mmi+"startIndex>=msma.size() !");
      this.measurement= null;
      this.trackIndex= 0;
    }

    return this;
  }

  /**
   * Try to recursively find a Measurement having a specific time-stamp in
   * a List in forward mode and set this.measurement with it if it is found
   *
   * @param seconds    : The specific time-stamp wanted
   * @param startIndex : The start index in the List
   * @param msma       : The Measurement List.
   * @return WLMeasurement : this
   */
  protected final WLMeasurement findWLForward(/*@Min(0)*/ final long seconds,
                                              /*@Min(0)*/ final int startIndex,
                                              /*@NotNull @Size(min = 2)*/ final List<MeasurementCustom> msma) {
    //final String mmi= "findWLForward: ";

    //slog.info(mmi+"start: seconds dt wanted="+seconds);
    //          SecondsSinceEpoch.dtFmtString(seconds, true) + ", startIndex=" + startIndex);

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    if (startIndex == (msma.size() - 1)) {

      final MeasurementCustom ms= msma.get(startIndex);
      final long sse= ms.getEventDate().getEpochSecond();

      this.measurement= (sse == seconds) ? ms : null;
      this.trackIndex= startIndex;

    } else if (startIndex <= (msma.size() - 2)) {

      //--- Verify 1st that the sse wanted is at startIndex
      final MeasurementCustom ms= msma.get(startIndex);
      final long sse= ms.getEventDate().getEpochSecond();

      if (sse == seconds) {

        this.measurement= ms;
        this.trackIndex= startIndex;

      } else {

        //--- NOTE: recursive call:
        this.findWLForward(seconds, startIndex + 1, msma);
      }

    } else {

      //slog.info(mmi+"startIndex>=msma.size() !");

      this.measurement= null;
      this.trackIndex= 0;
    }

    //slog.info(mmi+"end: seconds dt wanted="+SecondsSinceEpoch.dtFmtString(seconds, true)+
    //              ", this.trackIndex="+this.trackIndex+", this.measurement object=" + this.measurement.toString());
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    return this;
  }

  // ---
  public final static List<MeasurementCustom> getNearest2TimeNeighMCs(final Instant instantFrom,
                                                                      final long timeIncrToUseSeconds,
                                                                      final double wlDataValue, final double uncertainty) {
    // --- No fool-proof checks here, we need performance
    //     because this method is used in heavy loops.
    List<MeasurementCustom> retListMCs= new ArrayList<MeasurementCustom>();

    final long instantFromSeconds= instantFrom.getEpochSecond();

    final long minTsInPastSeconds= instantFromSeconds - timeIncrToUseSeconds;
    final long maxTsInFutrSeconds= instantFromSeconds + timeIncrToUseSeconds;

    // --- Find the nearest timestamp in the past compared to instantFrom which is consistent with the timeIncrToUseSeconds
    //     argument but it should not be at a time offset differenc that is more than this timeIncrToUseSeconds argument.
    for (long tsIterSeconds= instantFromSeconds;
         tsIterSeconds>= minTsInPastSeconds; tsIterSeconds -= ITimeMachine.SECONDS_PER_MINUTE) {

      // --- Test for the modulo of tsIterSeconds to timeIncrToUseSeconds. A consistent tsIterSeconds
      //     with timeIncrToUseSeconds will give a modulo of 0
      if (tsIterSeconds % timeIncrToUseSeconds == 0) {
        retListMCs.add(new MeasurementCustom(Instant.ofEpochSecond(tsIterSeconds), wlDataValue, uncertainty));
        break;
      }
    }

    // --- Find the nearest timestamp in the future compared to instantFrom which is consistent with the timeIncrToUseSeconds
    //     argument but it should not be at a time offset difference that is more than this timeIncrToUseSeconds argument.
    for (long tsIterSeconds= instantFromSeconds;
         tsIterSeconds<= maxTsInFutrSeconds; tsIterSeconds += ITimeMachine.SECONDS_PER_MINUTE) {

     // --- Test for the modulo of tsIterSeconds to timeIncrToUseSeconds. A consistent tsIterSeconds
     //     with timeIncrToUseSeconds will give a modulo of 0
     if (tsIterSeconds % timeIncrToUseSeconds == 0) {
         retListMCs.add( new MeasurementCustom(Instant.ofEpochSecond(tsIterSeconds), wlDataValue, uncertainty) );
         break;
      }
    }

    return retListMCs;
  }

  // ---
  public final static List<MeasurementCustom>
    removeHFWLOscillations(final long maxTimeIncr, List<MeasurementCustom> wlMcList) {

     final String mmi= "removeHFWLOscillations: ";

     slog.info(mmi+"start");

     try {
       wlMcList.size();

     } catch (NullPointerException npe) {
       throw new RuntimeException(mmi+npe);
     }

     final int nbWLIn= wlMcList.size();

     // --- Simply use a three values moving average
     //     NOTE: time incr. intervall should be no more than 15mins (3mins is better)
     //     otherwise it could produce unrealistic results.
     List<MeasurementCustom> newWLMcList= new ArrayList<MeasurementCustom>(); //wlMcList.size());

     // --- Add the first MeasurementCustom WLO data to the returned List<MeasurementCustom> object.
     newWLMcList.add( new MeasurementCustom(wlMcList.get(0)) );

     int newIdx= 0;

     for (int mcItemIter=1; mcItemIter< wlMcList.size()-1; mcItemIter++) {

       final MeasurementCustom wlMcPrev= wlMcList.get(mcItemIter-1);
       final MeasurementCustom wlMcHere= wlMcList.get(mcItemIter);
       final MeasurementCustom wlMcNext= wlMcList.get(mcItemIter+1);

       // --- Need to avoid applying the moving average if the timestamps
       //     are too far in time (maxTimeIncr threshold) to each other
       final long checkTimeIncrPrev= MeasurementCustom.
         getDataTimeIntervallSecondsDiff(wlMcHere,wlMcPrev);

       final long checkTimeIncrNext= MeasurementCustom.
         getDataTimeIntervallSecondsDiff(wlMcNext,wlMcHere);

       //slog.info(mmi+"checkTimeIncrPrev="+checkTimeIncrPrev);
       //slog.info(mmi+"checkTimeIncrNext="+checkTimeIncrNext);
       //slog.info(mmi+"Debug exit 0");
       //System.exit(0);

       if (checkTimeIncrPrev > maxTimeIncr || checkTimeIncrNext > maxTimeIncr) {
         continue;
       }

       //final double wlOrigValuePrev= wlMcPrev.getValue();
       //final double wlOrigValueHere= wlMcHere.getValue();
       //final double wlOrigValueNext= wlMcNext.getValue();

       final double newWLValue= (wlMcPrev.getValue() + wlMcHere.getValue() + wlMcNext.getValue())/3.0;

       newIdx++;

       newWLMcList.add( new MeasurementCustom(wlMcHere.getEventDate(), newWLValue, wlMcHere.getUncertainty()) );

       //  setValue((wlMcPrev.getValue() + wlMcHere.getValue() + wlMcNext.getValue())/3.0);

       //slog.info(mmi+"wlMcPrev.getValue()="+wlMcPrev.getValue());
       //slog.info(mmi+"wlMcHere.getValue()="+wlMcHere.getValue());
       //slog.info(mmi+"wlMcNext.getValue()="+wlMcNext.getValue());
       //slog.info(mmi+"newWLMcList.get(mcItemIter).getValue()="+newWLMcList.get(newIdx).getValue()+"\n");
       //slog.info(mmi+"Debug exit 0");
       //System.exit(0);
     }

     // --- Add the last MeasurementCustom WLO to the returned List<MeasurementCustom> object.
     newWLMcList.add( new MeasurementCustom( wlMcList.get(wlMcList.size()-1) ) );

     final int nbWLOut= newWLMcList.size();

     slog.info(mmi+"nbWLIn="+nbWLIn);
     slog.info(mmi+"nbWLOut="+nbWLOut);

     slog.info(mmi+"end");
     slog.info(mmi+"Debug exit 0");
     System.exit(0);

     return newWLMcList;
  }

  /**
   * @return Instant : this.measurement.getEventDate()
   */
  public final Instant getInstant() {
    return this.measurement.getEventDate();
  }

  //--- Kept for possible future usage:
//    @Override
//    final public double getZDoubleError() {
//        return this.measurement.getUncertainty();
//    }
//
//
//    @Override
//    final public double getZDoubleValue() {
//        return this.measurement.getValue();
//    }

  /**
   * @return double : (double)this.measurement.getUncertainty()
   */
  @Override
  final public double getDoubleZError() {

    //final String mmi= "getDoubleZError: ";
    //slog.info(mmi+"this.measurement=" + this.measurement);

    return this.measurement.getUncertainty();
  }

  /**
   * @return double : (double)this.measurement.getValue()
   */
  @Override
  final public double getDoubleZValue() {
    return this.measurement.getValue();
  }

//--- Kept for possible future usage
//    /**
//     * Set the Z value error of this.measurement
//     * @param zError
//     * @return double : zError
//     */
//    @Override
//    final public double setZError(@Min(0) final double zError) {
//        this.measurement.setUncertainty(zError);
//        return zError;
//    }
//
//    /**
//     * Set the Z value of this.measurement
//     * @param zValue
//     * @return double : zValue
//     */
//    @Override
//    final public double setZValue(final double zValue) {
//        this.measurement.setValue(zValue);
//        return zValue;
//    }

  /**
   * @return long : return this.measurement seconds since the epoch.
   */
  //@Min(0)
  @Override
  final public long seconds() {
    return this.measurement.getEventDate().getEpochSecond();
  }

  /**
   * Set the Z value and its error of this.measurement
   *
   * @param zValue : The WL Z value to use for the set operation.
   * @param zError : The WL Z error value to use for the set operation.
   * @return WLMeasurement : this
   */
  @Override
  final public WLMeasurement set(final double zValue,
                                 /*@Min(0)*/ final double zError) {

    this.measurement.setValue(zValue);
    this.measurement.setUncertainty(zError);

    return this;
  }
}
