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

  /**
   * private logger utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private Instant eventDate;

  //private double value;
  //private double uncertainty;

  private Double value;
  private Double uncertainty;

  public MeasurementCustom() {

     this.eventDate= null;

     this.value= 0.0;
     this.uncertainty= 0.0;
  }

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

  public final void setValue(final Double value) {
     this.value= value;
  }

  public final void setUncertainty(final Double uncertainty) {
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
}
