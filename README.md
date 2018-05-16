# Scientific Animal Image Analysis (SANIMAL)

SANIMAL is a program developed for the University of Arizona's School of Natural Resources and the Environment department. It allows users to tag camera trap image data, transfer it onto a database, and then query it.

## Getting Started

Clone the repository and build it using maven. All dependencies will be automatically fetched.

### Prerequisites

Java 8:<br />
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html<br />
Maven:<br />
https://maven.apache.org/install.html

```
Give examples
```

### Installing

A step by step series of examples that tell you have to build the software from scratch

Clone the github repository into a directory:

```
git clone https://github.com/DavidM1A2/Sanimal <directory>
```

Build the project into an executable JAR file to run:

```
cd <directory>
mvn -U compile package
```

Run the program:

```
java -jar <directory>/target/SanimalFX-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **David Slovikosky** - *Developed the program* - [DavidM1A2](https://github.com/DavidM1A2)