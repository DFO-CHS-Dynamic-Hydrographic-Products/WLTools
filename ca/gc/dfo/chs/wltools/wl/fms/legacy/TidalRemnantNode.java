package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;

/**
 *
 */

//---

import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSWLMeasurement;
import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
import ca.gc.dfo.iwls.fmservice.modeling.wl.WLZE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//---
//---

/**
 * Class mapping a WLStationTimeNode as a TidalRemnantNode. It is only used for WL stations having
 * a significant tidal influence.
 */
final public class TidalRemnantNode extends WLStationTimeNode implements ILegacyFMS {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * WLZE object to store the tidal remnant component data
   */
  protected WLZE remnant = null;
  
  /**
   * @param pstrTidalRemnantNode : The WL station TidalRemnantNode(could be null for the 1st time iteration)
   *                             just before in time compared to the SecondsSinceEpoch object.
   * @param secondsSinceEpoch    : A SecondsSinceEpoch object with the next time stamp to use.
   * @param data                 : A List of 4 FMSWLMeasurement(PREDICTION, OBSERVATION, FORECAST, EXT_STORM_SURGE
   *                             (which could be
   *                             NULL)) objects.
   */
  public TidalRemnantNode(final TidalRemnantNode pstrTidalRemnantNode,
                          @NotNull final SecondsSinceEpoch secondsSinceEpoch,
                          @NotNull @Size(min = 4) final FMSWLMeasurement[] data) {
    
    super(pstrTidalRemnantNode, secondsSinceEpoch, data);
    this.remnant = new WLZE(0.0, 0.0);
    
    this.log.debug("TidalRemnantNode constr.: this=" + this);
    this.log.debug("TidalRemnantNode constr.: this dt=" + this.sse.dateTimeString(true));
    
    if (pstrTidalRemnantNode != null) {
      this.log.debug("TidalRemnantNode constr.: pstr=" + pstrTidalRemnantNode);
      this.log.debug("TidalRemnantNode constr.: pstr dt=" + pstrTidalRemnantNode.sse.dateTimeString(true));
      this.log.debug("TidalRemnantNode constr.: pstr futr=" + pstrTidalRemnantNode.futr);
    }
  }
  
  /**
   * @return this.remnant
   */
  @NotNull
  protected final WLZE remnant() {
    return this.remnant;
  }
}
