//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

//import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.WLZE;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.wl.fms.FMSWLMeasurement;

/**
 *
 */

//---
//import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSWLMeasurement;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLZE;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---
//---

/**
 * Class mapping a WLStationTimeNode as a TidalRemnantNode. It is only used for WL stations having
 * a significant tidal influence.
 */
final public class TidalRemnantNode extends WLStationTimeNode implements ILegacyFMS {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.fms.legacy.TidalRemnantNode";

  /**
   * log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * WLZE object to store the tidal remnant component data
   */
  protected WLZE remnant= null;

  /**
   * @param pstrTidalRemnantNode : The WL station TidalRemnantNode(could be null for the 1st time iteration)
   *                             just before in time compared to the SecondsSinceEpoch object.
   * @param secondsSinceEpoch    : A SecondsSinceEpoch object with the next time stamp to use.
   * @param data                 : A List of 4 FMSWLMeasurement(PREDICTION, OBSERVATION, FORECAST, EXT_STORM_SURGE
   *                             (which could be
   *                             NULL)) objects.
   */
  public TidalRemnantNode(final TidalRemnantNode pstrTidalRemnantNode,
                          /*@NotNull*/ final SecondsSinceEpoch secondsSinceEpoch,
                          /*@NotNull @Size(min = 4)*/ final FMSWLMeasurement[] data) {

    super(pstrTidalRemnantNode, secondsSinceEpoch, data);

    final String mmi= "TidalRemnantNode main constructor: ";

    this.remnant= new WLZE(0.0, 0.0);

    slog.debug(mmi+"this=" + this);
    slog.debug(mmi+"this dt=" + this.sse.dateTimeString(true));

    if (pstrTidalRemnantNode != null) {
      slog.debug(mmi+"pstr=" + pstrTidalRemnantNode);
      slog.debug(mmi+"pstr dt=" + pstrTidalRemnantNode.sse.dateTimeString(true));
      slog.debug(mmi+"pstr futr=" + pstrTidalRemnantNode.futr);
    }
  }

  /**
   * @return this.remnant
   */
  //@NotNull
  protected final WLZE getRemnant() {
    return this.remnant;
  }
}
