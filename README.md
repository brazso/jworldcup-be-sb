# jworldcup-be-sb
JWorldcup soccer bet game backend created on Java / Spring Boot

## For developers

### 3rd party tools
In order to start development of the project, some tools must be installed on the developer machine.
* docker
sudo snap install docker

### Project database

Data persisted to mysql database in the project. There is a remote database for production and local databases for the developers. Additonal info about the developer databases is described at the docker images header.

### Docker images

There are more docker images in the project which must be started by developers before launching the IDE. At least it is a must for the test phase in the build process, or for just running the application. All of them are located in the docker folder. One of them contains development databases, 2 mysql databases, for unit and integration tests. The other image contains a phpmyadmin application, an excellent tool to manage mysql databases. The latter one is also served to create database export file for the application tests, it is always loaded automatically in the beginning of the unit test phase.

This is how to start the images from command prompt. It should be executed from the docker folder.
$ docker-compose up

### PhpMyAdmin

After the docker images started you may open phpMyAdmin in a web browser using this url.
$ http://localhost:8080
Select the local developer server as a developer. Use worldcup / worldcup.org for authentication.
There are 2 databases enlisted
* worldcup - application linked to it
* worldcup_test - unit tests may use it unless the test phase is configured to use only memory

### Gradle usage

After gradle test (or build if test is not disabled there) the result can be checked here:
file://{$buildDir}/reports/tests/test/index.html
