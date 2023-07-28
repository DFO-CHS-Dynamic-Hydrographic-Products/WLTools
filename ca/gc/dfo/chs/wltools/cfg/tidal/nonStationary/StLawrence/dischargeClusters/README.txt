The St. Lawrence discharge clusters non-stationary tidal constituents values must be
used for the non-stationary tidal predictions with the following equations:

The stages discharges data used for the non-stationary tidal analysis was (2nd order polynomial):

log(discharge(t-CS1.lag) and  (log(discharge(t-CS2lag)))**2


Stage part only (no trigo function and no iteration):
-----------------------------------------------------

CS0 + CS1.factor * log(discharge(t-CS1.lag)) + CS2.factor * (log(discharge(t-CS2lag)))**2 

non-stationary tidal part:
---------------------------

constituents (AmpCSn, PhaCSn) summation, note that the Z0 of the TCF files is in fact the S0 of the Stage part so
it MUST not be defined in the list of AmpC0 coefficients.

AmpC0 * cos(fCS0*t + phaCS0) + AmpCS1 * cos(fCS1*t + phaCS1) * log(discharge(t-CS1.lag)) + AmpCS2 * cos(fCS1*t + phaCS2) * (log(discharge(t-CS2.lag)))**2  
