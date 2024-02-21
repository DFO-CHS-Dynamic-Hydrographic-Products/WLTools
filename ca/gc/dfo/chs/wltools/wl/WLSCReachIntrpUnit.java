package ca.gc.dfo.chs.wltools.wl;

// ---
import java.io.File;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
final public class WLSCReachIntrpUnit implements IWL {

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
    
    slog.info(mmi+"shipChannelPointLocsTCInputDir="+shipChannelPointLocsTCInputDir);
    
    slog.info(mmi+"tg0Cfg.getIdentity()="+tg0Cfg.getIdentity());
    slog.info(mmi+"tg1Cfg.getIdentity()="+tg1Cfg.getIdentity());
    
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    System.out.flush();

    this.lowerSideScLocTGId= tg0Cfg.getIdentity();
    this.upperSideScLocTGId= tg1Cfg.getIdentity();

    // --- Need to get the 2 tide gauges config from the json main config object.
    //     if their config is not yet done.
    if (!tg0Cfg.isConfigOkay()) {
	    
      slog.info(mmi+"Setting the config of tide gauge -> "+this.lowerSideScLocTGId);
      tg0Cfg.setConfig(mainJsonTGInfoMapObj.getJsonObject(this.lowerSideScLocTGId));
    }

    if (!tg1Cfg.isConfigOkay()) {

      slog.info(mmi+"Setting the config of tide gauge -> "+this.upperSideScLocTGId);	    
      tg1Cfg.setConfig(mainJsonTGInfoMapObj.getJsonObject(this.upperSideScLocTGId));
    }

    System.out.flush();
	
