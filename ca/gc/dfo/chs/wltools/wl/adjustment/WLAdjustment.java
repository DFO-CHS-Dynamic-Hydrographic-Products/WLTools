package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentType;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpineIPP;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpineFPP;

/**
 * Comments please!
 */
final public class WLAdjustment implements IWLAdjustment { // extends WLAdjustmentIO

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustment";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //private IWLAdjustment.Type adjType= null;

  private IWLAdjustmentType adjInstance= null;

  /**
   * Comments please!
   */
  public WLAdjustment() {

    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  /**
   * Parse the main program arguments using a constructor.
   */
  public WLAdjustment(/*NotNull*/ final HashMap<String,String> argsMap) {

    final String mmi= "WLAdjustment main constructor: ";

    slog.info(mmi+"start");

    final Set<String> argsMapKeySet= argsMap.keySet();

    if (!argsMapKeySet.contains("--adjType")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --adjType defined !!");
    }

    final String adjType= argsMap.get("--adjType");

    // slog.info(mmi+"adjType="+adjType);
    // slog.info(mmi+"Debug System.exit(0)");
    // System.exit(0);

    if (!IWLAdjustment.allowedTypes.contains(adjType)) {
      throw new RuntimeException(mmi+"Invalid WL adjustment type -> "+adjType+
                                 " ! Must be one of -> "+IWLAdjustment.allowedTypes.toString());
    }

    //if (adjType.equals(Type.CHS_IWLS.name())) {
    //  throw new RuntimeException(mmi+"The WL location adjustment type "+
    //                            Type.CHS_IWLS.name()+" is not yet ready to be used !!");
    //}

    //if (adjType.equals(Type.MODEL_BARYCENTRIC.name())) {
    //  throw new RuntimeException(mmi+"The WL adjustment type "+
    //                             Type.MODEL_BARYCENTRIC.name()+" is not yet ready to be used !!");
    //}

    //if (adjType.equals(Type.MODEL_NEAREST_NEIGHBOR.name())) {
    //  throw new RuntimeException(mmi+"The WL adjustment type "+
    //                             Type.MODEL_NEAREST_NEIGHBOR.name()+" is not yet ready to be used !!");
    //}

    slog.info(mmi+"Will use WL adjustment type "+adjType);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    //slog.info(mmi+"Will use input data type -> "+
    //          inputDataType+" with input data format -> "+inputDataFormat);

    //slog.info(mmi+"Will use location Id info  -> "+locationIdInfo);

    if (adjType.equals(IWLAdjustment.Type.TideGauge.name())) {

      slog.info(mmi+"Doing WL adjustment at tide gauge type setup");

      this.adjInstance= new WLAdjustmentTideGauge(argsMap);

      slog.info(mmi+"Done with WL adjustment at tide gauge type setup");
    }

    if (adjType.equals(IWLAdjustment.Type.SpineIPP.name())) {

      slog.info(mmi+"Doing Spine WL adjustment intial pre-processing (IPP) type setup");

      //this.adjType=IWLAdjustment.Type.WDS;
      //final String wdsLocationIdInfoFile=
      //  WLToolsIO.getMainCfgDir() + "/"+ locationIdInfo;
      //slog.info(mmi+"wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

      this.adjInstance= new WLAdjustmentSpineIPP(argsMap); //wdsLocationIdInfoFile);

      slog.info(mmi+"Done with Spine WL adjustment initial pre-processing type setup");
    }

    if (adjType.equals(IWLAdjustment.Type.SpineFPP.name())) {

      slog.info(mmi+"Doing Spine WL adjustment final pre-processing (FPP) type setup");

      //this.adjType=IWLAdjustment.Type.WDS;
      //final String wdsLocationIdInfoFile=
      //  WLToolsIO.getMainCfgDir() + "/"+ locationIdInfo;
      //slog.info(mmi+"wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

      this.adjInstance= new WLAdjustmentSpineFPP(argsMap); //wdsLocationIdInfoFile);

      slog.info(mmi+"Done with Spine WL adjustment final pre-processing type setup");  
      
    }
    
    //slog.info(mmi+"Test dist. rad="+Trigonometry.getDistanceInRadians(-73.552528,45.5035,-73.5425,45.528667));

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
  }

  // ---
  final public String getLocationIdentity() {

    final String mmi="getLocationIdentity: ";

    try {
      this.adjInstance.getLocationIdentity();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

   return this.adjInstance.getLocationIdentity();
  }

  // ---
  final public String getFMFLeadTimeECCCOperStr() {

    final String mmi= "getFMFLeadTimeECCCOperStr: ";
      
    try {
      this.adjInstance.getLocationIdentity();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }
      
    return this.adjInstance.getFMFLeadTimeECCCOperStr(); 
  }

  /**
   * Comments please.
   */
  final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

    //final String mmi= "getAdjustment: ";
    //List<MeasurementCustom> adjustmentRet= null;
    ///slog.info(mmi+"start: this.adjType.name()="+this.adjType.name());

    return this.adjInstance.getAdjustment(outputDirectory);
  }
}
