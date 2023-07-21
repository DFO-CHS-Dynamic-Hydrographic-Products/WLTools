The St. Lawrence discharge clusters values must be used for the non-stationary tidal
predictions with the following equations:

Stage part only (no trigo function and no iteration:
-----------------------------------------------------

S0 + S1 * log(discharge(t)) + S2 * (log(discharge(t)))**2 

non-stationary tidal part:
---------------------------
constituents (AmpC, PhaC) summation, note that the Z0 of the TCF files is in fact the S0 of the Stage part so
it MUST not be defined in the list of AmpC0 coefficients.

AmpC0 * cos(fC*t + phaC0) + AmpC1 * cos(fC*t + phaC1) * log(discharge(t)) + AmpC2 * cos(fC*t + phaC2) * (log(discharge(t)))**2  
