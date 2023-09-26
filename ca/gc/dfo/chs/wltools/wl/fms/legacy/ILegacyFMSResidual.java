//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

/**
 * Created by Gilles Mercier on 2017-12-04.
 * Modified-adapted for the Spine 2.0 API system by G. Mercier on 2023-09-26
 */
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.wl.fms.legacyILegacyFMS;

//---
//import javax.validation.constraints.Max;

//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import javax.validation.constraints.NotNull;

//---

/**
 * Interface for Legacy temporal WL residual errors covariances computations generic methods declarations.
 */
public interface ILegacyFMSResidual extends ILegacyFMS {

  /**
   * Compute an estimated Legacy WL residual error for a WLStationTimeNode object which time stamp attribute could
   * be in
   * the past or in the future compared with the time stamp of the last valid WLO available.
   *
   * @param wlStationTimeNode : A WLStationTimeNode object.
   * @param apply             : boolean to signal that the estimated WL residual errors are to be applied to get a
   *                          new forecast
   *                          (i.e. the time stamp of the WLStationTimeNode is in the future).
   * @return The WLStationTimeNode object.
   */
  //@NotNull
  WLStationTimeNode estimate(/*@NotNull*/ final WLStationTimeNode wlStationTimeNode, final boolean apply);

  /**
   * Compute the update of the Legacy WL residual error for a WLStationTimeNode which time stamp attribute is in
   * the past
   * compared with the time stamp of the last valid WLO available.
   *
   * @param wlStationTimeNode : A WLStationTimeNode object.
   * @return The WLStationTimeNode object.
   */
  //@NotNull
  WLStationTimeNode update(/*@NotNull*/ final WLStationTimeNode wlStationTimeNode);

  /**
   * To update some Legacy temporal WL residual errors time-weighted parameters.
   *
   * @return This ILegacyFMSResidual object.
   */
  //@NotNull
  ILegacyFMSResidual updateAlphaParameters();
}
