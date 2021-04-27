#!/bin/bash

ITERS=5

source msetup
ant
rm output_script_cacheSize.txt

cd ./benchmarks

for d in */; do
  cd $d
  printf "%s," $d >>../../output_script_cacheSize.txt
  threads=0; # total threads

  # FT2 -> avg memUsed
  ft2_mem_acc=0
  for ((i = 1; i <= $ITERS; i++)); do
    ./Test -tool=FT2
    ((ft2_mem_acc += $(xpath -q -e '/entry/system/memUsed/text()' ./log/log.xml)))
    ((threads = $(xpath -q -e '/entry/threadCount/text()' ./log/log.xml)))
  done
  ft2_mem=$(bc -l <<<"$ft2_mem_acc/$ITERS")
  printf "%d,,%.2f,," $threads $ft2_mem >>../../output_script_cacheSize.txt


  # SF -> avg sor (space optimization ratio) for each cache size
  for ((sz = 5; sz <= 50; sz += 5)); do
    sf_sor_acc=0
    for ((i = 1; i <= $ITERS; i++)); do
      sf_mem=0
      ./Test -tool=SF -cacheSize=$sz
      ((sf_mem = $(xpath -q -e '/entry/system/memUsed/text()' ./log/log.xml)))
      sf_sor_acc=$(bc -l <<<"($ft2_mem/$sf_mem) + $sf_sor_acc")
    done
    printf "%.2f," $(bc <<<"scale=2; $sf_sor_acc/$ITERS") >>../../output_script_cacheSize.txt
  done

  printf "\n" >>../../output_script_cacheSize.txt
  cd ..
done

cd ..
printf '\n\nDONE\n\n' >>output_script_cacheSize.txt
