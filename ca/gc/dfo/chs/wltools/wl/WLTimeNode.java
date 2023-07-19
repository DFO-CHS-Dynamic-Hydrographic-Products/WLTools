package ca.gc.dfo.iwls.fmservice.modeling.wl;

/**
 *
 */

//---

import ca.gc.dfo.iwls.fmservice.modeling.util.TimeNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

//---
//---

/**
 * Class WLTimeNode contains a private List of WLStationTimeNode objects having all the same time-stamp.
 */
final public class WLTimeNode extends TimeNodeFactory implements IWL {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
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
  public WLTimeNode(final WLTimeNode pstr, @NotNull @Size(min = 1) final List<WLStationTimeNode> wlsda) {
    
    //---  NOTE: it is mandatory that all WLStationTimeNode Objects of wlsda have the same time-stamp
    super(wlsda.get(0).dbData[PREDICTION].seconds());
    
    this.log.debug("WLTimeNode constructor: this.sse dt=" + this.sse.dateTimeString(true));
    
    this.pstr = pstr;
    this.stationsNodes = new ArrayList<>(wlsda.size());
    
    int stnIdx = 0;
    
    for (final WLStationTimeNode wlsd : wlsda) {
      
      //--- Check if we have time-synchronization between stations:
      if (wlsd.dbData[PREDICTION].seconds() != this.sse.seconds()) {
        
        this.log.error("WLTimeNode constructor: wlsd.wlpr.seconds() != this.sse.seconds() !, stnIdx=" + stnIdx);
        throw new RuntimeException("WLTimeNode constructor");
      }
      
      //--- And check if the station data is also time-synchronized with itself
      if (!wlsd.checkTimeSync()) {
        
        this.log.error("WLTimeNode constructor: wlsd.checkTimeSync() == false !, stnIdx=\"+ Integer.toString(stnIdx) ");
        throw new RuntimeException("WLTimeNode constructor");
      }
      
      //--- Set the sse of the WLStationTimeNode as a reference to this.sse
      //    (No need to create a new Object with the same time info it it)
      wlsd.setSseRef(this.sse);
      
      if (pstr != null) {
        
        //--- Fool-proofpast vs future time-stamps ordering validation:
        if (pstr.seconds() >= this.seconds()) {
          
          this.log.error("WLTimeNode constructor: pstr.seconds() >= this.seconds() !");
          throw new RuntimeException("WLTimeNode constructor");
        }
      }
      
      this.log.debug("WLTimeNode constructor : wlsd=" + wlsd);
      this.log.debug("WLTimeNode constructor : wlsd dt=" + wlsd.getSse().dateTimeString(true));
      
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
  public final WLStationTimeNode getStationNode(@Min(0) final int stationIndex) {
    
    this.log.debug("WLTimeNode getStationNode: stationIndex=" + stationIndex);
    
    WLStationTimeNode ret = null;
    
    try {
      ret = this.stationsNodes.get(stationIndex);
      
    } catch (ArrayIndexOutOfBoundsException e) {
      
      e.printStackTrace();
      throw new RuntimeException("WLTimeNode getStationNode");
    }
    
    return ret;
  }
}
