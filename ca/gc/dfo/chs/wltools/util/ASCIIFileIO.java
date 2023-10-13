//package ca.gc.dfo.iwls.fmservice.modeling.util;
package ca.gc.dfo.chs.wltools.util;

/**
 * Created by Gilles Mercier on 2017-12-19.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.List;
import java.util.Locale;
import java.time.Instant;
import java.util.ArrayList;

// ---
import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;

//---
//---
//---

/**
 * specific class for ASCII files IO:
 */
public abstract class ASCIIFileIO implements IASCIIFileIO {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.util.ASCIIFileIO";

  /**
   * static log utility
   */
   private final static Logger slog = LoggerFactory.getLogger(whoAmI);
   //private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * @param aSCIIFilePath : Complete file path of an ASCII input file.
   * @return A List of all the lines contained in the ASCII input file.
   */
  //final public static List<String> getFileLinesAsArrayList(@NotNull final String aSCIIFilePath) {
  final public static List<String> getFileLinesAsArrayList(final String aSCIIFilePath) {

    final String mmi= "getFileLinesAsArrayList: ";

    String tmpLine = null;
    FileReader fr = null;
    BufferedReader br = null;

    final List<String> allLines = new ArrayList<>();

    try {
      aSCIIFilePath.length();

    } catch (NullPointerException e) {

      slog.error(mmi+"aSCIIFilePath==null!!");
      throw new RuntimeException(e);
    }

    slog.info(mmi+"aSCIIFilePath=" + aSCIIFilePath);

    try {

      fr= new FileReader(aSCIIFilePath);

    } catch (FileNotFoundException e) {

      slog.info(mmi+"ASCII file " + aSCIIFilePath + " not found !!");
      throw new RuntimeException(mmi+e);
    }

    if ((br = new BufferedReader(fr)) == null) {

      slog.error(mmi+"Cannot create BufferedReader for file " + aSCIIFilePath);
      throw new RuntimeException(mmi);
    }

    try {

      while ((tmpLine = br.readLine()) != null) {
        allLines.add(tmpLine);
      } //---- while loop

    } catch (IOException e) {

      slog.error(mmi+"Problem reading ASCII file " + aSCIIFilePath);
      throw new RuntimeException(e);
    }

    try {

      br.close();

    } catch (IOException e) {

      slog.error(mmi+"Cannot close file " + aSCIIFilePath);
      throw new RuntimeException(mmi+e);
    }

    slog.info(mmi+"end");

    return allLines;
  }