    // --- Now get the corresponding nearest ship channel points locations for those two TGs
    //     from their json config:
    final String tg0NearestSCLocId= tg0Cfg.
      getNearestSpinePointId().split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR)[2];
    
    final String tg1NearestSCLocId= tg1Cfg.
      getNearestSpinePointId().split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR)[2]; 

    slog.info(mmi+"tg0NearestSCLocId="+tg0NearestSCLocId);
    slog.info(mmi+"tg1NearestSCLocId="+tg1NearestSCLocId);		     

    slog.info(mmi+"shipChannelPointLocsTCInputDir="+shipChannelPointLocsTCInputDir);
    System.out.flush();
    
    // --- Build the paths to find the tidal consts. files of the two ship channel point locations
    //     that are the nearests to the two TGs considered.
    final String tg0NearestSCLocTCFile= shipChannelPointLocsTCInputDir + //File.separator +
      tg0NearestSCLocId + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;
    
    final String tg1NearestSCLocTCFile= shipChannelPointLocsTCInputDir + //File.separator +
      tg1NearestSCLocId + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;

    slog.info(mmi+"tg0NearestSCLocTCFile="+tg0NearestSCLocTCFile);
    slog.info(mmi+"tg1NearestSCLocTCFile="+tg1NearestSCLocTCFile);
    System.out.flush();
		     
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

    // --- Need the polar stereographic great circle half-distance in radians between the
    //     two ship channel point locations that are the nearests to the two TGs considered.
    final double tgsNearestsLocsHalfDistRad= 0.5 * this.tgsNearestsLocsDistRad;

    // --- Calulate the square of the half distance between the two ship channel point locations
    //     to use it later (it is in fact the radius of the circle that has its center at
    //     the mid-point between that are the nearests to the two TGs considered
    //final double tgsNearestsLocsHalfDistRadSquared= Math.pow(0.5 * tgsNearestsLocsDistRad, 2);
		     
    // --- longitude of the mid-point between the two ship channel point locations
    final double tgsNearestsLocsCenterLon= 0.5 * (tg0NearestSCLocLon + tg1NearestSCLocLon);

    // --- latitude of the mid-point between the two ship channel point locations
    final double tgsNearestsLocsCenterLat= 0.5 * (tg0NearestSCLocLat + tg1NearestSCLocLat);

    slog.info(mmi+"tgsNearestsLocsCenterLon="+tgsNearestsLocsCenterLon);
    slog.info(mmi+"tgsNearestsLocsCenterLat="+tgsNearestsLocsCenterLat);
    System.out.flush();

    // --- Now get the HBCoords of all the ship channel points locations that are in-between
    //     the two ship channel points locations that are the nearests to the two TGs considered.
    //     Verify at the same time if they are all inside the circle which center is located at the mid-point
    //     between the two ship channel points locations that are the nearests to the two TGs considered.
    //     The distance in radians between the mid-point and the in-between ship channel points locations
    //     MUST be smaller that the radius of this circle.
    
    this.tg0NearestSCLocIndex= Integer
      .parseInt(tg0NearestSCLocId.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[1]);
    
    this.tg1NearestSCLocIndex= Integer
      .parseInt(tg1NearestSCLocId.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[1]);

    if (this.tg1NearestSCLocIndex == 0) {
      throw new RuntimeException(mmi+"this.tg1NearestSCLocIndex cannot be 0 here!!");
    }
    
    if (this.tg1NearestSCLocIndex == this.tg0NearestSCLocIndex) {
      throw new RuntimeException(mmi+"this.tg1NearestSCLocIndex and this.tg0NearestSCLocIndex cannot be the same here!!");
    }
   
    slog.info(mmi+"this.tg0NearestSCLocIndex="+this.tg0NearestSCLocIndex);
    slog.info(mmi+"this.tg1NearestSCLocIndex="+this.tg1NearestSCLocIndex);
    System.out.flush();

    // --- Need to have a HBCoords object reference for the
    //     spatial linear interpolation of FMF residuals.
    HBCoords tgNearestSCLocCoordsRef= tg0NearestSCLocHBCoords;

    // --- Case where this.tg0NearestSCLocIndex < this.tg1NearestSCLocIndex
    this.scLoopStartIndex= this.tg0NearestSCLocIndex + 1;
    this.scLoopEndIndex=   this.tg1NearestSCLocIndex - 1;

    //this.lowerSideScLocTGId= this.locations.get(0).getIdentity();
    //this.upperSideScLocTGId= this.locations.get(1).getIdentity();

    // --- DO NOT ASSUME HERE that this.tg0NearestSCLocIndex is always smaller than this.tg1NearestSCLocIndex!!
    if (this.tg0NearestSCLocIndex > this.tg1NearestSCLocIndex) {

      slog.warn(mmi+"Need to switch tide gauges ids. to respect the increasing indices order");
	
      this.scLoopStartIndex= this.tg1NearestSCLocIndex + 1;
      this.scLoopEndIndex=   this.tg0NearestSCLocIndex - 1;

      // --- Need to switch the tide gauges ids here.
      this.lowerSideScLocTGId= tg1Cfg.getIdentity(); //this.locations.get(1).getIdentity();
      this.upperSideScLocTGId= tg0Cfg.getIdentity(); //this.locations.get(0).getIdentity();      

      // --- Also need to use the HBCoord object of the tg1 here
      //     for the tgNearestSCLocCoordsRef (convention of using
      //     the smallest ship channel point location index for it)
      tgNearestSCLocCoordsRef= tg1NearestSCLocHBCoords;
    }		     

    final double tgNearestSCLocLonRef= tgNearestSCLocCoordsRef.getLongitude();
    final double tgNearestSCLocLatRef= tgNearestSCLocCoordsRef.getLatitude();
    
    slog.info(mmi+"this.scLoopStartIndex="+this.scLoopStartIndex);
    slog.info(mmi+"this.scLoopEndIndex="+this.scLoopEndIndex);
    
    slog.info(mmi+"this.lowerSideScLocTGId="+this.lowerSideScLocTGId);
    slog.info(mmi+"this.upperSideScLocTGId="+this.upperSideScLocTGId);
    System.out.flush();

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    // --- Get the common file name prefix string to use for the ship channel points locations
    this.scLocFNameCommonPrefix= tg0NearestSCLocId.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[0];

    this.scLocsDistances= new HashMap<String,Double>();

    // --- Get the HBCoords info of all the in-between ship channel points locations and also
    //     read the related prediction data for them.
    for (int idx= this.scLoopStartIndex; idx <= this.scLoopEndIndex; idx++) {

      // --- Specific file name prefix string to use for the ship channel point location
      //     being processed
      final String scLocFNameSpecPrefix= this.scLocFNameCommonPrefix +
	IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(idx);	

      final String scLocTCFile= shipChannelPointLocsTCInputDir + File.separator +
	scLocFNameSpecPrefix + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;	

      //slog.info(mmi+"scLocTCFile="+scLocTCFile);
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
      final HBCoords scLocHBCoords= HBCoords.getFromCHSJSONTCFile(scLocTCFile); //this.getHBCoordsFromNSTCJsonFile(scLocTCFile);

      final double scLocHBLon= scLocHBCoords.getLongitude();
      final double scLocHBLat= scLocHBCoords.getLatitude();

      final double distanceFromMidPoint= Trigonometry.
	getDistanceInRadians(scLocHBLon, scLocHBLat, tgsNearestsLocsCenterLon, tgsNearestsLocsCenterLat);

      //slog.info(mmi+"distanceFromMidPoint="+distanceFromMidPoint+", tgsNearestsLocsHalfDistRad="+tgsNearestsLocsHalfDistRad);

      if (distanceFromMidPoint > tgsNearestsLocsHalfDistRad) {
	throw new RuntimeException(mmi+"distanceFromMidPoint > tgsNearestsLocsHalfDistRad for ship channell point location -> "+scLocFNameSpecPrefix);
      }

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);

      // --- Now calculate the distance from the HBCoords reference.
      final double distanceFromRef= Trigonometry.
	getDistanceInRadians(scLocHBLon, scLocHBLat, tgNearestSCLocLonRef, tgNearestSCLocLatRef);

      // --- Store this distanceFromRef for later usage.
      this.scLocsDistances.put(scLocFNameSpecPrefix, distanceFromRef);

      //// --- Now read the prediction data for this in-between ship channel point location.
      //final String fileGlobExpr=  
      //final PathMatcher fileMatch= FileSystems.getDefault().getPathMatcher( "glob:"+)
    }

    // --- Lower side ship channel point location: subtract 1 from this.scLoopStartIndex to build its proper str id
    this.lowerSideScLocStrId= this.scLocFNameCommonPrefix +
      IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(this.scLoopStartIndex-1);    

    // --- Upper side ship channel point location: add 1 from this.scLoopEndIndex to build its proper str id
    this.upperSideScLocStrId= this.scLocFNameCommonPrefix +
      IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(this.scLoopEndIndex+1);  
		     		     
    slog.info(mmi+"end");

    System.out.flush();
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
  }

  // --- Return the stored distance for the
  //     ship channel point location having the
  //     scLocId String key id.
  public double getDistanceForScLoc(final String scLocId) {
      
    final String mmi= "getScLocDistanceFor :";
      
    try {
      this.scLocsDistances.get(scLocId);
    } catch (NullPointerException npe) {	
      throw new RuntimeException(mmi+"ship channel point location String key -> "+
				   scLocId+" not found in this.scLocsDistances Map !!");
    }
    
    return this.scLocsDistances.get(scLocId);
  }					
    
  public int getScLoopEndIndex() {
    return this.scLoopEndIndex;
  }    
    
  public int getScLoopStartIndex() {
    return this.scLoopStartIndex;
  }
    
  public int getTg0NearestSCLocIndex() {
    return this.tg0NearestSCLocIndex;
  }

  public int getTg1NearestSCLocIndex() {
    return this.tg1NearestSCLocIndex;
  }    

  public String getLowerSideScLocStrId() {
    return this.lowerSideScLocStrId;
  }       

  public String getUpperSideScLocStrId() {
    return this.upperSideScLocStrId;
  }     

  public String getLowerSideScLocTGId() {
    return this.lowerSideScLocTGId;
  }       

  public String getUpperSideScLocTGId() {
    return this.upperSideScLocTGId;
  }   

  public String getScLocFNameCommonPrefix() {
    return this.scLocFNameCommonPrefix;
  }

  public double getTgsNearestsLocsDistRad() {
    return this.tgsNearestsLocsDistRad;
  }

  // public Set<String> getScLocsDistancesKeySetCopy() {    
  //   final String mmi= "getScLocsDistancesKeySetCopy :";
  //   try {
  //     this.scLocsDistances.size();
  //   } catch (NullPointerException npe) {
  //     throw new RuntimeException(mmi+"this.scLocsDistances cannot be null here !!");
  //   }
  //   return new HashSet<String>(this.scLocsDistances.keySet());
  // }
}
