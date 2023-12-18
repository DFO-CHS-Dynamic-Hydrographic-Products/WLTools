package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.lang.Math;
import java.util.HashMap;
import java.util.HashSet;
import java.time.Instant;
import java.util.TreeSet;
import java.util.SortedSet;

//---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MeasurementCustomBundle: its name says it all except that
 * each MeasurementCustom object is indexed in the private mcData
 * Map attribute by a reference to its own eventData Instant object
 * attribute
 */
final public class MeasurementCustomBundle {

  final static private String whoAmI= "ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle";

  /**
   * private static logger utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  private Map<Instant, MeasurementCustom> mcData= null;

  //private Set<Instant> instantsKeySet= null;
  private SortedSet<Instant> instantsKeySet= null;

  // ---
  public MeasurementCustomBundle() {
    this.mcData= null;
    this.instantsKeySet= null;
  }

  // ---
  public MeasurementCustomBundle(final List<MeasurementCustom> mcDataList) {

    final String mmi= "MeasurementCustom main constructor: ";

    try {
      mcDataList.size();

    } catch (NullPointerException npe) {

      //npe.printStackTrace(System.err);
      throw new RuntimeException(mmi+npe);
    }

    if (mcDataList.size() > 0) {

      this.mcData= new HashMap<Instant, MeasurementCustom>();

      for (final MeasurementCustom mcIter: mcDataList) {
        this.mcData.put(mcIter.getEventDate(), mcIter);
      }

      //this.instantsKeySet= this.mcData.keySet();
      this.instantsKeySet= new TreeSet<Instant>(this.mcData.keySet());

    } else {
      slog.warn(mmi+"Empty mcDataList !! Nothing to do here !!");
    }
  }

 // ---
  public Instant getLeastRecentInstantCopy() {

    final String mmi= "getLeastRecentInstantCopy: ";

    try {
      this.instantsKeySet.first();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    // --- Simply return a copy of this.instantsKeySet.first() with
    //     a zero seconds offset, it is the equivalent of a clone().
    return this.instantsKeySet.first().plusSeconds(0L);
    //return this.instantsKeySet.first();
  }

  // ---
  public Instant getMostRecentInstantCopy() {

    final String mmi= "getMostRecentInstantCopy: ";

    try {
      this.instantsKeySet.last();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    return this.instantsKeySet.last().plusSeconds(0L);
  }

  // --- No fool-proof here, need performancels

  public boolean contains(final Instant anInstant) {
    return this.instantsKeySet.contains(anInstant);
  }

  // --- No fool-proof here, need performance
  public MeasurementCustom getAtThisInstant(final Instant anInstant) {

    // ---
    //return this.mcData.containsKey(eventDate) ? this.mcData.get(eventDate) : null;
    return this.contains(anInstant) ? this.mcData.get(anInstant) : null;
  }

  // --- Can return null!!
  public SortedSet<Instant> getInstantsKeySetCopy() {

    final String mmi= "getInstantsKeySetCopy: ";

    try {
      this.instantsKeySet.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    return (this.instantsKeySet != null) ? new TreeSet(this.instantsKeySet) : null ; //mcData.keySet();
  }

  // --- Can return null !!
  //public SortedSet<Instant> getInstantsKeySet() {
  //  return this.instantsKeySet; //mcData.keySet();
  //}

  // ---
  public MeasurementCustomBundle removeElement(final Instant anInstant) {

    //final String mmi= "removeElement: ";

    // --- No fool-proof checks here, need performance

    //try {
    //  this.mcData.size();
    //} catch (NullPointerException npe) {
    //  throw new RuntimeException(mmi+npe);
    //}

    this.mcData.remove(anInstant);
    return this;
  }

  // ---
  public MeasurementCustom getNearestTSMCWLDataNeighbor(final Instant anInstant,
                                                        final long maxTimeDiffSeconds) {
                                                       //        final MeasurementCustomBundle mcbAtNonValidTimeStamps) {
     // --- No fool-proof checks here, this method is supposed
     //    to be used in heavy loops
     //final String mmi= "getNearestTSMCWLDataNeighbor: ";

     MeasurementCustom retMCObj= null;

     long maxTSDiff= (long) Long.MAX_VALUE;

     //Instant checkInstant= null;

     // --- loop on all the Instants object of this MeasurementCustomBundle object.
     for ( final Instant instantIter: this.instantsKeySet ) { //mcbAtNonValidTimeStamps.getInstantsKeySet()) {

       final long checkTSDiff= Math.abs(anInstant.getEpochSecond() - instantIter.getEpochSecond());

       if (checkTSDiff < maxTSDiff) {
         retMCObj= this.getAtThisInstant(instantIter); //mcbAtNonValidTimeStamps.getAtThisInstant(instantIter);
         maxTSDiff= checkTSDiff;

       } // --- if block
     } // --- for loop

     // --- Return retMCObj only if its timestamo difference with the anInstant Instant
     //     is smaller than timeIncrToUseSeconds
     return (maxTSDiff <= maxTimeDiffSeconds) ? retMCObj : null;
  }

} // --- class scope block
