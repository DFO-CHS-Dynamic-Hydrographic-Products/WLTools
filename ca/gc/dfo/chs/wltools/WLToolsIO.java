package ca.gc.dfo.chs.wltools;

// ---
import java.util.Map;
import java.util.List;
import java.time.Instant;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.ArrayList;

import org.slf4j.Logger;
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
import java.nio.file.StandardCopyOption;

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

// --- chs WLTools package
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

// --- chs SProduct package
import ca.gc.dfo.chs.dhp.SProduct;
import ca.gc.dfo.chs.dhp.ISProductIO;

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

    final String ippAdjResultsInputDirName=  new File(ippAdjResultsInputDir).getName();

    // --- Copy the S104 DCF8 template file from the dhp package config folder to the output folder
    //     to use it as a target where to write (append mode) the new SpineIPP results.
    final String s104Dcf8TmplFile= mainCfgDir +
      File.separator + ISProductIO.PKG_LOWSTL_S104_DCF8_TMPLF_RPATH;

    if (!checkForFileExistence(s104Dcf8TmplFile)) {
      throw new RuntimeException(mmi+"Template file -> "+s104Dcf8TmplFile+" not found");
    }
    
    slog.info(mmi+"s104Dcf8TmplFile="+s104Dcf8TmplFile);

    // --- Need to convert the s104Dcf8TmplFile to a Path object in order
    //     to copy it in the output folder with its new name which also needs to be a Path.
    final Path s104Dcf8TmplFilePath= FileSystems.getDefault().getPath(s104Dcf8TmplFile);

    final String outputFileName= ippAdjResultsInputDirName +
      OUTPUT_DATA_FMT_SPLIT_CHAR + new File(s104Dcf8TmplFile).getName();

    final Path outputFilePath= FileSystems.getDefault().
	getPath(outputDirectory + File.separator + outputFileName);

    slog.info(mmi+"outputFilePath="+outputFilePath.toString());

    try {
      Files.copy(s104Dcf8TmplFilePath, outputFilePath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ioex) {
      throw new RuntimeException(mmi+"Problem with Files.copy()!!");
    }
    
    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);    

    // --- Get the paths of all the ship channel points locations adjusted WL
    //     (SpineIPP outputs) data input files (CHS_JSON format) in a List<Path> object 
    final List<Path> adjSpineIPPInputDataFilesList=
      getRelevantFilesList(ippAdjResultsInputDir, "*"+IWLAdjustmentIO.ADJ_HFP_ATTG_FNAME_PRFX+"*"+JSON_FEXT);

    Map<String, MeasurementCustomBundle> allSCLocsIPPInputData= new HashMap<String, MeasurementCustomBundle>();

    // --- Need to determine the most recent Instant of all the ship channel grid point locations
    //     (they could be slighly different at this point) then start with the 1970-01-01T00:00;00Z
    //     as a time reference. Otherewise said it is the most recent Instant of all the least recent
    //     Instant objects of all the ship channel grid point locations
    Instant mostRecentFirstInstant= Instant.EPOCH;

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

      final Instant scLocLeastRecentInstantCheck=
	allSCLocsIPPInputData.get(scLocIndexKeyStr).getLeastRecentInstantCopy();
      
      mostRecentFirstInstant= ( mostRecentFirstInstant.
	isBefore(scLocLeastRecentInstantCheck) ) ? scLocLeastRecentInstantCheck: mostRecentFirstInstant;
      
      //slog.info(mmi+"debug System.exit(0)");
      //System.exit(0);       
    }

    slog.info(mmi+"Done with reading all the SpineIPP results input files");   

    slog.info(mmi+"mostRecentFirstInstant="+mostRecentFirstInstant.toString());
    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);

    // --- Take the SortedSet<Instant> of the "0" ship channel point location to
    //     use it for the conversion loop starting at the mostRecentFirstInstant
    //     (It is very unlikely that the mostRecentFirstInstant object key is not present
    //     in the keys of the related MeasurementCustomBundle object)
    final SortedSet<Instant> scLocsInstants= allSCLocsIPPInputData.
      get("0").getInstantsKeySetCopy().tailSet(mostRecentFirstInstant);

    // --- mostRecentSCLocsInstant is in fact the Instant related to the last timestamp.
    final Instant mostRecentSCLocsInstant= scLocsInstants.last();

    // --- leastRecentSCLocsInstant is in fact the Instant related to the first timestamp.
    final Instant leastRecentSCLocsInstant= scLocsInstants.first();

    slog.info(mmi+"leastRecentSCLocsInstant="+leastRecentSCLocsInstant.toString());
    slog.info(mmi+"mostRecentSCLocsInstant="+mostRecentSCLocsInstant.toString());
    
    // --- Get the time intervall in seconds (the ship channel point location is not important here)
    //     Assuming that the String index "0" key exists in the allSCLocsIPPInputData Map
    final Instant [] scLocsInstantsArr= scLocsInstants.toArray(new Instant[0]);

    slog.info(mmi+"scLocsInstantsArr[0]="+scLocsInstantsArr[0].toString());
    slog.info(mmi+"scLocsInstantsArr[1]="+scLocsInstantsArr[1].toString());

    final long timeIntervallSeconds=
      scLocsInstantsArr[1].getEpochSecond() - scLocsInstantsArr[0].getEpochSecond();

    slog.info(mmi+"timeIntervallSeconds="+timeIntervallSeconds);

    final int nbInstants= scLocsInstants.size();

    slog.info(mmi+"nbInstants="+nbInstants);

    System.out.flush();
    
    // --- Now do the conversion to S104 DCF8 format (one file for all the ship channel points locations).

    // --- Open the HDF5 output file in append mode (default when it is already existing);
    final int checkOpenOutFile= HDFql.execute("USE FILE " + outputFilePath);

    if (checkOpenOutFile != HDFqlConstants.SUCCESS ) {
      throw new RuntimeException(mmi+"hdfql.execute problem: checkOpenOutFile="+checkOpenOutFile);
    }

    final int shf= HDFql.execute("SHOW USE FILE");
    
    //slog.info(mmi+"shf="+shf);

    // --- Need to update the issueDate and issueTime HDF5 root attributes
    final String [] nowStrSplit= Instant.now().toString().split(ISO8601_DATETIME_SEP_CHAR);

    // --- issueDate String 
    final String YYYYMMDDStr= nowStrSplit[0];  

    // --- Instant.now() puts the milliseconds at the end of the String after a "."
    //     split needs two escape backslashes here to get what we want.
    final String hhmmssZStr= nowStrSplit[1].
      split("\\.")[0].replace(INPUT_DATA_FMT_SPLIT_CHAR,"") + ISO8601_UTC_EXT;

    //slog.info(mmi+"YYYYMMDD="+YYYYMMDD);
    //slog.info(mmi+"hhmmssZ="+hhmmssZ);

    // --- MUST use HDFql.variableTransientRegister here!
    SProduct.updTransientAttrInGroup(ISProductIO.ISSUE_YYYYMMDD_ID, ISProductIO.GRP_SEP_ID,
                                     HDFql.variableTransientRegister(new String [] {YYYYMMDDStr}) );
    
    // --- USE GROUP Probably not necessary here
    //HDFql.execute("USE GROUP "+ISProductIO.GRP_SEP_ID);

    //// --- Need to use an array of one String for the YYYYMMDDStr String in order
    //     to be able to update the related HDF5 String attribute
    //int cqc= HDFql.execute("INSERT INTO ATTRIBUTE "+ ISProductIO.ISSUE_YYYYMMDD_ID +
    //			   " VALUES FROM MEMORY " + HDFql.variableTransientRegister( new String [] {YYYYMMDDStr} ) );     
    //if (cqc != HDFqlConstants.SUCCESS) {
    //  throw new RuntimeException(mmi+"Problem with HDFql INSERT for "+ISProductIO.ISSUE_YYYYMMDD_ID+" ATTRIBUTE");
    //}
    // int cqc= HDFql.execute("INSERT INTO ATTRIBUTE "+ ISProductIO.ISSUE_HHMMSS_ID +
    // 			   " VALUES FROM MEMORY " + HDFql.variableTransientRegister( new String [] {hhmmssZStr} ) );
    // if (cqc != HDFqlConstants.SUCCESS) {
    //   throw new RuntimeException(mmi+"Problem with HDFql INSERT for "+ISProductIO.ISSUE_HHMMSS_ID+" ATTRIBUTE");
    // }

    SProduct.updTransientAttrInGroup(ISProductIO.ISSUE_HHMMSS_ID, ISProductIO.GRP_SEP_ID,
				     HDFql.variableTransientRegister( new String [] {hhmmssZStr} ) );
				     
    final String s104FeatureId= ISProductIO.FEATURE_IDS.get(ISProductIO.FeatId.S104);

    final String s104FcstDataGrpId= ISProductIO.GRP_SEP_ID +
      s104FeatureId + ISProductIO.GRP_SEP_ID + s104FeatureId + ISProductIO.FCST_ID;
    
    // --- Set the GROUP to the S104 /<feature id>.<NN>/<feature id>.<NN> for the forecast type
    //cqc= HDFql.execute("USE GROUP " + s104FcstDataGrpId);
    //if (cqc != HDFqlConstants.SUCCESS) {
    //  throw new RuntimeException(mmi+"Group: "+s104FcstDataGrpId+" not found in HDF5 output file!");
    //}

    // --- Update the first and last timestamps attributes of the
    //     S104 /<feature id>.<NN>/<feature id>.<NN> GROUP 
    final String firstTimeStampStr=
      leastRecentSCLocsInstant.toString()
       .replace(OUTPUT_DATA_FMT_SPLIT_CHAR, "")
	 .replace(INPUT_DATA_FMT_SPLIT_CHAR,"");

    final String lastTimeStampStr=
      mostRecentSCLocsInstant.toString()
       .replace(OUTPUT_DATA_FMT_SPLIT_CHAR, "")
	 .replace(INPUT_DATA_FMT_SPLIT_CHAR,"");

    SProduct.updTransientAttrInGroup(ISProductIO.LEAST_RECENT_TIMESTAMP_ID, s104FcstDataGrpId,
				     HDFql.variableTransientRegister( new String [] {firstTimeStampStr} ) ); 
    
    //cqc= HDFql.execute("INSERT INTO ATTRIBUTE "+ ISProductIO.LEAST_RECENT_TIMESTAMP_ID +
    //		       " VALUES FROM MEMORY " + HDFql.variableTransientRegister( new String [] {firstTimeStampStr} ) );
    //if (cqc != HDFqlConstants.SUCCESS) {
    //  throw new RuntimeException(mmi+"Problem with HDFql INSERT for "+ISProductIO.LEAST_RECENT_TIMESTAMP_ID+" ATTRIBUTE");
    //}
    // cqc= HDFql.execute("INSERT INTO ATTRIBUTE "+ ISProductIO.MOST_RECENT_TIMESTAMP_ID +
    // 		       " VALUES FROM MEMORY " + HDFql.variableTransientRegister( new String [] {lastTimeStampStr} ) );
    // if (cqc != HDFqlConstants.SUCCESS) {
    //   throw new RuntimeException(mmi+"Problem with HDFql INSERT for "+ISProductIO.LEAST_RECENT_TIMESTAMP_ID+" ATTRIBUTE");
    // }

    SProduct.updTransientAttrInGroup(ISProductIO.MOST_RECENT_TIMESTAMP_ID, s104FcstDataGrpId,
				     HDFql.variableTransientRegister( new String [] {lastTimeStampStr}) );
    
    HDFql.execute("CLOSE FILE " + outputFilePath);
    
    slog.info(mmi+"end");
  }	
}
