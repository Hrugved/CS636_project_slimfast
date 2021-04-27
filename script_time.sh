#!/bin/bash

ITERS=5

source msetup
ant
rm output_script_time.txt

cd ./benchmarks

for d in */; do
  cd $d
  printf "%s," $d >>../../output_script_time.txt

  ft2_time_acc=0
  for ((i = 1; i <= $ITERS; i++)); do
    ./Test -tool=FT2
    ((ft2_time_acc += $(xpath -q -e '/entry/time/text()' ./log/log.xml)))
  done
  ft2_time=$(bc -l <<<"$ft2_time_acc/$ITERS")
  printf "%.2f," $ft2_time >>../../output_script_time.txt

  sz=15 #cacheSize
  sf_tor_acc=0
  for ((i = 1; i <= $ITERS; i++)); do
    ./Test -tool=SF -cacheSize=$sz
    ((sf_time = $(xpath -q -e '/entry/time/text()' ./log/log.xml)))
    sf_tor_acc=$(bc -l <<<"($ft2_time/$sf_time) + $sf_tor_acc")
  done
  printf "%.2f," $(bc <<<"scale=2; $sf_tor_acc/$ITERS") >>../../output_script_time.txt

  printf "\n" >>../../output_script_time.txt
  cd ..
done

cd ..
printf '\n\nDONE\n\n' >>output_script_time.txt
