-- retrieveUserOfEvent
INSERT INTO user_of_event (user_of_event_id, user_id, event_id, fav_group_team_id, fav_knockout_team_id) VALUES
(1, 2, 1, 1, 2);

-- saveUserOfEvent
INSERT INTO user (user_id, full_name, login_name, login_password, reset_password, email_addr, email_new, user_status_id, token, zone_id, modification_time) VALUES
(6, 'Normal2 Dummy', 'normal2', '8538e3eb986d8c7643edc79923a640a672b29b1a121a342e1c3f7d2c30c7a65596f45c5f3a4f811caa67d14f126980ca17cd4640b22eeb410bd7a7e738620576', NULL, 'normal2.dummy@zematix.hu', NULL, 4, 'IF2YCcPnNulH8UEEkAI2', 'Europe/Budapest', '2024-02-05 12:00:00');
INSERT INTO user__role (user_id, role_id) VALUES
(6, 3);