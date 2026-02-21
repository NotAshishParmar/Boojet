-- 1) Rename Work Pay -> Salar/Wages
update categories
set name = 'Salary/Wages'
where user_id = 1
  and upper(code) = upper('INCOME_PAY');

-- 2) Hard delete the income subcategories
delete from categories c
where c.user_id = 1
  and upper(c.code) in (upper('INCOME_GIFT'), upper('INCOME_INTEREST'), upper('INCOME_OTHER'))
  and not exists (
    select 1
    from transactions t
    where t.category_id = c.id
  );