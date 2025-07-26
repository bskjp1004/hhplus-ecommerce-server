# 시퀀스 다이어그램

---
## 작성 흐름
- `유저(Acotr)` 작성
  - 프론트엔드와 협업에 쓰인다고 가정
  - 시퀀스 다이어그램 작성 학습을 위해 상세 작성
- `Contorller`-`Service`-`Repository`로의 흐름으로 작성
  - 여러 기능이 표현되는 경우 핵심 기능만 `Contorller`-`Service`-`Repository` 작성
- 동시성 제어는 우선 비관적 락(Pessimistic Lock)으로 작성
  - 분산 락(Distributed Lock) 개념 더 공부 후에 설계를 수정하기로 함
---  
## 잔액 충전
```mermaid
sequenceDiagram
    actor 유저
    participant BalanceController
    participant BalanceService
    participant UserRepository
    유저->>BalanceController: PATCH /users/{userId}/balance { ... }
    note over 유저,BalanceController: 요청 금액 유효성 검증은 User(도메인) 내에서 처리됨
    alt 요청 금액이 유효하지 않음
        BalanceController->>유저: 유효하지 않은 요청금액(400 Bad Request)
    else 요청 금액이 유효함
        BalanceController->>BalanceService: 잔액 충전 요청
      note over BalanceService,UserRepository: 트랜잭션 시작
        BalanceService->>UserRepository: 현재 잔액 요청 (동시성 제어, Pessimistic Lock)
        UserRepository-->>BalanceService: 현재 잔액 응답
        BalanceService->>BalanceService: 새 잔액(현재 잔액+요청 금액) 계산
        alt 잔고 초과
            BalanceService-->>BalanceController: 잔고 초과(ExceedMaxBalanceException)
            BalanceController-->>유저: 잔고 초과(400 Bad Request)

        else 잔액 충전 성공
            BalanceService->>UserRepository: 잔액 업데이트 요청
          note over UserRepository: 동시성 제어에 의해 중복 충전 방지
            UserRepository-->>BalanceService: 반영 성공 응답
          note over BalanceService: 트랜잭션 커밋
            BalanceService-->>BalanceController: 잔액 충전 성공 응답
            BalanceController-->>유저: 잔액 충전 성공(200 OK)
        end
    end
```
---
## 주문/결제
```mermaid
sequenceDiagram
  actor 유저
  participant OrderController
  participant OrderFacade
  participant ProductService
  participant CouponService
  participant UserService
  participant OrderService
  participant 외부 데이터플랫폼

  유저->>OrderController: POST /orders/{userId} { ... }
  OrderController->>OrderFacade: 주문 요청
  OrderFacade->>ProductService: 상품 재고 확인
  ProductService-->>OrderFacade: 상품 재고 응답

  alt 상품 재고 부족
    OrderFacade-->>OrderController: 주문/결제 실패 응답
    OrderController-->>유저: 재고 부족(400 Bad Request)
  else
    OrderFacade->>CouponService: 쿠폰 유효성 확인 및 할인 금액 조회
    CouponService-->>OrderFacade: 할인 금액 응답
    OrderFacade->>UserService: 잔액 조회
    UserService-->>OrderFacade: 잔액 반환
    OrderFacade->>OrderFacade: 결제 가능 여부 확인
    alt 잔액 부족
      OrderFacade-->>OrderController: 주문/결제 실패 응답
      OrderController-->>유저: 잔액 부족(400 Bad Request)
    else 결제 가능
      note over OrderFacade: 트랜젝션 시작
      OrderFacade->>ProductService: 재고 차감 업데이트
      OrderFacade->>CouponService: 쿠폰 사용 생성
      OrderFacade->>UserService: 잔액 차감 업데이트
      OrderFacade->>OrderService: 주문 생성
      note over OrderFacade: 트랜젝션 커밋
      OrderFacade-->>OrderController: 주문/결제 성공
      OrderController-->>유저: 주문/결제 성공(200 OK)
      OrderFacade->>외부 데이터플랫폼: 주문 정보 외부 플랫폼으로 전송
    end
  end
```

## 쿠폰 발급
```mermaid
sequenceDiagram
  actor 유저
  participant CouponController
  participant CouponService
  participant CouponRepository
  유저->>CouponController: POST /users/{userId}/coupon { ... }
    CouponController->>CouponService: 쿠폰 발급 요청
    note over CouponService,CouponRepository: 트랜잭션 시작
  CouponService->>CouponRepository: 쿠폰 정책 확인 (동시성 제어, Pessimistic Lock)
  CouponRepository-->>CouponService: 쿠폰 잔여 수량 응답
    alt 쿠폰 발급 불가
      CouponService-->>CouponController: 잔고 초과(OutOfCouponStockException)
      CouponController-->>유저: 쿠폰 잔여 수량 없음(400 Bad Request)

    else 쿠폰 발급 가능
      CouponService->>CouponRepository: 쿠폰 발급/남은 쿠폰 수량 업데이트 요청
      note over CouponRepository: 동시성 제어에 의해 중복 충전 방지
      CouponRepository-->>CouponService: 반영 성공 응답
      note over CouponService: 트랜잭션 커밋
      CouponService-->>CouponController: 쿠폰 발급 성공 응답
      CouponController-->>유저: 쿠폰 발급 성공(200 OK)
    end
```