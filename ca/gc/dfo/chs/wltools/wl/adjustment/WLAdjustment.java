package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;

/**
 * Comments please!
 */
final public class WLAdjustment implements IWLAdjustment { //extends <>

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustment";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Comments please!
   */
  public WLAdjustment() {
  }

  /**
   * Parse the main program arguments using a constructor.
   */
  public WLAdjustment(/*NotNull*/ final HashMap<String,String> argsMap) {

    final String mmi=
      "WLAdjustment(final HasMap<String,String> mainProgramOptions) constructor: ";

    slog.info(mmi+"start");

    final Set<String> argsMapKeySet= argsMap.keySet();

    if (!argsMapKeySet.contains("--adjType")) {

      throw new RuntimeException(mmi+"Must have the mandatory prediction location info option: --adjType="+
                                 Type.SPATIAL_LINEAR.name()+" OR --adjType="+Type.SINGLE_STATION.name()+" defined !!");
    }

    final String adjType= argsMap.get("--adjType");

    if (adjType.equals(Type.SINGLE_STATION.name())) {
      throw new RuntimeException(mmi+"The adjustment type "+
                                 Type.SINGLE_STATION.name()+" is not yet ready to be used !!");
    }

    slog.info(mmi+"Will use adjustment type "+adjType);

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    slog.info(mmi+"end");
  }

}

