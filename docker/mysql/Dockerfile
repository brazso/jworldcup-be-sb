FROM mysql:8.0.25

# Accept connections from any IP address
RUN set -x; \
  echo "[mysqld]\nbind-address=0.0.0.0" >> /etc/mysql/conf.d/docker.cnf

## enables connection to localhost instead of 127.0.0.1 -> mysqladmin: connect to server at 'localhost' failed
#RUN set -x; \
#  echo "[client]\nprotocol=tcp" >> /etc/mysql/conf.d/docker.cnf

## Loads init database scripts.
COPY docker/mysql/worldcup_init.sql /docker-entrypoint-initdb.d/01-worldcup_init.sql
# last 2 source files might not exist in the COPY, wildcard is necessary at the files
COPY src/test/resources/database/worldcup_test.sql docker/mysql/backup/[w]orldcup.sql docker/mysql/backup/[w]orldcup.sql.gz /docker-entrypoint-initdb.d/ 
RUN set -x; \
  if [ -f /docker-entrypoint-initdb.d/worldcup.sql ]; then \
    mv /docker-entrypoint-initdb.d/worldcup.sql /docker-entrypoint-initdb.d/02-worldcup.sql; \
    rm -f /docker-entrypoint-initdb.d/worldcup_test.sql; \
    rm -f /docker-entrypoint-initdb.d/worldcup.sql.gz; \
  elif [ -f /docker-entrypoint-initdb.d/worldcup.sql.gz ]; then \
    mv /docker-entrypoint-initdb.d/worldcup.sql.gz /docker-entrypoint-initdb.d/02-worldcup.sql.gz; \
    rm -f /docker-entrypoint-initdb.d/worldcup_test.sql; \
    rm -f /docker-entrypoint-initdb.d/worldcup.sql; \
  else \
    mv /docker-entrypoint-initdb.d/worldcup_test.sql /docker-entrypoint-initdb.d/02-worldcup.sql; \
  fi
COPY src/test/resources/database/worldcup_test.sql /docker-entrypoint-initdb.d/03-worldcup_test.sql
RUN set -x; \
  echo "USE worldcup_test;" > /docker-entrypoint-initdb.d/04-worldcup_test.sql; \
  cat /docker-entrypoint-initdb.d/03-worldcup_test.sql >> /docker-entrypoint-initdb.d/04-worldcup_test.sql; \
  rm /docker-entrypoint-initdb.d/03-worldcup_test.sql
