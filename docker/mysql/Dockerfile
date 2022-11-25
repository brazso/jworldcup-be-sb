FROM mysql:8.0.25

ARG ENV_POSTFIX
ARG MYSQL_USER
ARG MYSQL_PASSWORD

## install zip
#RUN set -x; \
#  apt-get update && \ 
#  apt-get install -y zip && \
#  rm -rf /var/lib/apt/lists/*

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
#COPY docker/mysql/worldcup.sql /docker-entrypoint-initdb.d/02-worldcup.sql
COPY src/test/resources/database/worldcup_test.sql docker/mysql/backup/worldcup.sql /docker-entrypoint-initdb.d/
RUN set -x; \
  if [ -f /docker-entrypoint-initdb.d/worldcup.sql ]; \
  then \
    mv /docker-entrypoint-initdb.d/worldcup.sql /docker-entrypoint-initdb.d/02-worldcup.sql; \
    rm /docker-entrypoint-initdb.d/worldcup_test.sql; \
  else \
    mv /docker-entrypoint-initdb.d/worldcup_test.sql /docker-entrypoint-initdb.d/02-worldcup.sql; \
  fi
#COPY src/test/resources/database/worldcup_test.sql /docker-entrypoint-initdb.d/02-worldcup.sql
#COPY ../docker-shares/mysql/backup/worldcu[p].sql /docker-entrypoint-initdb.d/02-worldcup.sql # pattern used inside that non-existing file do not invoke error
COPY src/test/resources/database/worldcup_test.sql /docker-entrypoint-initdb.d/03-worldcup_test.sql
RUN set -x; \
  echo "USE worldcup_test;" > /docker-entrypoint-initdb.d/04-worldcup_init.sql; \
  cat /docker-entrypoint-initdb.d/03-worldcup_test.sql >> /docker-entrypoint-initdb.d/04-worldcup_init.sql; \
  rm /docker-entrypoint-initdb.d/03-worldcup_test.sql

## Create /root/.my.cnf file for to support mysqldump usage without password
RUN set -x; \
  echo "[mysqldump]\n\
user=$MYSQL_USER\n\
password=$MYSQL_PASSWORD"\
  >> /root/.my.cnf

## Copy dbdump.sh which can be used to create dump from database
RUN set -x; \
  mkdir -p /root/jworldcup/mysql/backup
COPY docker/mysql/dbdump.sh /root/jworldcup/mysql/
RUN set -x; \
  chmod u+x /root/jworldcup/mysql/dbdump.sh

## Daily call of dumpdb.sh 
RUN set -x; \
  echo "#!/bin/sh\n\n\
cd /root/jworldcup/mysql || exit 0\n\
./dbdump.sh"\
  >> /etc/cron.daily/dbdump
RUN set -x; \
  chmod 755 /etc/cron.daily/dbdump
