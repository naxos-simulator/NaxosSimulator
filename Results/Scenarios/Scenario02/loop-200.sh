  #!/bin/bash
        for i in `seq 1 200`; do
            echo $i
            java -Xmx512m -Duser.country=US -Duser.language=en -cp dist/JavaSim.jar:lib/log4j-1.2.15.jar:lib/json-lib-2.4-jdk15.jar de.tzi.scenarios.Scenario 0.05 0.25 1 GEN13 1000000 > results/simulations/rho0_25smart_05_1trial_GEN13_1000000_14-$i.txt $i
            sleep 10
        done
        
