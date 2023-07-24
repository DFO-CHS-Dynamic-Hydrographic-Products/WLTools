package ca.gc.dfo.chs.wltools.tidal.nonstationary;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

import ca.gc.dfo.chs.wltools.tidal.ITidalIO;

// ---
public interface INonStationaryIO extends ITidalIO {

   String TIDAL_CONSTS_JSON_DICT_KEY= "Fluvial+Tidal"; // --- The json dict key for the non-stationary tidal constituents data.

   String TIDAL_CONSTS_JSON_AMP_KEYS= "CS*.amp"; // --- The json dict keys for the amplitudes values
   String TIDAL_CONSTS_JSON_PHA_KEYS= "CS*.pha"; // --- The json dict keys for the phases (greenwich lagged) values
}



