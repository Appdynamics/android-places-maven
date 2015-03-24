Overview
--------
Android demo using the Google Geocoding, Elevation and Civic Information APIs: the project builds a simple Android application that gives geocoding and elevation data, plus a complete list of elected officials (in the US at least), for a given address string. The application will run fine on a connected device or AVD emulator (use an Android-21 system image). The same source code project can be used to build Maven and ant (Eclipse ADT) builds.  

Using Google APIs
-----------------
You will need a Google API key to access the [Geocoding API](https://developers.google.com/maps/documentation/geocoding/), [Elevation API](https://developers.google.com/maps/documentation/elevation/) and [Google Civic Information API](https://developers.google.com/civic-information/): these services have courtesy limits, which allow limited use free of charge. Use the [Google API Console](https://code.google.com/apis/console) to create a Google API project, add the required services (APIs & Auth -> API) and then create an API Key (APIs & Auth -> Credentials). Create a Browser key and leaved the Allowed Referers field blank.

Add your Google Places API key to `src/com/example/appdynamics/placesdemo/GooglePlacesKey.java`

Eclipse ADT Builds
------------------
1.  Import -> Existing Android code into workspace
2.  Properties -> Android -> Android 5.0 (API Level 21)
3.  Build Path -> Configure Build Path -> Libraries -> Add External JARs
4.  Clean and rebuild the project

The following JAR files need to be added to the project:

* [google-api-client-1.19.0.jar](http://search.maven.org/remotecontent?filepath=com/google/api-client/google-api-client/1.19.0/google-api-client-1.19.0.jar)
* [google-api-services-civicinfo-v2-rev7-1.19.0.jar](http://search.maven.org/remotecontent?filepath=com/google/apis/google-api-services-civicinfo/v2-rev7-1.19.0/google-api-services-civicinfo-v2-rev7-1.19.0.jar)
* [google-http-client-1.19.0.jar](http://search.maven.org/remotecontent?filepath=com/google/http-client/google-http-client/1.19.0/google-http-client-1.19.0.jar)
* [google-http-client-android-1.19.0.jar](http://search.maven.org/remotecontent?filepath=com/google/http-client/google-http-client-android/1.19.0/google-http-client-android-1.19.0.jar)
* [google-http-client-gson-1.19.0.jar](http://search.maven.org/remotecontent?filepath=com/google/http-client/google-http-client-gson/1.19.0/google-http-client-gson-1.19.0.jar)
* [gson-2.3.jar](http://search.maven.org/remotecontent?filepath=com/google/code/gson/gson/2.3/gson-2.3.jar)
 
Maven Builds
------------
1. To build: `mvn clean install`
2. To deploy to a connected device or emulator: `mvn android:deploy`
3. To run: `mvn android:run`

To build with different Android platform versions, download the [maven-android-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer) tool and follow the instructions in the README file.  For example, to install Maven libraries for Android-21, run `mvn install -P 5.0` and use the following dependency:
```
<dependency>
  <groupId>android</groupId>
  <artifactId>android</artifactId>
  <version>5.0_r1</version>
  <scope>provided</scope>
</dependency>
```


