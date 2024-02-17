INSERT INTO user (user_id, full_name, login_name, login_password, reset_password, email_addr, email_new, user_status_id, token, zone_id, modification_time) VALUES
(6, 'Normal2 Dummy', 'normal2', '8538e3eb986d8c7643edc79923a640a672b29b1a121a342e1c3f7d2c30c7a65596f45c5f3a4f811caa67d14f126980ca17cd4640b22eeb410bd7a7e738620576', NULL, 'normal2.dummy@zematix.hu', NULL, 4, 'IF2YCcPnNulH8UEEkAI2', 'Europe/Budapest', '2024-02-05 12:00:00');
INSERT INTO user__role (user_id, role_id) VALUES
(6, 3);
-- processRegistrationToken
INSERT INTO user (user_id, full_name, login_name, login_password, reset_password, email_addr, email_new, user_status_id, token, zone_id, modification_time) VALUES
(7, 'Registration Dummy', 'registration', '3f3786256b7bccf0c6616d23632e2308a3abb498fc6645517d61bde6722e31c5f2afe6f9b4a4a3eea599d7eb6da9422372373dc4547b323b22599ee6555e7ec4', NULL, 'registration.dummy@zematix.hu', NULL, 5, 'IF2YCcPnregistration', 'Europe/Budapest', '2024-02-05 12:00:00');
INSERT INTO user__role (user_id, role_id) VALUES
(7, 3);
-- processChangeEmailToken
INSERT INTO user (user_id, full_name, login_name, login_password, reset_password, email_addr, email_new, user_status_id, token, zone_id, modification_time) VALUES
(8, 'Email Dummy', 'email', '3f3786256b7bccf0c6616d23632e2308a3abb498fc6645517d61bde6722e31c5f2afe6f9b4a4a3eea599d7eb6da9422372373dc4547b323b22599ee6555e7ec4', NULL, 'email.dummy@zematix.hu', 'email-new.dummy@zematix.hu', 4, 'IF2YCcPnNulH8UEemail', 'Europe/Budapest', '2024-02-05 12:00:00');
INSERT INTO user__role (user_id, role_id) VALUES
(8, 3);
-- processResetPasswordToken
INSERT INTO user (user_id, full_name, login_name, login_password, reset_password, email_addr, email_new, user_status_id, token, zone_id, modification_time) VALUES
(9, 'Reset Dummy', 'reset', '3f3786256b7bccf0c6616d23632e2308a3abb498fc6645517d61bde6722e31c5f2afe6f9b4a4a3eea599d7eb6da9422372373dc4547b323b22599ee6555e7ec4', '8538e3eb986d8c7643edc79923a640a672b29b1a121a342e1c3f7d2c30c7a65596f45c5f3a4f811caa67d14f126980ca17cd4640b22eeb410bd7a7e738620576', 'reset.dummy@zematix.hu', NULL, 4, 'IF2YCcPnNulH8UEreset', 'Europe/Budapest', '2024-02-05 12:00:00');
INSERT INTO user__role (user_id, role_id) VALUES
(9, 3);

