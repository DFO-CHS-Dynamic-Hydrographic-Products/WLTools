//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

/**
 *
 */

//---
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.wl.fms.FMSAuxCov;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;

//import javax.validation.constraints.Size;
//---

/**
 * Generic class for temporal errors covariances computations.
 */
abstract public class FMSCov implements IFMS {

  private static final whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.FMSCov";

  /**
   * static log utility.
   */
  private static final Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * List(min. size==1) of auxiliary temporal errors covariances objects.
   */
  //@Min(1)
  protected List<FMSAuxCov> auxCovs= null; //--- Contains the auxiliary covariance relations for a given WL station.

  /**
   * Default constructor.
   */
  public FMSCov() {
    this.auxCovs = null;
  }

  /**
   * @param nbAuxCovs : Number(min.==1) of auxiliary temporal errors covariances objects used.
   */
  public FMSCov(/*@Min(1)*/ final int nbAuxCovs) {

    //this.log.debug("Start FMCov constr., nbAuxCovs="+Integer.toString(nbAuxCovs));

    this.auxCovs= new ArrayList<FMSAuxCov> (nbAuxCovs);

    //this.log.debug("End  FMCov constr.");
  }

  /**
   * @return The Number of auxiliary temporal errors covariances objects used.
   */
  //@Min(1)
  public final int auxCovSize() {
    return this.auxCovs.size();
  }

  /**
   * Check if we have some auxiliary temporal errors covariances objects duplicates before starting computations.
   *
   * @param stationCode : Usual CHS TG station string Id.
   * @return false is no FMSAuxCov duplicate have been found, true otherwise.
   */
  protected final boolean gotDuplicate(/*@NotNull*/ final String stationId) {

    final String mmi= "gotDuplicate: ";

    boolean ret = false;

    // this.log.debug("Start: stationCode="+stationCode);
    for (final FMSAuxCov fmAuxCov : this.auxCovs) {

      if (fmAuxCov.stationCovarianceId.equals(stationId)) {

        slog.warn(mmi+"Found duplicate stationId in this.auxCovs !");
        ret= true;
        break;
      }
    }

    //this.log.debug("End: stationCode="+stationCode+", ret="+ret);

    return ret;
  }

  /**
   * Set the residual object reference of all the auxiliary temporal errors covariances objects in this.auxCovs.
   *
   * @param stationCode   : Usual CHS TG station string Id.
   * @param residualsList : A List of IFMResidual objects where to find the searched objects.
   * @return this FMSCov object.
   */
  //@NotNull
  protected final FMSCov setAuxCovsResiduals(/*@NotNull*/ final String stationId,
                                             /*@NotNull*/ final List<IFMSResidual> residualsList) {
    final String mmi= "setAuxCovsResiduals: ";

    slog.info(mmi+"Setting auxiliary residuals references for station: "+stationId+
              ", this.auxCovs.size()=" + this.auxCovs.size() + ", residualsList.size()=" + residualsList.size());

    for (final FMSAuxCov fmAuxCov : this.auxCovs) {

      final String stationCovarianceId = fmAuxCov.getStationCovarianceId();

      slog.info(mmi+"Processing stationCovarianceId=" + stationCovarianceId);

      for (final IFMSResidual ifmResidual: residualsList) {

        fmAuxCov.residual= null;

        final FMSResidualFactory fmResidualFactory= ifmResidual.getFMSResidualFactory();

        if ( stationCovarianceId.equals(fmResidualFactory.getStationId()) ) {

          slog.info("Setting residual covariance item: " + stationCovarianceId + " for station: " + stationId);

          fmAuxCov.residual= fmResidualFactory;
          break;
        }
      }
    }

    //--- Verify that all the residual objects references wanted have been found.
    //    If a residual objects reference is missing then simply remove the corresponding
    //    FMAuxCov object from this.auxCovs List and log this error but nonetheless keeping execution going on.
    for (final FMSAuxCov fmAuxCov: this.auxCovs) {

      final String stationCovarianceId= fmAuxCov.getStationCovarianceId();

      if (fmAuxCov.residual == null) {

        slog.info(mmi+"fmAuxCov.residual==null for stationCovarianceId="+
                  stationCovarianceId + " for station: " + stationId);

        slog.info(mmi+"Removing covariance item: " + stationCovarianceId +" from station:"+
                  stationId+ " covariance computations which incidently will not be optimal !");

        this.auxCovs.remove(fmAuxCov);
      }
    }

    slog.info(mmi+"Got "+this.auxCovs.size()+
              " valid auxiliary covariance item(s) for station:" + stationId);

    return this;
  }
}
