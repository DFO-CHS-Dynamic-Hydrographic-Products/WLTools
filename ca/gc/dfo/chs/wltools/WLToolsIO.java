package ca.gc.dfo.chs.wltools;

// ---
import java.util.Map;
import java.util.List;
import java.time.Instant;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

// ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import java.io.File;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;

// ---
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.DirectoryStream;
import java.nio.file.StandardCopyOption;

// ---
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonArrayBuilder;

// --- chs WLTools package
import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.wl.IWLPSLegacyIO;
import ca.gc.dfo.chs.wltools.wl.IWLMeasurement;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

// --- CHS SProduct package
import ca.gc.dfo.chs.dhp.SProduct;
import ca.gc.dfo.chs.dhp.ISProductIO;
import ca.gc.dfo.chs.dhp.S104DCF8CompoundType;

// --- HDFql lib
import as.hdfql.HDFql;
import as.hdfql.HDFqlJNI;
import as.hdfql.HDFqlCursor;
import as.hdfql.HDFqlConstants;

/**
 *
 */
abstract public class WLToolsIO implements IWLToolsIO {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.WLToolsIO";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  static private String mainCfgDir= null;

  static private String outputDirectory= null;

  static private String outputDataFormat= null;

    //static private HDFqlJNI hdfqlJNI= new HDFqlJNI();

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

    List<Path> inputDataDirFilesList= new ArrayList<Path>();

    // --- List all the relevant files in inputDir using a DirectoryStream<Path> object     
    //DirectoryStream<Path> inputDataDirFilesDS= null;

    try ( final DirectoryStream<Path> inputDataDirFilesDS= Files
           .newDirectoryStream(inputDataDir, relevantFilesRegExpr) ) {

      // --- Now put all the relevant files in the inputDataDirFilesList 
      //     object to be returned
      for (final Path inputFilePath: inputDataDirFilesDS) {
        inputDataDirFilesList.add(inputFilePath);
      }

    } catch (IOException ioex) {
      throw new RuntimeException(mmi+ioex);
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

    return getMainCfgDir() + File.separator +
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

    return mainCfgDir + "/tidal/nonStationary/" + regionIdInfo + "/dischargeClusters/" +
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
	    add(IWLToolsIO.UNCERTAINTY_JSON_JEY, mc.getUncertainty() ).
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
      
      //slog.info(mmi+"debug System.exit(0)");
      //System.exit(0); 
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

    slog.info(mmi+"file template copy done");

    // --- Get the paths of all the ship channel points locations adjusted WL
    //     (SpineIPP outputs) data input files (CHS_JSON format) in a List<Path> object 
    final List<Path> adjSpineIPPInputDataFilesList=
      getRelevantFilesList(ippAdjResultsInputDir, "*"+IWLAdjustmentIO.ADJ_HFP_ATTG_FNAME_PRFX+"*"+JSON_FEXT);

    // --- Unlikely bu twe never know!
    if (adjSpineIPPInputDataFilesList.size() > IWLAdjustmentIO.MAX_SCLOCS_NB) {
	
      throw new RuntimeException(mmi+"Too many ship channel point locations! -> "+
	adjSpineIPPInputDataFilesList.size()+", Max is -> "+ WLAdjustmentIO.MAX_SCLOCS_NB);
    }
    
    Map<String, MeasurementCustomBundle> allSCLocsIPPInputData= new HashMap<String, MeasurementCustomBundle>();

    // --- Need to determine the most recent Instant of all the ship channel grid point locations
    //     (they could be slighly different at this point) then start with the 1970-01-01T00:00;00Z
    //     as a time reference. Otherewise said it is the most recent Instant of all the least recent
    //     Instant objects of all the ship channel grid point locations
    Instant mostRecentFirstInstant= Instant.EPOCH;

    slog.info(mmi+"Reading all the SpineIPP results input files in CHS_JSON format (could take 5-7 mins)");

    String scLocWithMostRecent1stInstant= "0";
    
    // --- Loop on all the files of all the ship channel point locations adjusted WL
    //     (SpineIPP outputs) data input files (order is not important here since we are
    //      using a Map to store the data using their num. ids as keys) 
    for(final Path adjSpineIPPInputDataFilePath: adjSpineIPPInputDataFilesList) {

      final String adjSpineIPPInputDataFileStr= adjSpineIPPInputDataFilePath.toString();
	
      //slog.debug(mmi+"adjSpineIPPInputDataFileStr="+adjSpineIPPInputDataFileStr);

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
      
      //mostRecentFirstInstant= ( mostRecentFirstInstant.
      // isBefore(scLocLeastRecentInstantCheck) ) ? scLocLeastRecentInstantCheck: mostRecentFirstInstant;

      // --- Update the mostRecentFirstInstant if needed and also update
      //     the related scLocWithMostRecent1stInstant String
      if ( mostRecentFirstInstant.isBefore(scLocLeastRecentInstantCheck) ) {	  
	 mostRecentFirstInstant= scLocLeastRecentInstantCheck;
	 scLocWithMostRecent1stInstant= scLocIndexKeyStr;
      }
      
      //slog.info(mmi+"nb. files read="+allSCLocsIPPInputData.size());
      //slog.info(mmi+"debug System.exit(0)");
      //System.exit(0);       
    }

    slog.info(mmi+"Done with reading all the SpineIPP results input files");

    // --- Get the number of ship channel point locations.
    final int nbSCPointLocs= allSCLocsIPPInputData.size(); //adjSpineIPPInputDataFilesList.size();

    slog.info(mmi+"nbSCPointLocs="+nbSCPointLocs);
    
    slog.info(mmi+"mostRecentFirstInstant="+mostRecentFirstInstant.toString());
    slog.info(mmi+"scLocWithMostRecent1stInstant="+scLocWithMostRecent1stInstant);
    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);

