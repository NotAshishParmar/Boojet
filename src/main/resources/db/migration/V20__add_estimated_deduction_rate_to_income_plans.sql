ALTER TABLE income_plans
ADD COLUMN estimated_deduction_rate NUMERIC(5,4);

UPDATE income_plans
SET estimated_deduction_rate = 0.2200
WHERE estimated_deduction_rate IS NULL;

ALTER TABLE income_plans
ALTER COLUMN estimated_deduction_rate SET DEFAULT 0.2200;

ALTER TABLE income_plans
ALTER COLUMN estimated_deduction_rate SET NOT NULL;

ALTER TABLE income_plans
ADD CONSTRAINT chk_income_plans_estimated_deduction_rate
CHECK (estimated_deduction_rate >= 0 AND estimated_deduction_rate <= 1);