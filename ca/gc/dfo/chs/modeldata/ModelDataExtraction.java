package ca.gc.dfo.chs.modeldata;

import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.modeldata.IModelDataExtraction;

public class ModelDataExtraction implements IModelDataExtraction {

  private final static String whoAmI=
    "ca.gc.dfo.chs.modeldata.ModelDataExtraction";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // ---
  protected IModelDataExtraction.Type type= null;
    
  protected IModelDataExtraction.InputDataType inputDataType= null;

  protected IModelDataExtraction.SpatialInterpType spatialInterpType= null;

  protected IModelDataExtraction.WLDatumConv wlInputDatumConv= null;
  protected IModelDataExtraction.WLDatumConv wlOutputDatumConv= null;

  // ---
  public ModelDataExtraction(final Map<String,String> argsMap) {

    final String mmi= "ModelDataExtraction constructor: ";

    slog.info(mmi+"start");

    if (!argsMap.containsKey("--inputDataType")) {
      throw new RuntimeException(mmi+"Must have the --inputDataType in the args !!");
    }    

    if (!argsMap.containsKey("--extractionType")) {
      throw new RuntimeException(mmi+"Must have the --extractionType in the args !!");
    }

    final String inputDataTypeStr= argsMap.get("--inputDataType");

    if (!IModelDataExtraction.allowedInputDataTypes.contains(inputDataTypeStr)) {
      throw new RuntimeException(mmi+"Invalid input data type -> "+inputDataTypeStr);
    }

    this.inputDataType= IModelDataExtraction.InputDataType.valueOf(inputDataTypeStr); //enum.valueOf(IModelDataExtraction.InputDataType,inputDataTypeStr);

    slog.info(mmi+"this.inputDataType="+this.inputDataType.name());
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    if (this.inputDataType.equals(IModelDataExtraction.InputDataType.WaterLevels)) {

      slog.info(mmi+"Processing specific args. for WaterLevels input data");

      if (argsMap.containsKey("--wlInputDatumConv")) {

        final String wlInputDatumConvStr= argsMap.get("--wlInputDatumConv");
	  
        if (!IModelDataExtraction.allowedWLDatumConvTypes.contains(wlInputDatumConvStr)) {
          throw new RuntimeException(mmi+"Invalid wlInputDatumConv str id. -> "+wlInputDatumConvStr);
        }

	this.wlInputDatumConv= IModelDataExtraction.WLDatumConv.valueOf(wlInputDatumConvStr);

	slog.info(mmi+"this.wlInputDatumConv="+this.wlInputDatumConv.name());
	
      } else {
	slog.warn(mmi+"No datum conversion applied for the WL input data");
      }

      if (argsMap.containsKey("--wlOutputDatumConv")) {

        final String wlOutputDatumConvStr= argsMap.get("--wlOutputDatumConv");
	  
        if (!IModelDataExtraction.allowedWLDatumConvTypes.contains(wlOutputDatumConvStr)) {
          throw new RuntimeException(mmi+"Invalid wlOutputDatumConv str id. -> "+wlOutputDatumConvStr);
        }

	this.wlOutputDatumConv= IModelDataExtraction.WLDatumConv.valueOf(wlOutputDatumConvStr);

	slog.info(mmi+"this.wlOutputDatumConv="+this.wlOutputDatumConv.name());
	
      }  else {
	slog.warn(mmi+"No datum conversion applied for the WL output data");
      }
    }

    // if (this.inputDataType.equals(IModelDataExtraction.InputDataType.Currents2D)) {
    //   throw new RuntimeException(mmi+"Model currents 2D data extraction not yet implemented !!");
    // }

    // if (this.inputDataType.equals(IModelDataExtraction.InputDataType.Currents3D)) {
    //   throw new RuntimeException(mmi+"Model currents 3D data extraction not yet implemented !!");
    // }    

    //final String extractionTypeInterpStr= argsMap.get("--extractionType");

    final String [] extractionTypeInterpSplit= argsMap.get("--extractionType").split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (extractionTypeInterpSplit.length != 2 ) {
      throw new RuntimeException(mmi+" extractionTypeInterpSplit.size() != 2 !!");
    }
    
    final String extractionTypeStr= extractionTypeInterpSplit[0];

    if (!IModelDataExtraction.allowedTypes.contains(extractionTypeStr)) {
      throw new RuntimeException(mmi+"Invalid extraction type -> "+extractionTypeStr);
    }

    this.type= IModelDataExtraction.Type.valueOf(extractionTypeStr);

    slog.info(mmi+"this.type="+this.type.name());

    final String spatialInterpTypeStr= extractionTypeInterpSplit[1];

    if (!IModelDataExtraction.allowedSpatialInterpTypes.contains(spatialInterpTypeStr)) {
      throw new RuntimeException(mmi+"Invalid spatial interp. type -> "+extractionTypeStr);
    }

    this.spatialInterpType= ModelDataExtraction.SpatialInterpType.valueOf(spatialInterpTypeStr);

    //slog.info(mmi+"extractionTypeStr="+extractionTypeStr);
    slog.info(mmi+"this.spatialInterpType="+this.spatialInterpType.name());
    
    slog.info(mmi+"end");

    slog.info(mmi+"Debug exit 0");
    System.exit(0);
  }
}