    // --- Take the SortedSet<Instant> of the ship channel point location
    //     that is indexed at scLocWithMostRecent1stInstant to
    //     use it for the conversion loop starting at the mostRecentFirstInstant
    //     of all the timestamps of the all the ship channel point locations
    final SortedSet<Instant> scLocsInstants= allSCLocsIPPInputData.
      get(scLocWithMostRecent1stInstant).getInstantsKeySetCopy().tailSet(mostRecentFirstInstant);

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

    // --- Get the nb. of Instants (timestamps)
    final int nbInstants= scLocsInstants.size();

    slog.info(mmi+"nbInstants="+nbInstants);

    //System.out.flush();
    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);

    // --- Use only one thread for HDFql operations. It avoids a possible cluttering-up of the cores 
    //     because HDFql try to use all the core available by default which is not a good idea most of
    //     the time.
    //HDFql.execute("SET THREAD 1");
    
    // --- Now do the conversion to S104 DCF8 format (one file for all the ship channel points locations).
    //     first open the unique S104 DCF8 HDF5 output file in append mode (default when it is already existing);
    final int checkOpenOutFile= HDFql.execute("USE FILE " + outputFilePath);

    if (checkOpenOutFile != HDFqlConstants.SUCCESS ) {
      throw new RuntimeException(mmi+"hdfql.execute problem: checkOpenOutFile="+checkOpenOutFile);
    }

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
    
    SProduct.updTransientAttrInGroup(ISProductIO.ISSUE_HHMMSS_ID, ISProductIO.GRP_SEP_ID,
				     HDFql.variableTransientRegister( new String [] {hhmmssZStr} ) );

    // --- Get the S104 feature code String
    final String s104FeatureId= ISProductIO.FEATURE_IDS.get(ISProductIO.FeatId.S104);

    // --- Build the S104 forecast feature code HDF5 GROUP id. 
    final String s104FcstDataGrpId= ISProductIO.GRP_SEP_ID +
      s104FeatureId + ISProductIO.GRP_SEP_ID + s104FeatureId + ISProductIO.FCST_ID;
    
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

    // --- Update the first (least recent) timestamp HDF5 file attribute
    //     in the S104 forecast feature code HDF5 GROUP   
    SProduct.updTransientAttrInGroup(ISProductIO.LEAST_RECENT_TIMESTAMP_ID, s104FcstDataGrpId,
				     HDFql.variableTransientRegister( new String [] {firstTimeStampStr} )); 
    
    // --- Update the last (most recent) timestamp HDF5 file attribute
    //     in the S104 forecast feature code HDF5 GROUP  
    SProduct.updTransientAttrInGroup(ISProductIO.MOST_RECENT_TIMESTAMP_ID, s104FcstDataGrpId,
				     HDFql.variableTransientRegister( new String [] {lastTimeStampStr} ));

    // --- Define the unary array of int for the nb. of ship channel point locations.
    final int [] tmpNBSCPointLocsArr= new int [] {nbSCPointLocs};
    
    // --- Update the number of GROUPS for the point locations HDF5 file attribute
    //     in the S104 forecast feature code HDF5 GROUP  
    SProduct.updTransientAttrInGroup(ISProductIO.NB_GROUPS_ID, s104FcstDataGrpId,
				     HDFql.variableTransientRegister( tmpNBSCPointLocsArr ));

    // --- Update the number of "stations" for the point locations HDF5 file attribute
    //     in the S104 forecast feature code HDF5 GROUP (Same as for the number of GROUPS for DCF8).  
    SProduct.updTransientAttrInGroup(ISProductIO.NB_STATIONS_ID, s104FcstDataGrpId,
				     HDFql.variableTransientRegister( tmpNBSCPointLocsArr ));

    // --- Update the nb. of timestamps HDF5 file attribute
    //     in the S104 forecast feature code HDF5 GROUP
    SProduct.updTransientAttrInGroup(ISProductIO.NB_TIMESTAMPS_ID, s104FcstDataGrpId,
				     HDFql.variableTransientRegister( new int [] {nbInstants} ));
    
    // --- Update the time intervall of the timestamps HDF5 file attribute
    //     in the S104 forecast feature code HDF5 GROUP
    SProduct.updTransientAttrInGroup(ISProductIO.TIME_INTRV_ID, s104FcstDataGrpId,
				     HDFql.variableTransientRegister( new int [] { (int)timeIntervallSeconds } ));

    // --- Now using the unknown WL trend flag (useless for Spine stuff for now).
    final byte unknownTrendByte= ISProductIO
      .S104_TREND_FLAGS.get(ISProductIO.S104TrendFlag.Unknown).byteValue();

    // --- Need to use an array of S104DCF8CompoundType objects in order
    //     to fill-up the HDF5 dataset of S104 compound types objects for
    //     this ship channel point location (Dimension -> nbInstants)
    final S104DCF8CompoundType [] s104Dcf8CmpdTypeArray= new S104DCF8CompoundType[nbInstants];
    //final S104DCF8CompoundType [] s104Dcf8CmpdTypeArray= S104DCF8CompoundType.createAndInitArray(nbInstants);
    
    int checkStatus= -1;
	
    // --- Now loop on all the ship channel point locations to update their metadata and
    //     their water levels and related uncertainties (Unknown trend flags for now).
    for (final String scLocStrId: allSCLocsIPPInputData.keySet() ) {
  
	//slog.info(mmi+"scLocStrId="+scLocStrId);

      // --- Define the name of the HDF5 GROUP for this specific ship channel point location.
      final String scLocGrpNNNNIdStr= s104FcstDataGrpId + ISProductIO.GRP_SEP_ID +
	ISProductIO.GRP_PRFX + String.format("%04d", Integer.parseInt(scLocStrId) + 1) ;

      //slog.info(mmi+"scLocGrpNNNNIdStr="+scLocGrpNNNNIdStr);

      checkStatus= HDFql.execute("USE GROUP "+scLocGrpNNNNIdStr);

      if (checkStatus != HDFqlConstants.SUCCESS) {
	// --- TODO: Create the new GROUP if not found instead of crashing here 
	throw new RuntimeException(mmi+"GROUP -> "+scLocGrpNNNNIdStr+ " not found in the output file!");
      }

      //slog.info(mmi+"scLocStrId="+scLocStrId);

      // --- Update the ship channel location string id. in his own group
      SProduct.updTransientAttrInGroup(ISProductIO.DCF8_STNID_ID, scLocGrpNNNNIdStr,
				       HDFql.variableTransientRegister( new String [] {scLocStrId} ));

      // --- Define the ship channel location string (human readable) name
      final String scLocStnName= IWLAdjustmentIO.SCLOC_STN_ID_PRFX + scLocStrId;

      //slog.info(mmi+"scLocStnName="+scLocStnName);

      // --- Update the ship channel location string (human readable) name HDF5 attribute.
      SProduct.updTransientAttrInGroup(ISProductIO.DCF8_STN_NAME_ID, scLocGrpNNNNIdStr,
				       HDFql.variableTransientRegister( new String [] {scLocStnName} ));
      
      // --- Update the nb. of timestamps HDF5 file attribute
      //     in the ship channel point location Group_nnnn
      SProduct.updTransientAttrInGroup(ISProductIO.NB_TIMESTAMPS_ID, scLocGrpNNNNIdStr,
      			               HDFql.variableTransientRegister( new int [] {nbInstants} ));

      // --- Update the first timestamp for this ship channel location
      //     (Same value as for the S104 feature forecast GROUP)
      SProduct.updTransientAttrInGroup(ISProductIO.DCF8_STN_FIRST_TIMESTAMP_ID, scLocGrpNNNNIdStr,
				       HDFql.variableTransientRegister( new String [] {firstTimeStampStr} ));

      // --- Update the last timestamp for this ship channel location
      //     (Same value as for the S104 feature forecast GROUP)
      SProduct.updTransientAttrInGroup(ISProductIO.DCF8_STN_LAST_TIMESTAMP_ID, scLocGrpNNNNIdStr,
				       HDFql.variableTransientRegister( new String [] {lastTimeStampStr} ));

      // --- Update the time interval of the timestamps HDF5 file attribute
      //     for this ship channel location (Same value as for the S104 feature forecast GROUP)
      SProduct.updTransientAttrInGroup(ISProductIO.TIME_INTRV_ID, scLocGrpNNNNIdStr,
  				       HDFql.variableTransientRegister( new int [] { (int)timeIntervallSeconds } ));
      // --- 
      final String valuesDSetIdInGrp= scLocGrpNNNNIdStr + ISProductIO.GRP_SEP_ID + ISProductIO.VAL_DSET_ID;

      //slog.info(mmi+"valuesDSetIdInGrp="+valuesDSetIdInGrp);
      //slog.info(mmi+" checkStatus bef. comm="+checkStatus);

      // --- Delete dataset to be able to re-create it with its new dimension (nbInstants)
      checkStatus= HDFql.execute("DROP DATASET " + valuesDSetIdInGrp);

      if (checkStatus != HDFqlConstants.SUCCESS) {
	throw new RuntimeException(mmi+"Cannot delete dataset -> "+valuesDSetIdInGrp);
      }

      //slog.info(mmi+"ISProductIO.S104_CMPD_TYPE_HGHT_ID="+ISProductIO.S104_CMPD_TYPE_HGHT_ID);
      
      // --- re-create dataset of compound type of 3 items with its new dimension (nbInstants)
      checkStatus= HDFql.execute("CREATE DATASET \""+
        valuesDSetIdInGrp+"\" AS COMPOUND("+ISProductIO.S104_CMPD_TYPE_HGHT_ID+" AS FLOAT, "+
	  ISProductIO.FEAT_CMPD_TYPE_UNCERT_ID+" AS FLOAT, "+ISProductIO.S104_CMPD_TYPE_TRND_ID +" as UNSIGNED TINYINT)(+"+nbInstants+")");

      if (checkStatus != HDFqlConstants.SUCCESS) {
	throw new RuntimeException(mmi+"Cannot re-create dataset -> "+valuesDSetIdInGrp);
      }

      // --- Get the MeasurementCustomBundle object for this ship channel point location
      final MeasurementCustomBundle scLocMCB= allSCLocsIPPInputData.get(scLocStrId);

      // --- Now using the unknown WL trend flag (useless for Spine stuff for now).
      //final byte unknownTrendByte= ISProductIO
      //	.S104_TREND_DEF.get(ISProductIO.S104TrendIds.Unknown).byteValue();

      // --- Need to use an array of S104DCF8CompoundType objects in order
      //     to fill-up the HDF5 dataset of S104 compound types objects for
      //     this ship channel point location (Dimension -> nbInstants)
      //S104DCF8CompoundType [] s104Dcf8CmpdTypeArray= new S104DCF8CompoundType[nbInstants];
      
      int cmpdTypeIdx=0;
      
      // --- Loop on all the relevant and ordered (increasing) Instant objects
      //     of this ship channel point location
      for (final Instant scLocsInstant : scLocsInstants) {

	final MeasurementCustom mc= scLocMCB.getAtThisInstant(scLocsInstant);   

	//slog.info(mmi+"mc.getValue()="+mc.getValue());
	//slog.info(mmi+"mc.getUncertainty()="+mc.getUncertainty());
        //slog.info(mmi+"debug System.exit(0)");
        //System.exit(0);

	if (cmpdTypeIdx == nbInstants) {
	  throw new RuntimeException(mmi+"Cannot have cmpdTypeIdx == nbInstants in loop !!");
	}

	  //s104Dcf8CmpdTypeArray[cmpdTypeIdx++]
	  //.set(mc.getValue().floatValue(), mc.getUncertainty().floatValue(), unknownTrendByte);
						 
        s104Dcf8CmpdTypeArray[cmpdTypeIdx++]= new
	  S104DCF8CompoundType(mc.getValue().floatValue(), mc.getUncertainty().floatValue(), unknownTrendByte);
      }

      //slog.info(mmi+"s104Dcf8CmpdTypeArray now filled-up");

      // --- Register tmpValuesArr for the HDFql lib, need to do it
      //     for each INSERT command (i.e. for each ship channel point location) 
      //     TODO check if we have a performance gain by using the HDFql.variableRegister
      //     method just once outside of the loop on the ship channel point locations.
      final int registerNb= HDFql.variableTransientRegister(s104Dcf8CmpdTypeArray);

      if (registerNb < 0) {
	throw new RuntimeException(mmi+"Problem with HDFql.variableTransientRegister(s104Dcf8CmpdTypeArray), registerNb  ->"+registerNb);
      }

      // --- Put the (values,uncertainty) compound type items in the dataset.
      checkStatus= HDFql.execute("INSERT INTO \""+valuesDSetIdInGrp+"\" VALUES FROM MEMORY " + registerNb);

      if (checkStatus != HDFqlConstants.SUCCESS) {
	throw new RuntimeException(mmi+"Problem with INSERT INTO for dataset -> " + valuesDSetIdInGrp);
      }      
      
      //slog.info(mmi+"debug System.exit(0)");
      //System.exit(0);
    }
    
    checkStatus= HDFql.execute("CLOSE FILE " + outputFilePath);

    if (checkStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Problem with HDFql CLOSE FILE for file -> " +outputFilePath);
    }      
    
    slog.info(mmi+"end");
    
  } // --- method ippCHSJsonToS104DCF8 

  // ---
  //public final static Map<String,MeasurementCustomBundle> getMCBFromS104DCF8File() {
  public final static List<MeasurementCustomBundle> getMCBsFromS104DCF8File(final String s104DCF8FilePath) {

    final String mmi= "getMCBFromS104DCF8File: ";

    try {
      s104DCF8FilePath.hashCode();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe+" s104DCF8FilePath cannot be null here !!");
    }

    if (!checkForFileExistence(s104DCF8FilePath)) {
      throw new RuntimeException(mmi+"s104DCF8FilePath -> "+s104DCF8FilePath+" not found !!");
    }
    
    slog.info(mmi+"start");

    //// --- Tell the HDFql lib to use just one thread here.
    //HDFql.execute("SET THREAD 1");
    //System.out.flush();
    
    //if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
    //  throw new RuntimeException(mmi+"Problem with HDFql command \"SET THREAD 1\", hdfqlCmdStatus="+hdfqlCmdStatus);
    //}

    int hdfqlCmdStatus= HDFql.execute("USE READONLY FILE "+s104DCF8FilePath);

    if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Problem with HDFql open file command \"USE READONLY FILE \" for file -> "
				 +s104DCF8FilePath+", hdfqlCmdStatus="+hdfqlCmdStatus);
    }

    // --- Get the S104 feature code String
    final String s104FeatureId= ISProductIO.FEATURE_IDS.get(ISProductIO.FeatId.S104);

    // --- Build the S104 forecast feature code HDF5 GROUP id. 
    final String s104FcstDataGrpId= ISProductIO.GRP_SEP_ID +
      s104FeatureId + ISProductIO.GRP_SEP_ID + s104FeatureId + ISProductIO.FCST_ID;

    // --- Get the number of ship channel point locations
    //     from the ISProductIO.NB_STATIONS_ID attribute.
    
    // --- NOTE: Need to use a local unary array of Integer
    //     and it seems that we really need to use
    //     the "new" operator here instead of just
    //     using a local declaration which seems
    //     to cause something like a memory leak in
    //     the JNI part of the HDFql lib and which
    //     seems to randomly crash the java exec.
    //final int [] nbScLocs= {0};
    //Integer [] nbScLocs= {0};
    Integer [] nbScLocs= new Integer [] {0}; //{ Integer.valueOf(0) }; //{0};

    SProduct.setTransientAttrFromGroup(ISProductIO.NB_STATIONS_ID,
				       s104FcstDataGrpId, HDFql.variableTransientRegister(nbScLocs));

    slog.info(mmi+"nbScLocs[0]="+nbScLocs[0]);
    System.out.flush();
    
    //final int [] nbInstants= {0};
    //Integer [] nbInstants= {0};
    Integer [] nbInstants= new Integer [] {0};//{ Integer.valueOf(0) }; // {0};

    SProduct.setTransientAttrFromGroup(ISProductIO.NB_TIMESTAMPS_ID,
				       s104FcstDataGrpId, HDFql.variableTransientRegister(nbInstants));

    slog.info(mmi+"nbInstants[0]="+nbInstants[0]);
    System.out.flush();

    //final String [] dateTimeOfFirstRecord= {""};
    //String [] dateTimeOfFirstRecord= {""};
    String [] dateTimeOfFirstRecord= new String [] { new String() };
    
    SProduct.setTransientAttrFromGroup(ISProductIO.LEAST_RECENT_TIMESTAMP_ID,
				       s104FcstDataGrpId, HDFql.variableTransientRegister(dateTimeOfFirstRecord));

    slog.info(mmi+"dateTimeOfFirstRecord[0]="+dateTimeOfFirstRecord[0]);
    //System.out.flush();
    
    final String fmfLeastRecentInstantStr= dateTimeOfFirstRecord[0].substring(0,4)   + ISO8601_YYYYMMDD_SEP_CHAR +
	                                   dateTimeOfFirstRecord[0].substring(4,6)   + ISO8601_YYYYMMDD_SEP_CHAR +
	                                   dateTimeOfFirstRecord[0].substring(6,8)   + ISO8601_DATETIME_SEP_CHAR +
	                                   dateTimeOfFirstRecord[0].substring(9,11)  + INPUT_DATA_FMT_SPLIT_CHAR +
	                                   dateTimeOfFirstRecord[0].substring(11,13) + INPUT_DATA_FMT_SPLIT_CHAR + "00Z";
    
    slog.info(mmi+"fmfLeastRecentInstantStr="+fmfLeastRecentInstantStr);
    System.out.flush();
    
    final Instant fmfLeastRecentInstant= Instant.parse(fmfLeastRecentInstantStr);

    //slog.info(mmi+"fmfLeastRecentInstant.toString()="+fmfLeastRecentInstant.toString());

    //final String [] dateTimeOfLastRecord= {""};
    //String [] dateTimeOfLastRecord= {""};
    String [] dateTimeOfLastRecord= new String [] { new String() };
    
    SProduct.setTransientAttrFromGroup(ISProductIO.MOST_RECENT_TIMESTAMP_ID,
				       s104FcstDataGrpId, HDFql.variableTransientRegister(dateTimeOfLastRecord));    

    slog.info(mmi+"dateTimeOfLastRecord[0]="+dateTimeOfLastRecord[0]);
    System.out.flush();

    //final int [] timeIntervalSeconds=
    //Integer [] timeIntervalSeconds=
    Integer [] timeIntervalSeconds= new Integer [] {0}; //{ Integer.valueOf(0) };	
    
    SProduct.setTransientAttrFromGroup(ISProductIO.TIME_INTRV_ID,
				       s104FcstDataGrpId, HDFql.variableTransientRegister(timeIntervalSeconds));

    slog.info(mmi+"timeIntervalSeconds[0]="+timeIntervalSeconds[0]);
    System.out.flush();
    
    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);
    
    // --- Create the ArrayList of the MeasurementCustomBundle objects
    //     (one for each ship channel point location)
    List<MeasurementCustomBundle> mcbsFromS104DCF8=
      new ArrayList<MeasurementCustomBundle>(nbScLocs[0]);

    // --- Use an array of Instant objects to define them just once.
    //final Instant [] fmfInstants= new Instant[nbInstants[0]];
    Instant [] fmfInstants= new Instant[nbInstants[0]];
    
    // --- Allocate the array of S104DCF8CompoundType objects.
    final S104DCF8CompoundType [] s104Dcf8CmpdTypeArray= new S104DCF8CompoundType[nbInstants[0]];
    
    // --- Instantiate all S104DCF8CompoundType objects in the s104Dcf8CmpdTypeArray
    for (int instantIdx= 0; instantIdx < nbInstants[0]; instantIdx++) {
	
      s104Dcf8CmpdTypeArray[instantIdx]= new S104DCF8CompoundType();
      
      fmfInstants[instantIdx]= fmfLeastRecentInstant.plusSeconds((long)instantIdx*timeIntervalSeconds[0]);
    }

    // ---
    final int s104Dcf8CmpdTypeArrRegisterNb= HDFql.variableRegister(s104Dcf8CmpdTypeArray);

    if (s104Dcf8CmpdTypeArrRegisterNb < 0) {
      throw new RuntimeException(mmi+"Problem with HDFql.variableTransientRegister(s104Dcf8CmpdTypeArray), s104Dcf8CmpdTypeArrRegisterNb ->"+s104Dcf8CmpdTypeArrRegisterNb);
    }

    slog.info(mmi+"Reading the full model forecast data for all the ship channel point locations from the HDF5 file, could take ~10 secs");
    System.out.flush();
    
    // --- Loop on all the ship channel point locations (int indices here)
    for (int scLoc= 0; scLoc < nbScLocs[0]; scLoc++) {

      // --- Define the name of the HDF5 GROUP for this specific ship channel point location.
      final String scLocGrpNNNNIdStr= s104FcstDataGrpId +
	ISProductIO.GRP_SEP_ID + ISProductIO.GRP_PRFX + String.format("%04d", scLoc + 1);

      //slog.info(mmi+"scLocGrpNNNNIdStr="+scLocGrpNNNNIdStr);

      hdfqlCmdStatus= HDFql.execute("USE GROUP "+scLocGrpNNNNIdStr);

      if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
        throw new RuntimeException(mmi+"Group: "+scLocGrpNNNNIdStr+" not found in input file -> "+s104DCF8FilePath);
      }

      //final String [] checkStartDate= {""};
      //String [] checkStartDate= {""};
      String [] checkStartDate= new String [] { new String() };

      SProduct.setTransientAttrFromGroup(ISProductIO.DCF8_STN_FIRST_TIMESTAMP_ID,
					 scLocGrpNNNNIdStr, HDFql.variableTransientRegister(checkStartDate));

      if (!checkStartDate[0].equals(dateTimeOfFirstRecord[0])) {
	throw new RuntimeException(mmi+"Must have checkStartDate[0] being the same as dateTimeOfFirstRecord[0] here!!");
      }

      //final String [] checkLastDate= {""};
      //String [] checkLastDate= {""};
      String [] checkLastDate= new String [] { new String() };

      SProduct.setTransientAttrFromGroup(ISProductIO.DCF8_STN_LAST_TIMESTAMP_ID,
					 scLocGrpNNNNIdStr, HDFql.variableTransientRegister(checkLastDate));

      if (!checkLastDate[0].equals(dateTimeOfLastRecord[0])) {
	throw new RuntimeException(mmi+"Must have checkLastDate[0] being the same as dateTimeOfLastRecord[0] here!!");
      }      

      final String valuesDSetIdInGrp= scLocGrpNNNNIdStr + ISProductIO.GRP_SEP_ID + ISProductIO.VAL_DSET_ID;

      //final int s104Dcf8CmpdTypeArrRegisterNb= HDFql.variableTransientRegister(s104Dcf8CmpdTypeArray);
      //if (s104Dcf8CmpdTypeArrRegisterNb < 0) {
      //  throw new RuntimeException(mmi+"Problem with HDFql.variableTransientRegister(s104Dcf8CmpdTypeArray), s104Dcf8CmpdTypeArrRegisterNb ->"+s104Dcf8CmpdTypeArrRegisterNb);
      //}

      hdfqlCmdStatus= HDFql.execute("SELECT FROM DATASET \""+valuesDSetIdInGrp+"\" INTO MEMORY " + s104Dcf8CmpdTypeArrRegisterNb);

      if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
        throw new RuntimeException(mmi+"Problem with HDFql cmd \"SELECT FROM DATASET \""+
				   valuesDSetIdInGrp+"\" INTO MEMORY, hdfqlCmdStatus="+ hdfqlCmdStatus );
      }

      //slog.info(mmi+"scLocGrpNNNNIdStr="+scLocGrpNNNNIdStr);
      //slog.info(mmi+"s104Dcf8CmpdTypeArray[0].getWaterLevelHeight()="+s104Dcf8CmpdTypeArray[0].getWaterLevelHeight());
      //slog.info(mmi+"s104Dcf8CmpdTypeArray[0].getUncertainty()="+s104Dcf8CmpdTypeArray[0].getUncertainty()+"\n");
      //slog.info(mmi+"s104Dcf8CmpdTypeArray[0].getWaterLevelTrend()="+s104Dcf8CmpdTypeArray[0].getWaterLevelTrend());

      // --- NOTE: Performance is way better if we re-create the tmpScLocMCList ArrayList object here
      //           instead of creating it just once outside the outer loop on the ship channel point locations
      List<MeasurementCustom> tmpScLocMCList= new ArrayList<MeasurementCustom>(nbInstants[0]); 
      
      //--- Now populate the tmpScLocMCList ArrayList with the content of the
      //    s104Dcf8CmpdTypeArray for this ship channel point location for all
      //    the timestamps.
      for (int instantIdx= 0; instantIdx < nbInstants[0]; instantIdx++) {

	final S104DCF8CompoundType s104CmpTypeAtInstant= s104Dcf8CmpdTypeArray[instantIdx];

	// --- Using a copy of the Instant objects for the eventDate attribute of the
	//     new MeasurementCustom object.
	final MeasurementCustom mcAtInstant= new MeasurementCustom( fmfInstants[instantIdx].plusSeconds(0L),
								    Double.valueOf(s104CmpTypeAtInstant.getWaterLevelHeight()),
								    Double.valueOf(s104CmpTypeAtInstant.getUncertainty()) ) ;

	// --- No need to use the instantIdx itself here but
	//     there is no significant performance degradation
	//     compared to the simple add() without an index.
	tmpScLocMCList.add(instantIdx, mcAtInstant);
	//tmpScLocMCList.add(mcAtInstant);
      }

      // --- Finally create the MeasurementCustomBundle object for this ship channel point location
      mcbsFromS104DCF8.add(new MeasurementCustomBundle(tmpScLocMCList));
      
      //slog.info(mmi+"debug System.exit(0)");
      //System.exit(0);
      //System.out.flush();
      
    } // --- Loop block on all the ship channel point locations 

    // --- Avoid JNI calls SEGFAULTs with HDFql.variableUnregister(Object obj)
    HDFql.variableUnregister(s104Dcf8CmpdTypeArrRegisterNb);

    // --- HDFql.variableUnregister(Object obj) seems to be buggy and would cause intermittent SEGFAULTs
    //hdfqlCmdStatus= HDFql.variableUnregister(s104Dcf8CmpdTypeArray);
    //if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
    //  throw new RuntimeException(mmi+"Problem with HDFql.unregisterVariable(registerNb)!!, hdfqlCmdStatus="+hdfqlCmdStatus);
    //}
 
    hdfqlCmdStatus= HDFql.execute("CLOSE FILE "+s104DCF8FilePath);

    if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Problem with HDFql close file command \"CLOSE FILE \" for file -> "+s104DCF8FilePath+" !!");
    }    
    
    slog.info(mmi+"end");
    
    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);
    System.out.flush();

    return mcbsFromS104DCF8;
  }	
    
  // --- TODO1: Use the HttpURLConnection class instead of the URLConnection class?
  //     TODO2: Manage errors in a more fool-proof manner?     
  public final static JsonArray getJsonArrayFromAPIRequest(final String apiRequestStr) {

    final String mmi= "getJsonArrayFromAPIRequest: ";
      
    URLConnection uc= null;
    //HttpURLConnection uc= null;

    slog.info(mmi+"start");
    
    try {
      uc= new URL(apiRequestStr).openConnection();
	//uc= new HttpURLConnection(new URL(apiRequestStr))
    } catch (IOException ioe) {
	//ioe.printStackTrace();	
      throw new RuntimeException(mmi+ioe+"\nProblem with openConnection() with apiRequestStr -> "+apiRequestStr);
    }
    
    slog.info(mmi+"Connection opened for apiRequestStr -> "+apiRequestStr);
    System.out.flush();
    
    InputStream ist= null;

    try {
      ist= uc.getInputStream();
    } catch (IOException ioe) {
	//ioe.printStackTrace();    	
      throw new RuntimeException(mmi+ioe+"\nProblem with new uc.getInputStream() with apiRequestStr -> "+apiRequestStr);
    }

    //final JsonReader jsr= Json.createReaderFactory(null).createReader(ist);
    //return jsr.readArray();

    //try { 
    //  uc.finalize();
    //} catch (IOException ioe) {
    //  throw new RuntimeException(mmi+ioe+"\nProblem with uc.finalize() !!");
    //}

    slog.info(mmi+"end");
    System.out.flush();

    return Json.createReaderFactory(null).createReader(ist).readArray();
    
  } // --- getJsonArrayFromAPIRequest method

  // ---
  public final static MeasurementCustomBundle getMCBFromIWLSJsonArray(final JsonArray iwlsJsonArray,
		                                                      final long timeIntrvSeconds, final double datumConvValue, final boolean applyHFOscRemoval) {
    final String mmi= "getMCBFromIWLSJsonArray: ";

    try {
      iwlsJsonArray.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe+" iwlsJsonArray cannot be null here !!");
    }

    if (iwlsJsonArray.size() == 0) {
      throw new RuntimeException(mmi+"Cannot have iwlsJsonArray.size() == 0 here !!");
    }

    if (timeIntrvSeconds <= 1L) {
      throw new RuntimeException(mmi+"Invalid timeIntrvSeconds -> "+timeIntrvSeconds+", it must be at least 1 second !!");
    }

    slog.info(mmi+"start");
	
    List<MeasurementCustom> tmpWLDataList= new ArrayList<MeasurementCustom>(iwlsJsonArray.size());

    // --- 
    for (int itemIter= 0; itemIter < iwlsJsonArray.size(); itemIter++) {

       final JsonObject jsoItem= iwlsJsonArray.getJsonObject(itemIter);

       final String checkQCFlag= jsoItem.getString(IWLToolsIO.IWLS_DB_QCFLAG_KEY);

       if (checkQCFlag.equals(IWLToolsIO.IWLS_DB_QCFLAG_VALID)) {

	 final double itemValue= jsoItem.getJsonNumber(IWLToolsIO.VALUE_JSON_KEY).doubleValue();

	 final Instant itemInstant= Instant.parse(jsoItem.getString(IWLToolsIO.INSTANT_JSON_KEY));

	 //slog.info(mmi+"itemValue="+itemValue); 
	 //slog.info(mmi+"itemInstant="+itemInstant.toString());
         //slog.info(mmi+"debug exit 0");
         //System.exit(0);

	 // --- Only use data that has its Instant being an exact multiple of the timeIntrvSeconds
	 if ((itemInstant.getEpochSecond() % timeIntrvSeconds) == 0) {
	   tmpWLDataList.add( new MeasurementCustom(itemInstant, itemValue + datumConvValue, IWL.MINIMUM_UNCERTAINTY_METERS));
	 }
       }	
    }

    slog.info(mmi+"tmpWLDataList size="+tmpWLDataList.size());
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    System.out.flush();

    MeasurementCustomBundle mcbRet= null;

    if (tmpWLDataList.size() > IWLMeasurement.MIN_NUMBER_OF_WL_HFOSC_RMV) {

      // --- We have at least IWLAdjustment.MIN_NUMBER_OF_OBS_SPINE_FPP valid WLO data
      //     Apply the removeHFWLOscillations method to the WLO data and create the
      //     MeasurementCustomBundle to return with the filtered tmpWLDataList
      if (applyHFOscRemoval) {

	slog.info(mmi+"Applying HF oscillations removal");
	  
	mcbRet= new MeasurementCustomBundle(WLMeasurement
          .removeHFWLOscillations(IWLAdjustment.MAX_TIMEDIFF_FOR_HF_OSCILLATIONS_REMOVAL_SECONDS,tmpWLDataList));
	
      } else {

	 slog.info(mmi+"Not applying HF oscillations removal"); 
	  
	 mcbRet= new MeasurementCustomBundle(tmpWLDataList);
      }
    }

    slog.info(mmi+"end");
    System.out.flush();
    
    return mcbRet;
    
  } // --- method getMCBFromIWLSJsonArray

  // ---
  public static final void writeSpineAPIInputData(final Instant whatTimeIsItNow, final List<MeasurementCustomBundle> mcbForSpine)  {

    final String mmi= "writeSpineInputFiles: ";

    slog.info(mmi+"start: whatTimeIsItNow -> "+whatTimeIsItNow.toString());
    
    if (outputDataFormat.equals(Format.LEGACY_ASCII.name())) {
	
      IWLPSLegacyIO.writeFiles(whatTimeIsItNow, mcbForSpine, outputDirectory);
      
    } else {
      throw new RuntimeException(mmi+"Invalid SpineAPI input format -> "+outputDataFormat);
      
    }  // --- if-else case block

    slog.info(mmi+"end");
     
  } // --- Method writeSpineInputFiles

  // ---
  final public static void writeGZippedFileFromString(final String strToWrite, final String gzippedFileDest) {

    final String mmi= "writeGZippedFileFromString: ";

    slog.info(mmi+"start: gzippedFileDest -> "+gzippedFileDest);

    try {
      strToWrite.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"strToWrite cannot be null here !!");
    }

    try {
      gzippedFileDest.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"gzippedFileDest cannot be null here !!");
    }

    try (FileOutputStream fos= new FileOutputStream(gzippedFileDest)) {

      final GZIPOutputStream gzos= new GZIPOutputStream(fos, strToWrite.length());	
	
      final Writer gzosWriter= new OutputStreamWriter(gzos, java.nio.charset.StandardCharsets.US_ASCII);
     
      gzosWriter.write(strToWrite);

      // --- Need to close (and it does an auto flush) the gzosWriter object here in order to
      //     not loose bytes at the end of the String.
      gzosWriter.close(); 
	
    } catch (IOException ioe) {
      //ioe.printStackTrace();
      throw new RuntimeException(mmi+"Problem writing strToWrite String in  gzipped file ->"+gzippedFileDest);
    }

    slog.info(mmi+"end");
    
  } // --- Method writeGZippedFileFromString
	
} // --- class WLToolsIO
