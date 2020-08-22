age

```sql
select current_date, age('2020-08-18 10:10:10'::timestamp);

-- current date         age
-- 8/21/20, 12:00 AM	2 days 13:49:50

```

date_part

```sql
select date_part('hour', visit_start_local) hr, count(*) vst_counts
from museum_visit
where visit_start_local >= '2020-08-01 00:00:00'
and visit_start_local < '2020-08-02 00:00:00'
group by hr
order by vst_counts desc;
```
