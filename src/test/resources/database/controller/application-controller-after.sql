-- retrieveTopUsers
DELETE FROM bet WHERE bet_id=1;

-- retrieveChats
DELETE FROM chat WHERE chat_id in (1, 2);
DELETE FROM user__user_group WHERE user_id=2 AND user_group_id=1;
DELETE FROM user_group WHERE user_group_id=1;
