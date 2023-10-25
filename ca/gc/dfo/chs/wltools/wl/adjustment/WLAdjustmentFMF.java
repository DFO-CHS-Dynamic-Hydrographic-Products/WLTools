package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.TideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
//import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO.InputDataType;

/**
 * Comments please!
 */
abstract public class WLAdjustmentFMF
  extends WLAdjustmentIO implements IWLAdjustmentType {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentFMF";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //private IWLAdjustment.Type adjType= null;

  /**
   * Comments please!
   */
  public WLAdjustmentFMF() {

    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  /**
   * Parse the main program arguments using a constructor.
   */
  public WLAdjustmentFMF(/*NotNull*/ final WLAdjustment.Type adjType,
                          /*NotNull*/ final HashMap<String,String> argsMap) {

    super(adjType, argsMap);

    //final String mmi= "WLAdjustmentFMF main constructor: ";
    //slog.info(mmi+"start");
    //slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
  }

  // --
  final public WLAdjustmentFMF simpleTimeDepFMFErrorStatsAdj() {

    final String mmi= "simpleTimeDepFMFErrorStatsAdj: ";

    slog.info(mmi+"start");

    slog.info(mmi+"end");
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    return this;
  }
}

