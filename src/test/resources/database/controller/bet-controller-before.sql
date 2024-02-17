-- retrieveBetsByEventAndUser
INSERT INTO bet (bet_id, event_id, user_id, match_id, goal_normal_by_team1, goal_normal_by_team2) VALUES
(1, 1, 2, 1, 2, 0);

-- retrieveBetsByMatchAndUserGroup
INSERT INTO user_group (user_group_id, priority, name, event_id, owner, is_public_visible, is_public_editable) VALUES
(1, 2, 'Zematix', 1, 2, 1, 0);
INSERT INTO user__user_group (user_id, user_group_id) VALUES
(2, 1);
