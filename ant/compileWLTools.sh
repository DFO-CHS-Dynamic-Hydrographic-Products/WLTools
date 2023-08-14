#!/bin/bash

# --- Modify according to the code location context.
javaCodeBaseMainDir=/home/gme042/slinks/fs7_isi_dfo_chs_enav/Dev/GH/CHS/WLTools
javaClassesMainDestDir=${javaCodeBaseMainDir}/lib

export CLASSPATH=${javaClassesMainDestDir}:${javaCodeBaseMainDir}:${CLASSPATH}

ant -buildfile ${javaCodeBaseMainDir}/ant/antCompLib.ant

if [ ${?} -ne 0 ];
then
  echo "ant -buildfile ${javaCodeBaseMainDir}/ant/antCompLib.ant FAILED!!"
  exit 1

fi

ant -buildfile ${javaCodeBaseMainDir}/ant/antCompMain.ant

if [ ${?} -ne 0 ];
then
  echo "ant -buildfile ${javaCodeBaseMainDir}/ant/antCompMain.ant FAILED!!"
  exit 1
fi
