# jworldcup-be-sb
JWorldcup soccer bet game backend created on Java / Spring Boot used by jworldcup-ui-an frontend

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

This is how to start the images from command prompt. It should be executed from the project root folder, where docker folder exists.

```
$ docker-compose up
```

It is recommended to shutdown the running images if the work is over.

```
$ docker-compose down
```

If you have to update docker mysql images in docker/mysql folder, the docker images must be rebuilt again.

```
$ docker-compose build
```

Or you can start the images with rebuilding those before in one command.

```
$ docker-compose up --build
```

The production application also runs in docker containers. There are the altered starter and finish commands for production stage, where build argument is optional at starter.
```
$ docker-compose --profile production --env-file .env.prod up --build -d
$ docker-compose --profile production down
```

If you just want to peek to a running docker container, e.g. rabbitmq, run the following command. You may retrive all used running docker services from docker-compose.yml file.

```
$ docker exec -it jworldcup.rabbitmq bash
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
```

run app using development profile
```
gradle -Dspring.profiles.active=develop bootRun
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

After the docker images started you may open phpMyAdmin in a web browser using [http://localhost:8100](http://localhost:8100) url.

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
If you use Eclipse IDE (4.23 2022-03) it is recommended to install the following items in Eclipse Marketplace
* Install lombok.jar (v1.18.22), see also [https://www.baeldung.com/lombok-ide#eclipse](https://www.baeldung.com/lombok-ide#eclipse)
* Java 17 Support for Eclipse 2021-09 (unsupported natively)
* Spring Tools 4 (Spring tool, e.g. running application easily as Spring Boot App)
* DBeaver 21.2.5 (if you want a more convenient database tool than PhpMyAdmin included in docker image)
* SonarLint 7.3.1 (sourcecode analyser tool)

### Rest API
* Swagger UI page is [http://localhost:8090/jworldcup-api/swagger-ui.html](http://localhost:8090/jworldcup-api/swagger-ui.html)
* OpenAPI description is available at the following url for json format: [http://localhost:8090/jworldcup-api/v3/api-docs](http://localhost:8090/jworldcup-api/v3/api-docs)

### Useful links

- [A secure way to encrypt any password in the config file in a Spring Boot project](https://medium.com/engineering-jio-com/a-secure-way-to-encrypt-any-password-in-the-config-file-in-a-spring-boot-project-20d12436b4b9)
