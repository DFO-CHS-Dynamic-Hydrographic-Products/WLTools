package ca.gc.dfo.chs.modeldata;

// ---
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

// --
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.dhp.sproduct.SProduct;
import ca.gc.dfo.chs.dhp.sproduct.SProductIO;
import ca.gc.dfo.chs.dhp.sproduct.ISProductIO;
import ca.gc.dfo.chs.dhp.sproduct.SProductDCF2;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.modeldata.ModelDataExtractionIO;
import ca.gc.dfo.chs.modeldata.IModelDataExtractionIO;
import ca.gc.dfo.chs.dhp.sproduct.S104DCFNCompoundType;
import ca.gc.dfo.chs.dhp.sproduct.S104HeightTrendCompoundType;

// // --- HDFql lib
// import as.hdfql.HDFql;
// import as.hdfql.HDFqlJNI;
// import as.hdfql.HDFqlCursor;
// import as.hdfql.HDFqlConstants;

// ---
public class ModelDataExtraction implements IModelDataExtractionIO {

  private final static String whoAmI=
    "ca.gc.dfo.chs.modeldata.ModelDataExtraction";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // ---
  protected IModelDataExtractionIO.Type type= null;
  protected IModelDataExtractionIO.InputDataType inputDataType= null;
  protected IModelDataExtractionIO.SpatialInterpType spatialInterpType= null;
  protected IModelDataExtractionIO.WLDatumConv wlInputDatumConv= null;
  protected IModelDataExtractionIO.WLDatumConv wlOutputDatumConv= null;

  // ---
  public ModelDataExtraction(final Map<String,String> argsMap) {

    final String mmi= "ModelDataExtraction constructor: ";

    slog.info(mmi+"start");

    if (!argsMap.containsKey("--inputDataType")) {
      throw new RuntimeException(mmi+"Must have the --inputDataType=<inputDataType> defined in the args !!");
    }    

    if (!argsMap.containsKey("--extractionType")) {
      throw new RuntimeException(mmi+"Must have the --extractionType=<extractionType> defined in the args !!");
    }

    final String inputDataTypeStr= argsMap.get("--inputDataType");

    if (!IModelDataExtractionIO.allowedInputDataTypes.contains(inputDataTypeStr)) {
      throw new RuntimeException(mmi+"Invalid input data type -> "+inputDataTypeStr);
    }

    this.inputDataType= IModelDataExtractionIO.InputDataType.valueOf(inputDataTypeStr); //enum.valueOf(IModelDataExtraction.InputDataType,inputDataTypeStr);

    slog.info(mmi+"this.inputDataType="+this.inputDataType.name());
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    final String [] extractionTypeInterpSplit= argsMap.get("--extractionType").split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (extractionTypeInterpSplit.length != 2 ) {
      throw new RuntimeException(mmi+" extractionTypeInterpSplit.size() != 2 !!");
    }
    
    final String extractionTypeStr= extractionTypeInterpSplit[0];

    if (!IModelDataExtractionIO.allowedTypes.contains(extractionTypeStr)) {
      throw new RuntimeException(mmi+"Invalid extraction type -> "+extractionTypeStr);
    }

    this.type= IModelDataExtractionIO.Type.valueOf(extractionTypeStr);

    slog.info(mmi+"this.type="+this.type.name());

    final String spatialInterpTypeStr= extractionTypeInterpSplit[1];

    if (!IModelDataExtractionIO.allowedSpatialInterpTypes.contains(spatialInterpTypeStr)) {
      throw new RuntimeException(mmi+"Invalid spatial interp. type -> "+extractionTypeStr);
    }

    this.spatialInterpType= IModelDataExtractionIO.SpatialInterpType.valueOf(spatialInterpTypeStr);

    //slog.info(mmi+"extractionTypeStr="+extractionTypeStr);
    slog.info(mmi+"this.spatialInterpType="+this.spatialInterpType.name());   

    if (!argsMap.containsKey("--pointLocationsInputInfo")) {
      throw new RuntimeException(mmi+"Must have the --pointLocationsInputInfo=<pointLocationsInputInfo> defined in the args !!");
    }

    final String [] pointLocationsInputInfoStrSplit= argsMap.get("--pointLocationsInputInfo").split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    // --- The file format for the target point(s) OR LOCATIONS_COORDS in case
    //     we just need one or more location(s) coordinates (like for currents data extraction)
    final String pointLocationsInputInfoFmt= pointLocationsInputInfoStrSplit[0];

    if (pointLocationsInputInfoFmt.equals(IModelDataExtractionIO.Format.SIMPLE_LOCATIONS_COORDS.name())) {
      throw new RuntimeException(mmi+"The data extraction for "+IModelDataExtractionIO.Format.SIMPLE_LOCATIONS_COORDS.name()+" not yet implemented !!");
    }

    slog.info(mmi+"pointLocationsInputInfoFmt="+pointLocationsInputInfoFmt);

    // --- Could be a path to a folder OR a path to a single file OR just a series
    //     of coordinate pairs if pointLocationsInputInfoFmt == ONE_POINT_COORD
    final String pointLocationsInputInfo= pointLocationsInputInfoStrSplit[1];

    slog.info(mmi+"pointLocationsInputInfo="+pointLocationsInputInfo);

    // ---
    if (this.inputDataType.equals(IModelDataExtractionIO.InputDataType.WaterLevels)) {

      slog.info(mmi+"Processing specific args. for WaterLevels input data");

      if (pointLocationsInputInfoFmt.equals(IModelDataExtractionIO.locationsCoordsId)) {
	throw new RuntimeException(mmi+"The data extraction for simple "+IModelDataExtractionIO.locationsCoordsId+" is not ready be used for Water levels yet !!");
      }

      slog.info(mmi+"Reading the WLLocation(s) input info");
      slog.info(mmi+"Debug exit 0");
      System.exit(0);
      
      // --- Read the WLLocation target(s) info.
      //     NOTE: The ModelDataExtractionIO class source file has been lost at some point
      //           after a merge that went wrong and this readAllWLLocationTargetsInfo method
      //           was defined in it. 
      //this.readAllWLLocationTargetsInfo(pointLocationsInputInfo, pointLocationsInputInfoFmt);

      slog.info(mmi+"Done with reading the WLLocation(s) input info");
      slog.info(mmi+"Debug exit 0");
      System.exit(0);
      
      if (argsMap.containsKey("--wlInputDatumConv")) {

        final String wlInputDatumConvStr= argsMap.get("--wlInputDatumConv");
	  
        if (!IModelDataExtractionIO.allowedWLDatumConvTypes.contains(wlInputDatumConvStr)) {
          throw new RuntimeException(mmi+"Invalid wlInputDatumConv str id. -> "+wlInputDatumConvStr);
        }

	this.wlInputDatumConv= IModelDataExtractionIO.WLDatumConv.valueOf(wlInputDatumConvStr);

	slog.info(mmi+"this.wlInputDatumConv="+this.wlInputDatumConv.name());
	
      } else {
	slog.warn(mmi+"No datum conversion applied for the WL input data");
      }

      if (argsMap.containsKey("--wlOutputDatumConv")) {

        final String wlOutputDatumConvStr= argsMap.get("--wlOutputDatumConv");
	  
        if (!IModelDataExtractionIO.allowedWLDatumConvTypes.contains(wlOutputDatumConvStr)) {
          throw new RuntimeException(mmi+"Invalid wlOutputDatumConv str id. -> "+wlOutputDatumConvStr);
        }

	this.wlOutputDatumConv= IModelDataExtractionIO.WLDatumConv.valueOf(wlOutputDatumConvStr);

	slog.info(mmi+"this.wlOutputDatumConv="+this.wlOutputDatumConv.name());
	
      }  else {
	slog.warn(mmi+"No datum conversion applied for the WL output data");
      }
     
    } // --- Water levels

    // if (this.inputDataType.equals(IModelDataExtraction.InputDataType.Currents2D)) {
    //   throw new RuntimeException(mmi+"Model currents 2D data extraction not yet implemented !!");
    // }

    // if (this.inputDataType.equals(IModelDataExtraction.InputDataType.Currents3D)) {
    //   throw new RuntimeException(mmi+"Model currents 3D data extraction not yet implemented !!");
    // } 

    slog.info(mmi+"end");

    slog.info(mmi+"Debug exit 0");
    System.exit(0);
  }

