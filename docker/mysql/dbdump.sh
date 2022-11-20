#!/usr/bin/bash

# add /root/.my.cnf file for mysqldump usage without password
# [mysqldump]
cat > .my.cnf <<EOL
[mysqldump]
user=worldcup
password=secret
EOL
#

mysqldump -u worldcup --no-tablespaces --single-transaction --quick --lock-tables=false worldcup | gzip > "/root/worldcup.zematix.hu/mysql/backup/worldcup_`date +"%Y%m%d%H%M%S"`.sql.gz"
