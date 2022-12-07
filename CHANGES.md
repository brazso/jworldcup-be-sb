## Changes

### Version 1.0.7 / 2022-12-07

* Feature: score gained on favourite team supplemented by asterisk

### Version 1.0.6 / 2022-12-06

* User activity is displayed at chat page because online status is incomprehensible
* Feature: automatically copying user's favourite team from group stage to knockout one when the team gets to knockout stage
* Fix: Jwt refresh token invalidation at logout
* Fix: WC2022 knockout matches might have overtime before penalties

### Version 1.0.5 / 2022-12-02

* Jwt refresh token used with extended expiration age

### Version 1.0.4 / 2022-12-01

* Fix: LazyInitializationException at selecting favourite teams page

### Version 1.0.3 / 2022-11-26

* Fix: wsdl absolute file path changed to relative
* Fix: wmk openligadb tournament label modification

### Version 1.0.2 / 2022-11-26

* Generation of Spring Boot application log into file in production mode
* Fix: call of db export from mysql docker container

### Version 1.0.1 / 2022-11-22

* MySql docker backup support in production mode
* Fix: Other's tips incorrectly read favourite teams, might have duplicated displayed scores at bets

### Version 1.0.0 / 2022-11-18

* Initial version developed and built on Spring Boot 2.5.7
