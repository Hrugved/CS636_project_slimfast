#!/bin/bash

Iters=1

source msetup
ant
rm output_script_correctness.txt
printf "errorTotal:distinctErrorTotal\n\n" >> output_script_correctness.txt
cd ./benchmarks
for d in */ ; do
    cd $d
    ft2_d=-1
    ft2_t=-1
    sf_d=-1
    sf_t=-1
    for ((i=1; i <= $Iters; i++));do
      ./Test -tool=FT2
      ((ft2_t=$(xpath -q -e '/entry/errorTotal/text()' ./log/log.xml)))
      ((ft2_d=$(xpath -q -e '/entry/distinctErrorTotal/text()' ./log/log.xml)))
      ./Test -tool=SF
      ((sf_t=$(xpath -q -e '/entry/errorTotal/text()' ./log/log.xml)))
      ((sf_d=$(xpath -q -e '/entry/distinctErrorTotal/text()' ./log/log.xml)))
    done
    printf "%s : (FT2: %d/%d) (SF: %d/%d)\n" $d $ft2_t $ft2_d $sf_t $sf_d >> ../../output_script_correctness.txt
    cd ..
done
cd ..

printf "\n\nDONE\n\n" >> output_script_correctness.txt

