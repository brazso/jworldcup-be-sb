BEGIN {
	print "SET FOREIGN_KEY_CHECKS = 0;";
}
{
	line = $0
	if (match(line, /CREATE TABLE `[^`]+`/) || match(line, /CREATE TABLE IF EXISTS `[^`]+`/)) {
		split(line, arr, "`");
		printf "DROP TABLE IF EXISTS `%s`;\n", arr[2];
	}
}
END {
	print "SET FOREIGN_KEY_CHECKS = 1;";
	print "COMMIT;"
}
