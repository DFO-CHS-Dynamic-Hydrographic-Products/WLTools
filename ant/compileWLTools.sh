#!/bin/bash

# --- Modify according to the code location context.
javaCodeBaseMainDir=../
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
