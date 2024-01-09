package ca.gc.dfo.chs.wltools;

// ---
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
//import java.time.Instant;
//import java.util.HashMap;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

// ---
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.DirectoryStream;

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
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

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
  final public static boolean checkForFileExistence(final String filePath) {

    final String mmi= "checkForAsciiFileExistence: ";

    //boolean asciiFileExists= true;

    try {
      filePath.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    final File fileOutThere= new File(filePath);

    return (fileOutThere.exists()) ? true: false;
  }

  // --- TODO: add some fool-proof checks
  final public static List<Path> getRelevantFilesList(final String inputDir, final String relevantFilesRegExpr) {

    final String mmi= "getRelevantFilesList: ";

    slog.info(mmi+"start");
    
    // --- Build a Path object for the folder where we want to find relevant files  
    final Path inputDataDir= FileSystems.getDefault().getPath(inputDir);

    slog.info(mmi+"inputDataDir="+inputDataDir.toString());

    // --- List all the relevant files in inputDir using a DirectoryStream<Path> object     
    DirectoryStream<Path> inputDataDirFilesDS= null;
    
    try {
      inputDataDirFilesDS= Files.
	newDirectoryStream(inputDataDir, relevantFilesRegExpr);
      
    } catch (IOException ioex) {
      throw new RuntimeException(mmi+ioex);
    }

    // --- Now put all the relevant files in a List<Path>
    //     object to be returned
    List<Path> inputDataDirFilesList= new ArrayList<Path>();    

    for (final Path inputFilePath: inputDataDirFilesDS) {
      inputDataDirFilesList.add(inputFilePath);
    }

    if (inputDataDirFilesList.size() == 0) {
      throw new RuntimeException(mmi+"inputDataDirFilesList cannot be empty here!");
    }
    
    slog.info(mmi+"end");

    return inputDataDirFilesList;
  }

  // --- TODO: add some fool-proof checks
  final public static String getSCLocFilePath(final List<Path> scLocsFilesPathsList, final String scLocFNameSubStr) {
      
      //final String mmi= "getSCLocFilePath: ";

    String scLocFilePathRet= null;
    
    for (final Path scLocFilePath: scLocsFilesPathsList) {
	
      final String checkFPath= scLocFilePath.toString();

      //slog.info(mmi+"checkFPath="+checkFPath);
      
      if (checkFPath.contains(scLocFNameSubStr)) {
	  
    	scLocFilePathRet= checkFPath;
    	break;  
       }
    }

    //slog.info(mmi+"scLocFilePathRet="+scLocFilePathRet);

    return scLocFilePathRet;
  }

  // --- Build the path of the tide gauges info, file name in the cfg DB folders.
  final public static String getTideGaugeInfoFilePath(final String tideGaugeInfoFileName) {

    return getMainCfgDir() + //File.separator +
      ITideGaugeConfig.INFO_FOLDER_NAME + File.separator + tideGaugeInfoFileName;
  }

  // --- Only for the tidal non-stationary (NS) data.
  final public static String getLocationNSTFHAFilePath(final String tidalConstsTypeId,
                                                       final String tidalConstsTypeModelId, final String locationIdInfo) {
    final String mmi= "getLocationTFHAFilePath: ";

    // --- No fool-proof check for the args. for now.

    final String [] locationIdInfoSplit=
      locationIdInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (locationIdInfoSplit.length != 3) {
      throw new RuntimeException(mmi+"ERROR: locationIdInfoSplit.length != 3 !!");
    }

    final String regionIdInfo= locationIdInfoSplit[0];
    final String subRegionIdInfo= locationIdInfoSplit[1];
    final String locationIdSpec= locationIdInfoSplit[2];

    return mainCfgDir + "tidal/nonStationary/" + regionIdInfo + "/dischargeClusters/" +
           subRegionIdInfo + File.separator + tidalConstsTypeId + File.separator + tidalConstsTypeModelId +
           File.separator + locationIdSpec + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;
  }

  // ---
  final public static void writeToOutputDir(final List<MeasurementCustom> wlDataToWrite,
					    final IWLToolsIO.Format outputFormat,
					    final String locationId, final String writeToOutputDirArg ) { //, final String writeToOutputDirArg ) {

    final String mmi= "writeToOutputDir: ";

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

    // --- Use the writeToOutputDirArg if it is not null, otherwise use the default
    //     statically defined outputDirectory
    final String outputDirToUse= (writeToOutputDirArg != null) ? writeToOutputDirArg: outputDirectory;

    if (outputFormat == IWLToolsIO.Format.CHS_JSON) {

      writeCHSJsonFormat(wlDataToWrite, locationId, outputDirToUse);

    } else {
       throw new RuntimeException(mmi+"IWLTools.Format output type -> "+outputFormat.name()+" not implemented yet !!");
    }
  }

  // ---
  final public static void writeCHSJsonFormat(final List<MeasurementCustom> wlDataToWrite,
					      final String locationId, final String outputDirectoryArg) {

    final String mmi= "writeCHSJsonFormat: ";

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
      outputDirectoryArg.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    final String jsonOutFileNamePrfx= locationId.
      replace(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR, IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR);

    final String jsonOutputFile= outputDirectoryArg +
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

    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);
  }
}
