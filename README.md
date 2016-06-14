# SolarTracer

Simple Java GUI application to log solar data from an MPPT solar charge controller using an arduino device to recieve serial data. The application uses a SQLite backend to store all data at a default rate of once per second. This project is currently in the WIP state. 
![](https://github.com/javachaos/SolarTracer/blob/master/src/main/resources/solar_tracer.png)
This software, gathers information from an arduino board using the arduino script found under... SolarTracer/src/main/resources/tracer.ino and aggrigates the data to an SQLite database and updates some visual graphs. It also creates a small embedded webpage on the localhost of the running machine with an up to date datapoint. (Refreshes every second).

See [Wiki](https://github.com/javachaos/SolarTracer/wiki) for more information.

DOWNLOAD: [Here](https://github.com/javachaos/SolarTracer/blob/master/bin/SolarTracer.exe)

Although I may not be currently aware of any bugs in this application some may still exist, therefore...
USE THIS SOFTWARE AT YOUR OWN RISK.
