#!/bin/bash

# SpringBoot 무중단 배포 스크립트 - Blue-Green 방식

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

log() { echo -e "${GREEN}[$(date +'%H:%M:%S')] $1${NC}"; }
error() { echo -e "${RED}[ERROR] $1${NC}"; }

get_active_environment() {
    if docker ps --format "{{.Names}}" | grep -q "app-blue"; then
        echo "blue"
    elif docker ps --format "{{.Names}}" | grep -q "app-green"; then
        echo "green"
    else
        echo "none"
    fi
}

health_check() {
    local port=$1
    local max_attempts=30
    local attempt=1

    log "헬스체크 시작 (포트: $port)"

    while [ $attempt -le $max_attempts ]; do
        if curl -sf "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            log "헬스체크 성공"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    error "헬스체크 실패"
    return 1
}

update_nginx_config() {
    local target_env=$1

    if [ "$target_env" = "green" ]; then
        sed -i 's/server app-blue:8080/# server app-blue:8080/' nginx/nginx.conf
        sed -i 's/# server app-green:8080/server app-green:8080/' nginx/nginx.conf
    else
        sed -i 's/server app-green:8080/# server app-green:8080/' nginx/nginx.conf
        sed -i 's/# server app-blue:8080/server app-blue:8080/' nginx/nginx.conf
    fi

    docker exec nginx nginx -s reload
}

deploy() {
    log "=== 무중단 배포 시작 ==="

    current_env=$(get_active_environment)

    if [ "$current_env" = "blue" ]; then
        target_env="green"
        target_port="8081"
    else
        target_env="blue"
        target_port="8080"
    fi

    log "배포 타겟: $target_env"

    docker compose --profile "$target_env" up -d "app-$target_env"

    if ! health_check "$target_port"; then
        error "배포 실패"
        docker compose stop "app-$target_env"
        exit 1
    fi

    update_nginx_config "$target_env"
    sleep 10

    if [ "$current_env" != "none" ]; then
        docker compose stop "app-$current_env"
        docker compose rm -f "app-$current_env"
    fi

    log "=== 배포 완료 ==="
}

rollback() {
    log "=== 롤백 시작 ==="

    current_env=$(get_active_environment)
    target_env=$( [ "$current_env" = "blue" ] && echo "green" || echo "blue" )
    target_port=$( [ "$target_env" = "blue" ] && echo "8080" || echo "8081" )

    docker compose --profile "$target_env" up -d "app-$target_env"

    if ! health_check "$target_port"; then
        error "롤백 실패"
        exit 1
    fi

    update_nginx_config "$target_env"
    docker compose stop "app-$current_env"

    log "=== 롤백 완료 ==="
}

status() {
    current_env=$(get_active_environment)
    echo "현재 활성 환경: $current_env"
    docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "(app|mysql|redis|nginx)"

    curl -sf "http://localhost:8080/actuator/health" > /dev/null 2>&1 && echo "8080 (Blue): ✅" || echo "8080 (Blue): ❌"
    curl -sf "http://localhost:8081/actuator/health" > /dev/null 2>&1 && echo "8081 (Green): ✅" || echo "8081 (Green): ❌"
    curl -sf "http://localhost/health" > /dev/null 2>&1 && echo "Nginx: ✅" || echo "Nginx: ❌"
}

init() {
    mkdir -p nginx/conf.d mysql/init logs static
    docker network create dormung-network 2>/dev/null || true
    log "초기 설정 완료"
}

case "$1" in
    deploy) deploy ;;
    rollback) rollback ;;
    status) status ;;
    init) init ;;
    *) echo "사용법: $0 {deploy|rollback|status|init}"; exit 1 ;;
esac #!/bin/bash

# 무중단 배포 스크립트 - Blue-Green 방식

set -e

# 색상
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 로그 함수
log() { echo -e "${GREEN}[$(date +'%H:%M:%S')] $1${NC}"; }
error() { echo -e "${RED}[ERROR] $1${NC}"; }
info() { echo -e "${BLUE}[INFO] $1${NC}"; }

# 현재 활성 환경 확인
get_active_environment() {
    if docker ps --format "table {{.Names}}" | grep -q "springboot-blue"; then
        if docker ps --format "table {{.Names}}" | grep -q "springboot-green"; then
            echo "both"
        else
            echo "blue"
        fi
    elif docker ps --format "table {{.Names}}" | grep -q "springboot-green"; then
        echo "green"
    else
        echo "none"
    fi
}

# 헬스체크
health_check() {
    local port=$1
    local max_attempts=30
    local attempt=1

    log "헬스체크 시작 (포트: $port)"

    while [ $attempt -le $max_attempts ]; do
        if curl -sf "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            log "헬스체크 성공 ($attempt/$max_attempts)"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    error "헬스체크 실패"
    return 1
}

