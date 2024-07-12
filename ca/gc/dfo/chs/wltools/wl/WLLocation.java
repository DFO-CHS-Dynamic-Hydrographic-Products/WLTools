//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

/**
 *
 */

import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;

//---
//import ca.gc.dfo.chs.wltools.numbercrunching.D1Data;
//import ca.gc.dfo.chs.wltools.numbercrunching.D2Data;

//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D1Data;
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D2Data;

import java.util.List;
import org.slf4j.Logger;
import java.nio.file.Path;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

//---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;


//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---
//---

/**
 *
 */
public class WLLocation extends HBCoords implements IWLLocation {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.wl.WLLocation";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- For CHS TGs it is the CHS numeric string id. (e.g. "03248")
  protected String identity= null;

  //protected HBCoords hbCoords= null;

  protected double zcVsVertDatum= 0.0;

  protected boolean doZCConvToVertDatum= true;

  protected JsonObject jsonCfgObj= null;

  // ---
  public WLLocation(final String identity) {

    super();
    this.identity= identity;
  }

  // ---
  public WLLocation(final String identity,
                    final double zcVsVertDatum,
                    final boolean doZCConvToVertDatum,
                    final double locationLon, final double locationLat ) {

    super(locationLon,locationLat);

    this.identity= identity;
    this.zcVsVertDatum= zcVsVertDatum;
    this.doZCConvToVertDatum= doZCConvToVertDatum;

    // --- TODO: Add fool proof checks for the the EPSG:4326 CRS coordinates.
    //this.hbCoords= new HBCoords(locationLon,locationLat);
  }

  final public String getIdentity() {
    return this.identity;
  }

  final public double getZcVsVertDatum() {
    return this.zcVsVertDatum;
  }

  final public boolean getDoZCConvToVertDatum() {
    return this.doZCConvToVertDatum;
  }

  final public JsonObject getJsonCfgObj() {
    return this.jsonCfgObj;
  }

  final public WLLocation setHBCoords(final double longitude, final double latitude) {

    this.setLatitude(latitude);
    this.setLongitude(longitude);

    return this;
  }

  // ---
  public WLLocation setConfig(final JsonObject wllJsonCfgObj) {

    final String mmi= "setConfig: ";

    //slog.info(mmi+"start");

    try {
      wllJsonCfgObj.size();

    } catch (NullPointerException npe){
      new RuntimeException(mmi+npe);
    }

    this.jsonCfgObj= wllJsonCfgObj;

    this.zcVsVertDatum= this.jsonCfgObj.
      getJsonNumber(IWLLocation.INFO_JSON_ZCIGLD_CONV_KEY).doubleValue();

    if (this.jsonCfgObj.containsKey(IWLLocation.INFO_JSON_ZCIGLD_CONV_BOOL_KEY)) {

      this.doZCConvToVertDatum= this.jsonCfgObj.
        getBoolean(IWLLocation.INFO_JSON_ZCIGLD_CONV_BOOL_KEY);
    }

    //slog.info(mmi+"this.identity="+this.identity+
    //          ", this.zcVsVertDatum="+this.zcVsVertDatum+
    //          ", this.doZCConvToVertDatum="+this.doZCConvToVertDatum);

    this.setHBCoords(this.jsonCfgObj.getJsonNumber(IWLLocation.INFO_JSON_LONCOORD_KEY).doubleValue(),
                     this.jsonCfgObj.getJsonNumber(IWLLocation.INFO_JSON_LATCOORD_KEY).doubleValue());

    //slog.info(mmi+"end");
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    return this;
  }

  // ---
  public final static WLLocation setConfigFromJSONFile(final Path wllocationJsonFilePath) {

    final String mmi= "setConfigFromJSONFile: ";

    WLLocation wllRet= new WLLocation(wllocationJsonFilePath.getFileName().toString());
    
    // --- 
    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(wllocationJsonFilePath.toString());

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+e);
    }

    final JsonObject wllLocJsonFileObj= Json.createReader(jsonFileInputStream).readObject();

    wllRet.setConfig(wllLocJsonFileObj.getJsonObject(IWLLocation.INFO_JSON_DICT_KEY));    

    // --- We can close the tide gauges info Json file now
    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }    

    return wllRet;
  }

  //final public HBCoords getHBCoords() {
  //  return this.hbCoords;
  //}o

  //final public double getZcVsVertDatum() {
  //  return this.zcVsVertDatum;
  //}
}
