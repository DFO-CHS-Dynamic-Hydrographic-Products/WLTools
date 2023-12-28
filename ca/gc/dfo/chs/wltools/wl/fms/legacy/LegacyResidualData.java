//package ca.gc.dfo.iwls.fmservice.modeling.fms.legacy;
package ca.gc.dfo.chs.wltools.wl.fms.legacy;

//import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.numbercrunching.D1Data;
import ca.gc.dfo.chs.wltools.numbercrunching.D2Data;
import ca.gc.dfo.chs.wltools.numbercrunching.ScalarOps;
import ca.gc.dfo.chs.wltools.numbercrunching.D2RowMajorData;

/**
 *
 */

////---
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D1Data;
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D2Data;
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D2RowMajorData;
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.ScalarOps;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;

//---
//---

/**
 *  To store the WL residuals component data.
 */
public class LegacyResidualData implements ILegacyFMS {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.fms.legacy.LegacyResidualData";

  /**
   * static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Use time data directly as double instead of doing costly long to double casts in crunch loops.
   */
  protected double dt= 0.0;

  /**
   * Memory time scale (in seconds).
   */
  protected double tau= 0.0;

  /**
   * 1.0/tau (1/seconds units).
   */
  protected double tauInv= 0.0;

  /**
   * exp(-dt/tau) (dimensionless).
   */
  protected double alpha= 0.0;

  /**
   * 1.0/alpha (dimensionless).
   */
  protected double alphaInv= 0.0;

  /**
   * Store alpha*alpha instead of doing the same multiplication for each time-increment loop.
   */
  protected double squAlpha= 0.0;

  /**
   * 1.0/alpha*alpha
   */
  protected double squAlphaInv= 0.0;

  /**
   * weight accumulator of unaccounted variance (Using the same variable name as Legacy DVFM nomenclature).
   */
  protected double eps= 0.0;

  /**
   * Sccumulator for weights.(MUST be in double precision!)
   */
  protected double omega= 0.0;

  /**
   * Accumulator for squared weights.(MUST be in double precision!)
   */
  protected double omega2= 0.0;

  //    formula inv(X'*X)*X'y and it is an accumulator equivalent for the
  //    (m x n) x (n x 1) matrix * column vector scalar products in the Smith&Thompson
  //    theory. Note also that the variable y which is a (n x 1) vector in the Smith&Thompson
  //    algorithm is a scalar here. (See method OLSRegression below). m is the number of
  //    predictors and n is the sample size(or number of observations).
  /**
   * D1Data(vector) : It is named XpY as the covariance of predictors and predictand in Legacy DVFM src code.
   * (See D1Data.java source file itself for more detailed comments)
   */
  protected D1Data xpY= null; //--- Conceptually, it is the X'y vector of dimensions (m x 1) in the OLS regression

  // _model
  //    and (update,apply)_remnant of the source file remnant.c of legacy DVFM C source code.
  //    The developers of the DVFM  C source code should have named that variable x instead
  //    of X to avoid some confusion with the X matrix of the Smith&Thompson theory.
  //
  //    In the Smith&Thompson theory, X is a (n x m) matrix used to formally explain their
  //    iterative algorithm which does not require matrix inversion but here it is used
  //    as a (m x 1) column vector x. In fact, we do not need to have such a (n x m) X
  //    matrix to deal with because of  the iterative algorithm of Smith&Thompson but only
  //    the inv(X'*X) matrix which is alway of (m x m) dimensions (See below for invXpX attribute).
  /**
   * D1Data(vector) : zX is is the equivalent of the local variable named X in the legacy DVFM code
   * (See D1Data.java source file itself for more detailed comments).
   */
  protected D1Data zX= null; //--- zX is is the equivalent of the local variable X in the functions (update,apply)

  /**
   * D1Data(vector) : It contains auxiliary WL direct surge component(s) error(s)for the surges estimation
   * OR the OLS regression coefficients for the estimation of the tidal remnant component.
   */
  protected D1Data beta= null;

  /**
   * D1Data(vector) : Contains temporary vectors scalar products to compute OLS regressions.
   */
  protected D1Data xyScalProd= null;

