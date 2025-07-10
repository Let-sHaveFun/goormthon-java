#!/bin/bash
# docker-setup.sh
# 제주 오디오 가이드 Docker 환경 구축 스크립트

echo "🐳 제주 오디오 가이드 DB 환경 구축 시작..."

# 1. .env 파일 확인
if [ ! -f .env ]; then
    echo "❌ .env 파일이 없습니다!"
    echo "📋 .env.example을 복사해서 .env 파일을 만들어주세요:"
    echo "   cp .env.example .env"
    echo "   그리고 .env 파일에서 실제 값들을 설정해주세요."
    exit 1
fi

echo "✅ .env 파일 확인 완료"

# 2. init-db 폴더 생성
mkdir -p init-db

# 3. SQL 파일들 확인
if [ ! -f init-db/01-schema.sql ]; then
    echo "📂 init-db/01-schema.sql 파일을 생성해주세요"
fi

if [ ! -f init-db/02-initial-data.sql ]; then
    echo "📂 init-db/02-initial-data.sql 파일을 생성해주세요"
fi

# 4. .env 파일에서 변수 로드
source .env

# 5. 기존 컨테이너 정리 (선택적)
echo "🧹 기존 컨테이너 정리 중..."
docker-compose down 2>/dev/null

# 6. Docker Compose 실행
echo "📦 Docker 컨테이너 시작 중..."
docker-compose up -d

# 7. MySQL 컨테이너가 완전히 시작될 때까지 대기
echo "⏳ MySQL 초기화 대기 중... (30초)"
sleep 30

# 8. 연결 테스트
echo "🔗 DB 연결 테스트..."
docker exec jeju-audio-guide-db mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME -e "
SELECT 'DB 연결 성공!' as status;
SELECT COUNT(*) as spot_count FROM tourist_spots;
SELECT COUNT(*) as persona_count FROM personas;
" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ 데이터베이스 환경 구축 완료!"
else
    echo "❌ DB 연결 실패. 로그를 확인해주세요:"
    echo "   docker logs jeju-audio-guide-db"
fi

echo ""
echo "📋 DB 접속 정보:"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo "  Database: $DB_NAME"
echo "  Username: $DB_USER"
echo ""
echo "🚀 Redis 접속 정보:"
echo "  Host: $REDIS_HOST"
echo "  Port: $REDIS_PORT"
echo ""
echo "🔒 보안 알림: .env 파일을 .gitignore에 추가하세요!"
echo ""
echo "📊 컨테이너 상태 확인:"
docker ps --filter "name=jeju-audio-guide"
echo ""
echo "🎯 다음 단계: Spring Boot application.yml 설정"#!/bin/bash
# Docker MySQL 환경 구축 스크립트

echo "🐳 제주 오디오 가이드 DB 환경 구축 시작..."

# 1. .env 파일 확인
if [ ! -f .env ]; then
    echo "❌ .env 파일이 없습니다!"
    echo "📋 .env.example을 복사해서 .env 파일을 만들어주세요:"
    echo "   cp .env.example .env"
    echo "   그리고 .env 파일에서 실제 값들을 설정해주세요."
    exit 1
fi

echo "✅ .env 파일 확인 완료"

# 2. init-db 폴더 생성
mkdir -p init-db

# 3. SQL 파일들을 init-db 폴더로 이동
echo "📂 init-db 폴더에 SQL 파일들을 저장해주세요:"
echo "   - 01-schema.sql"
echo "   - 02-initial-data.sql"

# 4. .env 파일에서 변수 로드
source .env

# 5. Docker Compose 실행
echo "📦 Docker 컨테이너 시작 중..."
docker-compose up -d

# 6. MySQL 컨테이너가 완전히 시작될 때까지 대기
echo "⏳ MySQL 초기화 대기 중..."
sleep 30

# 7. 연결 테스트
echo "🔗 DB 연결 테스트..."
docker exec jeju-audio-guide-db mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME -e "SELECT COUNT(*) as spot_count FROM tourist_spots; SELECT COUNT(*) as persona_count FROM personas;"

echo "✅ 데이터베이스 환경 구축 완료!"
echo ""
echo "📋 DB 접속 정보:"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo "  Database: $DB_NAME"
echo "  Username: $DB_USER"
echo ""
echo "🚀 Redis 접속 정보:"
echo "  Host: $REDIS_HOST"
echo "  Port: $REDIS_PORT"
echo ""
echo "🔒 보안 알림: .env 파일을 .gitignore에 추가하세요!"
echo ""
echo "다음 단계: Spring Boot application.yml 설정"