INSERT INTO visiting_user(user_name)
SELECT ('updupdup')
WHERE NOT EXISTS (SELECT user_name from visiting_user where user_name='updupdup');

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
    'Dharahara centre',
    'Front door',
    '2019-08-02 18:00:00.000000-8',
    '2019-08-02 18:00:00.000000-8',
    '2019-08-02 18:10:00.000000-8',
    '2019-08-02 18:10:00.000000-8'
WHERE NOT EXISTS ( SELECT museum_name FROM museum_visit WHERE museum_name='Dharahara centre');

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
    'Golai centre',
    'Roof',
    '2019-09-02 18:00:00.000000-8',
    '2019-09-02 18:00:00.000000-8',
    '2019-09-02 18:10:00.000000-8',
    '2019-09-02 18:10:00.000000-8'
WHERE NOT EXISTS ( SELECT museum_name FROM museum_visit WHERE museum_name='Golai centre');

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
    '2019-09-02 18:00:00.000000-8',
    '2019-09-02 18:00:00.000000-8',
    '2019-09-02 18:10:00.000000-8',
    '2019-09-02 18:10:00.000000-8'
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
    'Khaptad Trail',
    'Roof',
    '2019-09-02 18:00:00.000000-8',
    '2019-09-02 18:00:00.000000-8',
    '2019-09-02 18:10:00.000000-8',
    '2019-09-02 18:10:00.000000-8'
WHERE NOT EXISTS ( SELECT museum_name FROM museum_visit WHERE museum_name='Khaptad Trail');
