# Naxos Simulator
Cellular automata traffic simulator with integrated carbon dioxide emission model

The main purpose of the Naxos traffic simulator is to perform computations related to vehicle movement according to predefined set of cellular automata rules. A built-in set of rules includes Rule-184, NaSch and also their extensions that incorporate CO<sub>2</sub>-emission model. The simulator is fully configurable, i.e. all parameters, such as vehicle density, percentage of transmitting vehicles, percentage of smart vehicles but also the layout of the virtual city, can be altered by the configuration file.

![Naxos Traffic Simulator](https://raw.githubusercontent.com/naxos-simulator/NaxosSimulator/master/Media/Cities/GEN02-small.png)

### Table of Contents

 * [Quick start](#quick-start)
 * [Configuration](#configuration)
 * [Troubleshooting](#troubleshooting)
 
## Quick start

1. Prior to run make sure you have Java Runtime Environment installed. If you don't have it go to [Java Download](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
2. Download the latest [build] (Distribution)
3. Extract files from the archive and execute run.bat or run.sh, depending on your operating system
4. Press 't' key to start a simulation
5. Have fun!

### Key bindings

| Key | Meaning |
| --- | :------ |
| t | Runs / pauses the simulation | 
| r | Performs a single computational simulation step and updates statistics l Runs the simulation for 25000 computational time steps | 
| 2 | Triggers the change of all the traffic lights within the city| 
| 4 | Shows all the properties of the simulated elements| 
| 5 | Shows the display settings| 
| q | Quits the application| 

### Mouse

| Action | Meaning |
| --- | :------ |
| Click+move | Moves a map |
| Scroll | Zooms in/zooms out |
| Shift+Scroll | Increase/decrease cell size |


### Command line

To start simulation from command line, go to the the main directory where you exctractd the latest build and simply type:

##### Windows
```bash
java -cp dist/JavaSim.jar;lib/log4j-1.2.15.jar de.tzi.Main
```

##### Unix/MacOS
```bash
java -cp dist/JavaSim.jar:lib/log4j-1.2.15.jar de.tzi.Main
```

[Simulation](https://raw.githubusercontent.com/naxos-simulator/NaxosSimulator/master/Media/Cities/GEN02-small.png) "Simulation window"


## Configuration
### Command line parameters 

Sample scenario is defined in `de.tzi.scenarios.Scenario` class. Input parameters provided in command line have the following meaning:

| Number | Sample value | Meaning |
| ------ | ------------ | ------- |
| 1 | 0.1 | Fraction of the vehicles that have access to dynamic route guidance system (DRGS) that provide up-to-date information about the quickest routes. Those vehicles are always choosing routes given by DRGS. (0.1 means around 10%) |
| 2 | 0.25 | Vehicle density - the ratio of cells occupied by vehicles to all cells. (0.25 means 25%) |
| 3 | 15 | Number of independent simulations to be run one after another. (15 simulations) | 
| 4 | data/GEN02 | Path to the definition of the test city ([GEN02](https://raw.githubusercontent.com/naxos-simulator/NaxosSimulator/master/Media/Cities/GEN02-small.png)) |
| 5 | 1000000 | For how long the simulation should be performed, measured in computational time steps (1 milion time steps) |
| 6 | 0 | Segment standard deviation describes whenever the vehicles would be placed with the same probability across all road segments or not (still in experimental phase) |

#### Example

##### Windows
```bash
java -Duser.country=US -Duser.language=en -cp dist/JavaSim.jar;lib/log4j-1.2.15.jar \
de.tzi.scenarios.Scenario 0.25 0.1 15 GEN02 1000000
```

##### Unix/MacOS
```bash
java -Duser.country=US -Duser.language=en -cp dist/JavaSim.jar:lib/log4j-1.2.15.jar \
de.tzi.scenarios.Scenario 0.25 0.1 15 GEN02 1000000
```


### Configuration file

The application settings are stored in the configuration file `simulation.properties`. It has a form of a list of key-value pairs. If the file does not exist or if any of the specified configuration entries are missing, the defaults are used. The meaning of all the parameters are described below:

| Key | Meaning |
| --- | ------- |
|data.files.directory | Name of the directory from which layout of the road network will be taken|
|persistence.file | Name of the file from which serialized objects that represent the road network is loaded. If it does not exist, then it is created, once input files are read. This is used to speed up the start of the simulation. |
| data.files.crossings | The name of the file that contains information about crossings. |
| data.files.segments | The name of the file that contains information about road segments. |
| data.files.map.segments | The name of the file that contains geospatial information about road segments. |
| vehicle.density|  Parameter used to setup a test scenario that states the ratio of cells occupied by vehicles to all cells. |
| vehicle.transmitting | Parameter that states what percentage of the vehicles are reporting their traveling times between crossings. |
| vehicle.icensePlate.ratio | Parameter that states the probability that the vehicle identity will be properly recognized at a crossing. |
| vehicle.smart.r | The ratio of smart vehicles of type R, i.e. the vehicles that have access to dynamic route guidance system (DRGS) that provide up-to-date information about the quickest routes. The Smart-R vehicles are always choosing routes given by DRGS. |
| vehicle.smart.s|  The ratio of smart vehicles of type S, i.e. the vehicles that additionally have access to the information regarding expected time of changing traffic lights, so they could slow down a little bit before approaching the red light to eliminate the stop-and-go and smoothly accelerate, once the traffic light changes back to green. |
| lights.sotl | Parameters d, r, e, u, m and threshold are specific for the Self-Organizing Traffic Lights algorithm. |
| lights.random.min | Minimum cycle length for random length traffic light controllers. |
| lights.random.max | Maximum cycle length for random length traffic light controllers. |
| lights.random.seed | Pseudorandom seed. If it is set to zero, then the default random seed will be taken, which is based on current time. |
| lights.fixed.period | Fixed period cycle length. |
| ltraffic.update.frequency | How often DRGS updates its internal map to be able to show the quickest route between any two crossings. |
| traffic.segments.stddev |  The standard deviation of a vehicle’s density among segments. |
| traffic.deadlock.wait | If the vehicle is not able to move in the desired direction because of a deadlock that lasts for longer than the value specified in this parameter, or the vehicle is trying to move in any other direction that currently has the green light. If the value is set to zero, this feature is turned off. |
| window.size.width | The width of the simulation window. |
| window.size.height | The height of the simulation window. |
| window.cell.size | Cell size for display in the simulation window. |
| window.zoom | Zoom factor for the whole virtual city in the simulation window. |
| window.fps | Frames per second for continuous simulation mode. This mode is triggered by pressing ’t’ key during simulation. |
| traffic.lights.type |  Traffic light type in the simulated city. Possible types are: FIXED for fixed period, RANDOM for random and SOTL for Self-Organizing Traffic Lights. |
| traffic.strategy.type|  Cellular automata traffic model, possible values are: RULE184, NASCH, RULE184 CO2 and NASCH CO2. |
| traffic.navigation.type | Specifies the type of navigation for the vehicles. Possible values are: SIMPLE – the crossroads are the vertexes of a navigation graph and the road segments are the edges, STANDARD – the segments are the vertexes of a navigation graph, and the passing possibilities are the edges, BROWNIAN –  vehicles are traveling without any specific goal, its movement is determined by the pseudorandom variable. |
| db.url | JDBC connection string to the database (used for GIS Creator). db.driver JDBC Driver class. |
| db.user | Username encoded in Base64. |
| db.pass | Password encoded in Base64. |
| interactive.mode|  If true shows the Graphical User Interface, otherwise the simulator is run in a batch mode.| 

#### Overriding configuration file

You extract `simulator.properties` from the distribution archive and place it in the main directory. Feel free to change any settings you like. To make a use of it, place current directory (dot) in classpath:


##### Windows
```bash
java -cp .;dist/JavaSim.jar;lib/log4j-1.2.15.jar de.tzi.Main
```

##### Unix/MacOS
```bash
java -cp .:dist/JavaSim.jar:lib/log4j-1.2.15.jar de.tzi.Main
```

## Troubleshooting

#### java.lang.NoClassDefFoundError: de/tzi/Main

It means that probably you are trying to run the simulator in a wrong directory. You should be in the directory where  `dist`, `lib` and other subdirectories were extracted from the distribution archive.


#### java.lang.OutOfMemoryError: Java heap space

```bash
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at de.tzi.traffic.navigation.Graph.<init>(Graph.java:58)
	at de.tzi.traffic.navigation.Navigator.<init>(Navigator.java:62)
	at de.tzi.traffic.navigation.StandardNavigator.<init>(StandardNavigator.java:42)
	at de.tzi.traffic.navigation.NavigatorFactory.createNavigator(NavigatorFactory.java:49)
	at de.tzi.traffic.TrafficManager.<init>(TrafficManager.java:110)
	at de.tzi.traffic.TrafficManager.<init>(TrafficManager.java:136)
	at de.tzi.traffic.TrafficManager.<init>(TrafficManager.java:75)
	at de.tzi.Main.main(Main.java:66)
```

Simply increase the amount of memory available for Java Virtual Machine (`-Xmx512m -Xms512m`). You might also consider running simulations with a less demanding virtual city.

##### Windows
```bash
java -Xmx512m -Xms512m -cp dist/JavaSim.jar;lib/log4j-1.2.15.jar de.tzi.Main
```

##### Unix/MacOS
```bash
java -Xmx512m -Xms512m -cp dist/JavaSim.jar:lib/log4j-1.2.15.jar de.tzi.Main
```
