## How to build

```sh
cd ant/ # will only work from ant/ dir
./antCompileWLTools.sh
```

## How to run

```sh
java -cp 'bin:lib' WLTools --tool=prediction --predDurationInDays=42 --stageType=DISCHARGE_CFG_STATIC --startTimeISOFormat=2023-09-24T06:00:00Z --stationPredType=TIDAL:NON_STATIONARY_FOREMAN --stationIdInfo=StLawrence:Deschaillons:gridPoint-540 --tidalConstsInputInfo=NON_STATIONARY_JSON:dischargeClimatoTFHA:OneDSTLT --outputDirectory=NSTidePred/2023100406
```

## How to manually run Sonar Qube analysis

From [SonarScanner CLI documentation](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner/). First install `sonar-scanner`, then run the following command. You might need to generate a token first <http://sonarqube.azure.cloud-nuage.dfo-mpo.gc.ca/>.

```sh
sonar-scanner -Dsonar.login=myAuthenticationToken
```

You can find the analysis results [here](http://sonarqube.azure.cloud-nuage.dfo-mpo.gc.ca/dashboard?id=spine%3Achs-wltools).
