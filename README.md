# Scientific Photo Analysis for Research & Conservation 'database (SPARCd), known during development as Scientific Animal Image Analysis (SANIMAL)

SPARC'd is a program developed for the University of Arizona's Jaguar and Ocelot Monitoring Project at the Wild Cat Research and Conservation Center, School of Natural Resources and the Environment Department.
* **It allows users to tag camera trap image data, transfer it onto a database, and then query it.*

## Getting Started

Clone the repository and build it using maven. All dependencies will be automatically fetched.

### Prerequisites

Java 15:<br />
https://www.oracle.com/java/technologies/javase/jdk15-archive-downloads.html<br />
Maven:<br />
https://maven.apache.org/install.html

### Prebuilt executable

If you want to run the software without needing to build from source, prebuilt executables can be found in the [Version History directory](https://github.com/CulverLab/sparcd/tree/master/Version%20History)<br>

Sanimal X.Y.Z.jar is the original legacy software written in Swing which was the predecessor to SanimalFX.<br>
Sanimal FX X.Y.jar is the current latest build of Sanimal FX including all dependencies.<br>
<br>
Executables are executed with:

```
java -jar 'Sanimal FX X.Y.jar' 
```

### Build from Source

Clone the github repository into a directory:

```
git clone https://github.com/DavidM1A2/Sanimal <directory>
```

Build the project into an executable JAR file to run:

```
cd '<directory>/Sanimal FX'
mvn -U compile package
```

Run the program:

```
java -jar '<directory>/Sanimal FX/target/SanimalFX-1.0-SNAPSHOT-jar-with-dependencies.jar'
```

## Sample screenshots

### Login - This screen is used to log into your account.

![login screen](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/login.PNG)

### Home - This tab is used to credit developers and provide an exit button.

![home tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/home.PNG)

### Import - This tab is used to tag images with metadata.

![import tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/import.PNG)

### Collections - This tab is used to show collections and upload images.

![collections tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/collections.PNG)

### Analyze/Query - This tab is used to query uploaded image data.

![query tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/query.PNG)

### Map - This tab is used to display all locations on an interactive map.

![map tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/map.PNG)

### Setings - This tab is used to change program settings.

![settings tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/settings.PNG)

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

* This Beta Version of “SPARC’d”  is being developed by University of Arizona School of Natural Resources and the Environment (SNRE) and CyVerse. 

## Authors

* **David Slovikosky** - *Lead Developer* - [DavidM1A2](https://github.com/DavidM1A2)
* **Susan Malusa** - *Project Coordination/Design* - [smalusa](https://github.com/smalusa)
* **Project Site Documentation** - [smalusa](https://github.com/CulverLab)

## Acknowledgements

* **Co-Principal Investigators** - *Dr. Melanie Culver and Nirav Merchant* 
* **Technical Advisors** - *Dr. Richard Snodgrass and Dr. Carlos Scheidegger* 
* **CyVerse Support** - *Dr. Blake Joyce, Tony Edgin, Julian Pistorius and Chris Schnaufer* 
* **Support and Ideas** - *The University of Arizona Jaguar & Ocelot Monitoring Team, The Slovikosky Family*



