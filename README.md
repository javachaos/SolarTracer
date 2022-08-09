# Solar Tracer

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/javachaos/SolarTracer.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/javachaos/SolarTracer/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/javachaos/SolarTracer.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/javachaos/SolarTracer/alerts/)
[![CodeQL](https://github.com/javachaos/SolarTracer/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/javachaos/SolarTracer/actions/workflows/codeql-analysis.yml)

Simple Java GUI application to log solar data from the "EPsolar Tracer 1215RN MPPT Solar Battery Charge Controller" using an arduino device to recieve serial data. The application uses a SQLite backend to store all data at a default rate of once per second.
![](https://github.com/javachaos/SolarTracer/blob/master/src/main/resources/solar_tracer.png)
This software, gathers information from an arduino board using the arduino script found under... solartracer/src/main/resources/tracer.ino and aggrigates the data to an SQLite database and updates some visual graphs.

See [Wiki](https://github.com/javachaos/SolarTracer/wiki) for more information.



