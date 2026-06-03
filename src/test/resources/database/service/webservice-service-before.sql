-- updateMatchResultsForWC2014
UPDATE match_ SET goal_normal_by_team1=null, goal_normal_by_team2=null, goal_extra_by_team1=null, goal_extra_by_team2=null, goal_penalty_by_team1=null, goal_penalty_by_team2=null WHERE event_id=1;
UPDATE match_ m SET m.team1_id=null, m.team2_id=null WHERE m.event_id=1 AND m.round_id in (SELECT round_id FROM round WHERE event_id=m.event_id AND is_groupmatch=0);

-- updateMatchResultsForEC2016
UPDATE match_ SET goal_normal_by_team1=null, goal_normal_by_team2=null, goal_extra_by_team1=null, goal_extra_by_team2=null, goal_penalty_by_team1=null, goal_penalty_by_team2=null WHERE event_id=2;
UPDATE match_ m SET m.team1_id=null, m.team2_id=null WHERE m.event_id=2 AND m.round_id in (SELECT round_id FROM round WHERE event_id=m.event_id AND is_groupmatch=0);
