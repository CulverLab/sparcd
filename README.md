# Scientific Animal Image Analysis (SANIMAL)

SANIMAL is a program developed for the University of Arizona's School of Natural Resources and the Environment department. It allows users to tag camera trap image data, transfer it onto a database, and then query it.

## Getting Started

Clone the repository and build it using maven. All dependencies will be automatically fetched.

### Prerequisites

Java 8:<br />
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html<br />
Maven:<br />
https://maven.apache.org/install.html

### Installing

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

## Sample screenshots

### Login

This screen is used to log into your account.
![login screen](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/login.PNG)

This tab is used to credit developers and provide an exit button.
![home tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/home.PNG)

This tab is used to tag images with metadata.
![import tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/import.PNG)

This tab is used to show collections and upload images.
![collections tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/collections.PNG)

This tab is used to query uploaded image data.
![query tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/query.PNG)

This tab is used to display all locations on an interactive map.
![map tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/map.PNG)

This tab is used to change program settings.
![settings tab](https://rawgit.com/DavidM1A2/Sanimal/master/screenshots/settings.PNG)


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **David Slovikosky** - *Developed the program* - [DavidM1A2](https://github.com/DavidM1A2)