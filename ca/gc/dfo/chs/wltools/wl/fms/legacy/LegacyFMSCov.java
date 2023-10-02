//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

/**
 *
 */

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.WLZE;
import ca.gc.dfo.chs.wltools.wl.fms.FMSCov;
import ca.gc.dfo.chs.wltools.wl.fms.FMSAuxCov;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.numbercrunching.D1Data;
import ca.gc.dfo.chs.wltools.numbercrunching.ScalarOps;
import ca.gc.dfo.chs.wltools.wl.fms.FMSStationCovarianceConfig;

//---
//import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSAuxCov;
//import ca.gc.dfo.iwls.fmservice.modeling.fms.FMSCov;
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D1Data;
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.ScalarOps;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLZE;
//import ca.gc.dfo.iwls.modeling.fms.StationCovariance;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;

//---
//---
//---
//---
//---
//import ca.gc.dfo.iwls.fmservice.modeling.fms.IFM;
//---

/**
 * Class used for the computation of temporal errors covariances computations in the context of the Legacy method.
 */
final public class LegacyFMSCov extends FMSCov implements ILegacyFMS {

  final private static String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.legacy";

  /**
   * static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * fall-back model error. It is simply named "e" in C struct model_status defined
   * in source file model.h from the legacy ODIN-DVFM 1990 kit.
   */
  final protected double squareFallBackError;

  /**
   * A vector(which could be unary if we only have one auxiliary covariance item) to store OLS regression parameter(s).
   */
  protected D1Data beta = null;

  /**
   * @param fallBackError          : To set this.fallBackError. It is defined in the Residual configuration object.
   * @param stationCode            : The CHS TG station usual string id.
   * @param stationsCovarianceCfgList A List of FMSStationCovarianceConfig configuration object(s).
   */
  public LegacyFMSCov(final double fallBackError, /*@NotNull*/ final String stationId,
                      /*@NotNull @Size(min = 4) */ final List<FMSStationCovarianceConfig> stationsCovarianceCfgList) {

    super(stationsCovarianceCfgList.size());

    final String mmi= "LegacyFMSCov main constructor: ";

    this.squareFallBackError= ScalarOps.square(fallBackError);

    this.beta= new D1Data(stationsCovarianceCfgList.size(), 0.0);

    slog.info(mmi+"start");

    final FMSStationCovarianceConfig stationCovarianceCfg0 = stationsCovarianceCfgList.get(0);

    final String firstStationInList= stationCovarianceCfg0.getStationId();

    if (!firstStationInList.equals(stationId)) {

      slog.error(mmi+"The first auxiliary covariance station found in stationsCovarianceCfgList MUST be the same as the station being processed !");
      slog.error(mmi+"Found -> " + firstStationInList + " as first station instead of -> " + stationId + " as it should be");

      throw new RuntimeException(mmi+"Cannot update forecast !!");
    }

    for (final FMSStationCovarianceConfig sc : stationsCovarianceCfgList) {

      final String auxCovStationId= sc.getStationId();

      if (this.gotDuplicate(auxCovStationId)) {

        slog.info(mmi+"Found TG station duplicate :" + auxCovStationId +
                  " in stationsCovarianceList ! Ignoring this TG station duplicate");
      } else {

        slog.info(mmi+"Adding TG station:" + auxCovStationId + " to this.auxCovs");

        this.auxCovs.
          add(new LegacyFMSAuxCov((int)sc.getTimeLagMinutes(), sc.getFallBackCoeff(), auxCovStationId) );
      }
    }

    //--- Verify if the processed station is itself in this.auxCovs.
    //    If it is not there then log a warning and add it with default auxiliary covariance parameters
    if (!this.gotDuplicate(stationId)) {

      slog.info(mmi+"The TGstation " + stationId +
          " was not found in stationsCovarianceList then add it to this.auxCovs with default auxiliary covariance parameters");

      this.auxCovs.
        add(new LegacyFMSAuxCov(DEFAULT_AUX_COV_TIME_LAG_MINUTES, DEFAULT_AUX_COV_FALL_BACK_COEFF, stationId));
    }

    slog.info(mmi+"nb. aux. cov=" + this.auxCovs.size());
  }

