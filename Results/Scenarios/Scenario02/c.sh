#!/bin/sh
#echo Usage ./c.sh start stop, i.e.: ./c.sh 1 200
#echo "tdcrNmS_CO2R <- matrix(ncol=$2, nrow=39)" > H.txt
#for i in `seq $1 $2`

for i in `seq 1 200`; do
	echo $i

grep "tdcrNmS_CO2R.,1."  /tmp/Scenario02_rho0_25smart_05_1trial_GEN13_1000000_14-$i.txt | sed "s/tdcrNmS_CO2R.,1./tdcrNmS_CO2R\[,$i\]/" >> H.txt
	
done
