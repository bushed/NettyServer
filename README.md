NettyServer
===========

Small HTTP server with DB. This is a test job.

How to build
------------

You should use Apache Ant to build project. Build.xml placed in `./ant`. 
You can just run ant in this directory to build and run project  - or - use the following commands:

* [compile] -- To compile.
* [build.jar] -- To create a jar-file
* [run] -- to run program. Notice that folder `./build` will be owerrited 

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
    |   \---classes
    |           *.class    
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

Main-class: NettyServer
[NettyServer] -- Starts and stops server.
[Database] -- provides access to Database
[HttpServerInitializer] -- specify pipeline factory, means how we will handle connections
[HttpServerHandler] -- Http handler. Accepts http requests and responds to them.
[ManualDBForm] -- Debug tool.
[StatDAO] -- implements methods to work with Database.
[StatusHolder] -- synchronized counter

Benchmark
---------

    GitHub::Markup.render('Benchmark/*.txt')
    
Screens
-------

![Page status in use](/Screens/Screen_in_use.png "Screen")