  /**
   * Compute an estimated WL direct surge component with the cumulative WL residuals errors statistics.
   *
   * @param surgeWeight    : The WL surge time weight value to use.
   * @param errorWeight    : The WL surge error time weight value to use.
   * @param eps            : The weight accumulator of unaccounted variance to use. Attribute of the class
   *                       LegacyResidualData.
   * @param zX             : The auxiliary WL direct surge(s) components used for temporal errors covariances.
   * @param errBeta        :  The auxiliary WL surge component(s) error(s) estimated with OLS regression.
   * @param estimatedSurge : The WLZE object for the estimated WL direct surge component.
   * @return The WLZE object with the newly estimated WL direct surge component.
   */
  //@NotNull
  public final WLZE computeEstimatedSurge(final double surgeWeight,
                                          final double errorWeight,
                                          final double eps,
                                          /*@NotNull*/ final D1Data zX,
                                          /*@NotNull*/ final D1Data errBeta,
                                          /*@NotNull*/ final WLZE estimatedSurge) {

    final String mmi= "computeEstimatedSurge: ";

    if (zX.size() != this.auxCovs.size()) {

      slog.error(mmi+"zX.size() != this.auxCovs.size()");

      throw new RuntimeException(mmi+"Cannot update forecast !!");
    }

    slog.info(mmi+"surgeWeight=" + surgeWeight);
    slog.info(mmi+"errorWeight=" + errorWeight);
    slog.info(mmi+"eps=" + eps);

    //--- Init local estimated surge value and error accumulators
    double value= 0.0;

    //--- Init surge error according to Equation 4.16 of the original DVFM documentation:
    double error= errorWeight * ScalarOps.square(eps) +
      (1.0 - errorWeight) * this.squareFallBackError;  //ScalarOps
    // .square(this.fallBackError);

    //--- The following loop is the equivalent of the loop used to compute the estimated surge value
    //    AND the estimated surge error in the C function apply_model of the source file residual.c
    //    from the 1990 legacy DVFM kit. (It is the implementation of the equation 4.15 of the DVFM documentation.)
    //
    //    i.e. :
    //    a_surge.value = 0;
    //
    //    for (i = 0; i < model_status.m; i++)
    //       a_surge.value +=  (X[i] * (w * model_status.X[i].beta +
    //		              (1 - w) * model_status.X[i].b));	/* Equation 4.15 */

    //--- d: data array index counter
    int d = 0;

    for (final FMSAuxCov fmsAuxCov : this.auxCovs) {

      final LegacyFMSAuxCov legacyFMSAuxCov = (LegacyFMSAuxCov) fmsAuxCov;

      final double fallBack= legacyFMSAuxCov.getFallBack();

      //--- Here we follow the Legacy DVFM C source code for the surge weight computation.
      //    TODO(in a new FMS residual package) : Get rid of the legacyFMSAuxCov.fallBack parameter ??.
      final double weight= fallBack + surgeWeight * (this.beta.at(d) - fallBack);

      final double longTermWLOffset= fmsAuxCov.getFMSLongTermWLOffset();

      slog.info(mmi+"d=" + d);
      slog.info(mmi+"fallBack=" + fallBack);
      slog.info(mmi+"this.beta.at(d)=" + this.beta.at(d));
      slog.info(mmi+"weight=" + weight);
      slog.info(mmi+"zX.at(d)=" + zX.at(d));
      slog.info(mmi+"fmsAuxCov.getFMSLongTermWLOffset()=" + longTermWLOffset);

      //--- G. Mercier 2017-12 addition to the Legacy DVFM method:
      //    Remove the corresponding long term surge from zX.at(d) to get only the short term(storm or outflow) surge
      //    effect here:
      //    (NOTE: This removal slows down the total surge signal decay when the long term surge is significant)
      value += weight * (zX.at(d) - longTermWLOffset);

      //--- NOTE: The d counter incrementation.
      error += ScalarOps.square(weight * errBeta.at(d++));
    }

    slog.info(mmi+"value=" + value);
    slog.info(mmi+"error=" + error);

    //---
    return estimatedSurge.set(value, error);
  }

