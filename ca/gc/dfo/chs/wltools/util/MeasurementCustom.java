package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.time.Instant;
import java.util.TreeSet;
import java.util.SortedSet;

//---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MeasurementCustom:
 * wrapper class that mimics the official IWLS package MeasurementCustom class.
 * But we added some more capabiliies (namely for statistics calculations)
 */
//abstract
final public class MeasurementCustom {

  final static private String whoAmI= "ca.gc.dfo.chs.wltools.util.MeasurementCustom";

  /**
   * private static logger utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  private Instant eventDate;

  //private double value;
  //private double uncertainty;

  private double value;
  private double uncertainty;

  //public final static double MINIMUM_UNCERTAINTY_METERS= 0.005; // --- Half centimeters
  public final static double UNDEFINED_UNCERTAINTY= -77777.0;

  public MeasurementCustom() {

     this.eventDate= null;

     this.value=
       this.uncertainty= 0.0;
  }

  // ---
  public MeasurementCustom(final MeasurementCustom mc) {

    final String mmi= "MeasurementCustom copy constructor: ";

    try {
      mc.getValue();

    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    this.value= mc.getValue();
    this.eventDate= mc.getEventDate();
    this.uncertainty= mc.getUncertainty();
  }

  // ---
  public MeasurementCustom(final Instant eventDate,
                           final Double value, final Double uncertainty) {
    this.value= value;
    this.eventDate= eventDate;
    this.uncertainty= uncertainty;
  }

  // ---
  public final Instant getEventDate() {
    return this.eventDate;
  }

  public final long getEpochSecond() {
    return this.eventDate.getEpochSecond();
  }

  public final Double getValue() {
    return this.value;
  }

  public final Double getUncertainty() {
    return this.uncertainty;
  }

  public final MeasurementCustom setEventDate(final Instant eventDate) {
    this.eventDate= eventDate;
    return this;
  }

  public final MeasurementCustom setValue(final double value) {
    this.value= value;
    return this;
  }

  public final MeasurementCustom setUncertainty(final double uncertainty) {
    this.uncertainty= uncertainty;
    return this;
  }

  // --- IMPORTANT: Do not use this method for obs MeasurementCustom data
  //     since we could have missing data so the result of this method could
  //     be dangerously misleading this kind of data.
  public final static int getDataTimeIntervallSeconds(final List<MeasurementCustom> mcList) {

    final long firstTimeStampSeconds= mcList.get(0).getEventDate().getEpochSecond();
    final long secondTimeStampSeconds= mcList.get(1).getEventDate().getEpochSecond();

    // --- We do not assume that the secondTimeStampSeconds value is larger than the
    //     firstTimeStampSeconds value so usr Math.abs here.
    return (int) Math.abs(secondTimeStampSeconds - firstTimeStampSeconds);
  }

  // ---
  public final static long getDataTimeIntervallSecondsDiff(final MeasurementCustom mc1,final MeasurementCustom mc2) {

    return Math.abs(mc1.getEventDate().getEpochSecond() - mc2.getEventDate().getEpochSecond());
  }

  // ---
  public final static double getValuesArithMean(final List<MeasurementCustom> mcDataList) {

    final String mmi= "getValuesArithMean: ";

    try {
      mcDataList.size();

    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    if (mcDataList.size() == 0) {
      throw new RuntimeException(mmi+"mcDataList.size() == 0 !!");
    }

    double valuesAcc= 0.0;

    for(final MeasurementCustom mcObj: mcDataList) {
      valuesAcc += mcObj.getValue();
    }

    return valuesAcc/mcDataList.size();
  }

  // ---
  static public Map<Long, MeasurementCustom> getTimeDepMCStats( final Map<Long, List<Double>> timeDepDoubleAccListMap ) {

    final String mmi= "getTimeDepMCStats: ";

    try {
      timeDepDoubleAccListMap.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    Map<Long, MeasurementCustom> timeDepMCStats= new HashMap<Long, MeasurementCustom>();

    final SortedSet<Long> timeDepDoubleAccListMapSSet= new TreeSet<Long>(timeDepDoubleAccListMap.keySet());

    for (final Long longObjIdx: timeDepDoubleAccListMapSSet) {

      final List<Double> dValueAccList= timeDepDoubleAccListMap.get(longObjIdx);

      double dValuesAcc= 0.0;
      double dValuesSquAcc= 0.0;

      // --- Assumming that dValueAccList.size() is not 0 here
      final int dValueAccListSize= dValueAccList.size();

      for (int dIter= 0; dIter < dValueAccListSize; dIter++) {

        final double dValue= dValueAccList.get(dIter);

        dValuesAcc += dValue;
        dValuesSquAcc += dValue*dValue;
      }

      final double dValuesAccArithAvg= dValuesAcc/dValueAccListSize;

      final double dValuesAccStdDev= Math.sqrt(dValuesSquAcc/dValueAccListSize - dValuesAccArithAvg*dValuesAccArithAvg) ;

      //slog.info(mmi+"longObjIdx="+longObjIdx);
      //slog.info(mmi+"dValuesAccArithAvg="+dValuesAccArithAvg);
      //slog.info(mmi+"dValuesAccStdDev="+dValuesAccStdDev+"\n");

      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);

      //--- We use a null Instant here to build the MeasurementCustom object because
      //     the Instant is irrelevant here since we use the Long object longObjIdx which is
      //    the time offset in seconds from an unknown Instant. This has to be managed by
      //    the calling method afterwards.
      timeDepMCStats.put(longObjIdx, new MeasurementCustom(null, dValuesAccArithAvg, dValuesAccStdDev));
    }

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    return timeDepMCStats;
  }

  // ---
  @Override
  public final String toString() {

     return whoAmI + " -> { this.eventDate= " +
                            this.eventDate.toString() + ", this.value= "+
                            this.value+ ", this.uncertainty= "+this.uncertainty+"}";
  }

  //// ---
  //public final static MeasurementCustom getNearestTSWLDataNeighbor(final long timeIncrToUseSeconds,
  //                                                                 final long timeStampReferenceSeconds,
  //                                                                 final List<MeasurementCustom> mcsAtNonValidTimeStamps) {
  //  // --- No fool-proofs checks here, we need performance
  //  //      because this method is intended to be used in loops
  //
  //  MeasurementCustom nearestTSWLDataNeighbor= null;
  //
  //  long maxTSDiffSeconds= timeIncrToUseSeconds;  //Long.MAX_VALUE.longValue();
  //
  //  for (final MeasurementCustom mcObj: mcsAtNonValidTimeStamps) {
  //
  //    final long checkTSDiffSeconds=
  //      abs(mcObj.getEventDate.getEpochSecond() - timeStampReferenceSeconds);
  //
  //    // --- Need to have checkTSDiffSeconds < minTSDiffSeconds
  //    if (checkTSDiffSeconds < minTSDiffSeconds) {
  //
  //      nearestTSWLDataNeighbor= mcObj;
  //      minTSDiffSeconds= checkTSDiffSeconds;
  //    }
  //  }
  //
  //  return nearestTSWLDataNeighbor;
  //}

}

