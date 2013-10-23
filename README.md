NettyServer
===========

Small HTTP server with DB. This is a test job.

How to build
------------

You should use [Apache Ant](http://ant.apache.org/) to build project. `Build.xml` placed in `/ant`.   
You can just run ant in this directory to build and run project  - or - see command list:

* `ant compile` -- To compile only.
* `ant build.jar` -- To compile and create a jar-file.
* `ant` or `ant run` -- compile, build jar and run the program with default settings. 
Notice that folder `/build` will be owerrited.   
Edit default settings right in build.xml. Default settings for ant build project:  
* [Port = 3000] - See line `<property name="port" value="3000"/>`


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

Default port is 3000. So, to check server, enter to address line in your browser `http://localhost:3000/status`.  
According to the test job features list, there is 3 pages:
* `/status` -- shows the statistic page
* `/hello` -- after 10 seconds says "Hello page"
* `/redirect?url=<url>` -- redirects to the specified in GET request URL.

Implementation details
----------------------
Project uses [Netty framework](http://netty.io/).
Number of threads by default = 2 * Cores.

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

See [JavaDoc](http://bushed.github.io/NettyServer/javadoc/) for more details.

### Database

This project uses embedded [HSQL](http://hsqldb.org/) DB. It's fast and threadsafe.
Database files placed in folder `/localdb` in build directory.  

To change DBMS edit `Database` class. 
Besides see `StatDAO` class, that contains all SQL code, to use your specific DBMS features.  

Database contains only two tables. `Connections` for each accepted request 
and `URI` for storing text strings and agregated data.

![Database diagram](https://raw.github.com/bushed/NettyServer/master/Screens/Database_diagram.png "Database")

Benchmark
---------

![Benchmark result](https://raw.github.com/bushed/NettyServer/master/Screens/benchmark.png "Benchmark")
  
Description: Apache benchmark compare the size of each response to the first received. 
Status page generated dynamically, so obviously there is a lot of failed requests by lengths.
    
Screens
-------
Status page in browser.  
![Page status in use](https://raw.github.com/bushed/NettyServer/master/Screens/Screen_in_use.png "Status")  
  
Start server in console.  
![Start server](https://raw.github.com/bushed/NettyServer/master/Screens/use.png "server")  

