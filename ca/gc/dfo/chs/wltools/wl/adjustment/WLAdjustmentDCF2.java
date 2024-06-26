package ca.gc.dfo.chs.wltools.wl.adjustment;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentType;

// ---
final public class WLAdjustmentDCF2 extends WLAdjustmentType {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentDCF2 ";

  /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  public WLAdjustmentDCF2() {
    super();
  }

  // ---
  public List<MeasurementCustom> getAdjustment(final String optionalOutputDir) {

    final String mmi= "getAdjustment: ";

    slog.info(mmi+"Not implemented yet!");
    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);
    
    return null;
  }  
}
