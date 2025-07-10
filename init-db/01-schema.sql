-- init-db/01-01-schema.sql
-- 제주 오디오 가이드 데이터베이스 스키마

USE jeju_audio_guide;

-- 관광지 정보 테이블
CREATE TABLE tourist_spots (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               external_id VARCHAR(100) UNIQUE, -- 제주 Visit API ID
                               name VARCHAR(255) NOT NULL,
                               address VARCHAR(500),
                               latitude DECIMAL(10, 8) NOT NULL,
                               longitude DECIMAL(11, 8) NOT NULL,
                               description TEXT,
                               image_url VARCHAR(500),
                               category VARCHAR(100),
                               phone VARCHAR(50),
                               operating_hours VARCHAR(200),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                               INDEX idx_location (latitude, longitude),
                               INDEX idx_category (category),
                               INDEX idx_name (name)
);

-- 페르소나 테이블
CREATE TABLE personas (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          voice_style VARCHAR(100),
                          language_code VARCHAR(10) DEFAULT 'ko-KR',
                          gender ENUM('MALE', 'FEMALE', 'NEUTRAL') DEFAULT 'NEUTRAL',
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 오디오 콘텐츠 테이블
CREATE TABLE audio_contents (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                spot_id BIGINT NOT NULL,
                                persona_id BIGINT NOT NULL,
                                script_standard TEXT NOT NULL,
                                script_dialect TEXT,
                                audio_url VARCHAR(500),
                                audio_duration INT, -- 초 단위
                                generation_status ENUM('PENDING', 'GENERATING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
                                error_message TEXT,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                FOREIGN KEY (spot_id) REFERENCES tourist_spots(id) ON DELETE CASCADE,
                                FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
                                INDEX idx_spot_persona (spot_id, persona_id),
                                INDEX idx_status (generation_status)
);

-- QR 코드 매핑 테이블
CREATE TABLE qr_mappings (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             qr_code VARCHAR(255) UNIQUE NOT NULL,
                             spot_id BIGINT NOT NULL,
                             is_active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             FOREIGN KEY (spot_id) REFERENCES tourist_spots(id) ON DELETE CASCADE
);

-- 사용자 조각 수집 테이블 (MVP에서는 선택적)
CREATE TABLE user_collections (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  user_id VARCHAR(100) NOT NULL, -- 임시 사용자 식별자
                                  spot_id BIGINT NOT NULL,
                                  audio_content_id BIGINT,
                                  collected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  FOREIGN KEY (spot_id) REFERENCES tourist_spots(id) ON DELETE CASCADE,
                                  FOREIGN KEY (audio_content_id) REFERENCES audio_contents(id) ON DELETE SET NULL,
                                  UNIQUE KEY unique_user_spot (user_id, spot_id)
);

-- 검색 키워드 로그 (검색 최적화용)
CREATE TABLE search_logs (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             keyword VARCHAR(255) NOT NULL,
                             result_count INT,
                             search_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             INDEX idx_keyword (keyword),
                             INDEX idx_search_time (search_at)
);