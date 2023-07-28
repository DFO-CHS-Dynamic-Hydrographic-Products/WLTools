//package ca.gc.dfo.iwls.fmservice.modeling.tides;
package ca.gc.dfo.chs.wltools.tidal.stationary.prediction;

/**
 * Created on 2018-01-19.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-20, Gilles Mercier
 */

//---
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1D;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1DData;
import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.StationaryTidalPredFactory;

//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1D;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1DData;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
//---
//---

/**
 * Generic class for producing 1D(i.e. only one spatial component) tidal predictions
 */
//abstract 
public class Stationary1DTidalPredFactory extends StationaryTidalPredFactory {
 
   private final static String whoAmI= "ca.gc.dfo.chs.wltools.tidal.stationary.prediction.Stationary1DTidalPredFactory";
 
 /**
   * log utility.
   */
   private final static Logger slog = LoggerFactory.getLogger(whoAmI);


  /**
   * Map of a group of tidal constituents informations coming from a file or a DB.
   */
  protected HashMap<String, Constituent1D> tcDataMap = null;

  /**
   * Constituent1DData object which will be used by the tidal predictions method.
   */
  private Constituent1DData constituent1DData = null;

  /**
   * unfortunateUTCOffsetSeconds: The WL tidal constituents could(unfortunately) be
   * defined for a local time zone so we have to apply a time zone offset to the
   * results in order to have the tidal predictions defined in UTC(a.k.a. ZULU).
   */
  private long unfortunateUTCOffsetSeconds = 0L;

  /**
   * Default constructor.
   */
  public Stationary1DTidalPredFactory() {
    
    super();
    
    this.tcDataMap = null;
    this.constituent1DData = null;
  }

  /**
   * Comments please!
   */
  final public Set<String> getTcNames() {
     return this.tcDataMap.keySet();
  }
  
  /**
   * @param timeStampSeconds : A time-stamp in seconds since the epoch where we want a single tidal prediction.
   * @return The newly computed single tidal prediction in double precision.
   */
  @Override
  public double computeTidalPrediction(final long timeStampSeconds) {
    return this.astroInfosFactory.computeTidalPrediction(timeStampSeconds, this.constituent1DData);
  }

  /**
   *
   */
  final public long getUnfortunateUTCOffsetSeconds() {
     return this.unfortunateUTCOffsetSeconds;
  }

  /**
   * @param method           : Tidal prediction method to use.
   * @param latitudeRadians  : Latitude of the 2D point where we want 1D tidal predictions
   * @param startTimeSeconds : Time-stamp in seconds since the epoch for the time reference used for astronomic
   *                         arguments computations.
   * @param constNames       : A Set of tidal constituents names to use for the tidal predictions.
   * @return The current Stationary1DTidalPredFactory object.
   */
  @Override
  public Stationary1DTidalPredFactory setAstroInfos(final Method method,
                                                    final double latitudeRadians,
                                                    final long startTimeSeconds,
                                                    /*@NotNull @Size(min = 1)*/ final Set<String> constNames) {
    try {
      constNames.size();
      
    } catch (NullPointerException e) {
      
      slog.error("setAstroInfos: constNames==null !!");
      throw new RuntimeException(e);
    }
    
    slog.info("setAstroInfos: start");
    
    super.setAstroInfos(method, latitudeRadians, startTimeSeconds, constNames);
    
    this.constituent1DData= new Constituent1DData(this.tcDataMap, this.astroInfosFactory);
    
    slog.info("setAstroInfos: end");
    
    return this;
  }

