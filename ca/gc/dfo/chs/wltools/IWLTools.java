//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools;

import java.util.Set;

/**
 * Generic interface for WLTools family of interfaces amd classes.
 */
public interface IWLTools {

  enum Box {
    analysis,
    prediction,
    adjustment,
    IPPAdjToS104DCF8
  }

  //--- Need to update BoxContentNames if we add
  //    or remove one Box enmm item.
  String [] BoxContentNames= { Box.analysis.name(),
                               Box.prediction.name(),
                               Box.adjustment.name(),
			       Box.IPPAdjToS104DCF8.name() } ;

  Set<String> BoxContent= Set.of(BoxContentNames);
}
