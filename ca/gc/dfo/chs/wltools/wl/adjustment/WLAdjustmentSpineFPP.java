package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.net.URL;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
//import java.util.Iterator;
import java.nio.file.Path;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.io.InputStream;
import java.util.SortedSet;
import java.util.ArrayList;
//import java.util.ListIterator;
import java.nio.file.Files;
import java.util.Collection;
import java.util.NavigableSet;
//import java.nio.file.PathMatcher;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.DirectoryStream;
import java.nio.file.StandardCopyOption;
import java.io.UnsupportedEncodingException;
//import java.nio.file.DirectoryIteratorException;

// ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpinePP;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

/**
 * Comments please!
 */
final public class WLAdjustmentSpineFPP extends WLAdjustmentSpinePP {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpineFPP: ";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- To store the non-ajusted WL predictions OR non-adjusted FMF data at the ship channel point locations that are
  //     in-between the tide gauges locations being processed (INPUT ONLY) 
  //private Map<String, MeasurementCustomBundle> scLocsNonAdjData= null;

  // --- To store the non-ajusted WL predictions OR non-adjusted FMF data at the two ship channel point locations that are
  //     the nearest to the nearest tide gauges locations (INPUT ONLY)
  //private Map<String, MeasurementCustomBundle>
  //  tgsNearestSCLocsNonAdjData= new HashMap<String, MeasurementCustomBundle>(2); //null;

  //private String fmfReferenceDateTimeStr= null;
    
  /**
   * Comments please!
   */
  public WLAdjustmentSpineFPP() {
    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  // ---
  public WLAdjustmentSpineFPP(/*@NotNull*/ final HashMap<String,String> argsMap) {

    super(IWLAdjustment.Type.SpineFPP,argsMap);

    final String mmi= "WLAdjustmentSpineFPP main constructor: ";

    slog.info(mmi+"start");

    // try {
    //   this.lowerSideScLocStrId.length();	
    // } catch (NullPointerException npe) {
    //   throw new RuntimeException(mmi+npe);
    // }

    // try {
    //   this.upperSideScLocStrId.length();	
    // } catch (NullPointerException npe) {
    //   throw new RuntimeException(mmi+npe);
    // }

    try {
      WLToolsIO.getOutputDataFormat();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe+"WLToolsIO.getOutputDataFormat() cannot be null here !");
    }

    // --- Verify the output file(s) format before going further
    //if (!WLToolsIO.getOutputDataFormat().equals(IWLToolsIO.Format.DHP_S104_DCF8.name())) {
    
    if (!WLToolsIO.getOutputDataFormat().equals(IWLToolsIO.Format.LEGACY_ASCII.name())) {
      throw new RuntimeException(mmi+"Invalid output file(s) data format -> "+
				 WLToolsIO.getOutputDataFormat()+" for the SpineFPP adjustment type sub-tool!");
    }

    if (!argsMap.containsKey("--localDownloadDirectory")) {
      throw new RuntimeException(mmi+
	"Must have the --localDownloadDirectory=<path of the folder where to download data> defined in the argsMap !!");
    }

    final String localDownloadDirectory= argsMap.get("--localDownloadDirectory");

    slog.info(mmi+"localDownloadDirectory="+localDownloadDirectory);

    if (!argsMap.containsKey("--inputFileURLToGet")) {
      throw new RuntimeException(mmi+
	"Must have the --inputFileURLToGet=<Complete https URL of the S104 DCF8 file that has to be downloaded> defined in the argsMap !!");
    }

    final String inputFileURLToGet= argsMap.get("--inputFileURLToGet");

    slog.info(mmi+"inputFileURLToGet="+inputFileURLToGet);
    
    final String inputFileNameToGet= new File(inputFileURLToGet).getName();

    slog.info(mmi+"inputFileNameToGet="+inputFileNameToGet);
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);

    final String inputFileDldLocalDest= localDownloadDirectory + File.separator + inputFileNameToGet;

    slog.info(mmi+"inputFileDldLocalDest="+inputFileDldLocalDest);
    
