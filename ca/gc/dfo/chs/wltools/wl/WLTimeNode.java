//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

/**
 *
 */

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;


//---
import ca.gc.dfo.chs.wltools.util.TimeNodeFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.util.TimeNodeFactory;

//---
//---

/**
 * Class WLTimeNode contains a private List of WLStationTimeNode objects having all the same time-stamp.
 */
final public class WLTimeNode extends TimeNodeFactory implements IWL {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.WLTimeNode";

  /**
   * log utility.
   */
  private static final Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * List which contains one or more reference(s) to already existing WLStationTimeNode objects.
   */
  private List<WLStationTimeNode> stationsNodes = null;

//    public WLTimeNode() {
//        this(0L);
//    }

//    public WLTimeNode(final long seconds) {
//
//        super(seconds);
//
//        this.pstr= null;
//        this.stationsNodes= new ArrayList<>(DEFAULT_NB_STATIONS);
//    }

  /**
   * @param pstr  : (could be null)WLTimeNode object which is just before(i.e. in the past) in time from the new
   *              WLTimeNode.
   * @param wlsda : A list of already existing WLStationTimeNode objects.
   */
  public WLTimeNode(final WLTimeNode pstr,
                    /*@NotNull @Size(min = 1)*/ final List<WLStationTimeNode> wlsda) {

    //---  NOTE: it is mandatory that all WLStationTimeNode Objects of wlsda have the same time-stamp
    super(wlsda.get(0).wlmData[PREDICTION].seconds());

    final String mmi= "WLTimeNode main constructor: ";

    slog.debug(mmi+"this.sse dt=" + this.sse.dateTimeString(true));

    this.pstr= pstr;
    this.stationsNodes= new ArrayList<WLStationTimeNode>(wlsda.size());

    int stnIdx = 0;

    for (final WLStationTimeNode wlsd : wlsda) {

      //--- Check if we have time-synchronization between stations:
      if (wlsd.wlmData[PREDICTION].seconds() != this.sse.seconds()) {

        slog.error(mmi+"wlsd.wlpr.seconds() != this.sse.seconds() !, stnIdx=" + stnIdx);
        throw new RuntimeException(mmi);
      }

      //--- And check if the station data is also time-synchronized with itself
      if (!wlsd.checkTimeSync()) {

        slog.error(mmi+"wlsd.checkTimeSync() == false !, stnIdx=\"+ Integer.toString(stnIdx) ");
        throw new RuntimeException(mmi);
      }

      //--- Set the sse of the WLStationTimeNode as a reference to this.sse
      //    (No need to create a new Object with the same time info it it)
      wlsd.setSseRef(this.sse);

      if (pstr != null) {

        //--- Fool-proofpast vs future time-stamps ordering validation:
        if (pstr.seconds() >= this.seconds()) {

          slog.error(mmi+"pstr.seconds() >= this.seconds() !");
          throw new RuntimeException(mmi);
        }
      }

      slog.info(mmi+"wlsd=" + wlsd);
      slog.info(mmi+"wlsd dt=" + wlsd.getSse().dateTimeString(true));

      this.stationsNodes.add(wlsd);

      stnIdx++;
    }
  }

  /**
   * Return the WLStationTimeNode at index stationIndex in this.stationsNodes List.
   *
   * @param stationIndex : index in this.stationsNodes List.
   * @return WLStationTimeNode at index stationIndex.
   */
  public final WLStationTimeNode getStationNode(/*@Min(0)*/ final int stationIndex) {

    final String mmi="getStationNode: ";

    slog.info(mmi+"stationIndex=" + stationIndex);

    WLStationTimeNode ret= null;

    try {
      ret= this.stationsNodes.get(stationIndex);

    } catch (ArrayIndexOutOfBoundsException e) {

      e.printStackTrace();
      throw new RuntimeException(mmi);
    }

    return ret;
  }
}
