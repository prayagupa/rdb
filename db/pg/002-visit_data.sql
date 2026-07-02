INSERT INTO visiting_user(user_name)
VALUES ('upadhyay')
ON CONFLICT DO NOTHING;

INSERT INTO museum_visit(
    user_id,
    museum_name,
    department,
    visit_start_tz,
    visit_start_local,
    visit_end_tz,
    visit_end_local
)
SELECT
     1,
    'The California Academy of Sciences',
    'Front door',
    '2019-08-02 18:00:00.000000-08:00',
    '2019-08-02 18:00:00.000000',
    '2019-08-02 18:10:00.000000-08:00',
    '2019-08-02 18:10:00.000000'
WHERE NOT EXISTS ( SELECT museum_name FROM museum_visit WHERE museum_name='The California Academy of Sciences');

INSERT INTO museum_visit(
    user_id,
    museum_name,
    department,
    visit_start_tz,
    visit_start_local,
    visit_end_tz,
    visit_end_local
)
SELECT
     1,
    'Crocker Art Museum',
    'Roof',
    '2019-09-02 18:00:00.000000-08:00',
    '2019-09-02 18:00:00.000000',
    '2019-09-02 18:10:00.000000-08:00',
    '2019-09-02 18:10:00.000000'
WHERE NOT EXISTS ( SELECT museum_name FROM museum_visit WHERE museum_name='Crocker Art Museum');

INSERT INTO museum_visit(
    user_id,
    museum_name,
    department,
    visit_start_tz,
    visit_start_local,
    visit_end_tz,
    visit_end_local
)
SELECT
     1,
    'Talkot View Tower',
    'Roof',
    '2019-09-02 18:00:00.000000-08:00',
    '2019-09-02 18:00:00.000000',
    '2019-09-02 18:10:00.000000-08:00',
    '2019-09-02 18:10:00.000000'
WHERE NOT EXISTS ( SELECT museum_name FROM museum_visit WHERE museum_name='Talkot View Tower');

INSERT INTO museum_visit(
    user_id,
    museum_name,
    department,
    visit_start_tz,
    visit_start_local,
    visit_end_tz,
    visit_end_local
)
SELECT
     1,
    'SF Museum of Modern Art',
    'Roof',
    '2019-09-02 18:00:00.000000-08:00',
    '2019-09-02 18:00:00.000000',
    '2019-09-02 18:10:00.000000-08:00',
    '2019-09-02 18:10:00.000000'
WHERE NOT EXISTS ( SELECT museum_name FROM museum_visit WHERE museum_name='SF Museum of Modern Art');
