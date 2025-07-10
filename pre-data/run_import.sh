#!/bin/bash
# quick_fix.sh - 패키지 설치 문제 빠른 해결

echo "🔧 패키지 설치 문제 해결 중..."

# 가상환경 활성화
source venv/bin/activate

# 캐시 삭제 및 업그레이드
echo "📦 pip 업그레이드 및 캐시 정리..."
pip install --upgrade pip
pip cache purge

# 개별적으로 패키지 설치
echo "🐍 pymysql 설치..."
pip install pymysql

# cryptography는 더 유연한 버전으로
echo "🔐 cryptography 설치..."
pip install "cryptography>=3.0.0"

# 설치 확인
echo "✅ 설치 확인..."
python3 -c "import pymysql; print('✅ pymysql 설치 성공')"
python3 -c "import cryptography; print('✅ cryptography 설치 성공')"

echo "🎯 이제 다시 임포트를 시도해보세요:"
echo "python3 csv_jeju_import.py"