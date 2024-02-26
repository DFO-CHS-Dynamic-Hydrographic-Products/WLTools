package ca.gc.dfo.chs.wltools.wl;

// ---
import java.io.File;
import java.util.List;
import java.lang.Math;
import java.time.Instant;
import java.util.HashMap;
import java.io.FileWriter;
import java.util.SortedSet;
import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedWriter;
import java.lang.StringBuilder;
import java.time.temporal.ChronoUnit;

// ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.wl.IIWLPSLegacyIO;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;

// ---
public final class IWLPSLegacyIO implements IIWLPSLegacyIO {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.IWLPSLegacyIO: ";

 /**
   * class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);
    
  // ---
  public IWLPSLegacyIO() {}

  // ---
  private static String doubleToSpineString(final Double dval) { // StringBuilder stringBuilder) {
      
    /*
     *  Convert Double to string used in SPINE ascii files
     ////*  and append to existing stringBuilder object
     *  Ex.: 1.2 > "00120;"
     * 
     * @param dval (Double) water level or uncertainty value, in metres
     ////* @param stringBuilder (StringBuilder) string builder object
     */
      
    final String mmi= "doubleToSpineString: ";
      
    String spineASCIIFormatedVal= null;
    
    final Long dvalLong= Math.round(dval*1000.0);
    
    if ( (dvalLong > 99999L) || (dvalLong < -9999L) ){
      throw new RuntimeException(dvalLong + " Invalid dval -> "+dval+" in source data");
       
    } else if (dvalLong < 0L){
      spineASCIIFormatedVal= "-" + String.format("%04d", Math.abs(dvalLong))+";";
       
    } else {
      spineASCIIFormatedVal= String.format("%05d", dvalLong)+";";
    }

