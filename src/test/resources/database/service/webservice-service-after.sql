-- updateMatchResultsForWC2014
DELETE FROM match_ WHERE event_id=1;
INSERT INTO match_ (match_id, event_id, match_n, team1_id, team2_id, start_time, round_id, goal_normal_by_team1, goal_normal_by_team2, goal_extra_by_team1, goal_extra_by_team2, goal_penalty_by_team1, goal_penalty_by_team2, participants_rule) VALUES
(1, 1, 1, 6, 12, '2014-06-12 20:00:00', 1, 3, 1, NULL, NULL, NULL, NULL, NULL),
(2, 1, 2, 24, 7, '2014-06-13 16:00:00', 1, 1, 0, NULL, NULL, NULL, NULL, NULL),
(3, 1, 3, 29, 25, '2014-06-13 19:00:00', 1, 1, 5, NULL, NULL, NULL, NULL, NULL),
(4, 1, 4, 8, 3, '2014-06-13 22:00:00', 1, 3, 1, NULL, NULL, NULL, NULL, NULL),
(5, 1, 5, 9, 18, '2014-06-14 16:00:00', 1, 3, 0, NULL, NULL, NULL, NULL, NULL),
(6, 1, 7, 31, 10, '2014-06-14 19:00:00', 1, 1, 3, NULL, NULL, NULL, NULL, NULL),
(7, 1, 8, 14, 21, '2014-06-14 22:00:00', 1, 1, 2, NULL, NULL, NULL, NULL, NULL),
(8, 1, 6, 11, 22, '2014-06-15 01:00:00', 1, 2, 1, NULL, NULL, NULL, NULL, NULL),
(9, 1, 9, 30, 13, '2014-06-15 16:00:00', 1, 2, 1, NULL, NULL, NULL, NULL, NULL),
(10, 1, 10, 15, 19, '2014-06-15 19:00:00', 1, 3, 0, NULL, NULL, NULL, NULL, NULL),
(11, 1, 11, 2, 5, '2014-06-15 22:00:00', 1, 2, 1, NULL, NULL, NULL, NULL, NULL),
(12, 1, 13, 16, 27, '2014-06-16 16:00:00', 1, 4, 0, NULL, NULL, NULL, NULL, NULL),
(13, 1, 12, 20, 26, '2014-06-16 19:00:00', 1, 0, 0, NULL, NULL, NULL, NULL, NULL),
(14, 1, 14, 17, 32, '2014-06-16 22:00:00', 1, 1, 2, NULL, NULL, NULL, NULL, NULL),
(15, 1, 15, 4, 1, '2014-06-17 16:00:00', 1, 2, 1, NULL, NULL, NULL, NULL, NULL),
(16, 1, 17, 6, 24, '2014-06-17 19:00:00', 2, 0, 0, NULL, NULL, NULL, NULL, NULL),
(17, 1, 16, 28, 23, '2014-06-17 22:00:00', 1, 1, 1, NULL, NULL, NULL, NULL, NULL),
(18, 1, 20, 3, 25, '2014-06-18 16:00:00', 2, 2, 3, NULL, NULL, NULL, NULL, NULL),
(19, 1, 19, 29, 8, '2014-06-18 19:00:00', 2, 0, 2, NULL, NULL, NULL, NULL, NULL),
(20, 1, 18, 7, 12, '2014-06-18 22:00:00', 2, 0, 4, NULL, NULL, NULL, NULL, NULL),
(21, 1, 21, 9, 11, '2014-06-19 16:00:00', 2, 2, 1, NULL, NULL, NULL, NULL, NULL),
(22, 1, 23, 31, 14, '2014-06-19 19:00:00', 2, 2, 1, NULL, NULL, NULL, NULL, NULL),
(23, 1, 22, 22, 18, '2014-06-19 22:00:00', 2, 0, 0, NULL, NULL, NULL, NULL, NULL),
(24, 1, 24, 21, 10, '2014-06-20 16:00:00', 2, 0, 1, NULL, NULL, NULL, NULL, NULL),
(25, 1, 25, 30, 15, '2014-06-20 19:00:00', 2, 2, 5, NULL, NULL, NULL, NULL, NULL),
(26, 1, 26, 19, 13, '2014-06-20 22:00:00', 2, 1, 2, NULL, NULL, NULL, NULL, NULL),
(27, 1, 27, 2, 20, '2014-06-21 16:00:00', 2, 1, 0, NULL, NULL, NULL, NULL, NULL),
(28, 1, 29, 16, 17, '2014-06-21 19:00:00', 2, 2, 2, NULL, NULL, NULL, NULL, NULL),
(29, 1, 28, 26, 5, '2014-06-21 22:00:00', 2, 1, 0, NULL, NULL, NULL, NULL, NULL),
(30, 1, 31, 4, 28, '2014-06-22 16:00:00', 2, 1, 0, NULL, NULL, NULL, NULL, NULL),
(31, 1, 32, 23, 1, '2014-06-22 19:00:00', 2, 2, 4, NULL, NULL, NULL, NULL, NULL),
(32, 1, 30, 32, 27, '2014-06-22 22:00:00', 2, 2, 2, NULL, NULL, NULL, NULL, NULL),
(33, 1, 36, 25, 8, '2014-06-23 16:00:00', 3, 2, 0, NULL, NULL, NULL, NULL, NULL),
(34, 1, 35, 3, 29, '2014-06-23 16:00:00', 3, 0, 3, NULL, NULL, NULL, NULL, NULL),
(35, 1, 33, 7, 6, '2014-06-23 20:00:00', 3, 1, 4, NULL, NULL, NULL, NULL, NULL),
(36, 1, 34, 12, 24, '2014-06-23 20:00:00', 3, 1, 3, NULL, NULL, NULL, NULL, NULL),
(37, 1, 39, 21, 31, '2014-06-24 16:00:00', 3, 0, 1, NULL, NULL, NULL, NULL, NULL),
(38, 1, 40, 10, 14, '2014-06-24 16:00:00', 3, 0, 0, NULL, NULL, NULL, NULL, NULL),
(39, 1, 37, 22, 9, '2014-06-24 20:00:00', 3, 1, 4, NULL, NULL, NULL, NULL, NULL),
(40, 1, 38, 18, 11, '2014-06-24 20:00:00', 3, 2, 1, NULL, NULL, NULL, NULL, NULL),
(41, 1, 43, 26, 2, '2014-06-25 16:00:00', 3, 2, 3, NULL, NULL, NULL, NULL, NULL),
(42, 1, 44, 5, 20, '2014-06-25 16:00:00', 3, 3, 1, NULL, NULL, NULL, NULL, NULL),
(43, 1, 41, 19, 30, '2014-06-25 20:00:00', 3, 0, 3, NULL, NULL, NULL, NULL, NULL),
(44, 1, 42, 13, 15, '2014-06-25 20:00:00', 3, 0, 0, NULL, NULL, NULL, NULL, NULL),
(45, 1, 46, 27, 17, '2014-06-26 16:00:00', 3, 2, 1, NULL, NULL, NULL, NULL, NULL),
(46, 1, 45, 32, 16, '2014-06-26 16:00:00', 3, 0, 1, NULL, NULL, NULL, NULL, NULL),
(47, 1, 47, 23, 4, '2014-06-26 20:00:00', 3, 0, 1, NULL, NULL, NULL, NULL, NULL),
(48, 1, 48, 1, 28, '2014-06-26 20:00:00', 3, 1, 1, NULL, NULL, NULL, NULL, NULL),
(49, 1, 49, 6, 8, '2014-06-28 16:00:00', 4, 1, 1, 1, 1, 4, 3, 'A1-B2'),
(50, 1, 50, 9, 31, '2014-06-28 20:00:00', 4, 2, 0, NULL, NULL, NULL, NULL, 'C1-D2'),
(51, 1, 51, 25, 24, '2014-06-29 16:00:00', 4, 2, 1, NULL, NULL, NULL, NULL, 'B1-A2'),
(52, 1, 52, 10, 18, '2014-06-29 20:00:00', 4, 1, 1, 1, 1, 6, 4, 'D1-C2'),
(53, 1, 53, 15, 26, '2014-06-30 16:00:00', 4, 2, 0, NULL, NULL, NULL, NULL, 'E1-F2'),
(54, 1, 54, 16, 1, '2014-06-30 20:00:00', 4, 0, 0, 2, 1, NULL, NULL, 'G1-H2'),
(55, 1, 55, 2, 30, '2014-07-01 16:00:00', 4, 0, 0, 1, 0, NULL, NULL, 'F1-E2'),
(56, 1, 56, 4, 32, '2014-07-01 20:00:00', 4, 0, 0, 2, 1, NULL, NULL, 'H1-G2'),
(57, 1, 57, 15, 16, '2014-07-04 16:00:00', 5, 0, 1, NULL, NULL, NULL, NULL, 'W53-W54'),
(58, 1, 58, 6, 9, '2014-07-04 20:00:00', 5, 2, 1, NULL, NULL, NULL, NULL, 'W49-W50'),
(59, 1, 59, 2, 4, '2014-07-05 16:00:00', 5, 1, 0, NULL, NULL, NULL, NULL, 'W55-W56'),
(60, 1, 60, 25, 10, '2014-07-05 20:00:00', 5, 0, 0, 0, 0, 4, 3, 'W51-W52'),
(61, 1, 61, 16, 6, '2014-07-08 20:00:00', 6, 7, 1, NULL, NULL, NULL, NULL, 'W57-W58'),
(62, 1, 62, 25, 2, '2014-07-09 20:00:00', 6, 0, 0, 0, 0, 2, 4, 'W60-W59'),
(63, 1, 63, 6, 25, '2014-07-12 20:00:00', 7, 0, 3, NULL, NULL, NULL, NULL, 'L61-L62'),
(64, 1, 64, 16, 2, '2014-07-13 19:00:00', 8, 0, 0, 1, 0, NULL, NULL, 'W61-W62');

