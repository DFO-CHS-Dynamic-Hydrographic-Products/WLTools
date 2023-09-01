//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.adjustment;

import java.util.Set;
//import java.util.Map;

/**
 * Comments please!
 */
public interface IWLAdjustment {

  enum Type {
    Spine, // --- Implies using WLF data coming from a fluvial-tidal model in a river or estuary and two-points WLF errors linear interpolation adjusments.
    IWLS_WLO_QC, // --- Implies using IWLS WLP and WLO data to produce the specific WLF that is used for the short-term WLO quality control by the IWLS
    MODEL_NEAREST_NEIGHBOR, // --- Implies use of WLF data coming from a model OR some WLP data to interpolate on the desired location using near. neigh. interp
    MODEL_BARYCENTRIC   // --- Implies usr of WLF data coming from a FEM nodel(like H2D2 family OR even NEMO native grid WLF data)
  }

  String [] allowedTypesDef= { Type.Spine.name(),
                               Type.IWLS_WLO_QC.name(),
                               Type.MODEL_NEAREST_NEIGHBOR.name(),
                               Type.MODEL_BARYCENTRIC.name() };

  Set<String> allowedTypes= Set.of(allowedTypesDef);

  //enum Target {
  //  WDSFluvial //,
  //  // WDSOceanic
  //}
}
