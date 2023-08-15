package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;

/**
 * Comments please!
 */
final public class WLAdjustment implements IWLAdjustment, IWLAdjustmentIO { //extends <>

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustment";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Comments please!
   */
  public WLAdjustment() {
  }

  /**
   * Parse the main program arguments using a constructor.
   */
  public WLAdjustment(/*NotNull*/ final HashMap<String,String> argsMap) {

    final String mmi=
      "WLAdjustment(final HasMap<String,String> mainProgramOptions) constructor: ";

    slog.info(mmi+"start");

    final Set<String> argsMapKeySet= argsMap.keySet();

    if (!argsMapKeySet.contains("--adjType")) {

      throw new RuntimeException(mmi+"Must have the mandatory option: --adjType="+
                                 Type.SPATIAL_LINEAR.name()+" OR --adjType="+Type.SINGLE_LOCATION.name()+" defined !!");
    }

    final String adjType= argsMap.get("--adjType");

    if (!IWLAdjustment.allowedTypes.contains(adjType)) {
      throw new RuntimeException(mmi+"Invalid WL adjustment type -> "+adjType+
                                 " ! Must be one of -> "+IWLAdjustment.allowedTypes.toString());
    }

    if (adjType.equals(Type.SPATIAL_LINEAR.name())) {
      throw new RuntimeException(mmi+"The adjustment type "+
                                 Type.SPATIAL_LINEAR.name()+" is not yet ready to be used !!");
    }

    slog.info(mmi+"Will use adjustment type "+adjType);

    if (!argsMapKeySet.contains("--inputDataType")) {

      throw new RuntimeException(mmi+"Must have the mandatory option: --inputDataType defined !!");
    }

    final String [] inputDataTypeFmtSplit= argsMap.
      get("--inputDataType").split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

    final String inputDataType= inputDataTypeFmtSplit[0];
    final String inputDataFormat= inputDataTypeFmtSplit[1];

    if (!IWLAdjustmentIO.allowedInputDataTypes.contains(inputDataType)) {
       throw new RuntimeException(mmi+"Invalid input data type -> "+inputDataType+
                                  " ! must be one of -> "+IWLAdjustmentIO.allowedInputDataTypes.toString());
    }

    final Set<String> allowedInputFormats=
      IWLAdjustmentIO.InputDataTypesFormats.get(inputDataType);

    if (!allowedInputFormats.contains(inputDataFormat)) {
       throw new RuntimeException(mmi+"Invalid input data format ->"+inputDataFormat+
                                  " for input data type -> "+inputDataType+" ! must be one of -> "+allowedInputFormats.toString());
    }

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    slog.info(mmi+"end");
  }

}

