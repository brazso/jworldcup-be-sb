FROM mysql:8.0.19
#FROM mysql:5.7.26

## Nothing to do with timezones, mysql (this version: 5.7.26) already contains them
#RUN set -x; \
#  echo "USE mysql;" > /docker-entrypoint-initdb.d/timezones.sql \
#  &&  mysql_tzinfo_to_sql /usr/share/zoneinfo >> /docker-entrypoint-initdb.d/timezones.sql
#RUN set -x; \
#  printf "[mysqld]\ndefault-time-zone = 'UTC'\n" > /etc/mysql/conf.d/docker.cnf

# Accept connections from any IP address
RUN set -x; \
  echo "[mysqld]\nbind-address=0.0.0.0" >> /etc/mysql/conf.d/docker.cnf

## enables connection to localhost instead of 127.0.0.1 -> mysqladmin: connect to server at 'localhost' failed
#RUN set -x; \
#  echo "[client]\nprotocol=tcp" >> /etc/mysql/conf.d/docker.cnf

## Loads init database scripts.
COPY docker/mysql/worldcup_init.sql /docker-entrypoint-initdb.d/01-worldcup_init.sql
COPY docker/mysql/worldcup.sql /docker-entrypoint-initdb.d/02-worldcup.sql
COPY src/test/resources/database/worldcup_test.sql /docker-entrypoint-initdb.d/03_worldcup_test.sql
RUN set -x; \
  echo "USE worldcup_test;" > /docker-entrypoint-initdb.d/04-worldcup_init.sql; \
  cat /docker-entrypoint-initdb.d/03_worldcup_test.sql >> /docker-entrypoint-initdb.d/04-worldcup_init.sql; \
  rm /docker-entrypoint-initdb.d/03_worldcup_test.sql
