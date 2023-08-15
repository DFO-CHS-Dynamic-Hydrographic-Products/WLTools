//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.adjustment;

import java.util.Set;
//import java.util.Map;

/**
 * Comments please!
 */
public interface IWLAdjustment {

  enum Type {
    SPATIAL_LINEAR,  // ---
    SINGLE_LOCATION  // ---
  }

  String [] allowedTypesDef= { Type.SINGLE_LOCATION.name(),
                               Type.SPATIAL_LINEAR.name() };

  Set<String> allowedTypes= Set.of(allowedTypesDef);
}