# Nginx 설정 업데이트
update_nginx_config() {
    local target_env=$1

    log "Nginx 설정을 $target_env 환경으로 업데이트"

    if [ "$target_env" = "green" ]; then
        sed -i 's/server springboot-blue:8080/# server springboot-blue:8080/' nginx/nginx.conf
        sed -i 's/# server springboot-green:8080/server springboot-green:8080/' nginx/nginx.conf
    else
        sed -i 's/server springboot-green:8080/# server springboot-green:8080/' nginx/nginx.conf
        sed -i 's/# server springboot-blue:8080/server springboot-blue:8080/' nginx/nginx.conf
    fi

    docker exec dormung-nginx nginx -s reload
    log "Nginx 설정 리로드 완료"
}

# 메인 배포 함수
deploy() {
    log "=== 무중단 배포 시작 ==="

    current_env=$(get_active_environment)
    log "현재 활성 환경: $current_env"

    # 타겟 환경 결정
    if [ "$current_env" = "blue" ] || [ "$current_env" = "both" ]; then
        target_env="green"
        target_port="8081"
        old_env="blue"
    else
        target_env="blue"
        target_port="8080"
        old_env="green"
    fi

    log "배포 타겟: $target_env (포트: $target_port)"

    # 새 환경 시작
    log "$target_env 환경 컨테이너 시작"
    docker compose --profile "$target_env" up -d "springboot-$target_env"

    # 헬스체크
    if ! health_check "$target_port"; then
        error "배포 실패: 헬스체크 실패"
        log "롤백 중..."
        docker compose --profile "$target_env" down "springboot-$target_env"
        exit 1
    fi

    # 트래픽 전환
    log "트래픽을 $target_env 환경으로 전환"
    update_nginx_config "$target_env"

    sleep 10

    # 이전 환경 종료
    if [ "$current_env" != "none" ] && [ "$old_env" != "$target_env" ]; then
        log "이전 환경($old_env) 종료"
        docker compose stop "springboot-$old_env"
        docker compose rm -f "springboot-$old_env"
    fi

    log "=== 배포 완료 ==="
    log "활성 환경: $target_env"
}

# 롤백
rollback() {
    log "=== 롤백 시작 ==="

    current_env=$(get_active_environment)

    if [ "$current_env" = "blue" ]; then
        target_env="green"
        target_port="8081"
    elif [ "$current_env" = "green" ]; then
        target_env="blue"
        target_port="8080"
    else
        error "롤백할 환경을 찾을 수 없습니다"
        exit 1
    fi

    log "롤백 타겟: $target_env"

    # 이전 환경 시작
    docker compose --profile "$target_env" up -d "springboot-$target_env"

    # 헬스체크
    if ! health_check "$target_port"; then
        error "롤백 실패"
        exit 1
    fi

    # 트래픽 전환
    update_nginx_config "$target_env"

    # 현재 환경 종료
    docker compose stop "springboot-$current_env"

    log "=== 롤백 완료 ==="
}

# 상태 확인
status() {
    log "=== 시스템 상태 ==="

    current_env=$(get_active_environment)
    echo "현재 활성 환경: $current_env"

    echo -e "\n실행 중인 컨테이너:"
    docker ps --format "table {{.Names}}\t{{.Status}}" | grep dormung

    echo -e "\n헬스체크:"
    curl -sf "http://localhost:8080/actuator/health" > /dev/null 2>&1 && echo "8080 (Blue): ✅" || echo "8080 (Blue): ❌"
    curl -sf "http://localhost:8081/actuator/health" > /dev/null 2>&1 && echo "8081 (Green): ✅" || echo "8081 (Green): ❌"
    curl -sf "http://localhost:8001/health" > /dev/null 2>&1 && echo "8001 (Python): ✅" || echo "8001 (Python): ❌"
    curl -sf "http://localhost:80/health" > /dev/null 2>&1 && echo "80 (Nginx): ✅" || echo "80 (Nginx): ❌"
}

# 로그 확인
logs() {
    local service=$1
    local lines=${2:-50}

    if [ -z "$service" ]; then
        echo "사용법: $0 logs <service> [lines]"
        echo "서비스: springboot-blue, springboot-green, mysql, redis, nginx"
        return 1
    fi

    docker compose logs --tail="$lines" -f "$service"
}


# 사용법
usage() {
    echo "사용법: $0 {deploy|rollback|status|logs|init}"
    echo "  deploy    : 무중단 배포"
    echo "  rollback  : 롤백"
    echo "  status    : 상태 확인"
    echo "  logs      : 로그 확인"
    echo "  init      : 초기 설정"
}

case "$1" in
    deploy)
        deploy
        ;;
    rollback)
        rollback
        ;;
    status)
        status
        ;;
    logs)
        logs "$2" "$3"
        ;;
    init)
        init
        ;;
    *)
        usage
        exit 1
        ;;
esac