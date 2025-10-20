# 결제 서비스 프로젝트 변경 이력

## 주요 구현 사항

1. **TestPg 클라이언트 구현**
  - TestPg 연동을 위한 REST API 클라이언트 구현
  - 결제 승인 프로세스 구현

2. **결제 조회 기능 구현**
  - 커서 기반 페이지네이션 적용
  - 결제 통계 기능 구현 (건수, 총액 등)
  - 필터링 기능 (partnerId, status, 기간) 구현

3. **서버 배포 및 CI/CD 파이프라인 구축**
  - GitHub Actions를 통한 자동 빌드 및 배포
  - Docker 컨테이너화 및 Docker Compose 구성
  - AWS 서버 환경 설정

4. **모니터링 시스템 구축**
  - Spring Boot Actuator 의존성 추가
  - Prometheus 메트릭 수집 연동
  - Grafana 대시보드 구성 및 시각화

5. **API 문서화 개선**
  - Swagger/OpenAPI 문서 통합
  - API 엔드포인트 상세 설명 및 예제 추가

6. **초기 테스트 데이터 설정**
  - 샘플 파트너 및 수수료 정책 데이터 추가
  - 결제 테스트 데이터 생성

7. **데이터베이스 구성**
  - MariaDB 연동 및 Docker 컨테이너 설정
  - 도메인에 복합 인덱스 적용

## 실행 방법

### 서버 환경

서비스는 AWS 클라우드에 배포되어 있으며, 다음 URL을 통해 접근할 수 있습니다:

1. **API 서버**:
  - [API 서버 주소](https://flow.madras.p-e.kr) 
  - 실행 중인 서버에서 바로 API 테스트 가능
  - Docker Compose를 통해 서비스, MariaDB, Prometheus, Grafana가 함께 구동됩니다.

2. **API 문서**:
  - [Swagger UI](https://flow.madras.p-e.kr/swagger-ui/index.html)
  - 모든 API 엔드포인트 확인 및 테스트 가능

3. **모니터링 대시보드**:
  - [Grafana 대시보드](http://43.201.27.162:3000/d/b88bb2ca-6bde-48fe-8e1b-1a33d2fdaf6e/3fbaf5c?orgId=1&from=now-24h&to=now&timezone=browser&var-application=bigs&var-instance=app:8080&var-jvm_memory_pool_heap=$__all&var-jvm_memory_pool_nonheap=$__all&var-jvm_buffer_pool=$__all&refresh=30s)
  - ID: admin, PW: admin


