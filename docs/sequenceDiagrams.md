# 시퀀스 다이어그램

---
## 작성 흐름
- `유저(Acotr)` 작성
  - 프론트엔드와 협업에 쓰인다고 가정
  - 시퀀스 다이어그램 작성 학습을 위해
- `Contorller`-`Service`-`Repository`로의 흐름으로 작성
  - 여러 기능이 표현되는 경우 핵심 기능만 `Contorller`-`Service`-`Repository` 작성
---  
## 잔액 충전
```mermaid
sequenceDiagram
    actor 유저
    participant BalanceController
    participant BalanceService
    participant UserRepository
    유저->>BalanceController: POST /balances { userId, amount }
    note over 유저,BalanceController: 요청 금액 유효성 검증은 User(도메인) 내에서 처리됨
    alt 요청 금액이 유효하지 않음
        BalanceController->>유저: 유효하지 않은 요청금액(400 Bad Request)
    else 요청 금액이 유효함
        BalanceController->>BalanceService: 잔액 충전 요청
        BalanceService->>UserRepository: 현재 잔액 요청
        UserRepository-->>BalanceService: 현재 잔액 응답
        BalanceService->>BalanceService: 새 잔액(현재 잔액+요청 금액) 계산
        alt 잔고 초과
            BalanceService-->>BalanceController: 잔고 초과(ExceedMaxBalanceException)
            BalanceController-->>유저: 잔고 초과(400 Bad Request)

        else 잔액 충전 성공
            BalanceService->>UserRepository: 잔액 업데이트 요청
            UserRepository-->>BalanceService: 반영 성공 응답
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
    participant OrderService
    participant Payment
    participant OrderRepository
    participant Balance

    유저->>OrderController: POST /orders { userId, items: [{productId, quantity}, ...] }
    OrderController->>OrderService: 주문 요청
    OrderService->>Balance: 잔액 요청
    Balance-->>OrderService: 잔액 반환
    OrderService->>Payment: 결제 요청
    alt 잔액 부족
        Payment-->>OrderService: 결제 실패 (잔액 부족)
        OrderService-->>OrderController: 실패 응답
        OrderController-->>유저: 결제 실패(400 Bad Request)
    else 결제 가능
        Payment->>OrderRepository: 주문 생성 및 저장
        Payment->>Balance: 잔액 업데이트
        Balance-->>Payment: 잔액 업데이트 성공 응답
        Payment-->>OrderService: 결제 완료 응답
        OrderService-->>OrderController: 결제 성공 응답
        OrderController-->>유저: 주문 완료(200 OK)
    end
```