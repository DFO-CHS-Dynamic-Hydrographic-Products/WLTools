package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.time.Instant;

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

  // ---
  public MeasurementCustomBundle() {
    this.mcData= null;
  }

  // ---
  public MeasurementCustomBundle(final List<MeasurementCustom> mcDataList) {

    final String mmi= "MeasurementCustom main constructor: ";

    try {
      mcDataList.get(0);

    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    this.mcData= new HashMap<Instant, MeasurementCustom>();

    for (final MeasurementCustom mcIter: mcDataList) {

      this.mcData.put(mcIter.getEventDate(), mcIter);
    }
  }

  // ---
  public MeasurementCustom getAtThisInstant(final Instant eventDate) {

    //final MeasurementCustom mcRet= this.mcData.containsKey() ? this.mcData.get(eventDate): null;
    return this.mcData.containsKey(eventDate) ? this.mcData.get(eventDate) : null;
  }
}
