package ca.gc.dfo.chs.wltools;

// ---
import java.util.Map;
import java.util.List;
import java.time.Instant;
import java.util.HashMap;
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

// --- HDFql lib
import as.hdfql.HDFql;
import as.hdfql.HDFqlCursor;
import as.hdfql.HDFqlConstants;

// --- 
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

/**
 *
 */
abstract public class WLToolsIO implements IWLToolsIO {

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

    slog.debug(mmi+"start");

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

    slog.debug(mmi+"jsonOutputFile="+jsonOutputFile);

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

    slog.debug(mmi+"end");

    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);
  }

  // ---
  final public static void ippAdjToS104DCF8(final Map<String,String> argsMap) {

    final String mmi= "ippAdjToS104DCF8: ";

    slog.info(mmi+"start");

    if (!argsMap.containsKey("--inputDataFormat")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --inputDataFormat defined !!");
    }

    final String checkInputDataFormat= argsMap.get("--inputDataFormat");

    if (!checkInputDataFormat.equals(IWLToolsIO.Format.CHS_JSON.name())) {

      throw new RuntimeException(mmi+"Invalid input data format ->"+checkInputDataFormat+
				 " Only the "+IWLToolsIO.Format.CHS_JSON.name()+" input data format is accepted for now");
    }

    //slog.info(mmi+"Using the checkInputDataFormat="+checkInputDataFormat);

    if (!argsMap.containsKey("--IPPAdjResultsInputDir")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --IPPAdjResultsInputDir defined !!");
    }

    final String ippAdjResultsInputDir= argsMap.get("--IPPAdjResultsInputDir");

    if (!checkForFileExistence(ippAdjResultsInputDir)) {
      throw new RuntimeException(mmi+"ippAdjResultsInputDir folder not found !!");
    } 
    
    slog.info(mmi+"ippAdjResultsInputDir="+ippAdjResultsInputDir);

    // --- 
    if (checkInputDataFormat.equals(IWLToolsIO.Format.CHS_JSON.name())) {
	    
      slog.info(mmi+"The IPPAdj input data format is -> "+IWLToolsIO.Format.CHS_JSON.name());

      ippCHSJsonToS104DCF8(ippAdjResultsInputDir);
      
      slog.info(mmi+"debug System.exit(0)");
      System.exit(0); 
    }

    //if (checkInputDataFormat.equals(IWLToolsIO.Format.ONELOC_S104DCF8.name())) {
    //}

    slog.info(mmi+"end");

    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0); 
  }

  // ---
  final public static void ippCHSJsonToS104DCF8(final String ippAdjResultsInputDir) {
      
    final String mmi= "ippCHSJsonToS104DCF8: ";

    slog.info(mmi+"start, ippAdjResultsInputDir="+ippAdjResultsInputDir);

    // --- Get the paths of all the ship channel points locations adjusted WL
    //     (SpineIPP outputs) data input files (CHS_JSON format) in a List<Path> object 
    final List<Path> adjSpineIPPInputDataFilesList=
      getRelevantFilesList(ippAdjResultsInputDir, "*"+IWLAdjustmentIO.ADJ_HFP_ATTG_FNAME_PRFX+"*"+JSON_FEXT);

    Map<String, MeasurementCustomBundle> allSCLocsIPPInputData= new HashMap<String, MeasurementCustomBundle>();

    slog.info(mmi+"Reading all the SpineIPP results input files in CHS_JSON format");
    
    // --- Loop on all the files of all the ship channel point locations adjusted WL
    //     (SpineIPP outputs) data input files (order is not important here since we are
    //      using a Map to store the data using their num. ids as keys) 
    for(final Path adjSpineIPPInputDataFilePath: adjSpineIPPInputDataFilesList) {

      final String adjSpineIPPInputDataFileStr= adjSpineIPPInputDataFilePath.toString();
	
      slog.debug(mmi+"adjSpineIPPInputDataFileStr="+adjSpineIPPInputDataFileStr);

      // --- Extract the name of the adjSpineIPPInputDataFile and get rid of its
      //     JSON name extension at the same time
      final String scLocFNamePrefix= new
	File(adjSpineIPPInputDataFileStr).getName().replace(JSON_FEXT,"");

      //slog.info(mmi+"scLocFNamePrefix="+scLocFNamePrefix);

      // --- Split the file name prefix in its parts.
      final String [] scLocFNamePrefixParts= scLocFNamePrefix.split(OUTPUT_DATA_FMT_SPLIT_CHAR);

      // --- Must have 4 parts here
      if (scLocFNamePrefixParts.length != 4) {
	throw new RuntimeException(mmi+"scLocFNamePrefixParts.length != 4");
      }

      // --- Get the int num. id (String) of the ship channel point location
      //final Integer scLocIndex= Integer.getInteger(scLocFNamePrefixParts[3]);
      final String scLocIndexKeyStr= scLocFNamePrefixParts[3];

      // --- Read the ship channel point location adjusted WL from its
      //     CHS_JSON input file in the Map of MeasurementCustomBundle objects
      allSCLocsIPPInputData.put(scLocIndexKeyStr,
				new MeasurementCustomBundle ( WLAdjustmentIO.getWLDataInJsonFmt(adjSpineIPPInputDataFileStr, -1L, 0.0) ));
      
      //slog.info(mmi+"scLocFNamePrefixParts="+scLocFNamePrefixParts.toString());

      //slog.info(mmi+"debug System.exit(0)");
      //System.exit(0);       
    }

    slog.info(mmi+"Done with reading all the SpineIPP results input files");   

    // --- Get the time intervall in seconds (the ship channel point location is not important)
    //     Assuming that the String index "0" key exists in the allSCLocsIPPInputData Map
    final Instant [] scLocInstants= allSCLocsIPPInputData.
      get("0").getInstantsKeySetCopy().toArray(new Instant[0]);

    slog.info(mmi+"scLocInstants[0]="+scLocInstants[0].toString());
    slog.info(mmi+"scLocInstants[1]="+scLocInstants[1].toString());

    final long timeIntervallSeconds=
      scLocInstants[1].getEpochSecond() - scLocInstants[0].getEpochSecond();

    slog.info(mmi+"timeIntervallSeconds="+timeIntervallSeconds);
    
    // --- Now do the conversion to S104 DCF8 format (one file forall the ship channel points locations).
    
    slog.info(mmi+"end");
  }	
}
