//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

// ---
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.time.Instant;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;

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
   * Try to recursively find a MeasurementCustom having a specific time-stamp
   * in a List in backward mode and set this.measurement with it if it is found.
   *
   * @param seconds    : The specific time-stamp (in seconds since epoch) wanted
   * @param startIndex : The start index in the List
   * @param msma       : The Measurement List.
   * @return WLMeasurement : this
   */
  protected final WLMeasurement findWLBackward(/*@Min(0)*/ final long seconds,
                                               /*@Min(0)*/ final int startIndex,
                                               /*@NotNull @Size(min = 2)*/ final List<MeasurementCustom> msma) {
    final String mmi= "findWLBackward: ";
    //slog.info(mmi+" Need to be (re-)tested before using this method ! exit 1 !");
    //System.exit(1);
    //slog.info(mmi+"start: seconds ts="+Instant.ofEpochSecond(seconds).toString());
    //slog.info(mmi+"start:  startIndex="+startIndex);

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

      slog.warn(mmi+"startIndex>=msma.size() !");
      this.measurement= null;
      this.trackIndex= 0;
    }

    //slog.info(mmi+"seconds ts end="+Instant.ofEpochSecond(seconds).toString());
    //slog.info(mmi+"this.measurement="+this.measurement);
    slog.debug(mmi+"end");
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

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
  public final static ArrayList<MeasurementCustom>
    findPossibleWLReplacements(final long timeIncrToUseSeconds,
                               final ArrayList<MeasurementCustom> mcsAtNonValidTimeStamps,
                               final ArrayList<MeasurementCustom> tmpRetListMCs, final long maxTimeDiffSeconds) {

    final String mmi= "findPossibleWLReplacements: ";

    try {
      mcsAtNonValidTimeStamps.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    try {
      tmpRetListMCs.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    final MeasurementCustomBundle mcbTmpRetListMCs= new MeasurementCustomBundle(tmpRetListMCs);
    final MeasurementCustomBundle mcbAtNonValidTimeStamps= new MeasurementCustomBundle(mcsAtNonValidTimeStamps);

    ArrayList<MeasurementCustom> retListMCs= new ArrayList<MeasurementCustom>();

    slog.info(mmi+"start");

    // --- 1st Instant timestamp wanted
    Instant instantIter= tmpRetListMCs.get(0).getEventDate(); //.plusSeconds(timeIncrToUseSeconds);

    // --- last Instant timestamp wanted
    final Instant lastInstant= tmpRetListMCs.
      get(tmpRetListMCs.size()-1).getEventDate();

    if ( instantIter.isAfter(lastInstant)) {
      throw new RuntimeException(mmi+"Cannot have instantIter being in the future compared to lastInstant !!");
    }

    if ( instantIter.equals(lastInstant)) {
      throw new RuntimeException(mmi+"Cannot have instantIter being equal to lastInstant at this point !!");
    }

    if ( (lastInstant.getEpochSecond()-instantIter.getEpochSecond()) < timeIncrToUseSeconds ) {
      throw new RuntimeException(mmi+"Cannot have (lastInstant.getEpochSecond()-instantIter.getEpochSecond()) < timeIncrToUseSeconds !!");
    }

    // --- Add the first MeasurementCustom object in the ArrayList that will be returned.
    retListMCs.add(tmpRetListMCs.get(0));

    slog.info(mmi+"1st Instant wanted="+instantIter.toString());
    slog.info(mmi+"lastInstant wanted="+lastInstant.toString());

    int nbWLReplacementsFound= 0;

    //List<Instant> checkInstants= new ArrayList<Instant>();

    // --- Iterate on the Instant objects for the wanted time interval in seconds
    //     starting at the 1st Instant wanted until the lastInstant wanted
    while(instantIter.isBefore(lastInstant)) {

      // --- Increment instantIter object.
      //     (Recall that the plusSeconds() method returns a copy of the
      //      instantIter object being incremented by timeIncrToUseSeconds)
      instantIter= instantIter.plusSeconds(timeIncrToUseSeconds);

      //if (checkInstants.contains(instantIter)) {
      //  throw new RuntimeException(mmi+"Abnormal behavior: An already processed Instant -> "+
      //                             instantIter.toString()+" was detected in the checkInstants List !!");
      //}

      //slog.info(mmi+"instantIter="+instantIter.toString());
      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);

      if (!mcbTmpRetListMCs.contains(instantIter)) {

        //slog.info(mmi+"instantIter="+instantIter.toString()+" not found in mcbTmpRetListMCs !!");

        final MeasurementCustom nearestTSWLDataNeighbor=
          mcbAtNonValidTimeStamps.getNearestTSMCWLDataNeighbor(instantIter, maxTimeDiffSeconds);

        //MeasurementCustomBundle.
        //getNearestTSMCWLDataNeighbor(instantIter, timeIncrToUseSeconds, mcbAtNonValidTimeStamps);

        if (nearestTSWLDataNeighbor != null) {

          //slog.info(mmi+"nearestTSWLDataNeighbor.getValue()="+nearestTSWLDataNeighbor.getValue());
          //slog.info(mmi+"orig. nearestTSWLDataNeighbor.getEventDate().toString()="+nearestTSWLDataNeighbor.getEventDate().toString());

          // --- This fool-proof check is probably overkill but we never knows.
          if (retListMCs.contains(nearestTSWLDataNeighbor)) {

            slog.warn(mmi+"Abnormal behavior: An already processed MeasurementCustom obj. at Instant -> "+
                     nearestTSWLDataNeighbor.getEventDate().toString()+" was detected in retListMCs List, but continue with the next timestamps !!");

            //slog.warn(mmi+"Debug exit 0");
            //System.exit(0);
            continue;

            //throw new RuntimeException(mmi+"Abnormal behavior: An already processed MeasurementCustom obj. at Instant -> "+
            //                           nearestTSWLDataNeighbor.getEventDate().toString()+" was detected in retListMCs List !!");
          }

          // --- Add this non-null nearestTSWLDataNeighbor MeasurementCustom object in the
          //     returned retListMCs List but with its Instant objec beign set with the
          //    instantIter object which is at the timestamp value we want here. The
          //     timestamps increasing order if kept okay here in the returbed List.
          retListMCs.add(nearestTSWLDataNeighbor.setEventDate(instantIter));

          //checkInstants.add(instantIter);

          nbWLReplacementsFound += 1;
          //slog.info(mmi+"new nearestTSWLDataNeighbor.getEventDate().toString()="+nearestTSWLDataNeighbor.getEventDate().toString());

          // --- Remeove the instantIter key from the mcbAtNonValidTimeStamps MeasurementCustomBundle obj.
          //     to avoid using it more than one time and end-up with some Instant objects duplicates in the
          //     returned List of MeasurementCustom objects which would cause (big) problems elsewhere.
          mcbAtNonValidTimeStamps.removeElement(instantIter);

          //if (nbWLReplacementsFound == 3 ) {
          //  slog.info(mmi+"Debiug exit 0");
          //  System.exit(0);
          //}
        }

        //slog.info(mmi+"Debug exit 0");
        //System.exit(0);

      } else {

        // --- Just add this WL MeasurementCustom object which has a normal timestamp
        //     value in the returned retListMCs List (timestamps increasing order is kept
        //     okay here in the returned List)
        retListMCs.add(mcbTmpRetListMCs.getAtThisInstant(instantIter));

        //checkInstants.add(instantIter);
      }
    }

    // --- Add the last MeasurementCustom object in the ArrayList that will be returned.
    //retListMCs.add(tmpRetListMCs.get(tmpRetListMCs.size()-1));

    slog.info(mmi+"nbWLReplacementsFound="+nbWLReplacementsFound);
    slog.info(mmi+"end");
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    return retListMCs;
  }

  // ---
  public final static List<MeasurementCustom>
    removeHFWLOscillations(final long maxTimeDiffSeconds, List<MeasurementCustom> wlMcList) {

     final String mmi= "removeHFWLOscillations: ";

     slog.info(mmi+"start");

     try {
       wlMcList.size();
     } catch (NullPointerException npe) {
       throw new RuntimeException(mmi+npe);
     }

     final int nbWLIn= wlMcList.size();

     if (nbWLIn < MIN_NUMBER_OF_WL_HFOSC_RMV) {
        throw new RuntimeException(mmi+"Cannot have nbWLIn="+nbWLIn+" < MIN_NUMBER_OF_WL_HFOSC_RMV here !!");
     }
     
     // --- Simply use a three values moving average
     //     NOTE: time incr. intervall should be no more than 15mins (3mins is better)
     //     otherwise it could produce unrealistic results.
     List<MeasurementCustom> newWLMcList= new ArrayList<MeasurementCustom>(); //wlMcList.size());

     // --- Add the first MeasurementCustom WLO data to the returned List<MeasurementCustom> object.
     newWLMcList.add( new MeasurementCustom(wlMcList.get(0)) );

     // --- Loop on all the MesurementCustom objects of the wlMcList input.
     //     except the 1st at index 0 and the last at index wlMcList.size()-1
     for (int mcItemIdx=1; mcItemIdx< wlMcList.size()-1; mcItemIdx++) {

       //--- Extract the 3 contiguous (in terms of indices) MesurementCustom objects
       //    centered on the actual index mcItemIdx
       final MeasurementCustom wlMcPrev= wlMcList.get(mcItemIdx-1);
       final MeasurementCustom wlMcHere= wlMcList.get(mcItemIdx);
       final MeasurementCustom wlMcNext= wlMcList.get(mcItemIdx+1);

       //slog.info(mmi+"wlMcPrev Instant="+wlMcPrev.getEventDate().toString());
       //slog.info(mmi+"wlMcHere Instant="+wlMcHere.getEventDate().toString());
       //slog.info(mmi+"wlMcNext Instant="+wlMcNext.getEventDate().toString());

       // --- Need to avoid applying the moving average if the timestamps
       //     are too far in time (maxTimeDiffSeconds threshold) to each other
       final long checkTimeDiffPrevSeconds=
         MeasurementCustom.getDataTimeIntervallSecondsDiff(wlMcHere,wlMcPrev);

       final long checkTimeDiffNextSeconds=
         MeasurementCustom.getDataTimeIntervallSecondsDiff(wlMcNext,wlMcHere);

       //slog.info(mmi+"checkTimeDiffPrevSeconds="+checkTimeDiffPrevSeconds);
       //slog.info(mmi+"checkTimeDiffNextSeconds="+checkTimeDiffNextSeconds);
       //slog.info(mmi+"Debug exit 0");
       //System.exit(0);

       // --- Skip the HF WL oscillations removal for this timestamp if one time diff
       //     is larger than maxTimeDiffSeconds
       if (checkTimeDiffPrevSeconds > maxTimeDiffSeconds || checkTimeDiffNextSeconds > maxTimeDiffSeconds) {
         continue;
       }

       //final double wlOrigValuePrev= wlMcPrev.getValue();
       //final double wlOrigValueHere= wlMcHere.getValue();
       //final double wlOrigValueNext= wlMcNext.getValue();

       final double newWLValue= (wlMcPrev.getValue() + wlMcHere.getValue() + wlMcNext.getValue())/3.0;

       // --- Use a copy of the wlMcHere.getEventDate() Instant object here, slower but safer.
       newWLMcList.add( new MeasurementCustom(wlMcHere.getEventDate().plusSeconds(0L), newWLValue, wlMcHere.getUncertainty()) );

       //  setValue((wlMcPrev.getValue() + wlMcHere.getValue() + wlMcNext.getValue())/3.0);

       //slog.info(mmi+"wlMcPrev.getValue()="+wlMcPrev.getValue());
       //slog.info(mmi+"wlMcHere.getValue()="+wlMcHere.getValue());
       //slog.info(mmi+"wlMcNext.getValue()="+wlMcNext.getValue());
       //slog.info(mmi+"newWLMcList.get(newWLMcList.size()-1).getValue()="+newWLMcList.get(newWLMcList.size()-1).getValue()+"\n");
       //slog.info(mmi+"newWLMcList.get(newIdx).getValue()="+newWLMcList.get(newIdx).getValue()+"\n");
       //slog.info(mmi+"Debug exit 0");
       //System.exit(0);
     }

     // --- Add the last MeasurementCustom WLO to the returned List<MeasurementCustom> object.
     newWLMcList.add( new MeasurementCustom( wlMcList.get(wlMcList.size()-1) ) );

     final int nbWLOut= newWLMcList.size();

     slog.info(mmi+"nbWLIn="+nbWLIn);
     slog.info(mmi+"nbWLOut="+nbWLOut);

     slog.info(mmi+"end");
     //slog.info(mmi+"Debug exit 0");
     //System.exit(0);

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
