package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

import java.util.List;
import java.time.Instant;

//---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * MeasurementCustom:
 * wrapper class that mimics the official IWLS package MeasurementCustom class.
 * This is a placeholder of the same name and we use it to be able to use the WL code 
 * developped alongside the IWLS code base. We will then be able to switch back quickly
 * to the official IWLS package MeasurementCustom class usage if needed.
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

     this.eventDate= eventDate;

     this.value= value;
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

  public final void setEventDate(final Instant eventDate) {
     this.eventDate= eventDate;
  }

  public final void setValue(final double value) {
    this.value= value;
  }

  public final void setUncertainty(final double uncertainty) {
     this.uncertainty= uncertainty;
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
  public final static long getDataTimeIntervallSecondsDiff(final MeasurementCustom mc1, final MeasurementCustom mc2) {
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

  @Override
  public final String toString() {

     return whoAmI + " -> { this.eventDate= " +
                            this.eventDate.toString() + ", this.value= "+
                            this.value+ ", this.uncertainty= "+this.uncertainty+"}";
  }
}
