#!/bin/bash
mysqldump -u worldcup --no-tablespaces --single-transaction --quick --lock-tables=false worldcup | gzip > "/root/jworldcup/mysql/backup/worldcup_`date +"%Y%m%d%H%M%S"`.sql.gz"
