//package ca.gc.dfo.iwls.fmservice.modeling.util;
package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

//---
//import java.util.List;
//import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;

//---

/**
 * abstract base class used by child classes needing a time-stamp and an access(via references) to past and future
 * TimeNodeFactory neighbors.
 * TODO: Implement a abstract template version of TimeNodeFactory.
 */
abstract public class TimeNodeFactory implements ITimeMachine {

  /**
   * Private static Logger
   */
  private static final Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Wraps a GregorianCalendar object compatible with both Java-1.7 and 1.8.
   */
  protected SecondsSinceEpoch sse= null;

  /**
   * Reference to a TimeNodeFactory  which is just before in time(i.e. this.sse.seconds() is larger than this.pstr
   * .seconds()).
   */
  protected TimeNodeFactory pstr= null;

  /**
   * Reference to a TimeNodeFactory  which is just after in time(i.e. this.sse.seconds() is smaller than this.futr
   * .seconds()).
   */
  protected TimeNodeFactory futr= null;

  /**
   * @param seconds : The seconds since epoch used to construct the underlying GregorianCalendar object this.sse.
   */
  //public TimeNodeFactory(@Min(0) final long seconds) {
  public TimeNodeFactory(final long seconds) {
    this();
    this.sse= new SecondsSinceEpoch(seconds);
  }

  //--- constructors:
  public TimeNodeFactory() {
    this.sse = null;
    this.pstr = this.futr = null;
  }

  //--- constructor kept for possible future usage.
//    public TimeNodeFactory(@NotNull final TimeNodeFactory wln) {
//        this(wln,false);
//    }

  /**
   * @param sse  : Already existing SecondsSinceEpoch object.
   * @param pstr : (Could be null) Reference to an already existing TimeNodeFactory  which is just before in time(i.e
   *             . this.sse.seconds() is larger than this.pstr.seconds()).
   * @param futr : (Could be null) eference to a TimeNodeFactory  which is just after in time(i.e. this.sse.seconds()
   *             is smaller than this.futr.seconds()).
   */
  //public TimeNodeFactory(@NotNull final SecondsSinceEpoch sse, final TimeNodeFactory pstr, final TimeNodeFactory futr) {
  public TimeNodeFactory(final SecondsSinceEpoch sse, final TimeNodeFactory pstr, final TimeNodeFactory futr) {

    //--- Only a reference here
    this.sse= sse;

    this.setRefs(pstr, futr);
  }

  //--- constructor for possible future usage.
  //    public TimeNodeFactory(@NotNull final TimeNodeFactory wln, final TimeNodeFactory pstr, final TimeNodeFactory
  //    futr) {
  //        this(wln,false);
  //
  //        this.setRefs(pstr,futr);
  //    }
  //--- constructor for possible future usage.
//    @NotNull
//    public TimeNodeFactory(@NotNull final TimeNodeFactory wln, final boolean newSseObj) {
//
//        this();
//
//        if (wln.sse==null) {
//            this.log.error("TimeNodeFactory constructor: wln.sse==null !");
//            throw new RuntimeException("TimeNodeFactory constructor");
//        }
//
//        this.sse= (newSseObj ? new SecondsSinceEpoch(wln.sse) : wln.sse );
//
//        this.setRefs(wln.pstr,wln.futr);
//    }

