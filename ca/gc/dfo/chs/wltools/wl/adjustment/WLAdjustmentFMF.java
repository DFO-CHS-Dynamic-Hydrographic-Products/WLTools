package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.SortedSet;
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
      this.location.getIdentity();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    // --- Get a local MeasurementCustomBundle object with the WLO data
    //     List<MeasurementCustom> object for this TG location.
    final MeasurementCustomBundle mcbWLO= this.getMcbWLOData();

    // --- Get a copy all the Instant objects of the MeasurementCustomBundle mcbWLO object
    //     as a Set to be able to test if some timestamps are missing for the WLO data
    final SortedSet<Instant> wloInstantsSet= mcbWLO.getInstantsKeySetCopy();

    //slog.info(mmi+"wloInstantsSet.size()="+wloInstantsSet.size());
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    // ---
    if (wloInstantsSet == null ) {
      throw new RuntimeException(mmi+
        "ERROR: wloInstantsSet == null !! It is abnormal that we have no WLO data to use at this point !!");
    }

    slog.info(mmi+"We have WLO data to use -> now produce the FMF simple linear adjustment equation coefficients");

    final int prevFMFIndex= IWLAdjustmentIO.
      FullModelForecastType.PREVIOUS.ordinal();

    final int actuFMFIndex= IWLAdjustmentIO.
      FullModelForecastType.ACTUAL.ordinal();

    if (this.modelForecastInputDataFormat == IWLAdjustmentIO.DataTypesFormatsDef.ECCC_H2D2_ASCII) {

      // --- Read the previous H2D2 full model forecast data
      this.getH2D2ASCIIWLFProbesData(prevFMFASCIIDataFilePath,
                                     uniqueTGMapObj, mainJsonMapObj, 0L, prevFMFIndex);
    } else {
      throw new RuntimeException(mmi+
        "Invalid full model forecast input format -> "+this.modelForecastInputDataFormat.name());
    }

    //slog.info(mmi+"prevPrevFMFASCIIDataFilePath="+prevPrevFMFASCIIDataFilePath);

    final List<MeasurementCustom> prevFMFData= this.
      nearestModelData.get(prevFMFIndex).get(this.location.getIdentity());

    final MeasurementCustom prevFMFDataMC0= prevFMFData.get(0);

    // --- Shortcut to the prevFMFDataMC0.getEventDate() Instant object
    final Instant prevFMFDataMC0Instant= prevFMFDataMC0.getEventDate();

    // --- Get the actual FMF data that we want to adjust.
    final List<MeasurementCustom> actuFMFData= this.
      nearestModelData.get(actuFMFIndex).get(this.location.getIdentity());

    final MeasurementCustom actuFMFDataMC0= actuFMFData.get(0);

    final Instant actuFMFDataMC0Instant= actuFMFDataMC0.getEventDate();

    final long forecastsDurationSeconds= actuFMFDataMC0Instant.
      getEpochSecond() - prevFMFDataMC0Instant.getEpochSecond(); //prevFMFDataMC0.getEventDate().getEpochSecond();

    if (forecastsDurationSeconds < MIN_FULL_FORECAST_DURATION_SECONDS) {
      throw new RuntimeException(mmi+"forecastsDurationSeconds < MIN_FULL_FORECAST_DURATION_SECONDS");
    }

    // --- Define the timestamp limit for the short term WLO-WLFMF residual errors calculation
    final Instant shortTermFMFTSThreshold=
      prevFMFDataMC0Instant.plusSeconds(SHORT_TERM_FORECAST_TS_OFFSET_SECONDS);

    slog.info(mmi+"prevFMFDataMC0.getEvenDate()="+prevFMFDataMC0.getEventDate().toString());
    slog.info(mmi+"shortTermFMFTSThreshold="+shortTermFMFTSThreshold.toString());
    slog.info(mmi+"forecastsDurationSeconds="+forecastsDurationSeconds);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    // --- Create a local MeasurementCustomBundle object with the previous full model
    //     forecast data List<MeasurementCustom> object for this TG location.
    final MeasurementCustomBundle mcbPrevFMF= new MeasurementCustomBundle( prevFMFData );

    // --- Get all the Instant objects of the MeasurementCustomBundle mcbPrevFMF object
    //     as a Set to be able to iterate on them.
    final Set<Instant> prevFMFInstantsSet= mcbPrevFMF.getInstantsKeySetCopy();

    //int shortTerm
    //double shortTermResErrorsAcc= 0.0;
    //double mediumTermResErrorsAcc= 0.0;

    List<Double> shortTermResErrors= new ArrayList<Double>();
    List<Double> mediumTermResErrors= new ArrayList<Double>();

    int nbMissingWLO= 0;

    // --- Get the WLO-WLFMF residual errors for the PREVIOUS full model forecast data for
    //     its timestamps that are less than SHORT_TERM_FORECAST_TS_THRESHOLD.
    for (final Instant prevFMFInstant: prevFMFInstantsSet) {

      //slog.info(mmi+"wlFMFInstant="+wlFMFInstant.toString());

      final MeasurementCustom wloAtInstant=
        mcbWLO.getAtThisInstant(prevFMFInstant);

        // --- Skip this timestamp when no valid WLO is available for it
      if ( wloAtInstant == null) {
        nbMissingWLO++;
        continue;
      }

      final double fmfResidualError= wloAtInstant.getValue() -
        mcbPrevFMF.getAtThisInstant(prevFMFInstant).getValue();

      if (prevFMFInstant.isBefore(shortTermFMFTSThreshold) ) {
        shortTermResErrors.add(fmfResidualError);
      } else {
        mediumTermResErrors.add(fmfResidualError);
      }

      //slog.info(mmi+"fmfResidualError="+fmfResidualError);
      //slog.info(mmi+"shortTermResErrors.size()="+shortTermResErrors.size());
      //slog.info(mmi+"mediumTermResErrors.size()="+mediumTermResErrors.size());
      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);

    } // --- end for loop 1

    slog.info(mmi+"nbMissingWLO="+nbMissingWLO);
    slog.info(mmi+"shortTermResErrors.size()="+shortTermResErrors.size());
    slog.info(mmi+"mediumTermResErrors.size()="+mediumTermResErrors.size());

    // --- Get the WLO - WLFMF residual errors arithmetic mean
    //     for the short term time period after the full forecast
    //     zero'th hour (0.0 if shortTermResErrors.size() == 0)
    final double shortTermResErrorsMean=
      (shortTermResErrors.size() > 0 ) ? Statistics.getDListValuesAritMean(shortTermResErrors) : 0.0 ;

    // --- Get the WLO - WLFMF residual errors arithmetic mean
    //     for the medium term time period after the full forecast
    //     zero'th hour (0.0 if mediumTermResErrors.size() == 0)
    final double mediumTermResErrorsMean=
      (mediumTermResErrors.size() > 0 ) ? Statistics.getDListValuesAritMean(mediumTermResErrors) : 0.0 ;

    slog.info(mmi+"shortTermResErrorsMean="+shortTermResErrorsMean);
    slog.info(mmi+"mediumTermResErrorMean="+mediumTermResErrorsMean);

    // --- datetime mid point (in seconds since epoch) for the short term time period
    //    after the full forecast zero'th hour
    final double shortTermResErrorsTSMidPointSse= (double) shortTermFMFTSThreshold.
      minusSeconds( (long) (0.5 * SHORT_TERM_FORECAST_TS_OFFSET_SECONDS)).getEpochSecond();

    //0.5 * SHORT_TERM_FORECAST_TS_OFFSET_SECONDS;

    final double mediumTermResErrorsDurationSeconds= 0.5 *
      (forecastsDurationSeconds + SHORT_TERM_FORECAST_TS_OFFSET_SECONDS);

    //final Instant prevFMFDataMC0Instant= prevFMFDataMC0.getEventDate();

    // --- datetime mid point (in seconds since epoch) for the medium term time period
    //     after the full forecast zero'th hour.
    final double mediumTermResErrorsTSMidPointSse= prevFMFDataMC0Instant.
      plusSeconds( (long) mediumTermResErrorsDurationSeconds ).getEpochSecond();

    if (mediumTermResErrorsTSMidPointSse <= shortTermResErrorsTSMidPointSse) {
      throw new RuntimeException(mmi+"Cannot have mediumTermResErrorsTSMidPointSse <= shortTermResErrorsTSMidPointSse !!");
    }

    // --- Get the FMF correction equation slope.
    final double correctionEquationSlope=
      (mediumTermResErrorsMean - shortTermResErrorsMean)/
        (mediumTermResErrorsTSMidPointSse - shortTermResErrorsTSMidPointSse);

    // --- Get the ordinate at origin value (i.e. origin here is the timestamp defined by the
    //     prevFMFDataMC0Instant object) of the FMF correction equation
    final double ordinateAtOrigin= shortTermResErrorsMean -
       (shortTermResErrorsTSMidPointSse - prevFMFDataMC0Instant.getEpochSecond()) * correctionEquationSlope;

    slog.info(mmi+"shortTermResErrorsTSMidPointSse="+shortTermResErrorsTSMidPointSse);
    slog.info(mmi+"mediumTermResErrorsTSMidPointSse="+mediumTermResErrorsTSMidPointSse);
    slog.info(mmi+"correctionEquationSlope="+correctionEquationSlope);
    slog.info(mmi+"ordinateAtOrigin="+ordinateAtOrigin);

    final double actuFMFDataMC0InstantSse= actuFMFDataMC0Instant.getEpochSecond();

    for ( MeasurementCustom mcObj: actuFMFData) {

      final double mcObjRelativeTimeOffset= (double) mcObj.
        getEventDate().getEpochSecond() - actuFMFDataMC0InstantSse;

      //slog.info(mmi+"bef. correction: mcObj.getValue()="+mcObj.getValue());

      mcObj.setValue(mcObj.getValue() + (correctionEquationSlope * mcObjRelativeTimeOffset + ordinateAtOrigin) );

      //slog.info(mmi+"aft. correction: mcObj.getValue()="+mcObj.getValue());
      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);

    } // --- end for loop 2

    ///slog.info(mmi+"shortTermResErrors.size()="+shortTermResErrors.size());
    //slog.info(mmi+"mediumTermResErrors.size()="+mediumTermResErrors.size());
    slog.info(mmi+"end");

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    return this;
  }

  // ---
  final public WLAdjustmentFMF multTimeDepFMFErrorStatsAdj(final String prevFMFASCIIDataFilePath,
                                                           final Map<String, HBCoords> uniqueTGMapObj,
                                                           final JsonObject mainJsonMapObj, final String tgResidualsStatsIODirectory) {

    final String mmi= "multTimeDepFMFErrorStatsAdj: ";

    slog.info(mmi+"start: prevFMFASCIIDataFilePath="+prevFMFASCIIDataFilePath);

    // --- Only the IWLAdjustmentIO.DataTypesFormatsDef.ECCC_H2D2_ASCII input file format is allowed for now
    if (this.modelForecastInputDataFormat != IWLAdjustmentIO.DataTypesFormatsDef.ECCC_H2D2_ASCII) {
      throw new RuntimeException(mmi+
        "Invalid full model forecast input format -> "+this.modelForecastInputDataFormat.name());
    }

    try {
      this.location.getIdentity();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    final String wlLocationIdentity= this.location.getIdentity();

    slog.info(mmi+"wlLocationIdentity="+wlLocationIdentity);

    // --- Get a local MeasurementCustomBundle object with the WLO data
    //     List<MeasurementCustom> object for this TG location.
    final MeasurementCustomBundle mcbWLO= this.getMcbWLOData();

    // --- Get a copy all the Instant objects of the MeasurementCustomBundle mcbWLO object
    //     as a Set to be able to test if some timestamps are missing for the WLO data
    final SortedSet<Instant> wloInstantsSet= mcbWLO.getInstantsKeySetCopy();

    // ---
    if (wloInstantsSet == null ) {
      throw new RuntimeException(mmi+
        "ERROR: wloInstantsSet == null !! It is abnormal that we have no WLO data to use at this point !!");
    }

    slog.info(mmi+"wloInstantsSet.size()="+wloInstantsSet.size());

    // --- Get copies of the most recent and least receint Intants objects
    //     of the WLO MeasurementCustomBundle object 
    final Instant mostRecentWLOInstant=  mcbWLO.getMostRecentInstantCopy();
    final Instant leastRecentWLOInstant= mcbWLO.getLeastRecentInstantCopy();

    slog.info(mmi+"leastRecentWLOInstant="+leastRecentWLOInstant.toString());
    slog.info(mmi+"mostRecentWLOInstant="+mostRecentWLOInstant.toString());

    // --- Local HashMap to accumulate the (WLO-WLFMF subtraction) residuals indexed by the
    //     time difference (in seconds since epoch) from the forecast lead time.
    Map<Long, List<Double>> timeDepResidualsAcc= new HashMap<Long, List<Double>>();

    int prevFMFIdxIter= IWLAdjustmentIO.
      FullModelForecastType.PREVIOUS.ordinal();

    String prevFMFASCIIDataFilePathIter= prevFMFASCIIDataFilePath;

    boolean wloTimeFrameOverlap= true;

    int nbOverlapsCount= 0;

    // ---
    while(wloTimeFrameOverlap) {

      slog.info(mmi+"Processing FMF input file -> "+prevFMFASCIIDataFilePathIter);

      // --- Read the previous H2D2 full model forecast data
      prevFMFASCIIDataFilePathIter= this.getH2D2ASCIIWLFProbesData(prevFMFASCIIDataFilePathIter,
                                                                   uniqueTGMapObj, mainJsonMapObj,
                                                                   IWLAdjustment.SYNOP_RUNS_TIME_OFFSET_HOUR, prevFMFIdxIter);

      // --- Store the FMF data in a local MeasurementCustomBundle object:
      final MeasurementCustomBundle mcbPrevFMF= new
        MeasurementCustomBundle( this.nearestModelData.get(prevFMFIdxIter).get(wlLocationIdentity));

      // --- Get the more recent FMF Instant to see if it is inside the
      //     WLO data time frame.
      final Instant mostRecentFMFInstant= mcbPrevFMF.getMostRecentInstantCopy();

      slog.info(mmi+"mostRecentFMFInstant="+mostRecentFMFInstant.toString());

      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);

      // --- Stop the iteration if the moreRecentFMFInstant is before the lessRecentWLOInstant. It
      //     means that all the FMF data is in the past compared to the less recent WLO data and we cannot
      //     use it for time dependant residuals calculations (no overlap for the respective time frames)
      wloTimeFrameOverlap= (mostRecentFMFInstant.isAfter(leastRecentWLOInstant)) ? true : false;

      // --- Calculate the residuals for the FMF if its time frame overlaps the WLO Time Frame.
      if (wloTimeFrameOverlap) {

        nbOverlapsCount++;

        // --- Get the FMF lead time (a.k.a. zero'th hour) to use it to define
        //     the time offset needed for time dependant residuals stats indexing.
        //     It is the least recent Instant of the FMF being processed.
        final Instant fmfLeadTimeInstant= mcbPrevFMF.getLeastRecentInstantCopy();
        final long fmfLeadTimeSeconds= fmfLeadTimeInstant.getEpochSecond();

        slog.info(mmi+"Overlap between WLO and FMF time frames: calculate the residuals for the FMF, fmfLeadTimeInstant="+fmfLeadTimeInstant.toString());

        final SortedSet<Instant> mcbPrevFMFInstantsSet= mcbPrevFMF.getInstantsKeySetCopy();

        for (final Instant mcbPrevFMFInstant : mcbPrevFMFInstantsSet) {

          // --- Check if the WLO data exists for this mcbPrevFMFInstant.
          //     This compeletely avoids the annoying usage of NO DATA flags
          //     and the related error of possibly using this NO DATA flag as
          //      a valid value. (But we still need to implement WL quality
          //      control for ridiculously low or high WL values like being
          //      2m under or 2m over the local ZC)
          if (wloInstantsSet.contains(mcbPrevFMFInstant)) {

            slog.debug(mmi+"WLO vs WLFMF Instant match for "+mcbPrevFMFInstant.toString());

            // --- Get the WLO at this Instant
            final double wloValue= mcbWLO.
              getAtThisInstant(mcbPrevFMFInstant).getValue();

            // --- Get the FMF WL at this same Instant
            final double fmfWLValue= mcbPrevFMF.
              getAtThisInstant(mcbPrevFMFInstant).getValue();

            //slog.info(mmi+"wloValue="+wloValue);
            //slog.info(mmi+"fmfWLValue="+fmfWLValue);

             // --- Get the time offset in seconds between the mcbPrevFMFInstant and the fmfLeadTimeInstant
            //    to keep track of the time dependent WLO - WLFMF residuals values
            final Long residualTimeOffsetIdx=
              mcbPrevFMFInstant.getEpochSecond() - fmfLeadTimeSeconds ; //fmfLeadTimeInstant.getEpochSecond();

            //slog.info(mmi+"residualTimeOffsetIdx="+residualTimeOffsetIdx);

            if (! timeDepResidualsAcc.containsKey(residualTimeOffsetIdx)) {
              timeDepResidualsAcc.put(residualTimeOffsetIdx, new ArrayList<Double>());
            }

            timeDepResidualsAcc.get(residualTimeOffsetIdx).add(wloValue-fmfWLValue);

            //slog.info(mmi+"timeDepResidualsAcc.get(residualTimeOffsetIdx).get(0)="+timeDepResidualsAcc.get(residualTimeOffsetIdx).get(0));
            //slog.info(mmi+"Debug exit 0");
            //System.exit(0);

          } // --- inner if (wloInstantsSet.contains(mcbPrevFMFInstant)) block
        } // --- inner for loop on mcbPrevFMFInstantsSet
      } // --- if (wloTimeFrameOverlap) block

      prevFMFIdxIter++;

    } // --- while(wloTimeFrameOtimeDepResidualsStats=verlap) loop block

    slog.info(mmi+"nbOverlapsCount="+nbOverlapsCount);

    // ---
    final Map<Long, MeasurementCustom> timeDepResidualsStats=
      MeasurementCustom.getTimeDepMCStats(timeDepResidualsAcc);

    //--- Now verify if we have all the time offsets for the FMF time frame.
    //    need to replace the missing time dependent residal stats by the
    //    values that are stored on disk for this location and for this
    //    time offset
    slog.info(mmi+"Now check if we have all the FMF time offsets available for the residuals stats");

    final int actuFMFIndex= IWLAdjustmentIO.
      FullModelForecastType.ACTUAL.ordinal();

    final List<MeasurementCustom> actualFMFMCList=
      this.nearestModelData.get(actuFMFIndex).get(wlLocationIdentity);

    final MeasurementCustomBundle actualFMFMcb= new MeasurementCustomBundle( actualFMFMCList );

    //final leastRecentActualFMFInstant= actualMcbFMF.getLeastRecentInstantRef();
    final long leastRecentActualFMFSeconds=
      actualFMFMcb.getLeastRecentInstantCopy().getEpochSecond();

    final Set<Instant> actuFMFInstantsSet= actualFMFMcb.getInstantsKeySetCopy();

    for (final Instant actualFMFInstant: actuFMFInstantsSet) {

      final Long longIdx= actualFMFInstant.getEpochSecond() - leastRecentActualFMFSeconds;

      if (! timeDepResidualsStats.containsKey(longIdx) ) {

        // --- replace the run time error rause by the code that takes the values stored on disk here
        //slog.error(mmi+"longIdx key -> "+longIdx+" not found in the timeDepResidualsStats Map! need to replace it by its corresponding values stored on disk!");
        throw new RuntimeException(mmi+"longIdx key -> "+longIdx+
          " not found in the timeDepResidualsStats Map! need to replace it by its corresponding values stored on disk!");
      }

      //slog.info(mmi+"actualFMFInstant="+actualFMFInstant.toString());

      // --- Get the MeasurementCustom object of the FMF for this instant:
      MeasurementCustom actuFMFMc= actualFMFMcb.getAtThisInstant(actualFMFInstant);

      // --- Get the time dependent residual stats MeasurementCustom object for the
      //    time offset between the FMF lead time and the Instant being processed
      final MeasurementCustom timeDepResidualMc= timeDepResidualsStats.get(longIdx);

      //final double timeDepResidualAvg= timeDepResidualsStats.get(longIdx).getValue();

      //slog.info(mmi+"actuFMFMc value bef adj.="+actuFMFMc.getValue());
      //slog.info(mmi+"timeDepResidualAvg="+timeDepResidualMc.getValue());
      //slog.info(mmi+"timeDepResidual uncertainty="+timeDepResidualMc.getUncertainty());

      // --- Set the adjusted FMF WL value using the time dependent residual avg. value
      actuFMFMc.setValue(actuFMFMc.getValue() + timeDepResidualMc.getValue());

      // --- Set the adjusted FMF WL uncertainty using the std dev of the related
      //    time dependent residual for this time offset.
      actuFMFMc.setUncertainty(timeDepResidualMc.getUncertainty());

      //slog.info(mmi+"actuFMFMc value aft adj.="+actuFMFMc.getValue()+"\n");

      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);
    }

    // --- Write the TG time dependent residual stats to be able to use them
    //     for cases where no WLO data is available.
    this.writeTGTimeDepResidualsStats(wlLocationIdentity,
                                      timeDepResidualsStats, tgResidualsStatsIODirectory);

    slog.info(mmi+"end");

    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    return this;
  }
}