-- updateMatchResultsForEC2016
DELETE FROM match_ WHERE event_id=2;
INSERT INTO match_ (match_id, event_id, match_n, team1_id, team2_id, start_time, round_id, goal_normal_by_team1, goal_normal_by_team2, goal_extra_by_team1, goal_extra_by_team2, goal_penalty_by_team1, goal_penalty_by_team2, participants_rule) VALUES
(65, 2, 1, 33, 34, '2016-06-10 19:00:00', 9, 2, 1, NULL, NULL, NULL, NULL, NULL),
(66, 2, 2, 35, 36, '2016-06-11 13:00:00', 9, 0, 1, NULL, NULL, NULL, NULL, NULL),
(67, 2, 14, 34, 36, '2016-06-15 16:00:00', 10, 1, 1, NULL, NULL, NULL, NULL, NULL),
(68, 2, 15, 33, 35, '2016-06-15 19:00:00', 10, 2, 0, NULL, NULL, NULL, NULL, NULL),
(69, 2, 25, 34, 35, '2016-06-19 19:00:00', 11, 0, 1, NULL, NULL, NULL, NULL, NULL),
(70, 2, 26, 36, 33, '2016-06-19 19:00:00', 11, 0, 0, NULL, NULL, NULL, NULL, NULL),
(71, 2, 3, 39, 40, '2016-06-11 16:00:00', 9, 2, 1, NULL, NULL, NULL, NULL, NULL),
(72, 2, 4, 37, 38, '2016-06-11 19:00:00', 9, 1, 1, NULL, NULL, NULL, NULL, NULL),
(73, 2, 13, 38, 40, '2016-06-15 13:00:00', 10, 1, 2, NULL, NULL, NULL, NULL, NULL),
(74, 2, 16, 37, 39, '2016-06-16 13:00:00', 10, 2, 1, NULL, NULL, NULL, NULL, NULL),
(75, 2, 27, 38, 39, '2016-06-20 19:00:00', 11, 0, 3, NULL, NULL, NULL, NULL, NULL),
(76, 2, 28, 40, 37, '2016-06-20 19:00:00', 11, 0, 0, NULL, NULL, NULL, NULL, NULL),
(77, 2, 6, 43, 44, '2016-06-12 16:00:00', 9, 1, 0, NULL, NULL, NULL, NULL, NULL),
(78, 2, 7, 41, 42, '2016-06-12 19:00:00', 9, 2, 0, NULL, NULL, NULL, NULL, NULL),
(79, 2, 17, 42, 44, '2016-06-16 16:00:00', 10, 0, 2, NULL, NULL, NULL, NULL, NULL),
(80, 2, 18, 41, 43, '2016-06-16 19:00:00', 10, 0, 0, NULL, NULL, NULL, NULL, NULL),
(81, 2, 29, 42, 43, '2016-06-21 16:00:00', 11, 0, 1, NULL, NULL, NULL, NULL, NULL),
(82, 2, 30, 44, 41, '2016-06-21 16:00:00', 11, 0, 1, NULL, NULL, NULL, NULL, NULL),
(83, 2, 5, 47, 48, '2016-06-12 13:00:00', 9, 0, 1, NULL, NULL, NULL, NULL, NULL),
(84, 2, 8, 45, 46, '2016-06-13 13:00:00', 9, 1, 0, NULL, NULL, NULL, NULL, NULL),
(85, 2, 20, 46, 48, '2016-06-17 16:00:00', 10, 2, 2, NULL, NULL, NULL, NULL, NULL),
(86, 2, 21, 45, 47, '2016-06-17 19:00:00', 10, 3, 0, NULL, NULL, NULL, NULL, NULL),
(87, 2, 31, 46, 47, '2016-06-21 19:00:00', 11, 0, 2, NULL, NULL, NULL, NULL, NULL),
(88, 2, 32, 48, 45, '2016-06-21 19:00:00', 11, 2, 1, NULL, NULL, NULL, NULL, NULL),
(89, 2, 9, 51, 52, '2016-06-13 16:00:00', 9, 1, 1, NULL, NULL, NULL, NULL, NULL),
(90, 2, 10, 49, 50, '2016-06-13 19:00:00', 9, 0, 2, NULL, NULL, NULL, NULL, NULL),
(91, 2, 19, 50, 52, '2016-06-17 13:00:00', 10, 1, 0, NULL, NULL, NULL, NULL, NULL),
(92, 2, 22, 49, 51, '2016-06-18 13:00:00', 10, 3, 0, NULL, NULL, NULL, NULL, NULL),
(93, 2, 35, 50, 51, '2016-06-22 19:00:00', 11, 0, 1, NULL, NULL, NULL, NULL, NULL),
(94, 2, 36, 52, 49, '2016-06-22 19:00:00', 11, 0, 1, NULL, NULL, NULL, NULL, NULL),
(95, 2, 11, 55, 56, '2016-06-14 16:00:00', 9, 0, 2, NULL, NULL, NULL, NULL, NULL),
(96, 2, 12, 53, 54, '2016-06-14 19:00:00', 9, 1, 1, NULL, NULL, NULL, NULL, NULL),
(97, 2, 23, 54, 56, '2016-06-18 16:00:00', 10, 1, 1, NULL, NULL, NULL, NULL, NULL),
(98, 2, 24, 53, 55, '2016-06-18 19:00:00', 10, 0, 0, NULL, NULL, NULL, NULL, NULL),
(99, 2, 33, 54, 55, '2016-06-22 16:00:00', 11, 2, 1, NULL, NULL, NULL, NULL, NULL),
(100, 2, 34, 56, 53, '2016-06-22 16:00:00', 11, 3, 3, NULL, NULL, NULL, NULL, NULL),
(101, 2, 37, 36, 43, '2016-06-25 13:00:00', 12, 1, 1, 1, 1, 5, 6, 'A2-C2'),
(102, 2, 38, 39, 44, '2016-06-25 16:00:00', 12, 1, 0, NULL, NULL, NULL, NULL, 'B1-ACD3'),
(103, 2, 39, 48, 53, '2016-06-25 19:00:00', 12, 0, 0, 0, 1, NULL, NULL, 'D1-BEF3'),
(104, 2, 40, 33, 51, '2016-06-26 13:00:00', 12, 2, 1, NULL, NULL, NULL, NULL, 'A1-CDE3'),
(105, 2, 41, 41, 40, '2016-06-26 16:00:00', 12, 3, 0, NULL, NULL, NULL, NULL, 'C1-ABF3'),
(106, 2, 42, 56, 49, '2016-06-26 19:00:00', 12, 0, 4, NULL, NULL, NULL, NULL, 'F1-E2'),
(107, 2, 43, 50, 45, '2016-06-27 16:00:00', 12, 2, 0, NULL, NULL, NULL, NULL, 'E1-D2'),
(108, 2, 44, 37, 54, '2016-06-27 19:00:00', 12, 1, 2, NULL, NULL, NULL, NULL, 'B2-F2'),
(109, 2, 45, 43, 53, '2016-06-30 19:00:00', 13, 1, 1, 1, 1, 4, 6, 'W37-W39'),
(110, 2, 46, 39, 49, '2016-07-01 19:00:00', 13, 3, 1, NULL, NULL, NULL, NULL, 'W38-W42'),
(111, 2, 47, 41, 50, '2016-07-02 19:00:00', 13, 1, 1, 1, 1, 7, 6, 'W41-W43'),
(112, 2, 48, 33, 54, '2016-07-03 19:00:00', 13, 5, 2, NULL, NULL, NULL, NULL, 'W40-W44'),
(113, 2, 49, 53, 39, '2016-07-06 19:00:00', 14, 2, 0, NULL, NULL, NULL, NULL, 'W45-W46'),
(114, 2, 50, 41, 33, '2016-07-07 19:00:00', 14, 0, 2, NULL, NULL, NULL, NULL, 'W47-W48'),
(115, 2, 51, 53, 33, '2016-07-10 19:00:00', 15, 0, 0, 1, 0, NULL, NULL, 'W49-W50');