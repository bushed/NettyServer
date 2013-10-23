NettyServer
===========

Small HTTP server with DB. This is a test job.

How to build
------------

Project directory tree:

    +---ant       
    |       build.xml
    |
    +---lib
    |       hsqldb.jar
    |       netty-all-4.0.10.Final.jar
    |
    +---src
    |   \---nettyserver
    |           Database.java
    |           HttpServerHandler.java
    |           HttpServerInitializer.java
    |           ManualDBForm.java
    |           NettyServer.java
    |           StatDAO.java
    |           statusHolder.java
    +---screens
    |       *.png;*.jpg
    |
    \---build
        |    NettyServer.jar
        \---classes
                *.class
    

You should use Apache Ant to build project. Build.xml placed in `./ant`. 
You can just run ant in this directory to build and run project  - or - use the following commands:

* [compile] -- To compile.
* [build.jar] -- To create a jar-file
* [run] -- to run program. Notice that folder `./build` will be owerrited 
