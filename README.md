# jworldcup-be-sb
JWorldcup soccer bet game backend created on Java / Spring Boot

## For developers

### 3rd party tools
In order to start development of the project, some tools must be installed on the developer machine.
* docker

```
$ sudo snap install docker
```

### Project database

Data persisted to mysql database in the project. There is a remote database for production and local databases for the developers. Additonal info about the developer databases is described at the docker images header.

### Docker images

There are more docker images in the project which must be started by developers before launching the IDE. At least it is a must for the test phase in the build process, or for just running the application. All of them are located in the docker folder. One of them contains development databases, 2 mysql databases, for unit and integration tests. The other image contains a phpmyadmin application, an excellent tool to manage mysql databases. The latter one is also served to create database export file for the application tests, it is always loaded automatically in the beginning of the unit test phase.

This is how to start the images from command prompt. It should be executed from the docker folder.

```
$ docker-compose up
```

It is recommended to shutdown the running images if the work is over. It should be executed from the docker folder.

```
$ docker-compose down
```

If you have to update docker mysql images in docker/mysql folder, the docker images must be rebuilt again. It should be executed from the docker folder.

```
$ docker-compose build
```
### Run application from Gradle

full build
```
gradle clean
gradle build
```
build without test
```
gradle build -x test
'''

run app using development profile
```
gradle -Dspring.profiles.active=development bootRun
```

run app using production profile, ie. without any profile
```
gradle bootRun
```

stop app
```
gradle -stop
```

### PhpMyAdmin

After the docker images started you may open phpMyAdmin in a web browser using [http://localhost:8080](http://localhost:8080) url.

Select the local developer server as a developer. Use worldcup / worldcup.org for authentication.
There are 2 databases enlisted
* worldcup - application linked to it
* worldcup_test - unit tests may use it unless the test phase is configured to use only memory

### Gradle usage

#### Gradle wrapper upgrade

Later spring boot versions needs at least Gradle 6.8.x. If you have an earlier version, upgrade it!

```
brazso@mars:~/work/jworldcup/jworldcup-be-sb$ ./gradlew wrapper --gradle-version=6.9 --distribution-type=bin
```

#### Gradle test

After gradle test (or build if test is not disabled there) the result can be checked here:

file://{$buildDir}/reports/tests/test/index.html

### Eclipse IDE
If you use Eclipse IDE it is recommended to install the following items in Eclipse Marketplace
* Spring Tools 4 (Spring tool, e.g. running application easily as Spring Boot App)
* DBeaver 21.0.3 (if you want a more convenient database tool than PhpMyAdmin included in docker image)
* Buildship Grandle Integration 3.0 (usually it is included with Eclipse, already installed)
* SonarLint 5.9 (sourcecode analyser tool)

### Rest API
* Swagger UI page is [http://localhost:8090/jworldcup/swagger-ui.html](http://localhost:8090/jworldcup/swagger-ui.html)
* OpenAPI description is available at the following url for json format: [http://localhost:8090/jworldcup/v3/api-docs](http://localhost:8090/jworldcup/v3/api-docs)
