#!/bin/bash

Iters=10

source msetup
ant
rm output.txt

cd ./benchmarks
for d in */ ; do
    cd $d
    ft2=0
    sf=0
    for ((i=1; i <= $Iters; i++));do
      ./Test -tool=FT2
      ((ft2+=$(xpath -q -e '/entry/system/memUsed/text()' ./log/log.xml)))
      ./Test -tool=SF
      ((sf+=$(xpath -q -e '/entry/system/memUsed/text()' ./log/log.xml)))
    done
    echo $ft2
    echo $sf
    echo $(bc<<<"scale=2; $ft2/$sf")
    printf "%s : %.2f\n" $d $(bc<<<"scale=2; $ft2/$sf")>> ../../output.txt
    cd ..
done

#printf '%s\n' "${ft2_arr[@]}"
printf '\n\nDONE\n\n'

