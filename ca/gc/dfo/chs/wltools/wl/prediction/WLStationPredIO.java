//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created by Gilles Mercier on 2023-08-15.
 *
 */

//---
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

//import javax.validation.constraints.NotNull;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonArrayBuilder;

// ---
import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory;

/**
 * Class for the computation of water level predictions
 */
abstract public class WLStationPredIO implements IWL, IWLStationPredIO {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredIO";

  /**
   * Usual class instance log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //protected String outputDirectory= null;

  protected List<MeasurementCustom> predictionData= null;

  /**
   * Default constuctor:
   */
  public WLStationPredIO() {
    //super();

    //this.outputDirectory= null;
    this.predictionData= null;
  }

  /**
   * comments please!
   */
  protected final WLStationPredIO writeJSONOutputFile(/*@NotNull*/ final String locationId) {

    final String mmi= "writeJSONOutputFile: ";

    slog.info(mmi+"start: locationId="+locationId);

    //try {
    //  this.outputDirectory.length();
    //} catch (NullPointerExceptio npe) {
    //  throw new RuntimeException(mmi+npe);
    //}

    try {
      this.predictionData.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    //try {
    //  IWLToolsIO.getOutputDataFormat();
    //} catch (NullPointerException npe) {
    //  throw new RuntimeException(mmi+npe);
    //}

    WLToolsIO.writeToOutputDir(this.predictionData,
                               IWLToolsIO.Format.CHS_JSON,locationId ); //, this.outputDirectory);
                               //WLToolsIO.getOutputDataFormat(), locationId );
    slog.info(mmi+"end");

    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);

    return this;
  }
}
