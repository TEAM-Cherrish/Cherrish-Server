-- =====================================================
-- Cherrish Demo Data SQL Script
-- 기존 데이터를 모두 삭제하고 데모용 데이터를 삽입합니다.
-- =====================================================

-- 1. 기존 데이터 삭제 (FK 제약조건 순서 고려)
TRUNCATE TABLE user_procedures CASCADE;
TRUNCATE TABLE procedure_worries CASCADE;
TRUNCATE TABLE procedures CASCADE;
TRUNCATE TABLE worries CASCADE;
TRUNCATE TABLE users CASCADE;

-- 시퀀스 리셋 (PostgreSQL)
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE worries_id_seq RESTART WITH 1;
ALTER SEQUENCE procedures_id_seq RESTART WITH 1;
ALTER SEQUENCE procedure_worries_id_seq RESTART WITH 1;
ALTER SEQUENCE user_procedures_id_seq RESTART WITH 1;

-- =====================================================
-- 2. Users (사용자)
-- =====================================================
INSERT INTO users (name, age, created_at, updated_at) VALUES
('최아리', 27, NOW(), NOW());

-- =====================================================
-- 3. Worries (피부 고민)
-- =====================================================
INSERT INTO worries (content, created_at, updated_at) VALUES
('피부결 ∙ 각질', NOW(), NOW()),
('색소 ∙ 잡티', NOW(), NOW()),
('페이스 라인', NOW(), NOW()),
('탄력 ∙ 주름', NOW(), NOW()),
('모공', NOW(), NOW()),
('바디 라인', NOW(), NOW());

-- =====================================================
-- 4. Procedures (시술)
-- =====================================================
INSERT INTO procedures (name, category, min_downtime_days, max_downtime_days, created_at, updated_at) VALUES
-- 탄력 ∙ 주름
('보톡스', '탄력 ∙ 주름', 3, 5, NOW(), NOW()),
('슈링크', '탄력 ∙ 주름', 3, 7, NOW(), NOW()),
('인모드', '탄력 ∙ 주름', 3, 7, NOW(), NOW()),
('온다 리프팅', '탄력 ∙ 주름', 0, 3, NOW(), NOW()),
('써마지', '탄력 ∙ 주름', 0, 3, NOW(), NOW()),
('티타늄', '탄력 ∙ 주름', 0, 1, NOW(), NOW()),
('울쎄라', '탄력 ∙ 주름', 0, 7, NOW(), NOW()),
('쥬베룩', '탄력 ∙ 주름', 7, 14, NOW(), NOW()),
('실 리프팅', '탄력 ∙ 주름', 7, 10, NOW(), NOW()),
-- 피부결 ∙ 각질
('아쿠아필', '피부결 ∙ 각질', 0, 0, NOW(), NOW()),
('라라필', '피부결 ∙ 각질', 0, 0, NOW(), NOW()),
('LDM', '피부결 ∙ 각질', 0, 0, NOW(), NOW()),
('세라필', '피부결 ∙ 각질', 0, 0, NOW(), NOW()),
('크라이오셀', '피부결 ∙ 각질', 0, 0, NOW(), NOW()),
('리쥬란', '피부결 ∙ 각질', 2, 5, NOW(), NOW()),
-- 색소 ∙ 잡티
('점 제거', '색소 ∙ 잡티', 2, 3, NOW(), NOW()),
('피코 토닝', '색소 ∙ 잡티', 0, 1, NOW(), NOW()),
('제네시스 토닝', '색소 ∙ 잡티', 1, 2, NOW(), NOW()),
('레이저 토닝', '색소 ∙ 잡티', 1, 2, NOW(), NOW()),
-- 페이스 라인
('사각턱 보톡스', '페이스 라인', 0, 0, NOW(), NOW()),
('침샘 보톡스', '페이스 라인', 0, 0, NOW(), NOW()),
('윤곽 주사', '페이스 라인', 1, 3, NOW(), NOW()),
('얼굴지방분해주사', '페이스 라인', 1, 3, NOW(), NOW()),
('자갈턱 보톡스', '페이스 라인', 0, 0, NOW(), NOW()),
-- 바디 라인
('승모근 보톡스', '바디 라인', 1, 3, NOW(), NOW()),
('종아리 보톡스', '바디 라인', 1, 3, NOW(), NOW()),
('허벅지 보톡스', '바디 라인', 1, 3, NOW(), NOW()),
('넥라인 보톡스', '바디 라인', 1, 2, NOW(), NOW()),
-- 모공
('포텐자', '모공', 2, 4, NOW(), NOW()),
('스킨보톡스', '모공', 1, 3, NOW(), NOW()),
('피코 프락셀', '모공', 3, 7, NOW(), NOW());

