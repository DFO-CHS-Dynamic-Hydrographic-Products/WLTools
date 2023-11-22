package ca.gc.dfo.chs.wltools;

// ---
import java.util.List;
import org.slf4j.Logger;
//import java.time.Instant;
//import java.util.HashMap;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

//import javax.validation.constraints.NotNull;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonArrayBuilder;

// ---
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;

/**
 *
 */
public class WLToolsIO implements IWLToolsIO {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.WLToolsIO";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  static private String mainCfgDir= null;

  static private String outputDirectory= null;

  static private String outputDataFormat= null;

  //public WLToolsIO(final String mainCfgDirArg) {
  //  mainCfgDir= mainCfgDirArg;
  //}

  final protected static String setMainCfgDir(final String mainCfgDirArg) {
    mainCfgDir= mainCfgDirArg;
    return mainCfgDir;
  }

  final protected static String setOutputDirectory(final String outputDirectoryArg) {
    outputDirectory= outputDirectoryArg;
    return outputDirectory;
  }

  // ---
  final protected static String setOutputDataFormat(final String outputDataFormatArg) {
    outputDataFormat= outputDataFormatArg;
    return outputDataFormat;
  }

  // ---
  final public static String getMainCfgDir() {
    return mainCfgDir;
  }

  // ---
  final public static String getOutputDirectory() {
    return outputDirectory;
  }

  // ---
  final public static String getOutputDataFormat() {
    return outputDataFormat;
  }

  // ---
  final public static void writeToOutputDir(final List<MeasurementCustom> wlDataToWrite,
                                            final IWLToolsIO.Format outputFormat, final String locationId) { //, final String writeToOutputDirArg ) {

    final String mmi= "ca.gc.dfo.chs.wltools.WLToolsIO.writeToOutputDir: ";

    try {
      wlDataToWrite.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    //try {
    //  writeToOutputDirArg.length();
    //} catch (NullPointerExceptio npe) {
    //  throw new RuntimeException(mmi+npe);
    // }
    //outputDirectory= writeToOutputDirArg;

    try {
      outputFormat.name();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    if (outputFormat == IWLToolsIO.Format.CHS_JSON) {

       writeCHSJsonFormat(wlDataToWrite, locationId);

    } else {
       throw new RuntimeException(mmi+"IWLTools.Format output type -> "+outputFormat.name()+" not implemented yet !!");
    }
  }

  // ---
  final public static void writeCHSJsonFormat(final List<MeasurementCustom> wlDataToWrite, final String locationId) {

    final String mmi= "ca.gc.dfo.chs.wltools.WLToolsIO.writeCHSJsonFormat: ";

    slog.info(mmi+"start");

    try {
      wlDataToWrite.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    try {
      locationId.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    try {
      outputDirectory.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    final String jsonOutFileNamePrfx= locationId.
      replace(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR, IWLAdjustmentIO.OUTPUT_DATA_FMT_SPLIT_CHAR);

    final String jsonOutputFile= outputDirectory +
      File.separator + jsonOutFileNamePrfx + IWLToolsIO.JSON_FEXT ;

    slog.info(mmi+"jsonOutputFile="+jsonOutputFile);

    FileOutputStream jsonFileOutputStream= null;

    try {
      jsonFileOutputStream= new FileOutputStream(jsonOutputFile);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+e);
    }

    JsonArrayBuilder jsonArrayBuilderObj= Json.createArrayBuilder();

    // --- Loop on all the MeasurementCustom objects that contains the WL data.
    for (final MeasurementCustom mc: wlDataToWrite) {

      //final String eventDateISO8601= mc.getEventDate().toString();
      //final double wlpValue= mc.getValue();

      jsonArrayBuilderObj.
        add( Json.createObjectBuilder().
          add(IWLToolsIO.VALUE_JSON_KEY, mc.getValue() ).
            add( IWLToolsIO.INSTANT_JSON_KEY, mc.getEventDate().toString() ) );
              //add( Json.createObjectBuilder().add(IWLStationPredIO.VALUE_JSON_KEY, mc.getValue() ));
    }

    // --- Now write the Json data bundle in the output file.
    Json.createWriter(jsonFileOutputStream).
      writeArray( jsonArrayBuilderObj.build() );

    // --- We can close the Json file now
    try {
      jsonFileOutputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }

    slog.info(mmi+"end");

    slog.info(mmi+"debug System.exit(0)");
    System.exit(0);
  }

}
