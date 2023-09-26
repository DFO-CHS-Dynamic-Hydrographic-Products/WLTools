package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;

/**
 * Created by Gilles Mercier on 2017-12-06.
 */

//---

import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSResidualFactory;
import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

//---
//---

/**
 * Generic intermediate class dealing with FMSResidualFactory lag nodes.
 * NOTE: This class seems not needed for now because it adds no attributes to the base class
 * FMSResidualFactory but it could be used to extend it if there is a need to do so.
 */
abstract public class LegacyFMSResidualFactory extends FMSResidualFactory implements ILegacyFMSResidual {
  
  /**
   * log utility
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  
  /**
   * @param stationCode : A WL station usual SINECO string ID.
   */
  public LegacyFMSResidualFactory(@NotNull final String stationCode) {
    super(stationCode, ResidualMethod.LEGACY);
  }
  
  /**
   * @param newLagNode : New WLStationTimeNode to use as the last lag node.
   * @return The WLStationTimeNode object.
   */
  protected final WLStationTimeNode udpateLagNode(final WLStationTimeNode newLagNode) {
    
    this.log.debug("LegacyFMResidualFactory udpateLagNode: newLagNode=" + newLagNode + ", newLagNode dt=" + newLagNode.getSse().dateTimeString(true));
    
    return (this.lastLagNodeAdded = newLagNode);
  }
}