  // --- 
  final static public List<MeasurementCustom> getNearestS104DCF2Data(final String S104DCF2InputDataFilePath, final WLLocation wlLocation) {

    final String mmi= "getNearestS104DCF2Data: ";

    List<MeasurementCustom> mcOfS104DCF2Data= new ArrayList<MeasurementCustom>();

    try {
      S104DCF2InputDataFilePath.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe+"S104DCF2InputDataFilePath cannot be null here !!");
    }

    if (!WLToolsIO.checkForFileExistence(S104DCF2InputDataFilePath)) {
      throw new RuntimeException(mmi+"S104DCF2InputDataFilePath -> "+S104DCF2InputDataFilePath+" not found !!");
    }

    slog.info(mmi+"start");
    slog.info(mmi+"S104DCF2InputDataFilePath="+S104DCF2InputDataFilePath);
    slog.info(mmi+"wlLocation id="+wlLocation.getIdentity());
    slog.info(mmi+"wlLocation lat="+wlLocation.getLatitude());
    slog.info(mmi+"wlLocation lon="+wlLocation.getLongitude());

    final SProduct S104DCF2InputData= new SProductDCF2(S104DCF2InputDataFilePath, ISProductIO.FILE_READ_ONLY_MODE);

    // int hdfqlCmdStatus= HDFql.execute("USE READONLY FILE "+S104DCF2InputDataFilePath);

    // if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
    //   throw new RuntimeException(mmi+"Problem with HDFql open file command \"USE READONLY FILE \" for file -> "
    // 				 +S104DCF2InputDataFilePath+", hdfqlCmdStatus="+hdfqlCmdStatus);
    // }
  
    // --- First check that the WLLocation coordinates are indeed inside the
    //     S104 DCF2 tile bounding box.
    if (!S104DCF2InputData.isHBCoordsInsideDHPTile(wlLocation)) {
       throw new RuntimeException(mmi+"The WLLocation (point) object is outside the S104 DCF2 tile bounding box !!"); 
    }

    slog.info(mmi+"The WLLocation (point) object is inside the S104 DCF2 tile bounding box");

    // --- Close the file from which the S104 DCF2 data was read.
    S104DCF2InputData.closeFileInUse();
    
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    // hdfqlCmdStatus= HDFql.execute("CLOSE FILE "+S104DCF2InputDataFilePath);

    // if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
    //   throw new RuntimeException(mmi+"Problem with HDFql close file command \"CLOSE \" for file -> "
    // 				 +S104DCF2InputDataFilePath+", hdfqlCmdStatus="+hdfqlCmdStatus);
    // } 
   
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    slog.info(mmi+"end");

    return mcOfS104DCF2Data;
  }
}