  /**
   * D2Data(matrix) invXpX: Inverse of residual errors covariance matrix. Named as inv(X'*X)
   * in Smith-Thompson theory and it is used as an accumulator in their iterative algorithm.
   * Its dimensions are always (m x m)
   */
  protected D2Data invXpX= null;

  //    No need to create this temp. matrix for each computing loop.
  /**
   * D2Data(matrix) mxxtM: Temporary matrix to store M * x * x' * M matrix product.
   * See method updateInvXpx below.
   */
  protected D2Data mxxtM= null; //--- D2Data(matrix) mxxtM:  Temporary matrix to store M * x * x' * M matrix product.

//    //--- For possible future usage
//    public LegacyResidualData() {
//        super();
//    }

  /**
   * @param nbAuxCov  : Number(min.==1) of auxiliary temporal errors covariances objects used.
   * @param tauHours  : The number of hours to go back in the past.
   * @param dtMinutes : : The time increment in minutes used for the WL residuals computations.
   */
  public LegacyResidualData(/*@Min(0)*/ final int nbAuxCov,
                            /*@Min(1)*/ final double tauHours,
                            /*@Min(1)*/ final double dtMinutes) {

    final String mmi= "LegacyResidualData constructor: ";

    slog.debug(mmi+"nbAuxCov="+nbAuxCov);
    slog.debug(mmi+"tauHours=" + tauHours);
    slog.debug(mmi+"dtMinutes=" + dtMinutes);

    if (tauHours > MEMORY_TIME_SCALE_HOURS_MAX) {
      slog.debug(mmi+"tauHours > MEMORY_TIME_SCALE_HOURS_MAX !");
      throw new RuntimeException(mmi);
    }

//        if (tauHours <= 0.0) {
//            this.log.error("LegacyResidualData constructor:tauHours <= 0.0 !");
//            throw new RuntimeException("\"LegacyResidualData constructor");
//        }

    if (dtMinutes > DELTA_T_MINUTES_MAX) {
      slog.debug(mmi+"dtMinutes > DELTA_T_MINUTES_MAX !");
      throw new RuntimeException(mmi);
    }

//        if (dtMinutes <= 0.0) {
//            this.log.error("LegacyResidualData constructor: dtMinutes <= 0.0 !");
//            throw new RuntimeException("\"LegacyResidualData constructor");
//        }

    this.tau= SECONDS_PER_HOUR * tauHours;
    this.dt= SECONDS_PER_MINUTE * dtMinutes;

    this.tauInv= 1.0 / this.tau;
    this.alpha= Math.exp(-this.dt / this.tau);

    //--- this.alphaInv attribute us used to avoid re-computing the same
    //    costly division a zillion times in heavy loops.
    this.alphaInv= 1.0 / this.alpha;

    //--- Avoid re-computing the same costly mult. a zillion times in heavy loops.
    //    NOTE: This is the equivalent of Math.exp(-2*(this.dt/this.tau));
    this.squAlpha= this.alpha * this.alpha;

    //--- this.quAlphaInv attribute us used to avoid re-computing the same costly
    //    division a zillion times in heavy loops.
    this.squAlphaInv= this.alphaInv * this.alphaInv;

    slog.debug(mmi+"this.tau=" + this.tau);
    slog.debug(mmi+"this.dt=" + this.dt);
    slog.debug(mmi+"this.alpha=" + this.alpha);

    if (nbAuxCov > 0) {

      slog.debug(mmi+"Allocating numbercrunching data size->" + nbAuxCov);
      this.allocInit(nbAuxCov);

    } else {
      slog.debug(mmi+"Allocating numbercrunching data is deferred to derived class(es)");
    }
  }

  /**
   * @param size : The size to use to allocate the memory for the working vectors and matrices.
   * @return this LegacyResidualData object.
   */
  protected final LegacyResidualData allocInit(final int size) {

    //--- Allocate memory for working vectors:
    this.xpY= new D1Data(size, 0.0);
    this.zX= new D1Data(size, 0.0);
    this.beta= new D1Data(size, 0.0);
    this.xyScalProd= new D1Data(size, 0.0);

    //--- Allocate memory for working matrices
    //    NOTE: size could be one here so we have 1x1 matrices.
    this.mxxtM= new D2RowMajorData(size, size, 0.0);
    this.invXpX= new D2RowMajorData(size, size, 0.0);

    return this;
  }

