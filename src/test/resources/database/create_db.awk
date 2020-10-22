BEGIN {
	isInsert = 0;
}
{
	line = $0
	if (match(line, "^--") || match(line, "^COMMIT;$")) {
		next;
	}
	if (match(line, "^INSERT ")) {
		isInsert = 1;
	}
	if (!isInsert) {
		printf "%s", line;
	}
	if (match(line, ";$")) {
		if (!isInsert) {
			printf "\n";
		}
		else {
			isInsert = 0;
		}
	}
}
END {
	print "COMMIT;";
}
