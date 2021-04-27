#!/bin/bash

ITERS=5

source msetup
ant
rm output_script_redundancy.txt

cd ./benchmarks

for d in */; do
  if [ $d = "jython" ]; then
    continue
  fi
  cd $d
  printf "%s," $d >>../../output_script_redundancy.txt
  for ((i = 1; i <= $ITERS; i++)); do
    ./Test -tool=FT2_r
    printf "%.5f," $(xpath -q -e '/entry/tool/Redundancy/text()' ./log/log.xml) >>../../output_script_redundancy.txt
  done
  printf "\n">>../../output_script_redundancy.txt
  cd ..
done
cd ..
printf "\n\nDONE\n" >>output_script_redundancy.txt