  /**
   * Extract tidal constituents data from a classic legacy DFO TCF ASCII file.
   *
   * @param aTCFFilePath : Complete WL tidal constituents TCF format input file path on a local disk.
   * @return The current WLStationTidalPredictionsFactory object (this).
   */
  //@NotNull
  final public Stationary1DTidalPredFactory getTCFFileData(/*@NotNull*/ final String aTCFFilePath) {

    //--- Deal with possible null aTCFFilePath String:
    try {
      aTCFFilePath.length();

    } catch (NullPointerException e) {

      slog.error("getTCFFileData: aTCFFilePath==null!!");
      throw new RuntimeException(e);
    }

    slog.debug("getTCFFileData: Start, aTCFFilePath=" + aTCFFilePath);

    //--- Get the TCF format ASCII lines in a List of Strings:
    final List<String> tcfFileLines = ASCIIFileIO.getFileLinesAsArrayList(aTCFFilePath);

    //--- Assume no UTC time offset.
    this.unfortunateUTCOffsetSeconds = 0L;

    if (this.tcDataMap != null) {

      slog.warn("getTCFFileData: this.tcDataMap!=null, need to clear it first !");
      this.tcDataMap.clear();

    } else {

      slog.debug("getTCFFileData: Creating this.tcDataMap.");
      this.tcDataMap = new HashMap<String, Constituent1D>();
    }

    //--- Process the TCF format lines
    for (final String line : tcfFileLines) {

      slog.debug("getTCFFileData: processing line: " + line);

      //--- Split( blank spaces as delimiters) line in an array of Strings:
      final String[] lineSplit = line.trim().split("\\s+");

      final String last2chars = line.substring(line.length() - 2);

      //--- The comment String marker is at the end of the line for the TCF format.
      if (last2chars.equals(TCF_COMMENT_FLAG)) {

        final String firstString = lineSplit[0];

        if (firstString.equals(TCF_UTC_OFFSET_FLAG)) {

          final String offsetString = lineSplit[TCF_UTC_OFFSET_LINE_INDEX];

          final String offsetSignString = offsetString.substring(0, 1);

          final long offset = Long.parseLong(offsetString.substring(1));

          this.unfortunateUTCOffsetSeconds = SECONDS_PER_HOUR * (offsetSignString.equals("+") ? offset : -offset);

          //this.log.debug("WLStationTidalPredictionsFactory getTCFFileData: getTCFData:
          // unfortunateUTCOffset="+unfortunateUTCOffset);

        } else {

          slog.debug("getTCFFileData: Skipping TCF file header line: " + line);
        }

        continue;
      }

      if (lineSplit.length != TCF_LINE_NB_ITEMS) {

        slog.error("getTCFFileData: lineSplit.length=" +
                   lineSplit.length + " !=" + " TCF_LINE_NB_ITEMS=" + TCF_LINE_NB_ITEMS);

        throw new RuntimeException("getTCFFileData");
      }

      final String tcName = lineSplit[0];

      final double phaseLagDegrees = Double.valueOf(lineSplit[TCF_PHASE_LAG_LINE_INDEX]);

//            final double phaseLagDegrees= Double.valueOf(lineSplit[TCF_PHASE_LAG_LINE_INDEX]) +
//                    (!tcName.equals("Z0") ? this.unfortunateUTCOffsetSeconds/SECONDS_PER_HOUR : 0.0);

      //--- NOTE: The Greenwich phase lag must always be expressed in radians in a Constituent1D Object:
      final Constituent1D c1d = new Constituent1D(Double.valueOf(lineSplit[TCF_AMPLITUDE_LINE_INDEX]),
          DEGREES_2_RADIANS * phaseLagDegrees);

      slog.debug("getTCFFileData:  Setting this.tcDataMap with tidal " +
                 "constitutent ->" + tcName + ", phaseLagDegrees=" + phaseLagDegrees);

      this.tcDataMap.put(tcName, c1d);

    } // --- End for loop

    slog.debug("getTCFFileData: end, unfortunateUTCOffset in hours=" + this.unfortunateUTCOffsetSeconds / SECONDS_PER_HOUR);

    return this;
  }

  /**
   * Comments please!
   */
   public Stationary1DTidalPredFactory getNSJSONFileData(/*@NotNull*/ final String jsonFilePath) {

      throw new RuntimeException(whoAmI + " - getNSJSONFileData method not implemented yet!");

      //return this;
   }
}
