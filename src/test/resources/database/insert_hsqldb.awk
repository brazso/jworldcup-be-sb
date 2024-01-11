BEGIN {
	isInsert = 0;
	isAlterTable = 0;

	# means insert block specified by index must be placed after its value block
	# there can be more shiftMap rules, but the indices must follow the order as they are in the source file
	shiftMap["match_"] = "team"; # "match_" must be placed after "team"
}
{
	line = $0;

	if (match(line, "^--")) {
		next;
	}

	if (match(line, "^SET time_zone =")) {
		print "SET TIME ZONE INTERVAL '+00:00' HOUR TO MINUTE;"
		next;
	}

	alteredLine = line; # replace all `abc` -> abc in line, hsqldb is case sensitive
	while (match(alteredLine,"`[^`]*`")) {
		fieldName = substr(alteredLine,RSTART+1,RLENGTH-2);
		sub("`" fieldName "`", fieldName, alteredLine);
	}

	if (match(line, "^INSERT INTO ")) {
		isInsert = 1;
		isInsertHead = 1;

		if (match(line,"`[^`]*`")) {
			tableName = substr(line,RSTART+1,RLENGTH-2);
		}

		# if it is amongs the keys of shiftMap, this insert block must be saved
		if (tableName in shiftMap) {
			isBuffered = 1;
		}
	}

	if (isInsert) {
		if (isInsertHead) {
			insertHead = alteredLine;
			isInsertHead = 0;
		}
		else {
			isInsertEnd = match(alteredLine, ";$");
			if (match(alteredLine, ",$")) {
				sub(/,$/, ";", alteredLine);
			}
			if (!isBuffered) {
				printf "%s %s\n", insertHead, alteredLine;
			}
			else {
				bufferMap[tableName] = bufferMap[tableName] insertHead " " alteredLine "\n";
			}

			if (isInsertEnd) {
				# if it is amongs the values of shiftMap, the key(s) must be placed after
				if (!isBuffered) {
					target = tableName;
					while (target) {
						foundKey = "";
						for (key in shiftMap) {
							if (shiftMap[key] == target) {
								foundKey = key;
								break;
							}
						}
						if (foundKey) {
							printf bufferMap[foundKey];

							# recursive call is a must
							# start another search
							target = foundKey;
						}
						else {
							target = "";
						}
					}
				}

				isInsert = 0;
				isBuffered = 0;
			}
		}
	}

	# AUTO_INCREMENT identity column values must be set

	if (match(line, "^ALTER TABLE ")) {
		isAlterTable = 1;
		isAlterTabledHead = 1;

		if (match(line,"`[^`]*`")) {
			tableName = substr(line,RSTART+1,RLENGTH-2);
		}
	}

	if (isAlterTable) {
		if (isAlterTableHead) {
			isAlterTableHead = 0;
		}
		else {
			isAlterTableEnd = match(alteredLine, ";$");
			if (match(line,"`[^`]*`")) {
				columnName = substr(line,RSTART+1,RLENGTH-2);
			}
			if (match(line,"AUTO_INCREMENT=[0-9]+")) {
				autoIncrementValue = substr(line,RSTART+15,RLENGTH-15);
				printf "ALTER TABLE %s ALTER COLUMN %s RESTART WITH %s;\n", tableName, columnName, autoIncrementValue;
				isAlterTable = 0;
			}
			if (isAlterTableEnd) {
				isAlterTable = 0;
			}
		}
	}

}
END {
	print "COMMIT;";
}
