- date time fields can be suffixed with `_on` or `_datetime_tz`
Ex. 
```
id, created_datetime, modified_datetime, deleted_datetime
id, event_datetime_utc, event_datetime_local
```


- Boolean fields should start with `is_`, `can_`, `should_`, `has_` question. Ex.
```bash
creative_id, is_deleted
region_id, should_track_user
```
