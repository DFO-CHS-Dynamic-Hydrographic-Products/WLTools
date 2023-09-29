//package ca.gc.dfo.iwls.fmservice.modeling.util;
package ca.gc.dfo.chs.wltools.util;

/**
 * Created by Gilles Mercier on 2017-12-19.
 */

//---

import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//--
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
  public static void writeOdinAsciiFmtFile(final String stationId,
                                           final WLStationTimeNode stationTimeNode0,
                                           final List<MeasurementCustom> updatedForecast,
                                           /*@NotNull*/ final String outDir) {

    final String mmi= "writeOdinAsciiFmtFile: ";

    try {
      stationId.length();
    } catch (NullPointerException e) {

      slog.error(mmi+"ASCIIFileIO writeOdinAsciiFmtFile: stationId==null !!");
      throw new RuntimeException(MMi+e);
    }

    slog.debug(mmi+"start: station=" + stationId);

    try {
      stationTimeNode0.getSse();

    } catch (NullPointerException e) {

      sog.error(mmi+"stationTimeNode0==null !!");
      throw new RuntimeException(mmi+e);
    }

    final SecondsSinceEpoch dt0= stationTimeNode0.getSse();

    final FileWriter[] fwra= new FileWriter[IWL.WLType.values().length];

    for (final IWL.WLType type : IWL.WLType.values()) {

      final String fpath= outDir + "\\" + stationCode + "-" +
                   dt0.dateTimeString() + "." + type.asciiFileExt;

      slog.info(mmi+"Opening odin format " + type + " output file: " + fpath);

      try {

        fwra[type.ordinal()] = new FileWriter(fpath);

      } catch (IOException e) {

        sog.error(mmi+"Cannot open output file: " + fpath);

        e.printStackTrace();
        throw new RuntimeException(mmi+e);
      }

      try {

        slog.debug(mmi+"write header: " + AsciiFormats.ODIN.header + " in " + fpath);
        fwra[type.ordinal()].write(AsciiFormats.ODIN.header + type.asciiFileExt.toUpperCase() + ";\n");

      } catch (IOException e) {

        e.printStackTrace();
        slog.error(mmi+"Cannot write header in " + fpath);
        throw new RuntimeException(mmi+e);
      }
    }

    WLStationTimeNode iter = stationTimeNode0;

    while (iter != null) {

      final String odinDtFmtString= SecondsSinceEpoch.odinDtFmtString(iter.getSse());

      //staticLog.debug("FileIO writeOdinAsciiFmtFile: iter.getSse() dt="+iter.getSse().dateTimeString(true));
      //staticLog.debug("FileIO writeOdinAsciiFmtFile: odinDtFmtString="+odinDtFmtString);

      for (final IWL.WLType type : IWL.WLType.values()) {

        //staticLog.debug("FileIO writeOdinAsciiFmtFile: type="+ type +", iter.get(type)="+ iter.get(type));
        //staticLog.debug("FileIO writeOdinAsciiFmtFile: iter.get(type).zDValue()="+iter.get(type).zDValue());

        if (iter.get(type) != null) {

          try {

            fwra[type.ordinal()].write(odinDtFmtString + ";   " +
                String.format(Locale.ROOT, AsciiFormats.ODIN.numericFormat, iter.get(type).getDoubleZValue()) + ";\n");

          } catch (IOException e) {

            slog.error(mmi+"annot write data line in output file type: " + type);
            e.printStackTrace();
            throw new RuntimeException(mmi+e);
          }
        }
      }

      iter= (WLStationTimeNode) iter.getFutr();

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

    ////--- Updated forecast could be null or empty:
    //if ((updatedForecast != null) && (updatedForecast.size() != 0)) {
    //
    //  slog.info(mmi+"writing udpdated forecast data for station: " + stationId);
    //
    //  final String fpath= outDir + "\\" + stationCode + "-" +
    ///      dt0.dateTimeString() + "-updated." + IWL.WLType.FORECAST.asciiFileExt;
    //
    //  slog.info(mmi+"Opening odin format updated " +
    //             IWL.WLType.FORECAST + " " +"output file: " + fpath);
    //
    //  FileWriter fw = null;
    //
    //  try {
    //    fw = new FileWriter(fpath);
    //
    //  } catch (IOException e) {
    //
    //    log.error("ASCIIFileIO writeOdinAsciiFmtFile: Cannot open output file: " + fpath);
    //
    //    e.printStackTrace();
    //    throw new RuntimeException(e);
    //  }
    //
    //  try {
    //
    //    log.debug("ASCIIFileIO writeOdinAsciiFmtFile: write header: " + AsciiFormats.ODIN.header + " in " + fpath);
    //    fw.write(AsciiFormats.ODIN.header + IWL.WLType.FORECAST.asciiFileExt.toUpperCase() + ";\n");
    //
    //  } catch (IOException e) {
    //
    //    log.error("ASCIIFileIO writeOdinAsciiFmtFile: Cannot write header in " + fpath);
    //
    //    e.printStackTrace();
    //    throw new RuntimeException(e);
    //  }
    //
    //  for (final MeasurementCustom msm : updatedForecast) {
    //
    //    final String odinDtFmtString = SecondsSinceEpoch.odinDtFmtString(msm.getEventDate().getEpochSecond());
    //
    //    try {
    //
    //      //--- NOTE: Need to use the arg. Locale.ROOT in String.format to get the decimal point in the results:
    //      fw.write(odinDtFmtString + ";   " + String.format(Locale.ROOT, AsciiFormats.ODIN.numericFormat,
    //          msm.getValue().doubleValue()) + ";\n");
    //
    //    } catch (IOException e) {
    //
    //      log.error("ASCIIFileIO writeOdinAsciiFmtFile: Cannot write data line in output file: " + fpath);
    //
    //      e.printStackTrace();
    //      throw new RuntimeException(e);
    //    }
    //  }
    //
    //  try {
    //    fw.close();
    //
    //  } catch (IOException e) {
    //
    //    log.error("ASCIIFileIO writeOdinAsciiFmtFile: Cannot close file " + fpath);
    //
    //    e.printStackTrace();
    //    throw new RuntimeException(e);
    //  }
    //
    //} else {
    //
    //  log.warn("ASCIIFileIO writeOdinAsciiFmtFile: updatedForecast is null or empty !");
    //}

    slog.info(mmi+"end for station=" + stationCode);
  }
}
