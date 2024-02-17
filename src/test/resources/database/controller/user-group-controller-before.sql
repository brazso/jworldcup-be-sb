INSERT INTO user (user_id, full_name, login_name, login_password, reset_password, email_addr, email_new, user_status_id, token, zone_id, modification_time) VALUES
(6, 'Normal2 Dummy', 'normal2', '8538e3eb986d8c7643edc79923a640a672b29b1a121a342e1c3f7d2c30c7a65596f45c5f3a4f811caa67d14f126980ca17cd4640b22eeb410bd7a7e738620576', NULL, 'normal2.dummy@zematix.hu', NULL, 4, 'IF2YCcPnNulH8UEEkAI2', 'Europe/Budapest', '2024-02-05 12:00:00');
INSERT INTO user__role (user_id, role_id) VALUES
(6, 3);

INSERT INTO user_group (user_group_id, priority, name, event_id, owner, is_public_visible, is_public_editable) VALUES
(1, 2, 'Zematix', 1, 2, 1, 0);
INSERT INTO user__user_group (user_id, user_group_id) VALUES
(2, 1),
(6, 1);

-- retrieveUserPositions
INSERT INTO bet (bet_id, event_id, user_id, match_id, goal_normal_by_team1, goal_normal_by_team2) VALUES
(1, 1, 2, 1, 2, 0);
INSERT INTO bet (bet_id, event_id, user_id, match_id, goal_normal_by_team1, goal_normal_by_team2) VALUES
(2, 1, 6, 1, 0, 1);