  /**
   * Do the time scaling of working vectors and matrices.
   *
   * @return The weighted sum of unaccounted WL surge variance (local variable used in method LegacyResidual.update).
   */
  protected final double dataTimeScaling() {

    final String mmi= "dataTimeScaling: ";

    //---  Scale invXpx matrix, xpy vector:
    this.xpY.multWith(this.squAlpha);
    this.invXpX.multWith(this.squAlphaInv);

    //--- Value to be returned should be computed before udpating this.omega2:
    double ret= this.omega2 * ScalarOps.square(this.alpha * this.eps);

    //--- NOTE: this.omega, this.omega2, are multiplications accumulators then they MUST be in double precision:
    this.omega *= this.alpha;
    this.omega2 *= this.squAlpha;

    slog.debug(mmi+"omega=" + omega);
    slog.debug(mmi+"omega2=" + omega2);

    //--- Return sum of squares of remnant time scale
    return ret;
  }

  /**
   * @param timeFactor : The current time factor to use.
   * @return The time weight to use for the WL residuals errors update and estimation.
   */
  protected final double getErrorWeight(/*@Min(0)*/ final double timeFactor) {

    final String mmi= "getErrorWeight: ";

    slog.debug(mmi+"this.omega2=" + this.omega2);

    return this.omega2 * Math.exp(-2.0 * timeFactor) * (1.0 - this.squAlpha);
  }

  /**
   * @param timeFactor : The current time factor to use.
   * @return The time weight to use for the WL residuals values update and estimation.
   */
  //@Min(0)
  protected final double getValueWeight(/*@Min(0)*/ final double timeFactor) {

    final String mmi= "getValueWeight: ";

    slog.debug(mmi+"this.omega=" + this.omega);

    return this.omega * Math.exp(-timeFactor) * (1.0 - this.alpha);
  }

  /**
   * @param zY   : A locally computed WL direct(without tidal remnant) surge component.
   * @param beta :  The beta vector of a LegacyFMSCov object.
   * @return The Ordinary Least Square Regression(OLS) result as a vector(which could be an unary vector of dimension 1)
   */
  //@NotNull
  protected final D1Data OLSRegression(final double zY, /*@NotNull*/ final D1Data beta) {

    final String mmi= "OLSRegression: ";

    slog.debug(mmi+"bef. update this.invXpX=" + this.invXpX.toString());

    this.updateInvXpx();

    slog.debug(mmi+"aft update this.invXpX=" + this.invXpX.toString());
    slog.debug(mmi+"zY=" + zY);
    slog.debug(mmi+"this.zX=" + this.zX);

    //--- XpY += zY(scalar)*zX(vector)
    // Legacy C code
//        for (i = 0; i < model_status->m; i++)
//        {
//            model_status->XpY[i] += Y * X[i];
//        }
    //--- Need to deepCopy this.zX in this.xyScalProd before multiplying it with scalar zY
    //    and finally add(accumulate) the vector result in this.xpY:
    this.xpY.add(this.xyScalProd.deepCopy(this.zX).multWith(zY));

    slog.debug(mmi+"this.xpY=" + this.xpY.toString());

    //--- Return beta vector:
    //
    //    NOTE: This is the classic OLS(Ordinary Least Square) mulitple
    //          regression equation application.
    //
    //    beta= inv(X'*X)*X'*y in Matlab-esque notation.
    //
    return beta.D2xColD1(this.invXpX, this.xpY);
  }

