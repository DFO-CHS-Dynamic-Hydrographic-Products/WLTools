package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NavigableSet;
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
import ca.gc.dfo.chs.wltools.util.TimeMachine;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.TideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.numbercrunching.Statistics;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;

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

  // --- Map for the previous time dependant residual stats data that
  //     is stored on disk. We could need to use it in case:
  //  
  //     1). Some of the time offsets Long keys are missing in the
  //         newly calculated time dependant residual stats.
  //
  //     2). There is no WLO data to use at all for the time frame
  //         being processed but we have some previous complete time
  //         dependant residual stats stoted on disk and that we can
  //         use for FMF WL data adjustments.
  //private Map<Long, MeasurementCustom> prevTimeDepResidualsStats= null;

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

    slog.error(mmi+"method has now been decommissioned !!");
    System.exit(1);
    //throw new RuntimeException(mmi+"singleTimeDepFMFErrorStatsAdj method has been decommissioned !!");
    
    slog.info(mmi+"start: prevFMFASCIIDataFilePath="+prevFMFASCIIDataFilePath);

    try {
      this.location.getIdentity();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    // --- this.haveWLOData MUST be true at this point!
    if (!this.haveWLOData) {
      throw new RuntimeException(mmi+"this.haveWLOData must be true here!");
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
  final public Map<Long, MeasurementCustom> getNewTimeDepResidualsStats(final String prevFMFASCIIDataFilePath,
									final Map<String, HBCoords> uniqueTGMapObj, final JsonObject mainJsonMapObj) {
    final String mmi= "getNewTimeDepResidualsStats: ";

    final String wlLocationIdentity= this.location.getIdentity();

    slog.info(mmi+"wlLocationIdentity="+wlLocationIdentity);
    
    // --- Instantiate this.mcbWLO MeasurementCustomBundle object with the WLO data
    //     List<MeasurementCustom> object for this TG location.
    //final MeasurementCustomBundle
    this.mcbWLO= this.getMcbWLOData();

    // --- Get a copy all the Instant objects of the MeasurementCustomBundle mcbWLO object
    //     as a Set to be able to test if some timestamps are missing for the WLO data
    final SortedSet<Instant> wloInstantsSet= this.mcbWLO.getInstantsKeySetCopy();

    // ---
    if (wloInstantsSet == null ) {
      throw new RuntimeException(mmi+
        "ERROR: wloInstantsSet == null !! It is abnormal that we have no WLO data to use at this point !!");
    }

    slog.info(mmi+"wloInstantsSet.size()="+wloInstantsSet.size());

    // --- Get a copy of the most recent WLO data Instant object
    //     in the this.mostRecentWLOInstant attribute.
    this.mostRecentWLOInstant= this.mcbWLO.getMostRecentInstantCopy();

    // -- Get a local copy of the least recent WLO data Instant object 
    final Instant leastRecentWLOInstant= this.mcbWLO.getLeastRecentInstantCopy();

    slog.info(mmi+"leastRecentWLOInstant="+leastRecentWLOInstant.toString());
    //slog.info(mmi+"mostRecentWLOInstant="+mostRecentWLOInstant.toString());

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

       // --- Unlikely but could happen. In any case we "should" have at least one H2D2 ASCII
       //     input file to use at this point.
       if (!WLToolsIO.checkForFileExistence(prevFMFASCIIDataFilePathIter)) {
          
        slog.warn(mmi+"WARNING: H2D2 FMF input file -> "+prevFMFASCIIDataFilePathIter+
                  " skipping it and need to stop the getNewTimeDepResidualsStats calculation here !!");
        break;  
      }
	
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

        //Instant lastValidInstantProcessed= null;

        for (final Instant mcbPrevFMFInstant : mcbPrevFMFInstantsSet) {

          //slog.info(mmi+"Checking mcbPrevFMFInstant="+mcbPrevFMFInstant.toString());

          // --- Get the time offset in seconds between the mcbPrevFMFInstant and the fmfLeadTimeInstant
          //    to keep track of the time dependent WLO - WLFMF residuals values
          //final Long residualTimeOffsetIdx=
          //  mcbPrevFMFInstant.getEpochSecond() - fmfLeadTimeSeconds ; //fmfLeadTimeInstant.getEpochSecond();

                    // --- Check if the WLO data exists for this mcbPrevFMFInstant.
          //     This compeletely avoids the annoying usage of NO DATA flags
          //     and the related error of possibly using this NO DATA flag as
          //      a valid value. (But we still need to implement WL quality
          //      control for ridiculously low or high WL values like being
          //      2m under or 2m over the local ZC)
          if (wloInstantsSet.contains(mcbPrevFMFInstant)) {

            //slog.info(mmi+"WLO vs WLFMF Instant match for "+mcbPrevFMFInstant.toString());

            // --- Get the WLO at this Instant
            final double wloValue= this.mcbWLO.
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

            // --- Allocate space in the timeDepResidualsAcc to store the residual values
            //   indexed by the residualTimeOffsetIdx key
            if (! timeDepResidualsAcc.containsKey(residualTimeOffsetIdx)) {
              timeDepResidualsAcc.put(residualTimeOffsetIdx, new ArrayList<Double>());
            }

            timeDepResidualsAcc.get(residualTimeOffsetIdx).add(wloValue-fmfWLValue);

            //slog.info(mmi+"timeDepResidualsAcc.get(residualTimeOffsetIdx).get(0)="+timeDepResidualsAcc.get(residualTimeOffsetIdx).get(0));
            //if (residualTimeOffsetIdx>=3600) {
            //slog.info(mmi+"Debug exit 0");
            //System.exit(0);
	 
	  } // --- if (wloInstantsSet.contains(mcbPrevFMFInstant)) block   
	} // ---  for (final Instant mcbPrevFMFInstant : mcbPrevFMFInstantsSet) block.
      } // --- if (wloTimeFrameOverlap) block
       
      prevFMFIdxIter++;
      
    } // ---  while(wloTimeFrameOverlap) block.

    slog.info(mmi+"nbOverlapsCount="+nbOverlapsCount);

    return MeasurementCustom.getTimeDepMCStats(timeDepResidualsAcc);
  }

  // ---
  final public WLAdjustmentFMF multTimeDepFMFErrorStatsAdj(final String prevFMFASCIIDataFilePath,
                                                           final Map<String, HBCoords> uniqueTGMapObj,
                                                           final JsonObject mainJsonMapObj, final String tgResidualsStatsIODirectory) {

    final String mmi= "multTimeDepFMFErrorStatsAdj: ";

    slog.info(mmi+"start: prevFMFASCIIDataFilePath="+prevFMFASCIIDataFilePath+
	      ", this.prdDataTimeIntervalSeconds="+this.prdDataTimeIntervalSeconds);

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

    // --- Read the previous time dependent residuals stats that is stored
    //     on disk first: We could need to use it in case:
    //  
    //     1). Some of the time offsets Long keys are missing in the
    //         newly calculated time dependant residual stats.
    //
    //     2). There is no WLO data to use at all for the time frame
    //         being processed but we have some previous complete time
    //         dependant residual stats stoted on disk and that we can
    //         use for FMF WL data adjustments.
    //
    //     NOTE: the this.readTGTimeDepResidualsStats method could return
    //           null if the previous time dependent residuals stats file
    //           do not exists. It is possible if it is a cold start which
    //           means that there is no previous time dependent residuals
    //           stats that already exists.
    //
    final Map<Long, MeasurementCustom> prevTimeDepResidualsStats=
      this.readTGTimeDepResidualsStats(wlLocationIdentity, tgResidualsStatsIODirectory);

    if (prevTimeDepResidualsStats != null ) {
      slog.info(mmi+"We have the previous time dependent residuals stats to use if needed"); 
    }
	       
    //  --- Define the local timeDepResidualsStats Map object that will be used
    //	    for the FMF WL data adjustment (null at this point as default)
    Map<Long, MeasurementCustom> timeDepResidualsStats= null;
   
    // --- Produce the new time dependant residuals stats if we have WLO data.
    if (this.haveWLOData) {

      slog.info(mmi+"this.haveWLOData == true: Produce the new time dependant residuals stats");
      
      timeDepResidualsStats= this.getNewTimeDepResidualsStats(prevFMFASCIIDataFilePath,
							      uniqueTGMapObj, mainJsonMapObj);
    } else if ( prevTimeDepResidualsStats != null) {

      slog.warn(mmi+"this.haveWLOData != true: Need to use the previous time dependant residuals stats");
      
      timeDepResidualsStats= prevTimeDepResidualsStats;
      
    } else {
	
      slog.warn(mmi+"prevTimeDepResidualsStats is null !, no previous time dependent residuals stats to use ! assuming a cold start here !");

      // --- Need to allocate timeDepResidualsStats here for cold starts.
      timeDepResidualsStats= new HashMap<Long, MeasurementCustom>();
    }

    // --- Need to be sure to have at least a MeasurementCustom object initialized with zero values
    //     at the time offset 0L in the timeDepResidualsStats Map for cold starts OR if it is
    //     missing for the 0L time offset (very unlikely but not impossible)
    if (!timeDepResidualsStats.containsKey(0L)) {
      timeDepResidualsStats.put(0L, new MeasurementCustom(null, 0.0, 0.0));
    }

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);  
    
    //--- Now verify if we have all the time offsets for the FMF time frame.
    //    need to replace the missing time dependent residal stats by the
    //    values that are stored on disk for this location and for this
    //    time offset
    //slog.info(mmi+"Now check if we have all the FMF time offsets available for the residuals stats");

    final int actuFMFIndex= IWLAdjustmentIO.
      FullModelForecastType.ACTUAL.ordinal();

    final List<MeasurementCustom> actualFMFMCList=
      this.nearestModelData.get(actuFMFIndex).get(wlLocationIdentity);

    final MeasurementCustomBundle actualFMFMcb= new MeasurementCustomBundle( actualFMFMCList );

    // --- Get the least recent timestamp in seconds of the FMF data
    //     that we want to adjust (it is the 1st time stamp)
    final long leastRecentActualFMFSeconds=
      actualFMFMcb.getLeastRecentInstantCopy().getEpochSecond();

    // --- Get the most recent timestamp in seconds of the FMF data
    //     that we want to adjust (it is the last timestamp)    
    final long mostRecentActualFMFSeconds=
      actualFMFMcb.getMostRecentInstantCopy().getEpochSecond();

    // --- Need to have the total time offset in seconds between the
    //     mostRecentActualFMFSeconds anf the leastRecentActualFMFSeconds
    final Long lastActuFMFTimeOffet= mostRecentActualFMFSeconds - leastRecentActualFMFSeconds;

    // --- Check if we have a valid time dependent residual value to use at
    //     the last time offset of the FMF data. If not then use a MeasurementCustom object
    //     initialized with zero values (means no residual -> no adjustment for this time offset)
    if (!timeDepResidualsStats.containsKey(lastActuFMFTimeOffet)) {
      timeDepResidualsStats.put(lastActuFMFTimeOffet, new MeasurementCustom(null, 0.0, 0.0));
    }

    // --- Now fill-up the missing time dependent residual stats data (if any) for all
    //     the 

    // --- Get the sorted set of the valid Long keys of the timeDepResidualsStats
    //     to possibly use it in case some time dependent residual stats are missung
    final SortedSet<Long> validLonIdxKeySet= Collections
      .synchronizedSortedSet(new TreeSet<Long>(timeDepResidualsStats.keySet()));

    //final SortedSet<Long> actuFMFInstantsSortedSet= Collections
    //  .synchronizedSortedSet(new TreeSet<Instant>(actualFMFMcb.getInstantsKeySetCopy()));
    
    // --- Need to use a NavigableSet here to be able to use its specific tailSet(E, boolean) method later
    NavigableSet<Instant> actuFMFInstantsSet= new TreeSet<Instant>(actualFMFMcb.getInstantsKeySetCopy());

    // --- Loop on all the Instant objects of the actuFMFInstantsSet
    for (final Instant actualFMFInstant: actuFMFInstantsSet) {

      //slog.info(mmi+"actualFMFInstant="+actualFMFInstant.toString());
	
      //--- Get the time offset in seconds between the actualFMFInstant and the
      //    leastRecentActualFMFSeconds to use it for indexing in the timeDepResidualsStats Map
      final Long longIdx= actualFMFInstant.getEpochSecond() - leastRecentActualFMFSeconds;

      //slog.info(mmi+"longIdx="+longIdx);
      //slog.info(mmi+"debug exit 0");
      //System.exit(0);

      // --- Could have missing obs for a given longIdx -> also missing residual stats at this longIdx
      if (! timeDepResidualsStats.containsKey(longIdx) ) {  

	// --- Find the two nearest neighbors time offsets (previous and next).
	final List<Long> twoNearestsLongSecondsKeys=
	  TimeMachine.getTwoNeareastsLongSeconds(longIdx, validLonIdxKeySet);

        final Long nearestPrevLongSeconds= twoNearestsLongSecondsKeys.get(0);
	final Long nearestNextLongSeconds= twoNearestsLongSecondsKeys.get(1);
	
	//slog.info(mmi+"nearestPrevLongSeconds="+nearestPrevLongSeconds);
	//slog.info(mmi+"nearestNextLongSeconds="+nearestNextLongSeconds);

	// --- Very unlikely but we never know.
	if (nearestNextLongSeconds == nearestPrevLongSeconds ) {
	  throw new RuntimeException(mmi+"Cannot have nearestNextLongSeconds == nearestPrevLongSeconds here !!");
	}

        final MeasurementCustom nearestPrevMc= timeDepResidualsStats.get(nearestPrevLongSeconds);
        final MeasurementCustom nearestNextMc= timeDepResidualsStats.get(nearestNextLongSeconds);

	// --- Doing linear time interpolation for the missing residual avg value and its uncertainty
	//     using the MeasurementCustom objects of the two nearest time offsets from the timeDepResidualsStats Map
	//final double timeOffsetDiff= (double)(nearestNextLongSeconds - nearestPrevLongSeconds);

	final double prevTimeOffsetWght=
	  (double)(nearestNextLongSeconds - longIdx)/(double)(nearestNextLongSeconds - nearestPrevLongSeconds);

	final double residualAvgTmInterp=
	  prevTimeOffsetWght * nearestPrevMc.getValue() + (1.0 - prevTimeOffsetWght) * nearestNextMc.getValue();

	final double residualUncTmInterp=
	  prevTimeOffsetWght * nearestPrevMc.getUncertainty() + (1.0 - prevTimeOffsetWght) * nearestNextMc.getUncertainty();

        //slog.info(mmi+"nearestPrevMc.getValue()="+nearestPrevMc.getValue());
	//slog.info(mmi+"nearestNextMc.getValue()="+nearestNextMc.getValue());
        //slog.info(mmi+"nearestPrevMc.getUncertainty()="+nearestPrevMc.getUncertainty());
	//slog.info(mmi+"nearestNextMc.getUncertainty()="+nearestNextMc.getUncertainty());
        //slog.info(mmi+"residualAvgTmInterp="+residualAvgTmInterp);
	//slog.info(mmi+"residualUncTmInterp="+residualUncTmInterp);

	// --- Assign the time interpolated time dependent residual avg. and its related uncertainty
	//     values in the MeasurementCustom for the missing time offset in the timeDepResidualsStats Map
        timeDepResidualsStats.put(longIdx, new MeasurementCustom(null, residualAvgTmInterp, residualUncTmInterp ));
	
	//slog.info(mmi+"Remove this debug exit 0 when this replacement strategy has been validated");
        //System.exit(0);
      }

      //slog.info(mmi+"end loop for instant:"+actualFMFInstant.toString());
    }

    //slog.info(mmi+"tesing this.haveWLOData part 1");
    //slog.info(mmi+"this.haveWLOData part 1="+this.haveWLOData);
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    
    // --- Get a copy of the SortedSet of Instant objects of the FMF data.
    //final
    //SortedSet<Instant> actuFMFInstantsSet= actualFMFMcb.getInstantsKeySetCopy();

    // --- Need to use a NavigableSet here to be able to use its specific tailSet() method
    //NavigableSet<Instant> actuFMFInstantsSet= new TreeSet(actualFMFMcb.getInstantsKeySetCopy());

    //slog.info(mmi+"1st actuFMFInstant before tailSet: "+actuFMFInstantsSet.first());

    //slog.info(mmi+"tesing this.haveWLOData part 2");
    //slog.info(mmi+"this.haveWLOData part 2="+this.haveWLOData);
    
    if (this.haveWLOData) {

      slog.info(mmi+"Ignore Instants that are <= than: "+this.mostRecentWLOInstant.toString());
	
      // --- We have to ignore the timestamps that are < than this.mostRecentWLOInstant
      //     for the FMF WL adjustment-correction i,e, we do not need to consider the timestamps
      //     of the past compared to the last WLO data time stamp. The true argument is used
      //     here to signal that we want the Instant objects that are >= than
      //     this.mostRecentWLOInstant
      actuFMFInstantsSet= actuFMFInstantsSet.tailSet(this.mostRecentWLOInstant, true);
    }

    slog.info(mmi+"1st actuFMFInstant that will be used: "+actuFMFInstantsSet.first().toString());
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
      
    // --- Allocate the List of MeasurementCustom object that will store
    //	   the adjusted FMF WL data
    this.locationAdjustedData= new ArrayList<MeasurementCustom>(actuFMFInstantsSet.size());

    // Acid test:
    //timeDepResidualsStats.remove(0L);
    //timeDepResidualsStats.remove(180L);
    //timeDepResidualsStats.remove(360L);
    //timeDepResidualsStats.remove(540L);
    
    //// --- Get the sorted set of the valid Long keys of the timeDepResidualsStats
    ////     to possibly use it in case some time dependent residual stats are missung
    //final SortedSet<Long> validLonIdxKeySet= new TreeSet<Long>(timeDepResidualsStats.keySet());

    slog.info(mmi+"before loop on actuFMFInstantsSet: timeDepResidualsStats.size()="+timeDepResidualsStats.size());

    // --- Loop on all the Instant objects of the actual FMF data that we want
    //     to adjust (i.e. only for the Instant objects that are > this.mostRecentWLOInstant )
    for (final Instant actualFMFInstant: actuFMFInstantsSet) {
	
      //--- Get the time offset in seconds between the actualFMFInstant and the
      //    leastRecentActualFMFSeconds to use it for indexing in the timeDepResidualsStats Map
      final Long longIdx= actualFMFInstant.getEpochSecond() - leastRecentActualFMFSeconds;

      //slog.info(mmi+"actualFMFInstant="+actualFMFInstant.toString());
      //slog.info(mmi+"longIdx="+longIdx);
      //slog.info(mmi+"debug exit 0");
      //System.exit(0);
      
      // --- Get the MeasurementCustom object of the FMF for this instant:
      final MeasurementCustom actuFMFMc= actualFMFMcb.getAtThisInstant(actualFMFInstant);

      // --- Get the time dependent residual stats MeasurementCustom object for the
      //     time offset between the FMF lead time and the Instant being processed
      final MeasurementCustom timeDepResidualMc= timeDepResidualsStats.get(longIdx);

      // --- Set the adjusted FMF WL value using the time dependent residual avg. value
      //actuFMFMc.setValue(actuFMFMc.getValue() + timeDepResidualMc.getValue());
      // --- Set the adjusted FMF WL uncertainty using the std dev of the related
      //     time dependent residual for this time offset.
      //actuFMFMc.setUncertainty(timeDepResidualMc.getUncertainty());

      final double adjustedFMFWLValue= actuFMFMc.getValue() + timeDepResidualMc.getValue();
      //final double adjustedFMFWLUncertainty= timeDepResidualMc.getUncertainty();

      //slog.info(mmi+"actuFMFMc value bef adj.="+actuFMFMc.getValue());
      //slog.info(mmi+"timeDepResidualAvg="+timeDepResidualMc.getValue());
      //slog.info(mmi+"adjustedFMFWLValue="+adjustedFMFWLValue);
      //slog.info(mmi+"timeDepResidual uncertainty="+timeDepResidualMc.getUncertainty());
      
      this.locationAdjustedData.add(new MeasurementCustom( actualFMFInstant,
							   adjustedFMFWLValue, timeDepResidualMc.getUncertainty() ));
      
      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);
      
    } // --- for (final Instant actualFMFInstant: actuFMFInstantsSet) loop block

    slog.info(mmi+"end: actualFMFMcb.size()="+actualFMFMcb.size());
    slog.info(mmi+"end: actuFMFInstantsSet.size()="+actuFMFInstantsSet.size());
    slog.info(mmi+"end: timeDepResidualsStats.size()="+timeDepResidualsStats.size());
    slog.info(mmi+"end: this.locationAdjustedData.size()="+this.locationAdjustedData.size());

    if (timeDepResidualsStats.size() != actualFMFMcb.size()) {
      throw new RuntimeException(mmi+
        "Cannot have timeDepResidualsStats.size() != actualFMFMcb.size() at this point !!");
    }
    
    // --- Write the TG time dependent residual stats to be able to use them
    //     for other cases where no WLO data is available. NOTE we write it
    //     even if the previous TG time dependent residual stats was used
    //     (which means that no WLO data is available for the TG) in order
    //      to facilitate the management of this data.
    this.writeTGTimeDepResidualsStats(wlLocationIdentity,
                                      timeDepResidualsStats, tgResidualsStatsIODirectory);
    slog.info(mmi+"end");

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    return this;
    
  } // --- method multTimeDepFMFErrorStatsAdj
}
