NettyServer
===========

Small HTTP server with DB. This is a test job.

How to build
------------

You should use Apache Ant to build project. Build.xml placed in `/ant`.   
You can just run ant in this directory to build and run project  - or - use see command list:

* `ant compile` -- To compile only.
* `ant build.jar` -- To compile and create a jar-file.
* `ant` or `ant run` -- compile, build jar and run the program with default settings. 
Notice that folder `/build` will be owerrited. Default settings for ant build project:  
* [Port = 3000]  
Edit default settings right in build.xml. See line `<property name="port" value="3000"/>`

To build project without Ant  
Summary:  
* JRE1.6  
* libraries: `../lib/*.jar`
* sources: `../src/nettyserver`
* Main-class: `nettyserver/NettyServer`  
See "Implementation details" below.

How to use
----------

To run program without Ant, run it from command line.

    java -jar NettyServer.jar [port]

If everything ok, program will type:

    Web server started at port 3000
    type "stop" to stop server

Default port is 3000. So, to check server, enter to address line in your browser `http://localhost:3000/status`

Implementation details
----------------------

### Project directory tree:

    +---ant       
    |       build.xml
    |
    +---benchmark
    |       *.txt
    |
    +---build
    |   |    NettyServer.jar
    |   +---classes
    |   |       *.class    
    |   \---localdb
    |           *
    |
    +---lib
    |       hsqldb.jar
    |       netty-all-4.0.10.Final.jar
    |
    +---screens
    |       *.png;*.jpg
    |
    \---src
        \---nettyserver
                Database.java
                HttpServerHandler.java
                HttpServerInitializer.java
                ManualDBForm.java
                NettyServer.java
                StatDAO.java
                StatusHolder.java

### Description

* [ant](https://github.com/bushed/NettyServer/blob/master/ant/) -- contains build script for Apache Ant
* [benchmark](https://github.com/bushed/NettyServer/blob/master/benchmark/) -- Apache benchmark results
* [build](https://github.com/bushed/NettyServer/blob/master/build/) -- destination folder for build project
* build/localdb -- will be created automatically
* [lib](https://github.com/bushed/NettyServer/blob/master/lib/) -- used libraries
* [screens](https://github.com/bushed/NettyServer/blob/master/screens/) -- screenshots 
* [src](https://github.com/bushed/NettyServer/blob/master/src/) -- source code

### Classes

Main-class: NettyServer
* `NettyServer` -- Starts and stops server.
* `Database` -- provides access to Database
* `HttpServerInitializer` -- specify pipeline factory, means how we will handle connections
* `HttpServerHandler` -- Http handler. Accepts http requests and responds to them.
* `ManualDBForm` -- Debug tool.
* `StatDAO` -- implements methods to work with Database.
* `StatusHolder` -- synchronized counter

Benchmark
---------

![Benchmark result](https://raw.github.com/bushed/NettyServer/master/Screens/benchmark.png "Benchmark")
    
Screens
-------

![Page status in use](https://raw.github.com/bushed/NettyServer/master/Screens/Screen_in_use.png "Status")