  /**
   * Retreive previously computed direct surge(s) and the associated error(s) from the auxiliary temporal errors
   * covariances data:
   *
   * @param seconds : The sse time stamp wanted for the auxiliary temporal errors covariances data
   * @param zX      : The auxiliary WL direct surge(s) components used for temporal errors covariances.
   * @param errBeta :  The auxiliary WL surge component(s) error(s) estimated with OLS regression.
   * @return The D1Data object zX. NOTE: the errBeta object is also modified by this method.
   */
  //@NotNull
  public final D1Data getAxSurgesErrsInZX(/*@Min(0)*/  final long seconds,
                                          /*@NotNull*/ final D1Data zX,
                                          /*@NotNull*/ final D1Data errBeta) {

    final String mmi= "getAxSurgesErrsInZX: ";

    slog.info(mmi+"start: seconds dt=" + SecondsSinceEpoch.dtFmtString(seconds, true));

    //--- Un-comment the following two if blocks for debugging purposes.
//        if (zX.size() != errBeta.size()) {
//            this.log.error("LegacyFMCov getAxSurgesErrsInZX: zX.size() != err.size() !");
//            throw new RuntimeException("LegacyFMCov getAxSurgesErrsInZX");
//        }
//
//        if (zX.size() < this.auxCovs.size())  {
//            this.log.error(" LegacyFMCov getAxSurgesErrsInZX: zX.size() < this.auxCovs.size() !");
//            throw new RuntimeException("LegacyFMCov getAxSurgesErrsInZX");
//        }

    zX.init(0.0);
    errBeta.init(0.0);

    int d= 0;

    //--- Populate zX vector with auxiliary covariance surge(s) data already computed.
    for (final FMSAuxCov fmsAuxCov : this.auxCovs) {

      final WLStationTimeNode pastWlsn= fmsAuxCov.getLagFMSWLStationTimeNode(seconds);

      slog.info(mmi+"pastWlsn=" + pastWlsn);

      if (pastWlsn != null) {

//                this.log.debug("LegacyFMCov getAxSurgesErrsInZX: wlsn!=null seconds dt="+ SecondsSinceEpoch
//                .dtFmtString(seconds,true) +", pastWlsn dt="+pastWlsn.getSse().dateTimeString(true));
//                this.log.debug("LegacyFMCov getAxSurgesErrsInZX: wlsn.surge().zw()="+pastWlsn.surge().zw()+", wlsn
//                .surge().error()="+pastWlsn.surge().error());
//                this.log.debug("LegacyFMCov getAxSurgesErrsInZX: wlsn.get(WLType.PREDICTION).zDValue()="+pastWlsn
//                .get(WLType.PREDICTION).zDValue());
//                this.log.debug("LegacyFMCov getAxSurgesErrsInZX: wlsn.get(WLType.PREDICTION).seconds dt="+
//                SecondsSinceEpoch.dtFmtString(pastWlsn.get(WLType.PREDICTION).seconds(),true));

        //--- Put direct surge value in zX vector:
        zX.put(pastWlsn.getSurgeZw(), d);

        //--- Put direct surge error in err vector and increment d index for the next iteration:
        errBeta.put(pastWlsn.getSurgeError(), d++);

      } else {

        slog.info(mmi+"pastWlsn==null for seconds dt=" + SecondsSinceEpoch.dtFmtString(seconds, true));
      }
    }

    slog.info(mmi+"zX=" + zX.toString());
    //this.log.debug("err="+err.toString());

    slog.info(mmi+"end");

    return zX;
  }

  /**
   * Get the valid auxiliary valid surge(s) components in the zX D1Data object.
   *
   * @param seconds :The sse time stamp wanted for the auxiliary temporal errors covariances data
   * @param zX      : the zX D1Data object to populate which is modified by this method.
   * @return true if no problem have been found, false otherwise.
   */
  public final boolean getValidAxSurgesInZX(/*@Min(0)*/ final long seconds, /*@NotNull*/ final D1Data zX) {

    final String mmi= "getValidAxSurgesInZX: ";

    //--- Un-comment the following if block for debugging purposes.
//        if (zX.size() < this.auxCovs.size())  {
//            this.log.debug("LegacyFMCov getValidAxSurgesInZX: zX.size() < this.auxCovs.size() !");
//            throw new RuntimeException("LegacyFMCov getValidAxSurgesInZX");
//        }

    slog.info(mmi+"start: seconds dt=" + SecondsSinceEpoch.dtFmtString(seconds, true));

    boolean ret= true;

    int d= 0;

    //--- Populate zX vector with auxiliary covariance surge data already computed.
    for (final FMSAuxCov fmsAuxCov : this.auxCovs) {

      final WLStationTimeNode wlsn= fmsAuxCov.getLagFMSWLStationTimeNode(seconds);

      slog.info(mmi+"wlsn=" + wlsn);

      if (wlsn == null) {
        ret = false;
        break;
      }

      zX.put(wlsn.getSurgeZw(), d++);
    }

    slog.info(mmi+"zX=" + zX.toString());

    return ret;
  }
}
