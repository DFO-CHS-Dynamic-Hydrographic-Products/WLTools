package ca.gc.dfo.chs.wltools.wl;

// ---
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

//---
import javax.json.Json;
import javax.json.JsonObject;

// ---
import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.TideGaugeConfig;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

// ---
final public class WLSCReachIntrpUnit implement IWL {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLSCReachIntrpUnit: ";

 /**
   * class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  private int scLoopEndIndex= -1; 
  private int scLoopStartIndex= -1;
    
  private int tg0NearestSCLocIndex= -1;
  private int tg1NearestSCLocIndex= -1;

  // --- tide gauges long names numeric ids. 
  private String lowerSideScLocStrId= null;
  private String upperSideScLocStrId= null;

  // --- tide gauges short names numeric ids.
  private String lowerSideScLocTGId= null;
  private String upperSideScLocTGId= null;

  private String scLocFNameCommonPrefix= null;

  private double tgsNearestsLocsDistRad= -1.0; 

  private Map<String, Double> scLocsDistances= null; 
    
  // ---  
  public WLSCReachIntrpUnit() { }

  // ---
  public WLSCReachIntrpUnit(final String shipChannelPointLocsTCInputDir,
			    final JsonObject mainJsonTGInfoMapObj, TideGaugeConfig tg0Cfg, TideGaugeConfig tg1Cfg) {

    final String mmi= "WLSCReachIntrpUnit main constructor ";

    try {
      shipChannelPointLocsTCInputDir.hashCode();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"shipChannelPointLocsTCInputDir cannot be null here !");
    }
    
    try {
      mainJsonTGInfoMapObj.hashCode();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"mainJsonTGInfoMapObj cannot be null here !");
    }    
    
    try {
      tg0Cfg.hashCode();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"tg0Cfg cannot be null here !");
    }
    
    try {
      tg1Cfg.hashCode();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"tg1Cfg cannot be null here !");
    }
    
    slog.info(mmi+"start");
    
    slog.info(mmi+"tg0Cfg.getIdentity()="+tg0Cfg.getIdentity());
    slog.info(mmi+"tg1Cfg.getIdentity()="+tg1Cfg.getIdentity());
    
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);

    // --- Need to get the 2 tide gauges config from the json main config object.     
    tg0Cfg.setConfig(mainJsonTGInfoMapObj.getJsonObject(tg0Cfg.getIdentity());
    tg1Cfg.setConfig(mainJsonTGInfoMapObj.getJsonObject(tg1Cfg.getIdentity());

    // --- Now get the corresponding nearest ship channel points locations for those two TGs
    //     from their json config:
    final String tg0NearestSCLocId= tg0Cfg.
      getNearestSpinePointId().split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR)[2];
    
    final String tg1NearestSCLocId= tg1Cfg.
      getNearestSpinePointId().split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR)[2]; 

    slog.info(mmi+"tg0NearestSCLocId="+tg0NearestSCLocId);
    slog.info(mmi+"tg1NearestSCLocId="+tg1NearestSCLocId);		     

    slog.info(mmi+"shipChannelPointLocsTCInputDir="+shipChannelPointLocsTCInputDir);

    // --- Build the paths to find the tidal consts. files of the two ship channel point locations
    //     that are the nearests to the two TGs considered.
    final String tg0NearestSCLocTCFile= shipChannelPointLocsTCInputDir + //File.separator +
      tg0NearestSCLocId + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;
    
    final String tg1NearestSCLocTCFile= shipChannelPointLocsTCInputDir + //File.separator +
      tg1NearestSCLocId + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;

    slog.info(mmi+"tg0NearestSCLocTCFile="+tg0NearestSCLocTCFile);
    slog.info(mmi+"tg1NearestSCLocTCFile="+tg1NearestSCLocTCFile);
		     
    // --- Get the HBCoords objects from the tidal consts. files of the two ship channel point locations
    //final HBCoords tg0NearestSCLocHBCoords= this.getHBCoordsFromNSTCJsonFile(tg0NearestSCLocTCFile);
    final HBCoords tg0NearestSCLocHBCoords= HBCoords.getFromCHSJSONTCFile(tg0NearestSCLocTCFile);
		     
    //final HBCoords tg1NearestSCLocHBCoords= this.getHBCoordsFromNSTCJsonFile(tg1NearestSCLocTCFile);
    final HBCoords tg1NearestSCLocHBCoords= HBCoords.getFromCHSJSONTCFile(tg1NearestSCLocTCFile);

    final double tg0NearestSCLocLon= tg0NearestSCLocHBCoords.getLongitude();
    final double tg0NearestSCLocLat= tg0NearestSCLocHBCoords.getLatitude();
    final double tg1NearestSCLocLon= tg1NearestSCLocHBCoords.getLongitude();
    final double tg1NearestSCLocLat= tg1NearestSCLocHBCoords.getLatitude();    

    // --- Calculate the polar stereographic great circle distance in radians between the
    //     two ship channel point locations that are the nearests to the two TGs considered.
    this.tgsNearestsLocsDistRad= Trigonometry.
      getDistanceInRadians(tg0NearestSCLocLon,tg0NearestSCLocLat,tg1NearestSCLocLon,tg1NearestSCLocLat);
		     
    slog.info(mmi+"end");
    slog.info(mmi+"debug exit 0");
    System.exit(0);
  }

  Set<String> getScLocsDistancesKeySetCopy() {
    return new HashSet<String>(this.scLocsDistances.keySet());
  }
    
  int getScLoopEndIndex() {
    return this.scLoopEndIndex;
  }    
    
  int getScLoopStartIndex() {
    return this.scLoopStartIndex;
  }
    
  int getTg0NearestSCLocIndex() {
    return this.tg0NearestSCLocIndex;
  }

  int getTg1NearestSCLocIndex() {
    return this.tg1NearestSCLocIndex;
  }    

  String getLowerSideScLocStrId() {
    return this.lowerSideScLocTGId;
  }       

  String getUpperSideScLocStrId() {
    return this.upperSideScLocTGId;
  }     

  String getLowerSideScLocTGId() {
    return this.lowerSideScLocTGId;
  }       

  String getUpperSideScLocTGId() {
    return this.upperSideScLocTGId;
  }   

  String getScLocFNameCommonPrefix() {
    return this.scLocFNameCommonPrefix;
  }

  double getTgsNearestsLocsDistRad() {
    return this.tgsNearestsLocsDistRad;
  }
    
}
