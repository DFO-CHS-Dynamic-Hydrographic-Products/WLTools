//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.adjustment;

import java.util.Set;
//import java.util.Map;

/**
 * Comments please!
 */
public interface IWLAdjustment {

  enum Type {
    IWLS,             // --- Implies direct use of IWLS TG station WLO and WLP data, no spatial interpolation
    MODEL_NEAREST_NEIGHBOR, // --- Implies use of WLF data coming from a model OR some WLP data to interpolate on the desired location
    MODEL_BARYCENTRIC   // --- Implies usr of WLF data coming from a FEM nodel(like H2D2 family OR even NEMO native grid WLF data)
  }

  String [] allowedTypesDef= { Type.IWLS.name(),
                               Type.MODEL_NEAREST_NEIGHBOR.name(),
                               Type.MODEL_BARYCENTRIC.name() };

  Set<String> allowedTypes= Set.of(allowedTypesDef);
}

