# 관광지 검색 API

## 1. 위경도 기반 근처 관광지 조회

dormung.goorm.training -> 

**GET** `/api/tour-spots/location`

사용자 위치 기준 반경 내 관광지를 거리순으로 조회합니다.

### 파라미터
- `latitude` (required): 위도 (BigDecimal)
- `longitude` (required): 경도 (BigDecimal)
- `radius` (optional): 반경(km), 기본값 10, 최대 10

### 예시
```bash
curl -X GET "https://dormung.goorm.training/api/tour-spots/location?latitude=33.462147&longitude=126.936424&radius=10"
```

---

## 2. 키워드 검색

**GET** `/api/tour-spots/search`

관광지 이름으로 검색합니다. 사용자 위치가 제공되면 거리순, 없으면 이름 매칭 우선순위로 정렬됩니다.

### 파라미터
- `keyword` (required): 검색 키워드 (String) - 한글의 경우 URL 인코딩 필요
- `latitude` (optional): 사용자 위도 (BigDecimal)
- `longitude` (optional): 사용자 경도 (BigDecimal)
- `limit` (optional): 결과 개수, 기본값 10, 최대 50

### 예시
```bash
# 한글 키워드 검색 (URL 인코딩 적용: 성산 -> %EC%84%B1%EC%82%B0)
curl -X GET "https://dormung.goorm.training/api/tour-spots/search?keyword=%EC%84%B1%EC%82%B0&latitude=33.462147&longitude=126.936424"

# 영어 키워드 검색
curl -X GET "https://dormung.goorm.training/api/tour-spots/search?keyword=unesco&latitude=33.462147&longitude=126.936424"
```

---

## 3. 관광지 상세 정보 조회

**GET** `/api/tour-spots/detail`

특정 관광지의 이미지, 오디오, 스크립트 정보를 조회합니다.

### 파라미터
- `contentId` (required): 관광지 고유 ID (String)

### 예시
```bash
curl -X GET "https://dormung.goorm.training/api/tour-spots/detail?contentId=CONT_000000000500150"
```
