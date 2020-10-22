BEGIN {
	isInsert = 0;

	# means insert block specified by index must be placed after its value block
	# there can be more shiftMap rules, but the indices must follow the order as they are in the source file
	shiftMap["match_"] = "team"; # "match_" must be placed after "team"
	shiftMap["user"] = "user_status";
	shiftMap["user_group"] = "user";
}
{
	line = $0
	if (match(line, "^--")) {
		next;
	}

	if (match(line, "^SET time_zone =")) {
		print $0;
	}

	if (match(line, "^INSERT INTO ")) {
		isInsert = 1;

		if (match(line,"`[^`]*`")) {
			tableName = substr(line,RSTART+1,RLENGTH-2);
		}

		# if it is amongs the keys of shiftMap, this insert block must be saved
		if (tableName in shiftMap) {
			isBuffered = 1;
		}
	}
	if (isInsert) {
		if (!isBuffered) {
			printf "%s", line;
		}
		else {
			bufferMap[tableName] = bufferMap[tableName] line;
		}

		if (match(line, ";$")) {
			if (!isBuffered) {
				printf "\n";
			}
			else {
				bufferMap[tableName] = bufferMap[tableName] "\n";
			}

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
END {
	print "COMMIT;";
}
