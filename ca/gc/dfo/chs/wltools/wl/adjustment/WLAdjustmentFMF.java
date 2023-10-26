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
import ca.gc.dfo.chs.wltools.numbercrunching.Statistics;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
//import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO.InputDataType;

/**
 * Comments please!
 */
abstract public class WLAdjustmentFMF
  extends WLAdjustmentIO implements IWLAdjustment, IWLAdjustmentType {

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

      final int prevFMFIndex= IWLAdjustmentIO.
        FullModelForecastType.PREVIOUS.ordinal();

      final int actuFMFIndex= IWLAdjustmentIO.
        FullModelForecastType.ACTUAL.ordinal();

      //String prevPrevFMFASCIIDataFilePath=
      this.getH2D2ASCIIWLFProbesData(prevFMFASCIIDataFilePath,
                                     uniqueTGMapObj, mainJsonMapObj, prevFMFIndex);

      //slog.info(mmi+"prevPrevFMFASCIIDataFilePath="+prevPrevFMFASCIIDataFilePath);

      final List<MeasurementCustom> prevFMFData= this.
        nearestModelData.get(prevFMFIndex).get(this.location.getIdentity());

      final MeasurementCustom prevFMFDataMC0= prevFMFData.get(0);

      final List<MeasurementCustom> actuFMFData= this.
        nearestModelData.get(actuFMFIndex).get(this.location.getIdentity());

      final MeasurementCustom actuFMFDataMC0= actuFMFData.get(0)

      final long forecastsDurationSeconds=
        actuFMFDataMC0.getEventDate().getEpochSeconds() -
          prevFMFDataMC0.getEventDate().getEpochSeconds();

      final Instant shortTermFMFTSThreshold= prevFMFDataMC0.
        getEventDate().plusSeconds(SHORT_TERM_FORECAST_TS_OFFSET)

      slog.info(mmi+"fmfDataMC0.getEvenDate()="+fmfDataMC0.getEventDate().toString());
      slog.info(mmi+"shortTermFMFTSThreshold="+shortTermFMFTSThreshold.toString());
      slog.info(mmi+"forecastsDurationSeconds="+forecastsDurationSeconds);
      slog.info(mmi+"Debug exit 0");
      System.exit(0);

      // --- Create a local MeasurementCustomBundle object with the previous full model
      //     forecast data List<MeasurementCustom> object for this TG location.
      final MeasurementCustomBundle mcbPrevFMF= new MeasurementCustomBundle( prevFMFData );

      final Set<Instant> prevFMFInstantsSet= mcbPrevFMF.getInstantsKeySet();

      //int shortTerm
      //double shortTermResErrorsAcc= 0.0;
      //double mediumTermResErrorsAcc= 0.0;

      List<Double> shortTermResErrors= new ArrayList<Double>();
      List<Double> mediumTermResErrors= new ArrayList<Double>();

      // --- Get the WLO-WLFMF residual errors for the PREVIOUS full model forecast data for
      //     its timestamps that are less than SHORT_TERM_FORECAST_TS_THRESHOLD.
      for (final Instant prevFMFInstant: prevFMFInstantsSet) {

        //slog.info(mmi+"wlFMFInstant="+wlFMFInstant.toString());

        final MeasurementCustom wloAtInstant= mcbWLO.getAtThisInstant(prevFMFInstant);

        // --- Skip this timestamp when no valid WLO is available for it
        if ( wloAtInstant == null) {
          continue;
        }

        final double fmfResidualError= wloAtInstant.getValue() -
          mcbPrevFMF.getAtThisInstant(prevFMFInstant).getValue();

        if (wlFMFInstant.isBefore(shortTermFMFTSThreshold) ) {
          shortTermResErrors.add(fmfResidualError);

        } else {
          mediumTermResErrors.add(fmfResidualError);
        }

        //slog.info(mmi+"fmfResidualError="+fmfResidualError);
        //slog.info(mmi+"shortTermResErrors.size()="+shortTermResErrors.size());
        //slog.info(mmi+"mediumTermResErrors.size()="+mediumTermResErrors.size());
        //slog.info(mmi+"Debug exit 0");
        //System.exit(0);
      }

      slog.info(mmi+"shortTermResErrors.size()="+shortTermResErrors.size());
      slog.info(mmi+"mediumTermResErrors.size()="+mediumTermResErrors.size());

      final double shortTermResErrorsMean=
        Statistics.getDListValuesAritMean(shortTermResErrors);

      final double mediumTermResErrorsMean=
        Statistics.getDListValuesAritMean(mediumTermResErrors);

      slog.info(mmi+"shortTermResErrorsMean="+shortTermResErrorsMean);
      slog.info(mmi+"mediumTermResErrorMean="+mediumTermResErrorsMean);

      final double shortTermResErrorsTSMiddle= SHORT_TERM_FORECAST_TS_OFFSET/2.0;

      final double mediumTermResErrorsTSMiddle= SHORT_TERM_FORECAST_TS_OFFSET + 

      final double corrEquationSlope= 

    } else {
      slog.info(mmi+"wloInstantsSet.size()== 0 !! No correction done for the full model forecast data ");
    }

    ///slog.info(mmi+"shortTermResErrors.size()="+shortTermResErrors.size());
    //slog.info(mmi+"mediumTermResErrors.size()="+mediumTermResErrors.size());
    slog.info(mmi+"end");
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    return this;
  }
}
