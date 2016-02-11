#!/bin/sh

echo "trial, tns, dns, cns, rns, tnsR, dnsR, cnsR, rnsR, totalCompared, normalCount, smartCount, removedVehiclesCount, totalCO2N, totalCO2S, totalTripsN ,totalTripsS, totalTimeN, totalTimeS, totalDistanceN, totalDistanceS, totalCO2N14, totalCO2S14, totalTripsN14, totalTripsS14, totalTimeN14, totalTimeS14, totalDistanceN14, totalDistanceS14, normalCount14, smartCount14, tns14, dns14, cns14, rns14, tnsR14, dnsR14, cnsR14, rnsR14, totalCompared14 " > H.csv

sed -e "s/)//" -e "s/\] <- c(/, /" -e "s/tdcrNmS_CO2R..//" -e "s/,\] <- c./,/" H.txt >> H.csv
	
