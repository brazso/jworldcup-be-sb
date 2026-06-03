DELETE FROM user__user_group WHERE user_group_id in (1, 2, 3);
DELETE FROM user_group WHERE user_group_id in (1, 2, 3);

-- retrieveUserPositions
DELETE FROM bet WHERE bet_id in (1,2);

DELETE FROM user__role WHERE user_id in (6);
DELETE FROM user WHERE user_id in (6);
