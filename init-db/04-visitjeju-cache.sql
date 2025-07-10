-- 비짓제주 API 캐시 테이블 생성 (간소화 버전)
-- init-db/04-visitjeju-cache.sql

USE jeju_audio_guide;

-- 비짓제주 API 응답 캐시 테이블 (7개 핵심 필드)
CREATE TABLE visitjeju_cache (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- 연관 관계
                                 tourist_spot_id BIGINT NOT NULL,
                                 external_id VARCHAR(100) NOT NULL, -- 비짓제주 콘텐츠 ID

    -- 핵심 7개 필드
                                 title VARCHAR(500) NOT NULL, -- 관광지명
                                 introduction TEXT, -- 관광지 간단 소개
                                 tag TEXT, -- 주요 키워드
                                 address VARCHAR(500), -- 지번 주소
                                 photo_id BIGINT, -- 이미지 고유번호
                                 img_path VARCHAR(1000), -- 원본 이미지 URL

    -- 캐시 관리
                                 cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 캐시 생성 시간
                                 expires_at TIMESTAMP, -- 캐시 만료 시간 (24시간 후)
                                 is_active BOOLEAN DEFAULT TRUE, -- 활성 상태

    -- 인덱스
                                 FOREIGN KEY (tourist_spot_id) REFERENCES tourist_spots(id) ON DELETE CASCADE,
                                 UNIQUE KEY uk_external_id (external_id),
                                 INDEX idx_tourist_spot (tourist_spot_id),
                                 INDEX idx_cached_at (cached_at),
                                 INDEX idx_expires_at (expires_at),
                                 INDEX idx_active (is_active)
);

-- 캐시 만료 시간 자동 설정 트리거
DELIMITER $
CREATE TRIGGER set_cache_expiry
    BEFORE INSERT ON visitjeju_cache
    FOR EACH ROW
BEGIN
    IF NEW.expires_at IS NULL THEN
        SET NEW.expires_at = DATE_ADD(NEW.cached_at, INTERVAL 24 HOUR);
    END IF;
END$
DELIMITER ;

-- 데이터 확인 쿼리
SELECT 'visitjeju_cache 테이블 생성 완료 (7개 필드)' as status;