  /**
   * Set the pstr and futr TimeNodeFactory time neighbors.
   *
   * @param pstr : Another TimeNodeFactory object just before in time.
   * @param futr : Another TimeNodeFactory object just after in time.
   * @return this TimeNodeFactory.
   */
  //@NotNull
  protected TimeNodeFactory setRefs(final TimeNodeFactory pstr, final TimeNodeFactory futr) {

    final String mmi= "setRefs: ";

    if (pstr == this) {
      slog.error(mmi+"pstr==this !");
      throw new RuntimeException(mmi);
    }

    if (futr == this) {
      slog.error(mmi+"futr==this !");
      throw new RuntimeException(mmi);
    }

    if ((pstr != null) && (futr != null)) {

      if (pstr == futr) {

        slog.error(mmi+"pstr==futr !");
        throw new RuntimeException(mmi);
      }
    }

    //--- Past TimeNodeFactory Object reference(could be null):
    this.pstr= pstr;

    if (this.pstr != null) {

      //--- Verify time-stamps order validity:
      if (this.pstr.sse.seconds() >= this.sse.seconds()) {

        slog.error(mmi+"this.pstr.sse.seconds() >= this.sse.seconds() !");
        throw new RuntimeException(mmi);
      }

      //--- Set past Object future reference as this TimeNodeFactory.
      this.pstr.futr= this;

      slog.info(mmi+"this=" + this);
      slog.info(mmi+"this dt=" + this.sse.dateTimeString(true));

      slog.info(mmi+"this.pstr=" + this.pstr);
      slog.info(mmi+"this.pstr.futr=" + this.pstr.futr);
    }

    //--- future TimeNodeFactory Object reference(could be null):
    this.futr= futr;

    if (this.futr != null) {

      //--- Verify time-stamps order validity:
      if (this.futr.sse.seconds() <= this.sse.seconds()) {

        slog.error(mmi+"this.futr.sse.seconds() <= this.sse.seconds() !");
        throw new RuntimeException(mmi);
      }

      //--- Set future Object past reference as this TimeNodeFactory.
      this.futr.pstr = this;
    }

    return this;
  }

  /**
   * Find a TimeNodeFactory having a specific time-stamp in the past using the pstr reference recursively.
   * NOTE: Recursive method.
   *
   * @param seconds : The specific time-stamp wanted.
   * @return TimeNodeFactory if found.
   */
  //public final TimeNodeFactory findInPastR(@Min(0) final long seconds) {
  public final TimeNodeFactory findInPastR(final long seconds) {

   final String mmi= "findInPastR: "

    if (seconds > this.sse.seconds()) {

      this.log.error(mmi+seconds dt=" +
          SecondsSinceEpoch.dtFmtString(seconds, true) + " > this.sse.seconds() dt=" + this.sse.dateTimeString(true) + " !");

      throw new RuntimeException(mmi);
    }

//        this.log.debug("TimeNodeFactory findInPastR: this="+this+", this dt="+this.sse.dateTimeString());
//        this.log.debug("TimeNodeFactory findInPastR: this.pstr=="+this.pstr);
//        this.log.debug("TimeNodeFactory findInPastR: seconds dt="+SecondsSinceEpoch.dtFmtString(seconds,true));

    TimeNodeFactory ret = null;

    if (this.sse.seconds() == seconds) {
      ret = this;

    } else if (this.pstr != null) {
      //--- continue the search with the previous(in time) TimeNodeFactory object this.pstr.
      ret = this.pstr.findInPastR(seconds);
    }

    return ret;
  }

  /**
   * @return The TimeNodeFactory object just after in time.
   */
  final public TimeNodeFactory getFutr() {
    return this.futr;
  }

  /**
   * @return The TimeNodeFactory object just before in time.
   */
  final public TimeNodeFactory getPstr() {
    return this.pstr;
  }

  /**
   * @return The SecondsSinceEpoch of this TimeNodeFactory
   */
  final public SecondsSinceEpoch getSse() {
    return this.sse;
  }

  /**
   * @return long : The seconds since the epoch returned by this SecondsSinceEpoch.
   */
  final public long seconds() {
    return this.sse.seconds();
  }

  /**
   * Set this.sse as a reference to an already existing SecondsSinceEpoch object.
   *
   * @param sse : SecondsSinceEpoch object.
   * @return The same SecondsSinceEpoch object.
   */
  //@NotNull
  //final public SecondsSinceEpoch setSseRef(@NotNull final SecondsSinceEpoch sse) {
  final public SecondsSinceEpoch setSseRef(final SecondsSinceEpoch sse) {

    return (this.sse = sse);
  }