  /**
   * @param stationId      : station String Id
   * @param stationTimeNode0 : The 1st item of a List of WLStationTimeNode objects
   * @param updatedForecast  : A new WL forecast Measurement data List (could be null or empty).
   * @param outDir           : The complete path of the directory where to write the WL ODIN DB ASCII format files
   */
  //public static void writeOdinAsciiFmtFile(@NotNull final String stationCode,
  //                                         @NotNull final WLStationTimeNode stationTimeNode0,
  //                                       final List<MeasurementCustom> updatedForecast,
  //                                         @NotNull final String outDir) {
  public static void writeOdinAsciiFmtFiles(final String stationId,
                                            final WLStationTimeNode stationTimeNode0,
                                            final List<MeasurementCustom> updatedForecast,
                                            final Instant firstInstantForWriting,
                                            /*@NotNull*/ final String outDir) {

    final String mmi= "writeOdinAsciiFmtFile: ";

    try {
      stationId.length();
    } catch (NullPointerException e) {

      //slog.error(mmi+"ASCIIFileIO writeOdinAsciiFmtFile: stationId==null !!");
      throw new RuntimeException(mmi+e);
    }

    slog.info(mmi+"start: station id.=" + stationId);

    try {
      stationTimeNode0.getSse();

    } catch (NullPointerException e) {

      //slog.error(mmi+"stationTimeNode0==null !!");
      throw new RuntimeException(mmi+e);
    }

    SecondsSinceEpoch dt0= stationTimeNode0.getSse();

    final long firstSecondsForWriting= firstInstantForWriting.getEpochSecond();

    // --- Replace the seconds since epoch of dt0 with the seconds since epoch
    //     of the firstInstantForWriting if the latter is larger. It ensure that
    //     predictions and full model forecast (and possibly obs. also) WL data
    //     begin at the same time stamp in the output files.
    if (firstSecondsForWriting > dt0.seconds() ) {
      dt0.set(firstSecondsForWriting);
    }

    final FileWriter[] fwra= new FileWriter[IWL.WLType.values().length];

    for (final IWL.WLType type : IWL.WLType.values()) {

      final String fpath= outDir + File.separator + stationId + "-" +
                   dt0.dateTimeString() + "." + type.asciiFileExt;

      slog.info(mmi+"Opening odin format " + type + " output file: " + fpath);

      try {
        fwra[type.ordinal()]= new FileWriter(fpath);

      } catch (IOException e) {

        slog.error(mmi+"Cannot open output file: " + fpath);

        e.printStackTrace();
        throw new RuntimeException(mmi+e);
      }

      try {

        slog.info(mmi+"write header: " + AsciiFormats.ODIN.header + " in " + fpath);

        fwra[type.ordinal()].write(AsciiFormats.ODIN.header + type.asciiFileExt.toUpperCase() + ";\n");
        fwra[type.ordinal()].flush();

      } catch (IOException e) {

        e.printStackTrace();
        slog.error(mmi+"Cannot write header in " + fpath);
        throw new RuntimeException(mmi+e);
      }
    }

    slog.info(mmi+"All files opened and their headers have been written");
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    WLStationTimeNode wlsTNIter= stationTimeNode0;

    while (wlsTNIter != null) {

      final SecondsSinceEpoch wlsTNIterSse= wlsTNIter.getSse();

      //--- Skip data write if the iterSse is in the past
      //    compared to the dt0 seconds since epoch.
      if (wlsTNIterSse.seconds() < dt0.seconds() ) {

        // --- Need to get the next WLStationTimeNode object
        //     for the next loop iteration.
        wlsTNIter= (WLStationTimeNode) wlsTNIter.getFutr();
        continue;
      }

      final String odinDtFmtString=
        SecondsSinceEpoch.odinDtFmtString(wlsTNIterSse);

      //staticLog.debug("FileIO writeOdinAsciiFmtFile: iter.getSse() dt="+iter.getSse().dateTimeString(true));
      //staticLog.debug("FileIO writeOdinAsciiFmtFile: odinDtFmtString="+odinDtFmtString);

      for (final IWL.WLType type : IWL.WLType.values()) {

        //staticLog.debug("FileIO writeOdinAsciiFmtFile: type="+ type +", iter.get(type)="+ iter.get(type));
        //staticLog.debug("FileIO writeOdinAsciiFmtFile: iter.get(type).zDValue()="+iter.get(type).zDValue());

        if (wlsTNIter.get(type) != null) {

          try {

            //--- NOTE: Need to use the arg. Locale.ROOT in String.format to get the decimal point in the result
            fwra[type.ordinal()].write(odinDtFmtString + ";   " +
                String.format(Locale.ROOT, AsciiFormats.ODIN.numericFormat, wlsTNIter.get(type).getDoubleZValue()) + ";\n");

          } catch (IOException e) {

            slog.error(mmi+"annot write data line in output file type: " + type);
            e.printStackTrace();
            throw new RuntimeException(mmi+e);
          }
        }
      }

      // --- Get the next WLStationTimeNode object in future
      //     for the next loop iteration
      wlsTNIter= (WLStationTimeNode) wlsTNIter.getFutr();

      //staticLog.debug("FileIO writeOdinAsciiFmtFile: next iter="+iter);
    }

    for (final IWL.WLType type: IWL.WLType.values()) {

      slog.info(mmi+"closing output file type: " + type);

      try {
        fwra[type.ordinal()].close();

      } catch (IOException e) {

        slog.error(mmi+"Cannot close output file for WL type: " + type.toString());
        e.printStackTrace();
        throw new RuntimeException(mmi+e);
      }
    }

    //--- Updated forecast could be null or empty:
    if ((updatedForecast != null) && (updatedForecast.size() != 0)) {

      slog.info(mmi+"writing adjusted forecast data for station: " + stationId);

      final String fpath= outDir + File.separator + stationId + "-" +
        dt0.dateTimeString() + "-adjusted." + IWL.WLType.MODEL_FORECAST.asciiFileExt;

      slog.info(mmi+"Opening odin format adjusted " +
                IWL.WLType.MODEL_FORECAST + " " +"output file: " + fpath);

      FileWriter fw= null;

      try {
        fw= new FileWriter(fpath);

      } catch (IOException e) {

        slog.error(mmi+"Cannot open output file: " + fpath);
        e.printStackTrace();
        throw new RuntimeException(e);
      }

      try {

        slog.info(mmi+"write header: " + AsciiFormats.ODIN.header + " in " + fpath);

        fw.write(AsciiFormats.ODIN.header + IWL.WLType.MODEL_FORECAST.asciiFileExt.toUpperCase() + ";\n");
        fw.flush();

      } catch (IOException e) {

        slog.info(mmi+"Cannot write header in " + fpath);

        e.printStackTrace();
        throw new RuntimeException(e);
      }

      for (final MeasurementCustom msm : updatedForecast) {

        final long checkSeconds= msm.getEventDate().getEpochSecond();

        // --- Skip data write if the checkSeconds is in the
        //     past compared to the dt0 seconds.
        if (checkSeconds < dt0.seconds()) {
          continue;
        }

        final String odinDtFmtString=
          SecondsSinceEpoch.odinDtFmtString(checkSeconds); //(msm.getEventDate().getEpochSecond());

        try {

          //--- NOTE: Need to use the arg. Locale.ROOT in String.format to get the decimal point in the results:
          fw.write(odinDtFmtString + ";   " +
                   String.format(Locale.ROOT, AsciiFormats.ODIN.numericFormat,
                   msm.getValue().doubleValue()) + ";\n");

        } catch (IOException e) {

          slog.error(mmi+"Cannot write data line in output file: " + fpath);
          e.printStackTrace();
          throw new RuntimeException(mmi+e);
        }
      }

      try {
        fw.close();

      } catch (IOException e) {

        slog.error(mmi+"Cannot close file " + fpath);
        e.printStackTrace();
        throw new RuntimeException(e);
      }

    } else {
      slog.info(mmi+"updatedForecast is null or empty !");
    }

    slog.info(mmi+"end for station=" + stationId);
  }
}