    boolean success= true;
    
    // --- 
    try {
      Files.copy( new URL(inputFileURLToGet).openStream(),
		  Paths.get(inputFileDldLocalDest), StandardCopyOption.REPLACE_EXISTING);
      
    } catch (IOException ioe) {
      slog.warn(mmi+" Problem with the 1st try for the "+inputFileURLToGet+" download !");
      success= false;		
    }

    slog.info(mmi+"1st download try done, success="+success);

    if (!success) {	
      try {
        Files.copy( new URL(inputFileURLToGet).openStream(),
		    Paths.get(inputFileDldLocalDest), StandardCopyOption.REPLACE_EXISTING);
	
      } catch (IOException ioe) {
	//throw new RuntimeException(mmi+ioe+" Also a problem with the 2nd try for the "+inputFileURLToGet+" download !");
        slog.warn(mmi+" Also a problem with the 2nd try for the "+inputFileURLToGet+" download !");
      }	
    }

    if (success) {
      slog.info(mmi+"Success for the "+inputFileURLToGet+" download to local destination: "+inputFileDldLocalDest);
    } else {
      slog.warn(mmi+"Had a problem to download the S104 DCF8 input file, will use the previous input file!");
    }

    // --- Check for the S104 DCF8 input file existence on local disk whatever its download has succeded or not
    if (!WLToolsIO.checkForFileExistence(inputFileDldLocalDest)) {
      throw new RuntimeException(mmi+" inputFileDldLocalDest -> "+inputFileDldLocalDest+" not found !");
    }

    // --- TODO: Now check if we have at least 31 days of data in the future for the S104 DCF8 input file.
    //     If not we stop the exec.
    
    // --- TODO: Tell the HDFql world to use just one thread here.
    
    // --- Now get the WLO data from the IWLS for all the tide gauge locations.
    URLConnection uc= null;

    String ec1;
    String ec2;

    try {
      ec1= URLEncoder.encode("2024-02-06T06%3A00%3A00Z","UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(mmi+e);
    }

    try {
      ec2= URLEncoder.encode("2024-02-08T11%3A00%3A00Z","UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(mmi+e);
    }   
 
    //final String es=
    // "https://api.test.iwls.azure.cloud.dfo-mpo.gc.ca/api/v1/stations/5cebf1e23d0f4a073c4bc0f6/data?time-series-code=wlo&from="+ec1+"&to="+ec2;

    final String es=
	"https://api.test.iwls.azure.cloud.dfo-mpo.gc.ca/api/v1/stations/5cebf1e23d0f4a073c4bc0f6/data?time-series-code=wlo&from=2024-02-06T06%3A00%3A00Z&to=2024-02-08T11%3A00%3A00Z";
	
    try {
     uc= new URL(es).openConnection();
    } catch (IOException ioe) {
      ioe.printStackTrace();	
      throw new RuntimeException(mmi+ioe+"\nProblem with openConnection()");
    }

    // --- no neee for a ":" char here after the accept
    //uc.setRequestProperty("accept ", "*/*");

    InputStream ist= null;
	//InputStreamReader inputStreamReader= null;

    try {
      ist= uc.getInputStream();
	//inputStreamReader= new InputStreamReader(uc.getInputStream());
    } catch (IOException ioe) {
      ioe.printStackTrace();    	
      throw new RuntimeException(mmi+ioe+"\nProblem with new uc.getInputStream()");
    }

    //final Map<String, Object> config = new HashMap<>();
    
    final JsonReader jsr= Json.createReaderFactory(null).createReader(ist);

    final JsonArray stnWLOJsArray= jsr.readArray();

    for (final 
    
    slog.info(mmi+"end");
    
    slog.info(mmi+"debug exit 0");
    System.exit(0);
  }

  // --- 
  final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

    final String mmi= "getAdjustment: ";
      
    List<MeasurementCustom> ret= null;
    
    slog.info(mmi+"start");
    
    
    slog.info(mmi+"end");
    slog.info(mmi+"debug exit 0");
    System.exit(0);     
     
    return ret;
  }
}
