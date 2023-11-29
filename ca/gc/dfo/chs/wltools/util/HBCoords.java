package ca.gc.dfo.chs.wltools.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


// ---
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;

// --- NOTE: This is a temporary Home Brew (HB) code that should
//     be replaced by either a class from java.awt.geom or a class
//     from the jts code https://github.com/locationtech/jts.
//     BUT we only need this class for now and it could probably
//     be overkill to use jts or the GDAL java wrappers.
//     NOTE: Assuming that the EPSG:4326 CRS is always used.
public class HBCoords implements IHBGeom {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.util.HBCoords";

 /**
   * private logger utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- Only EPSG:4326 normally.
  //protected double latitude;
  //protected double longitude;

  protected double [] llCoords= {UNDEFINED_COORD,UNDEFINED_COORD};

  public HBCoords() {

    this.llCoords[LON_IDX]=
      this.llCoords[LAT_IDX]= UNDEFINED_COORD;

    //this.latitude=
    //  this.longitude= IHBGeom.UNDEFINED_COORD;
  }

  public HBCoords(final double longitude, final double latitude) {

    this.llCoords[LAT_IDX]= latitude;
    this.llCoords[LON_IDX]= longitude;
    //this.latitude= latitude;
    //this.longitude= longitude;
  }

  public final double getLatitude() {
    return this.llCoords[LAT_IDX];
  }

  public final double getLongitude() {
    return this.llCoords[LON_IDX];
  }

  public final void setLatitude(final double latitude) {
    this.llCoords[LAT_IDX]= latitude;
  }

  public final void setLongitude(final double longitude) {
    this.llCoords[LON_IDX]= longitude;
  }

  public final void setLonOrLat(final int lonOrLatIndex, final double latOrLon) {

    // --- No fool-proof check here, need performance.
    this.llCoords[lonOrLatIndex]= latOrLon;
  }

  // ---
  public static final HBCoords getFromCHSJSONTCFile(final String jsonCHSJSONTCFile) {

    final String mmi= "getFromCHSJSONTCFile: ";

    try {
      jsonCHSJSONTCFile.length();

    } catch ( NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(jsonCHSJSONTCFile);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+e);
    }

    final JsonObject mainJsonTcDataInputObj= Json.
      createReader(jsonFileInputStream).readObject();

    // --- We can close the tide gauges info Json file now
    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }

    final JsonObject channelGridPointJsonObj=
       mainJsonTcDataInputObj.getJsonObject(IWLLocation.INFO_JSON_DICT_KEY);

    final double locationLatInDecDeg= channelGridPointJsonObj.
      getJsonNumber(IWLLocation.INFO_JSON_LATCOORD_KEY).doubleValue();

    final double locationLonInDecDeg= channelGridPointJsonObj.
      getJsonNumber(IWLLocation.INFO_JSON_LONCOORD_KEY).doubleValue();

    //HBCoords hbcRet= new HBCoords(locationLonInDecDeg, locationLatInDecDeg);
    // --- We can close the tide gauges info Json file now
    //try {
    //  jsonFileInputStream.close();
    //} catch (IOException e) {
    //  throw new RuntimeException(mmi+e);
    //}

    return new HBCoords(locationLonInDecDeg, locationLatInDecDeg);

  }
}
