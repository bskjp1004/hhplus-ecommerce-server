# 요구사항 명세서
## 📌 기능 요구사항
### 1. 잔액 충전 및 조회
- 유저는 결제에 사용될 금액을 충전할 수 있다.
- 유효하지 않은 금액(0 이하 또는 최대 잔고 초과)은 충전에 실패한다.
- 유저는 자신의 현재 잔액을 조회할 수 있다.
- 존재하지 않는 유저의 잔액 조회 요청은 실패한다.

### 2. 상품 조회
- 유저는 `상품ID`로 잔여 수량을 조회할 수 있다.
- 존재하지 않는 상품에 대한 조회 요청은 실패한다.
- 유저는 최근 3일간 가장 많이 팔린 상위 5개 상품을 확인할 수 있다.

### 3. 주문 및 결제
- 유저는 충전된 잔액을 사용하여 상품을 결제할 수 있으며, 결제 성공 시 잔액이 차감된다.
- 결제 시 잔액이 부족할 경우 결제가 실패한다.
- 결제가 성공하면 주문 정보를 데이터 플랫폼으로 전송한다.
- 주문 및 결제의 취소는 불가능하다. 

### 4. 선착순 쿠폰 발급 및 사용
- 유저는 선착순으로 할인 쿠폰을 발급받을 수 있다.
- 동일한 할인 쿠폰은 한 번만 발급받을 수 있다.
- 유저는 주문 시 유효한 쿠폰을 제출하여, 전체 주문 금액에 대한 할인을 받을 수 있다.
- 쿠폰의 만료일은 없다.
---

## ⚙️ 비기능 요구사항
### 동시성 제어
- 동시에 잔액 충전 요청이 들어올 경우 **동시성 제어**가 적용되어야 한다.
- 동시에 여러 상품 결제 요청이 들어올 경우 **동시성 제어**가 적용되어야 한다.
- 동시에 여러 할인 쿠폰 발급 요청이 들어올 경우 **동시성 제어**가 적용되어야 한다.

### 테스트
- 테스트 커버리지는 65% 이상을 목표로 한다.
- 주요 API는 Postman, Swagger 기반의 자동화된 테스트로 검증 가능 해야 한다.