    return spineASCIIFormatedVal;
    //stringBuilder.append(formatedNum);
    
  } // --- Method
    
  // ---  
  public static void writeFiles(final Instant whatTimeIsItNow, final List<MeasurementCustomBundle> mcbForSpineAPI, final String outputDir) {

    final String mmi= "writeFiles: ";

    slog.info(mmi+"start");
  
    try {
      whatTimeIsItNow.hashCode();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"whatTimeIsItNow cannot be null here !");
    }
    
    try {
      mcbForSpineAPI.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"mcbForSpineAPI cannot be null here !");
    }

    if (mcbForSpineAPI.size() != NB_SHIP_CHANNEL_PT_LOCS) {
      throw new RuntimeException(mmi+" mcbForSpineAPI.size() != NB_SHIP_CHANNEL_PT_LOCS -> "+NB_SHIP_CHANNEL_PT_LOCS);
    }

    final String nowStrISO8601= whatTimeIsItNow.toString();

    final String [] nowStrISO8601Split= nowStrISO8601.split(ISO8601_HHMMSS_SEP_CHAR);

    final int whatTimeIsItNowMinutes= Integer.valueOf(nowStrISO8601Split[1]);

    slog.info(mmi+"whatTimeIsItNowMinutes="+whatTimeIsItNowMinutes);

    // --- Build the YYMMDDhh file prefix name part which is common for all type of
    //     Spine API legacy ASCII files.
    String spineAPIInputValuesFName= nowStrISO8601.substring(2,10)
      .replace(ISO8601_YYYYMMDD_SEP_CHAR,"") + nowStrISO8601Split[0].split(ISO8601_DATETIME_SEP_CHAR)[1];

    //final Instant mcbLeastRecentInstant= mcbForSpineAPI.get(0).getLeastRecentInstantCopy();
    //slog.info(mmi+"mcbLeastRecentInstant="+mcbLeastRecentInstant.toString());
    //final long mcbTimeIntrvSeconds= mcbForSpineAPI.getDataTimeIntervallSeconds();

    // --- We will use spineFormatedUncertainties StringBuilder only if
    //     we have to produce the 30 days results for the Spine API
    StringBuilder spineASCIIFormatedUncertainties= null;
    
    // --- Define the daysOffsetInFuture for the Q2, Q3 and Q4 Spine API input files.
    int daysOffsetInFuture= 1;

    // --- 30 days in future case if whatTimeIsItNowMinutes < 15mins
    if (whatTimeIsItNowMinutes < 15) {
	
      spineAPIInputValuesFName += HOUR_FNAMES_COMMON_POSTFIX;

      // --- Need to set the daysOffsetInFuture to 3o days here.
      daysOffsetInFuture= 30;

      StringBuilder spineFormatedUncertainties= new StringBuilder();
	
    } else if (whatTimeIsItNowMinutes < 30) {

      spineAPIInputValuesFName += "Q2" + QUARTER_FNAME_COMMON_POSTFIX;

    } else if (whatTimeIsItNowMinutes < 45) {

      spineAPIInputValuesFName += "Q3" + QUARTER_FNAME_COMMON_POSTFIX; 

    } else { // --- 45 <= whatTimeIsItNowMinutes < 59
	
      spineAPIInputValuesFName += "Q4" + QUARTER_FNAME_COMMON_POSTFIX;
    }

    slog.info(mmi+"spineAPIInputValuesFName="+spineAPIInputValuesFName);

    // --- Assuming here that all the MeasurementCustomBundle objects of mcbForSpineAPI
    //     have the same Instant objects values.
    final Instant mcbLeastRecentInstant= mcbForSpineAPI.get(0).getLeastRecentInstantCopy();

    slog.info(mmi+"mcbLeastRecentInstant="+mcbLeastRecentInstant.toString());

    // --- Get the time intervall in seconds of the adj. FMF WL data.
    //     (which we assume that it is the same for all the MeasurementCustomBundle objects of mcbForSpineAPI
    final long mcbTimeIntrvSeconds= mcbForSpineAPI.get(0).getDataTimeIntervallSeconds();

    slog.info(mmi+"mcbTimeIntrvSeconds="+mcbTimeIntrvSeconds);

    slog.info(mmi+"daysOffsetInFuture="+daysOffsetInFuture);

    // --- Define the last Instant in the future that is used to define the Instant objects range
    //     for the Spine API depending on the daysOffsetInFuture value.
    final Instant instantsRangeLimitInFuture=
      mcbLeastRecentInstant.plus(daysOffsetInFuture,ChronoUnit.DAYS);

    slog.info(mmi+"instantsRangeLimitInFuture="+instantsRangeLimitInFuture.toString());

    // --- NOTE: instantsRangeLimitInFuture Instant is not included in the subset of
    //           Instant objects here.
    final SortedSet<Instant> wantedInstantsRange= mcbForSpineAPI.get(0)
      .getInstantsKeySetCopy().subSet(mcbLeastRecentInstant, instantsRangeLimitInFuture);

    // --- Need to add mcbTimeIntrvSeconds to lastInstantWantedInFuture for the SortedSet<Instant>.subSet() method
    //     here in order to include the lastInstantWantedInFuture in this wantedInstantsRange.	
    //.subSet(mcbLeastRecentInstant, lastInstantWantedInFuture.plusSeconds(mcbTimeIntrvSeconds));

    slog.info(mmi+"wantedInstantsRange first="+wantedInstantsRange.first().toString());
    slog.info(mmi+"wantedInstantsRange last="+wantedInstantsRange.last().toString());
    slog.info(mmi+"wantedInstantsRange.size()="+wantedInstantsRange.size());

    StringBuilder spineASCIIFormatedWLValues= new StringBuilder();

    // --- Loop on all the ship channel point locations
    for (int scLocIter= 0; scLocIter < mcbForSpineAPI.size(); scLocIter++) {

      // --- MeasurementCustomBundle object for this ship channel point location
      final MeasurementCustomBundle scLocMcb= mcbForSpineAPI.get(scLocIter);

      // --- Loop on the wanted Instant objects in the future
      for (final Instant wantedInstant: wantedInstantsRange) {

	// --- MesurementCustom object at the  wanted Instant for this  ship channel point location
	final MeasurementCustom mcAtInstant= scLocMcb.getAtThisInstant(wantedInstant);
	  
	spineASCIIFormatedWLValues.append(doubleToSpineString(mcAtInstant.getValue()));

	if (spineASCIIFormatedUncertainties != null) {
	  spineASCIIFormatedUncertainties.append( doubleToSpineString(mcAtInstant.getUncertainty() ));
	}
      } // --- Inner for loop block on the Instant objects.

      // --- Deal with the tnconsistency between the 30 days file (and uncertainties file)
      //     for the end of line characters (Why a blank character there instead of the semi-columm???) 
      if (spineASCIIFormatedUncertainties != null) {

	spineASCIIFormatedWLValues
	  .setLength(spineASCIIFormatedWLValues.length() - 1);
		    
	spineASCIIFormatedWLValues.append(" ");
	
        spineASCIIFormatedUncertainties
	  .setLength(spineASCIIFormatedUncertainties.length() - 1);
		    
	spineASCIIFormatedUncertainties.append(" ").append("\n");
      }

      spineASCIIFormatedWLValues.append("\n");
      
    } // --- Outer for loop block on the the ship channel point locations
    
    // --- Write the Spine ASCII formated values file.
    final File spineASCIIInputValuesFile= new File(outputDir + File.separator + spineAPIInputValuesFName);

    try (BufferedWriter writer= new BufferedWriter(new FileWriter(spineASCIIInputValuesFile))) {
      writer.append(spineASCIIFormatedWLValues);
      
    } catch (IOException ioe) {
      throw new RuntimeException(mmi+ioe+"Problem writing in Spine API input values file ->"+spineASCIIInputValuesFile);
    }

    // ---  Write the Spine ASCII formated uncertainties file.
    if (spineASCIIFormatedUncertainties != null) {

      final File spineASCIIUncertaintiesFile= new File(outputDir + File.separator + UNCERTAINTIES_STATIC_FNAME);

      try (BufferedWriter writer= new BufferedWriter(new FileWriter(spineASCIIUncertaintiesFile))) {
        writer.append(spineASCIIFormatedUncertainties);
      
      } catch (IOException ioe) {
        throw new RuntimeException(mmi+ioe+"Problem writing in Spine API input uncertainties file ->"+spineASCIIUncertaintiesFile);
      }      
      	
    } // --- end if block

    
    slog.info(mmi+"debug exit 0");
    System.exit(0);
    
    slog.info(mmi+"end");
    
  } // --- method writeFiles
}
 
