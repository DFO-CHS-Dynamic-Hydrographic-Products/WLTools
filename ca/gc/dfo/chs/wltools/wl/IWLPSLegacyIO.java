package ca.gc.dfo.chs.wltools.wl;

import java.util.List;
import java.time.Instant;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;

// ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.wl.IIWLPSLegacyIO;
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
  public static void writeFiles(final Instant whatTimeIsItNow, final List<MeasurementCustomBundle> mcbForSpineAPI) {

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

    final String nowStrISO8601= whatTimeIsItNow.toString();

    final String [] nowStrISO8601Split= nowStrISO8601.split(ISO8601_HHMMSS_SEP_CHAR);

    final int whatTimeIsItNowMinutes= Integer.valueOf(nowStrISO8601Split[1]);

    slog.info(mmi+"whatTimeIsItNowMinutes="+whatTimeIsItNowMinutes);

    // --- Build the YYMMDDhh file prefix name part which is common for all type of
    //     Spine API legacy ASCII files.
    String spineAPIInputFName= nowStrISO8601.substring(2,10)
      .replace(ISO8601_YYYYMMDD_SEP_CHAR,"") + nowStrISO8601Split[0].split(ISO8601_DATETIME_SEP_CHAR)[1];

    //final Instant mcbLeastRecentInstant= mcbForSpineAPI.get(0).getLeastRecentInstantCopy();
    //slog.info(mmi+"mcbLeastRecentInstant="+mcbLeastRecentInstant.toString());
    //final long mcbTimeIntrvSeconds= mcbForSpineAPI.getDataTimeIntervallSeconds();

    // --- Define the daysOffsetInFuture for the Q2, Q3 and Q4 Spine API input files.
    int daysOffsetInFuture= 1;

    // --- 30 days in future case if whatTimeIsItNowMinutes < 15mins
    if (whatTimeIsItNowMinutes < 15) {
	
      spineAPIInputFName += HOUR_FNAMES_COMMON_POSTFIX;

      // --- Need to set the daysOffsetInFuture to 3o days here.
      daysOffsetInFuture= 30; 
	
    } else if (whatTimeIsItNowMinutes < 30) {

      spineAPIInputFName += "Q2" + QUARTER_FNAME_COMMON_POSTFIX;

    } else if (whatTimeIsItNowMinutes < 45) {

      spineAPIInputFName += "Q3" + QUARTER_FNAME_COMMON_POSTFIX; 

    } else { // --- 45 <= whatTimeIsItNowMinutes < 59
	
      spineAPIInputFName += "Q4" + QUARTER_FNAME_COMMON_POSTFIX;
    }

    slog.info(mmi+"spineAPIInputFName="+spineAPIInputFName);

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
    final Instant instantsRangeLimitInFuture= mcbLeastRecentInstant.plus(daysOffsetInFuture,ChronoUnit.DAYS);

    slog.info(mmi+"instantsRangeLimitInFuture="+instantsRangeLimitInFuture.toString());

    // --- NOTE: instantsRangeLimitInFuture Instant is not included in the subset of
    //           Instant objects here.
    final SortedSet<Instant> wantedInstantsRange= mcbForSpineAPI.get(0).
      getInstantsKeySetCopy().subSet(mcbLeastRecentInstant,instantsRangeLimitInFuture);

    // --- Need to add mcbTimeIntrvSeconds to lastInstantWantedInFuture for the SortedSet<Instant>.subSet() method
    //     here in order to include the lastInstantWantedInFuture in this wantedInstantsRange.	
    //.subSet(mcbLeastRecentInstant, lastInstantWantedInFuture.plusSeconds(mcbTimeIntrvSeconds));

    slog.info(mmi+"wantedInstantsRange first="+wantedInstantsRange.first().toString());
    slog.info(mmi+"wantedInstantsRange last="+wantedInstantsRange.last().toString());
    slog.info(mmi+"wantedInstantsRange.size()="+wantedInstantsRange.size());
    
    slog.info(mmi+"debug exit 0");
    System.exit(0);
    
    slog.info(mmi+"end");
    
  } // --- method writeFiles
}
 
