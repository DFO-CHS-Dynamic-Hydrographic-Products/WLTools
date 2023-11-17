The St. Lawrence discharge clusters non-stationary tidal constituents values in the 
dischargeClusters/<cluster>/dischargeClimatoTFHA/gridPoint-NNN-TFHA.json files must be used for the
non-stationary tidal predictions with the following equations:

The stages discharges data used for the non-stationary tidal analysis by the NS_TIDE python code was (2nd order polynomial):
(NOTE: log() is the Neperian logarithm here and not the log10())

log(discharge(t-CS1.lag)) and  (log(discharge(t-CS2lag)))**2

Stage reconstruction part only (no trigo function and no iteration):
-----------------------------------------------------

CS0 + CS1.factor * log(discharge(t-CS1.lag)) + CS2.factor * (log(discharge(t-CS2lag)))**2 

non-stationary tidal reconstruction part:
---------------------------

constituents (AmpCSn, PhaCSn) summation for one constituent, note that the Z0 of the TCF files
is in fact the CS0 value of the Stage part so it MUST not be defined in the list of AmpC0 
coefficients in the input files.

AmpC0 * cos(fCS0*t + phaCS0) + AmpCS1 * cos(fCS1*t + phaCS1) * log(discharge(t-CS1.lag)) + AmpCS2 * cos(fCS1*t + phaCS2) * (log(discharge(t-CS2.lag)))**2  

--------------------------------------------
<cluster>/dischargeStationsInfo.json files 
--------------------------------------------

<cluster>/dischargeStationsInfo.json files content can be used to build the climatologic discharges for all
the grid points of this cluster.

"uppStreamStnIds" : Discharge stations that are considered to be located at the upstream boundary of this <cluster>.
"downStreamStnIds": Discharge stations that are considered to be located at the downstream boundary of this <cluster>.

A simple spatial linear interpolation between the cumulative discharges of the uppStreamStnIds and the downStreamStnIds 
can be done for all the grid points of this <cluster>. The stations discharges climatologies are located in the
WLTools/ca/gc/dfo/chs/wltools/cfg/tidal/nonStationary/StLawrence/dischargeClimatos/hourly-MoyInter_<stn id.>.csv files
