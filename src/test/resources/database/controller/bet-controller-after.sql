-- retrieveBetsByEventAndUser
DELETE FROM bet WHERE bet_id=1;

-- retrieveBetsByMatchAndUserGroup
DELETE FROM user__user_group WHERE user_id=2 AND user_group_id=1;
DELETE FROM user_group WHERE user_group_id=1;

-- saveBet
DELETE FROM bet WHERE bet_id=2;