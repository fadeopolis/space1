=====================
MozartSpaces Examples
=====================

This directory contains example applications for MozartSpaces version 2.

The following example applications are available:
a) Hello Space: very simple example to start with MozartSpaces
   uses an embedded space
b) Chat: simple chat program to send messages between several peers
   uses the embedded space of a participating peer or a standalone space
c) Producer, Consumer, Observer (PCO): simple demo program for these patterns
   uses a standalone space (server) and several producer, consumer or observer
   instances
d) Tutorial examples: complete programs for the code snippets shown in the
   MozartSpaces tutorial
e) Flughafen (airport, in German): airport with a display panel and several
   runways where airplanes land

Maven 2 and Eclipse projects are included. There is a Maven project for all
examples, the individual examples are modules of it. The Eclipse project in
this directory contains also all examples and the MozartSpaces package with all
dependencies in the lib directory. You can also use the Eclipse projects for
the individual examples but need to adjust the path to the MozartSpaces library.
Furthermore, you can generate Eclipse projects with the Maven Eclipse plugin.
Then the sources and Javadoc is attached to the project, which need to be added
manually otherwise.


Starting and using the examples:
================================

If you use Maven, you need to execute the commands in the directory where the
pom.xml of the specific example is and first compile the example with
"mvn compile".

1) Hello Space
Execute one or both of the example's classes. There is no GUI, the output is on
stdout.
Maven command: mvn test

2) Chat
Execute several instances of the ChatWindow class, a simple Chat GUI. Use an
empty Space URI for the first instance, look in the logging output for the port
that is automatically choosen ("Bound server socket to free port <port>") and
use that space for the other instances. Alternatively, you can start a stand-
alone core instance and use its Space URI for all chat instances.
Maven command: mvn exec:exec

3) Producer, Consumer, Observer (PCO)
Start first a standalone core (server) and then as many producers, consumers,
and observers as you like in an arbitrary order. Besides Maven, you can also
use the batch files in the "pco" directory.
Maven commands:
mvn exec:java -Pserver
mvn exec:exec -Pproducer
mvn exec:exec -Pconsumer
mvn exec:exec -Pobserver

4) Tutorial
Each tutorial example is in its own package. The Maven project is not configured
to run the examples, use an IDE instead.

5) Flughafen
Start first a standalone core (server) and then "Flughafen" instances, for each
Flughafen an "Anzeigetafel" and several "Flugzeug" instances that start and land
on one of the "Flughafen" instances. Further information about the example is in
the file Flughafen.pdf/ppt in the "flughafen" directory.
Maven command configurations:
mvn exec:java -Pserver
mvn exec:exec -Pflughafen -Dname=<name> -Dlandebahnen=<landebahnen-anzahl>
mvn exec:exec -Panzeigetafel -Dflughafen=<flughafen-name>
mvn exec:exec -Pflugzeug -Did=<ID> -Dflughafen=<flughafen-name>

You should start a "Flughafen" instance before a "Anzeigetafel" or "Flugeug"
instances for it.
There are batch files in the "flughafen" directory for an example scenario that
correspond to the following Maven commands:
mvn exec:java -Pserver
mvn exec:exec -Pflughafen -Dname=VIE -Dlandebahnen=3
mvn exec:exec -Pflughafen -Dname=MUC -Dlandebahnen=2
mvn exec:exec -Panzeigetafel -Dflughafen=VIE
mvn exec:exec -Panzeigetafel -Dflughafen=MUC
mvn exec:exec -Pflugzeug -Did=123-AB -Dflughafen=VIE
mvn exec:exec -Pflugzeug -Did=234-CD -Dflughafen=MUC
mvn exec:exec -Pflugzeug -Did=345-DE -Dflughafen=MUC
mvn exec:exec -Pflugzeug -Did=456-EF -Dflughafen=MUC
