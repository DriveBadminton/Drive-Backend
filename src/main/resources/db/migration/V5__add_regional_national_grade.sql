-- user_profile: 지역/전국 급수 컬럼 추가
ALTER TABLE user_profile
ADD COLUMN regional_grade VARCHAR(30),
ADD COLUMN national_grade VARCHAR(30);

-- user_grade_history: 급수 타입 구분 컬럼 추가
ALTER TABLE user_grade_history
ADD COLUMN grade_type VARCHAR(20) NOT NULL;

-- 기존 grade 컬럼을 제거하려면 아래 주석을 해제하세요
-- ALTER TABLE user_profile DROP COLUMN grade;