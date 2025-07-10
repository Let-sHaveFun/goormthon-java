#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
제주 관광지 CSV 데이터 임포트 스크립트 (최소 의존성 버전)
mysql.connector 사용 (시스템에 기본 설치됨)
"""

import csv
import logging
import os
from datetime import datetime
from typing import List, Dict, Any

# MySQL 연결을 위한 다양한 시도
DB_CONNECTORS = []

# 1. mysql.connector 시도
try:
    import mysql.connector
    DB_CONNECTORS.append(('mysql.connector', mysql.connector))
    print("✅ mysql.connector 사용 가능")
except ImportError:
    pass

# 2. pymysql 시도
try:
    import pymysql
    DB_CONNECTORS.append(('pymysql', pymysql))
    print("✅ pymysql 사용 가능")
except ImportError:
    pass

# 3. MySQLdb 시도
try:
    import MySQLdb
    DB_CONNECTORS.append(('MySQLdb', MySQLdb))
    print("✅ MySQLdb 사용 가능")
except ImportError:
    pass

if not DB_CONNECTORS:
    print("❌ MySQL 연결 라이브러리를 찾을 수 없습니다!")
    print("🔧 다음 중 하나를 설치해주세요:")
    print("   pip install pymysql")
    print("   pip install mysql-connector-python")
    exit(1)

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class MinimalJejuImporter:
    def __init__(self):
        self.db_config = {
            'host': 'localhost',
            'port': 3306,
            'user': 'developer',
            'password': 'devpassword123!',
            'database': 'jeju_audio_guide'
        }
        self.connection = None
        self.db_type = None

    def connect_db(self):
        """데이터베이스 연결 (여러 라이브러리 시도)"""
        for db_name, db_module in DB_CONNECTORS:
            try:
                logger.info(f"🔗 {db_name}로 연결 시도...")

                if db_name == 'mysql.connector':
                    self.connection = db_module.connect(**self.db_config)
                elif db_name == 'pymysql':
                    self.connection = db_module.connect(**self.db_config)
                elif db_name == 'MySQLdb':
                    self.connection = db_module.connect(
                        host=self.db_config['host'],
                        user=self.db_config['user'],
                        passwd=self.db_config['password'],
                        db=self.db_config['database'],
                        port=self.db_config['port']
                    )

                self.db_type = db_name
                logger.info(f"✅ {db_name}로 데이터베이스 연결 성공")
                return True

            except Exception as e:
                logger.warning(f"⚠️ {db_name} 연결 실패: {e}")
                continue

        logger.error("❌ 모든 데이터베이스 연결 시도 실패")
        return False

    def execute_query(self, query, params=None):
        """쿼리 실행 (DB 타입별 처리)"""
        cursor = self.connection.cursor()

        if params:
            cursor.execute(query, params)
        else:
            cursor.execute(query)

        return cursor

    def load_csv_data(self, file_path):
        """CSV 파일 로드"""
        logger.info(f"📂 CSV 파일 로드: {file_path}")

        data = []
        encodings = ['utf-8', 'cp949', 'euc-kr', 'utf-8-sig']

        for encoding in encodings:
            try:
                with open(file_path, 'r', encoding=encoding) as file:
                    reader = csv.DictReader(file)
                    data = list(reader)
                    logger.info(f"✅ {encoding} 인코딩 성공: {len(data)}개 행")
                    break
            except UnicodeDecodeError:
                continue
            except Exception as e:
                logger.warning(f"⚠️ {encoding} 실패: {e}")
                continue

        return data

    def filter_and_clean_data(self, data):
        """데이터 필터링 및 정제"""
        logger.info("🧹 데이터 정제 중...")

        cleaned = []
        for row in data:
            # 관광지만 필터링
            if row.get('콘텐츠분류', '').strip() != '관광지':
                continue

            try:
                # 좌표 검증
                lat = float(row.get('위도', 0) or 0)
                lng = float(row.get('경도', 0) or 0)

                if lat == 0 or lng == 0:
                    continue

                name = row.get('제목', '').strip()
                if not name:
                    continue

                # 주소 처리
                address = row.get('도로명주소', '').strip() or row.get('지번주소', '').strip()

                cleaned_item = {
                    'external_id': row.get('콘텐츠아이디', '').strip(),
                    'name': name,
                    'address': address,
                    'latitude': lat,
                    'longitude': lng,
                    'description': f"{name}은(는) 제주도의 아름다운 관광지입니다.",
                    'category': '관광지'
                }

                cleaned.append(cleaned_item)

            except (ValueError, TypeError) as e:
                continue

        logger.info(f"✅ 정제 완료: {len(cleaned)}개")
        return cleaned

    def insert_data(self, data):
        """데이터 삽입"""
        try:
            # 기존 데이터 삭제
            logger.info("🗑️ 기존 데이터 정리...")

            cursor = self.execute_query("DELETE FROM audio_contents")
            cursor.close()

            cursor = self.execute_query("DELETE FROM qr_mappings")
            cursor.close()

            cursor = self.execute_query("DELETE FROM user_collections")
            cursor.close()

            cursor = self.execute_query("DELETE FROM tourist_spots")
            cursor.close()

            # 새 데이터 삽입
            logger.info("📝 새 데이터 삽입...")

            insert_query = """
            INSERT INTO tourist_spots 
            (external_id, name, address, latitude, longitude, description, category, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
            """

            success_count = 0
            for item in data:
                try:
                    cursor = self.execute_query(insert_query, (
                        item['external_id'],
                        item['name'],
                        item['address'],
                        item['latitude'],
                        item['longitude'],
                        item['description'],
                        item['category']
                    ))
                    cursor.close()
                    success_count += 1
                except Exception as e:
                    logger.warning(f"⚠️ 삽입 실패 ({item['name']}): {e}")
                    continue

            # 커밋
            self.connection.commit()

            logger.info(f"✅ 데이터 삽입 완료: {success_count}개")

            # 확인
            cursor = self.execute_query("SELECT COUNT(*) FROM tourist_spots")
            count = cursor.fetchone()[0]
            cursor.close()

            logger.info(f"📊 총 관광지: {count}개")

            return success_count > 0

        except Exception as e:
            logger.error(f"❌ 데이터 삽입 실패: {e}")
            if self.connection:
                self.connection.rollback()
            return False

    def run_import(self, csv_file):
        """전체 임포트 실행"""
        logger.info("🚀 제주 관광지 데이터 임포트 시작")

        # 1. CSV 로드
        data = self.load_csv_data(csv_file)
        if not data:
            return False

        # 2. 데이터 정제
        cleaned_data = self.filter_and_clean_data(data)
        if not cleaned_data:
            return False

        # 3. DB 연결
        if not self.connect_db():
            return False

        # 4. 데이터 삽입
        success = self.insert_data(cleaned_data)

        # 5. 연결 종료
        if self.connection:
            self.connection.close()

        return success

def main():
    """메인 함수"""
    csv_file = "datasample.csv"

    if not os.path.exists(csv_file):
        print(f"❌ 파일 없음: {csv_file}")
        return

    importer = MinimalJejuImporter()
    success = importer.run_import(csv_file)

    if success:
        print("\n🎉 임포트 성공!")
        print("🚀 다음: ./gradlew bootRun")
    else:
        print("\n❌ 임포트 실패")

if __name__ == "__main__":
    main()