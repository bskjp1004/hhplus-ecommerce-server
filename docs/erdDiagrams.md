# ERD 다이어그램
## 작성 흐름
- DB 네이밍 규칙은 `소문자`, `스네이크 케이스(snake_case)`
- 참조가 많이 되는 테이블은 ID를 `테이블명_id`로 작명 → 조인이 많이 될 때 가독성 높이기
- JPA 사용을 위해 모든 테이블 `PK` 지정
- `default` 설정: `[유저]` 테이블의 잔액 이외 다른 모든 컬럼은 코드에서 필수로 넣어줘야 하는 것으로 판단
- 실제 DB 설정 
  - `user_id` `FK` 설정 관련 사항
    - `[주문]` 테이블과 같이 유저 삭제되어도 데이터 보존을 위한 테이블은 `FK`미설정
    - `[잔액이력]`은 `[유저]`와 직접 관련 있으니 `FK`설정
  - 클러스터 인덱스 설정 테이블: `[주문]`, `[주문상품]`
  
```mermaid
erDiagram
  USER {
    BIGINT id PK "유저ID"
    DECIMAL balance "잔액 (10,2)"
  }
  BALANCE_HISTORY {
    BIGINT id PK "PK"
    BIGINT user_id FK "유저ID"
    ENUM balance_type "잔액타입 (CHARGE, USE)"
    DECIMAL amount "금액 (10,2)"
    DATETIME processed_at "반영일시"
  }
  PRODUCT {
    BIGINT id PK "상품ID"
    DECIMAL price "금액 (10,2)"
    INT stock "재고수량"
  }
  ORDER {
    BIGINT id PK "주문ID"
    BIGINT user_id FK "유저ID"
    BIGINT coupon_id FK "쿠폰ID"
    DATETIME ordered_at "주문일시"
    DECIMAL total_price "주문금액 (10,2)"
    DECIMAL discount_rate "할인율 (5,2)"
    DECIMAL paid_price "결제금액 (10,2)"
  }
  ORDER_ITEM {
    BIGINT id PK "PK"
    BIGINT order_id FK "주문ID"
    BIGINT product_id FK "상품ID"
    INT quantity "수량"
    DECIMAL product_price "상품금액 (10,2)"
  }
  COUPON_POLICY {
    BIGINT id PK "쿠폰정책ID"
    DECIMAL discount_rate "할인율 (5,2)"
    INT total_count "수량"
    INT remaining_count "잔여수량"
  }
  USER_COUPON {
    BIGINT id PK "PK"
    BIGINT coupon_policy_id FK "쿠폰정책ID"
    BIGINT user_id FK "유저ID"
    DATETIME issued_at "발급일시"
    ENUM status "쿠폰상태 (ISSUED, USED)"
  }

  USER ||--o{ BALANCE_HISTORY : ""
  USER ||--o{ ORDER : ""
  USER ||--o{ USER_COUPON : ""
  ORDER ||--o{ ORDER_ITEM : ""
  ORDER }o--|| COUPON_POLICY : ""
  ORDER_ITEM }o--|| PRODUCT : ""
  USER_COUPON }o--|| COUPON_POLICY : ""

```
