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
import javax.json.JsonObject;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---
//---

/**
 *
 */
abstract public class WLLocation extends HBCoords implements IWLLocation {

  // --- For CHS TGs it is the CHS numeric string id. (e.g. "03248")
  protected String identity= null;

  //protected HBCoords hbCoords= null;

  protected double zcVsVertDatum= 0.0;

  // ---
  public WLLocation(final String identity) {

    super();
    this.identity= identity;
  }

  // ---
  public WLLocation(final String identity, final double zcVsVertDatum,
                    final double locationLon, final double locationLat ) {

    super(locationLon,locationLat);

    this.identity= identity;
    this.zcVsVertDatum= zcVsVertDatum;

    // --- TODO: Add fool proof checks for the the EPSG:4326 CRS coordinates.
    //this.hbCoords= new HBCoords(locationLon,locationLat);
  }

  final public String getIdentity() {
    return this.Identity;
  }

  final public double getZcVsVertDatum() {
    return this.zcVsVertDatum;
  }

  final public WLLocation setHBCoords(final double longitude, final double latitude) {

    this.setLatitude(latitude);
    this.setLongitude(longitude);

    return this;
  }

  public WLLocation setConfig(final JsonObject wllJsonObj) {

    this.zcVsVertDatum= wllJsonObj.
      getJsonNumber(IWLLocatiom.INFO_JSON_ZCIGLD_CONV_KEY).doubleValue();

    this.setHBCoords(wllJsonObj.getJsonNumber(IWLLocation.INFO_JSON_LONCOORD_KEY).doubleValue(),
                     wllJsonObj.getJsonNumber(IWLLocation.INFO_JSON_LATCOORD_KEY).doubleValue());
    return this;
  }

  //final public HBCoords getHBCoords() {
  //  return this.hbCoords;
  //}

  //final public double getZcVsVertDatum() {
  //  return this.zcVsVertDatum;
  //}
}