  //--- for possible future usage.
//    public static final boolean checkTimeSync(@NotNull final TimeNodeFactory tn1, @NotNull final TimeNodeFactory
//    tn2) {
//
//        if (tn1==tn2) {
//            staticLogger.warn("TimeNodeFactory checkTimeSync: tn1==tn2 !");
//        }
//
//        return tn1.checkTimeSync(tn2);
//    }
  //--- for possible future usage
//    @NotNull
//    public final boolean checkTimeSync(@NotNull final TimeNodeFactory other) {
//
////        if (other==null) {
////            staticLogger.error("TimeNodeFactory checkTimeSync:other==null !");
////            throw new RuntimeException("TimeNodeFactory checkTimeSync");
////        }
//
//        return (this.seconds() == other.seconds());
//    }
  //--- for possible future usage
//    public final static TimeNodeFactory find(@NotNull final SecondsSinceEpoch sse, @NotNull @Size(min=1) final
//    List<TimeNodeFactory> bunch) {
//        return find(sse.seconds(),bunch);
//    }
//
//    public final static TimeNodeFactory find(@Min(0) final long seconds, @NotNull @Size(min=1) final
//    List<TimeNodeFactory> bunch) {
//
//        TimeNodeFactory ret= null;
//
//        for (final TimeNodeFactory tn : bunch) {
//
//            if (tn.seconds() == seconds) {
//                ret= tn;
//                break;
//            }
//        }
//
//        return ret;
//    }
  //--- commented for possible future usage
//    public final TimeNodeFactory findR(@Min(0) final long seconds) {
//
//        TimeNodeFactory ret = null;
//
//        if (seconds == this.sse.seconds()) {
//            ret = this;
//        } else if (seconds < this.sse.seconds()) {
//            ret = this.pstr.findInPastR(seconds);
//        } else {
//            ret = this.futr.findInFutureR(seconds);
//        }
//
//        return ret;
//    }
  //--- commented for possible future usage
//    public final TimeNodeFactory findInFuture(@NotNull final SecondsSinceEpoch sse) {
//        return this.findInFuture(sse.seconds());
//    }

//--- commented for possible future usage
//    public final TimeNodeFactory findInFuture(@Min(0) final long seconds) {
//
//        TimeNodeFactory ret = null;
//        TimeNodeFactory ftr = this.futr;
//
//        if (seconds < this.sse.seconds()) {
//            this.log.error("TimeNodeFactory findInFuture: seconds < this.sse.seconds() !");
//            throw new RuntimeException("TimeNodeFactory findInFuture");
//        }
//
//        while (ret == null) {
//
//            ret = (ftr.seconds() == seconds) ? ftr : null;
//
//            //--- Get the next future TimeNode Object. Stop if it is null;
//            if ((ftr = ftr.futr) == null) break;
//        }
//
//        return ret;
//    }

//    //---commented for possible future usage
//    public final TimeNodeFactory findInPast(@NotNull final SecondsSinceEpoch sse) {
//        return this.findInPast(sse.seconds());
//    }
  ////--- commented for possible future usage
//    public final TimeNodeFactory findInFutureR(@Min(0) final long seconds) {
//
//        if (seconds < this.sse.seconds()) {
//            this.log.error("TimeNodeFactory findInFutureR: seconds < this.sse.seconds() !");
//            throw new RuntimeException("TimeNodeFactory findInFutureR");
//        }
//
//        TimeNodeFactory ret = null;
//
//        if (this.sse.seconds() == seconds) {
//            ret = this;
//        } else if (this.futr != null) {
//
//            //--- continue the search with the next one:
//            ret = this.futr.findInFutureR(seconds);
//        }
//
//        return ret;
//    }
//    //--- commented for possible future usage
//    public final TimeNodeFactory findInPast(@Min(0) final long seconds) {
//
//        TimeNodeFactory ret= null;
//        TimeNodeFactory psr= this.pstr;
//
//        //this.log.debug("findInPast: seconds="+seconds+", psr="+psr+", this.sse="+this.sse);
//
//        if (seconds > this.sse.seconds()) {
//            this.log.error("TimeNodeFactory findInPast: seconds > this.sse.seconds() !");
//            throw new RuntimeException("TimeNodeFactory findInPast");
//        }
//
//        if (psr!=null) {
//
//            while (ret == null) {
//
//                ret= (psr.seconds() == seconds()) ? psr : null;
//
//                //--- Get the previous past WLSser Object. Stop if it is null;
//                if ((psr= psr.pstr) == null) break;
//            }
//        }
//
//        return ret;
//    }

//
//    public final static TimeNodeFactory findNearestInFuture(@Min(0) final SecondsSinceEpoch sse, @NotNull final
//    TimeNodeFactory start) {
//
//        TimeNodeFactory ret = null;
//        TimeNodeFactory ftr = start.futr;
//
//        if (sse.seconds() < start.sse.seconds()) {
//            staticLogger.error("TimeNodeFactory findNearestInFuture: sse.seconds() < start.sser.seconds() !");
//            throw new RuntimeException("TimeNodeFactory findNearestInFuture");
//        }
//
//        while (ret == null) {
//
//            ret = (ftr.seconds() >= sse.seconds() ? ftr : null);
//
//            //--- Get the previous past WLSser Object. Stop if it is null;
//            if ((ftr = ftr.futr) == null) break;
//        }
//
//        return ret;
//    }
//
//    public final static TimeNodeFactory findNearestInPast(@NotNull final SecondsSinceEpoch sse, @NotNull final
//    TimeNodeFactory start) {
//
//        TimeNodeFactory ret = null;
//        TimeNodeFactory psr = start.pstr;
//
//        if (sse.seconds() > start.sse.seconds()) {
//            staticLogger.error("TimeNodeFactory findNearestInPast sse.seconds() > start.sse.seconds() !");
//            throw new RuntimeException("TimeNodeFactory findNearestInPast");
//        }
//
//        while (ret == null) {
//
//            ret = (psr.seconds() <= sse.seconds() ? psr : null);
//
//            //--- Get the previous past WLSser Object. Stop if it is null;
//            if ((psr = psr.pstr) == null) break;
//        }
//
//        return ret;
//    }
//
//    public final static List<TimeNodeFactory> getReverseRefs(@NotNull @Size(min = 1) final List<TimeNodeFactory>
//    tnl) {
//
//        final List<TimeNodeFactory> ret = new ArrayList<>(tnl.size());
//
//        for (int rvIt = tnl.size() - 1; rvIt >= 0; rvIt--) {
//
//            //--- NOTE: no new object creation, only references here:
//            ret.add(tnl.get(rvIt));
//        }
//
//        return ret;
//    }
//--- method kept for possible future usage
//    @NotNull
//    @Size(min=1)
//    protected final static List<TimeNodeFactory> setAllRefs(@NotNull @Size(min=1) final List<TimeNodeFactory> tnl) {
//
//        if (tnl.size() < 2) {
//            staticLogger.warn("TimeNodeFactory setAllRefs: tnl.size() < 2 ! Nothing to do here !");
//
//        } else {
//
//            //--- 0th WLSser:
//            TimeNodeFactory pstrr= tnl.get(0);
//
//            pstrr.pstr= null;
//            pstrr.futr= tnl.get(1);
//
//            int aftIdx= 2;
//
//            for (final TimeNodeFactory tn : tnl.subList(1,tnl.size()-1)) {
//
//                tn.pstr= pstrr;
//                tn.futr= tnl.get(aftIdx++);
//
//                if (tn.pstr.seconds() >= tn.seconds()) {
//                    staticLogger.error("TimeNodeFactory setAllRefs: tn.pstr.seconds() >= tn.seconds() !");
//                    throw new RuntimeException("TimeNodeFactory setAllRefs");
//                }
//
//                if (tn.futr.seconds() <= tn.seconds()) {
//                    staticLogger.error("TimeNodeFactory setAllRefs: tn.futr.seconds() <= tn.seconds() !");
//                    throw new RuntimeException("TimeNodeFactory setAllRefs");
//                }
//
//                //--- pstrr now needs to point to the current TimeNode for the next loop iteration:
//                pstrr= tn;
//            }
//
//            //--- final TimeNode:
//            tnl.get(tnl.size()-1).pstr= tnl.get(tnl.size()-2);
//            tnl.get(tnl.size()-1).futr= null;
//        }
//
//        return tnl;
//    }

}