  /**
   * @return this LegacyResidualData object with its invXpx matrix updated according to the Smith-Thompson iterative
   * matrix inversion algorithm. See the src file for more detailed comments and some ASCII art which (try to)
   * illustrate
   * the linear algebra used by this algorithm. (You can also read the old paper docs. of the legacy DVFM kit to get
   * even
   * more details about this algorithm).
   */
  //@NotNull
  protected final LegacyResidualData updateInvXpx() {

    final String mmi= "updateInvXpx: ";

    //--- Compute invXpx * zX only once
    //   ( M * x product of the function update_cov_matrix
    //    in source file residual.c from the legacy ODIN-DVFM 1990 kit)
    //
    //    M = inv(X'*X) in Matlab-esque synthax
    //
    //    NOTE: Here M is this.invXpX and x is this.zX
    //
    final D1Data mx = this.invXpX.D2xColD1(this.zX);

    slog.debug(mmi+"mx=" + mx.toString());

    //--- Compute the scalar denominator((zX)' * this.invXpx * zX + 1.0) of the invXpx matrix update
    //    NOTE: Do the division only once here:
    final double denomInv = 1.0 / (this.zX.dotProd(mx) + 1.0);

    slog.debug(mmi+"denomInv=" + denomInv);

    //--- NOTE: The notation for this denom variable used in source file residual.c from the legacy
    //          ODIN-DVFM 1990 kit is:
    //
    //           denom= x' * M * x + 1
    //
    //           M * x is done here with this.invXpx.D2xColD1(zX) see above.
    //           x' * (M * x) == zX.dotProd(this.invXpx.D2xColD1(zX)) see above.
    //
    //--- Do the update of this.invXpx :

    this.invXpX.
      subtract(this.mxxtM.colD1xRowD1(mx, zX.rowD1xD2(this.invXpX)).multWith(denomInv));

    //--- NOTE: The matrix notation used for this update procedure in source file residual.c from the legacy
    //          ODIN-DVFM 1990 kit is:
    //
    //    M -  M * x * x' * M
    //         --------------
    //         x' * M * x + 1
    //
    //    Here the matrix   M * x * x' * M
    //                      --------------
    //                      x' * M * x + 1
    //
    //    is computed with the chained call(which is the argument to this.invXpX.subtract method):
    //
    //       this.mxxtM.colD1xRowD1(mx,zX.rowD1xM(this.invXpx)).multWith(denomInv)
    //
    //       mx is the already computed column vector M * x
    //       zX.rowD1xM(this.invXpx) is the row vector x' * M
    //
    //       this.mxxtM.colD1xRowD1(mx,zX.rowD1xM(this.invXpx)) is the numerator matrix  M * x * x' * M
    //
    //       and denomInv is the scalar        1.0
    //                                   --------------
    //                                    x' * M * x + 1
    //
    //    And the update is then completed with:
    //
    //    this.invXpx.subtract( this.mxxtM.colD1xRowD1(mx,zX.rowD1xM(this.invXpx)).multWith(denomInv) );
    //

//--- TODO: The following explicit loop could be more efficient than the chained methods calls:
//    for (int r= 0; r< nrows; r++) {
//      for (int c= 0; c< ncols; c++) {
//
//        final double upd= this.invXpx.at(r,c) - denomInv * this.invXpx.rowDotProd(r,zX) * this.invXpx.colDotProd(c,
//        zX) ;
//
//        this.invXpx.put(r,c,upd);
//      }
//    }

    return this;
  }

  /**
   * Validate the size of the working vectors and matrices to use for the computations.
   *
   * @param finalSize : The size of the working vectors and matrices to use for the computations.
   * @return this LegacyResidualData object.
   */
  //@NotNull
  protected final LegacyResidualData checkNumberCrunchingSize(final int finalSize) {

    final String mmi= "checkNumberCrunchingSize: ";

    final int initialSize= this.xpY.size();

    slog.debug(mmi+"start, initialSize=" + initialSize + ", finalSize=" + finalSize);

    if (initialSize < finalSize) {

      slog.error(mmi+"finalSize must be less or equal to initialSize !!, initialSize="+ initialSize + ", finalSize=" + finalSize);
      throw new RuntimeException(mmi);

    } else if (initialSize > finalSize) {

      slog.debug(mmi+"Need to downsize numbercrunching data size from "+initialSize + " to " + finalSize);
      this.allocInit(finalSize);

    } else {
      slog.debug(mmi+"No need to downsize numbercrunching data! ");
    }

    slog.debug(mmi+"end");

    return this;
  }
}