-- =====================================================
-- 5. Procedure-Worries (시술-고민 매핑)
-- =====================================================
INSERT INTO procedure_worries (procedure_id, worry_id, created_at)
SELECT p.id, w.id, NOW()
FROM procedures p, worries w
WHERE
    -- 탄력 ∙ 주름 시술 → 탄력 ∙ 주름 고민
    (p.name IN ('보톡스', '슈링크', '인모드', '온다 리프팅', '써마지', '티타늄', '울쎄라', '쥬베룩', '실 리프팅') AND w.content = '탄력 ∙ 주름')
    -- 피부결 ∙ 각질 시술 → 피부결 ∙ 각질 고민
    OR (p.name IN ('아쿠아필', '라라필', 'LDM', '세라필', '크라이오셀', '리쥬란') AND w.content = '피부결 ∙ 각질')
    -- 색소 ∙ 잡티 시술 → 색소 ∙ 잡티 고민
    OR (p.name IN ('점 제거', '피코 토닝', '제네시스 토닝', '레이저 토닝') AND w.content = '색소 ∙ 잡티')
    -- 페이스 라인 시술 → 페이스 라인 고민
    OR (p.name IN ('사각턱 보톡스', '침샘 보톡스', '윤곽 주사', '얼굴지방분해주사', '자갈턱 보톡스') AND w.content = '페이스 라인')
    -- 바디 라인 시술 → 바디 라인 고민
    OR (p.name IN ('승모근 보톡스', '종아리 보톡스', '허벅지 보톡스', '넥라인 보톡스') AND w.content = '바디 라인')
    -- 모공 시술 → 모공 고민
    OR (p.name IN ('포텐자', '스킨보톡스', '피코 프락셀') AND w.content = '모공');

-- =====================================================
-- 6. User Procedures (사용자 시술 기록)
-- =====================================================
INSERT INTO user_procedures (user_id, procedure_id, scheduled_at, downtime_days, recovery_target_date, created_at, updated_at)
SELECT
    u.id,
    p.id,
    v.scheduled_at,
    v.downtime_days,
    v.recovery_target_date,
    NOW(),
    NOW()
FROM users u
CROSS JOIN (VALUES
    -- 2025년 11월
    ('피코 토닝', '2025-11-03 10:00:00'::timestamp, 1, '2025-11-15'::date),
    ('슈링크', '2025-11-10 14:00:00'::timestamp, 7, '2025-11-25'::date),
    ('보톡스', '2025-11-15 11:00:00'::timestamp, 3, '2025-11-26'::date),
    ('라라필', '2025-11-20 15:00:00'::timestamp, 0, '2025-11-28'::date),
    -- 2025년 12월
    ('실 리프팅', '2025-12-10 13:00:00'::timestamp, 10, '2025-12-28'::date),
    ('포텐자', '2025-12-15 11:00:00'::timestamp, 4, '2025-12-27'::date),
    ('티타늄', '2025-12-22 14:00:00'::timestamp, 1, '2025-12-31'::date),
    -- 2026년 1월
    ('제네시스 토닝', '2026-01-03 10:00:00'::timestamp, 2, '2026-01-15'::date),
    ('사각턱 보톡스', '2026-01-06 11:00:00'::timestamp, 0, '2026-01-22'::date),
    ('슈링크', '2026-01-06 14:00:00'::timestamp, 7, '2026-01-20'::date),
    ('리쥬란', '2026-01-10 10:00:00'::timestamp, 5, '2026-01-25'::date),
    ('실 리프팅', '2026-01-18 13:00:00'::timestamp, 10, '2026-01-30'::date),
    ('점 제거', '2026-01-25 15:00:00'::timestamp, 3, '2026-01-31'::date),
    ('라라필', '2026-01-31 11:00:00'::timestamp, 0, '2026-01-31'::date),
    -- 2026년 2월
    ('아쿠아필', '2026-02-01 10:00:00'::timestamp, 0, '2026-02-14'::date),
    ('승모근 보톡스', '2026-02-06 14:00:00'::timestamp, 2, '2026-02-15'::date),
    ('울쎄라', '2026-02-10 11:00:00'::timestamp, 7, '2026-02-22'::date),
    ('윤곽 주사', '2026-02-15 13:00:00'::timestamp, 3, '2026-02-25'::date),
    ('스킨보톡스', '2026-02-20 15:00:00'::timestamp, 3, '2026-02-28'::date),
    -- 2026년 3월
    ('쥬베룩', '2026-03-05 10:00:00'::timestamp, 14, '2026-03-28'::date),
    ('피코 프락셀', '2026-03-10 14:00:00'::timestamp, 7, '2026-03-25'::date),
    ('넥라인 보톡스', '2026-03-15 11:00:00'::timestamp, 2, '2026-03-22'::date),
    ('인모드', '2026-03-20 13:00:00'::timestamp, 7, '2026-03-30'::date),
    ('레이저 토닝', '2026-03-25 15:00:00'::timestamp, 2, '2026-03-31'::date)
) AS v(procedure_name, scheduled_at, downtime_days, recovery_target_date)
JOIN procedures p ON p.name = v.procedure_name
WHERE u.name = '최아리';

-- =====================================================
-- 데이터 확인
-- =====================================================
SELECT 'users' AS table_name, COUNT(*) AS count FROM users
UNION ALL
SELECT 'worries', COUNT(*) FROM worries
UNION ALL
SELECT 'procedures', COUNT(*) FROM procedures
UNION ALL
SELECT 'procedure_worries', COUNT(*) FROM procedure_worries
UNION ALL
SELECT 'user_procedures', COUNT(*) FROM user_procedures;
