//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

/**
 * Created by G. Mercier on 2017-12-06.
 * Adapted-modified for the Spine 2.0 API system by Mercier on 2023-09-28.
 */

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.wl.fms.FMSResidualFactory;

/////---
//import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSResidualFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import javax.validation.constraints.NotNull;

//---
//---

/**
 * Generic intermediate class dealing with FMSResidualFactory lag nodes.
 * NOTE: This class seems not needed for now because it adds no attributes to the base class
 * FMSResidualFactory but it could be used to extend it if there is a need to do so.
 */
abstract public class LegacyFMSResidualFactory extends FMSResidualFactory implements ILegacyFMSResidual {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.legacy";

  /**
   * log utility
   */
  private final static Logger slog = LoggerFactory.getLogger(whoAmI);

  /**
   * @param stationCode : CHS TG station Id.
   */
  public LegacyFMSResidualFactory(/*@NotNull*/ final String stationId) {

    super(stationId, ResidualMethod.LEGACY);
  }

  /**
   * @param newLagNode : New WLStationTimeNode to use as the last lag node.
   * @return this.lastLagNodeAdded (WLStationTimeNode) object.
   */
  protected final WLStationTimeNode udpateLagNode(final WLStationTimeNode newLagNode) {

    final String mmi= "udpateLagNode: ";

    slog.info(mmi+"newLagNode=" + newLagNode +
              ", newLagNode dt=" + newLagNode.getSse().dateTimeString(true));

    return (this.lastLagNodeAdded= newLagNode);
  }
}
