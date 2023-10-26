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
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.TideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
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

  // ---
  final public WLAdjustmentFMF singleTimeDepFMFErrorStatsAdj(final String prevFMFASCIIDataFilePath,
                                                             final Map<String, HBCoords> uniqueTGMapObj, final JsonObject mainJsonMapObj) {

    final String mmi= "singleTimeDepFMFErrorStatsAdj: ";

    slog.info(mmi+"start: prevFMFASCIIDataFilePath="+prevFMFASCIIDataFilePath);

    try {
      this.nearestObsData.size();

    } catch (NullPointerException e) {

      slog.error(mmi+"this.nearestObsData is null !!");
      throw new RuntimeException(mmi+e);
    }

    try {
      this.nearestObsData.get(this.location.getIdentity()).size();

    } catch (NullPointerException e) {

      slog.error(mmi+"this.nearestObsData.get(this.location.getIdentity()) is null !!");
      throw new RuntimeException(mmi+e);
    }

    // --- Create a local MeasurementCustomBundle object with the WLO data
    //     List<MeasurementCustom> object for this TG location.
    final MeasurementCustomBundle mcbWLO= new
      MeasurementCustomBundle( this.nearestObsData.get(this.location.getIdentity()) );

    final Set<Instant> wloInstantsSet= mcbWLO.getInstantsKeySet();

    slog.info(mmi+"wloInstantsSet.size()="+wloInstantsSet.size());

    if (wloInstantsSet.size() > 0 ) {

      final int prevFMFIndex= IWLAdjustmentIO.FullModelForecastType.PREVIOUS.ordinal();

      //String prevPrevFMFASCIIDataFilePath=
      this.getH2D2ASCIIWLFProbesData(prevFMFASCIIDataFilePath,
                                   uniqueTGMapObj, mainJsonMapObj, prevFMFIndex);

      //slog.info(mmi+"prevPrevFMFASCIIDataFilePath="+prevPrevFMFASCIIDataFilePath);

      // --- Create a local MeasurementCustomBundle object with the full model
      //     forecast data List<MeasurementCustom> object for this TG location.
      final MeasurementCustomBundle mcbWLFMF= new
         MeasurementCustomBundle( this.nearestModelData.get(prevFMFIndex).get(this.location.getIdentity()) );

      final Set<Instant> wlFMFInstantsSet= mcbWLFMF.getInstantsKeySet();

      double shortTermResErrorsAcc= 0.0;

      // --- Get the WLO-WLFMF residual errors for the PREVIOUS full model forecast data for
      //     its timestamps that are less than SHORT_TERM_FORECAST_TS_THRESHOLD.
      for (final Instant wlFMFInstant: wlFMFInstantsSet) {

        slog.info(mmi+"wlFMFInstant="+wlFMFInstant.toString());
        slog.info(mmi+"Debug exit 0");
        System.exit(0);
      }

    } else {
      slog.info(mmi+"wloInstantsSet.size()== 0 !! No correction will be done for the full model forecast data ");
    }

    slog.info(mmi+"end");
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    return this;
  }
}

