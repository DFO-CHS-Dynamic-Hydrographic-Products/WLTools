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
import java.util.Collections;

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

  private long dataTimeIntervallSeconds= -1;

  // ---
  public MeasurementCustomBundle() {
    this.mcData= null;
    this.instantsKeySet= null;
    this.dataTimeIntervallSeconds= -1;
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

	// --- Use a copy for the Instant key instead of a reference.
	//     Behavior should be the same.
	this.mcData.put(mcIter.getEventDate().plusSeconds(0L), mcIter);
	//this.mcData.put(mcIter.getEventDate(), mcIter);
      }

      // --- Use a (Thread safe) TreeSet<Instant> here in order
      //     to be sure to have the Instant objects in increasing order
      //     in this.instantsKeySet
      this.instantsKeySet= Collections
	.synchronizedSortedSet(new TreeSet<Instant>(this.mcData.keySet()));

      if (mcDataList.size() >= 2) {
      
        // --- Note: this is useless for WLO data because the time incr. between sucessive data
        //     can be non-constant in case WLO data are missing
	this.dataTimeIntervallSeconds=
	  mcDataList.get(1).getEventDate().getEpochSecond() - mcDataList.get(0).getEventDate().getEpochSecond();

        if (dataTimeIntervallSeconds < 0) {
	  throw new RuntimeException(mmi+"dataTimeIntervallSeconds cannot be < 0 here !");
        }
      }

    } else {
      slog.warn(mmi+"Empty mcDataList !! Nothing to do here !!");
    }
  }

  // ---
  public int size() {

    final String mmi= "size: ";

    try {
      this.instantsKeySet.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    return this.instantsKeySet.size();
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
  //public TreeSet<Instant> getInstantsKeySetCopy() {

    final String mmi= "getInstantsKeySetCopy: ";

    try {
      this.instantsKeySet.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    // --- Copy of this.instantsKeySet. The Collections.synchronizedSortedSet
    //     might be not necessary since this.instantsKeySet is itself thread safe
    //     but better to use it anyways.
    return (this.instantsKeySet != null) ?
	    Collections.synchronizedSortedSet(new TreeSet<Instant>(this.instantsKeySet)) : null ; //mcData.keySet();
  }

  // ---
  public long getDataTimeIntervallSeconds() {
    return this.dataTimeIntervallSeconds;
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

  // --
  static final public MeasurementCustom getSimpleStats(final MeasurementCustomBundle mcb, final SortedSet<Instant> instantsToUse) {

    final String mmi= "getSimpleStats: ";
      
    try {
      mcb.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    final double mcbSize= (double) mcb.size();

    //slog.info(mmi+"mcbSize="+mcbSize);

    if (mcbSize < 2.0 ) {
      throw new RuntimeException(mmi+"MeasurementCustomBundle mcbSize must be at least 2!"); 	
    }

    double nbMc= 0.0;
    double mcValuesAvgAcc= 0.0;
    double mcValuesSquAcc= 0.0;

    final SortedSet<Instant> instantsForIter= (instantsToUse != null ) ? instantsToUse : mcb.getInstantsKeySetCopy();

    for (final Instant mcInstant: instantsForIter) {

      final double mcValue= mcb.getAtThisInstant(mcInstant).getValue();
        
      mcValuesAvgAcc += mcValue;
      mcValuesSquAcc += mcValue*mcValue;

      nbMc += 1.0;
    }

    slog.info(mmi+"nbMc="+nbMc);

    final double mcValuesArithAvg= mcValuesAvgAcc/nbMc;
    
    final double mcValuesStdDev= Math.sqrt(mcValuesSquAcc/nbMc - mcValuesArithAvg*mcValuesArithAvg);

    return new MeasurementCustom(null, mcValuesArithAvg, mcValuesStdDev);
  }

} // --- class scope block
