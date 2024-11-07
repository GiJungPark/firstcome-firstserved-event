# 요구사항
- 선착순 100명에게 쿠폰을 발행한다.
- 쿠폰은 선착순 100명에게만 발행되어야 하며, 101개 이상 지급되면 안된다.
- 순간적으로 몰리는 트래픽을 버틸 수 있어야 한다.
- 쿠폰은 1인당 1매만 발행한다.
<br> <br>
# 구현
## [쿠폰 발행](https://github.com/GiJungPark/firstcome-firstserved-event/pull/1)
### 상세
- JPA와 MySQL를 사용하여, 쿠폰 발행시 저장한다.
- 발행된 쿠폰의 개수는 저장된 쿠폰의 개수를 사용한다.
### 문제점
- 트래픽을 분산하기 위해 서버를 수평 확장한 경우, Race Condition이 발생하게 된다.
- 여러 쓰레드가 동시에 요청하는 테스트 케이스에서, 이 구현 방식은 100개를 초과하여 쿠폰을 발행할 가능성이 있다.
### Race Condition
- 두 개 이상의 프로세스가 공통 자원을 읽거나 쓰는 동작을 할 때, 공용 데이터에 대한 접근 순서에 따라 그 실행 결과가 같지 않고 달라지는 상황을 의미한다.
- 쉽게 말해, 여러 프로세스가 공유 자원을 두고 동시에 접근하려 할 때 발생하는 경쟁 상태이다.
<br>
## [쿠폰 100개 제한 발행](https://github.com/GiJungPark/firstcome-firstserved-event/pull/2)
### 해결 방안
- Synchronized 키워드를 사용해 공유 자원을 한 스레드만 접근하도록 할 수 있지만, 서버가 여러 대일 경우 이 방법으로는 Race Condition을 해결할 수 없다.
- Lock을 사용해 쿠폰 개수 조회부터 발행까지의 과정을 모두 Lock 처리할 수도 있지만, Lock 구간이 길어져서 성능에 불이익이 생길 수 있다.
### Redis 선택
- Redis는 Single Thread로 동작하기 때문에 Atomic한 연산을 보장한다.
- 또한, Redis의 incr 명령은 1을 증가시키고, 증가된 값을 반환하기 때문에 해당 상황에 적합하다.
### 상세
- Redis의 incr 명령을 사용해 쿠폰이 발행될 때 마다 카운트를 증가시키고, 반환된 값으로 발행된 쿠폰의 개수를 확인한다.
### 문제점
- 만약 쿠폰 발행을 저장하는 DB에 다양한 서비스의 정보를 저장하는 경우, 쿠폰 이벤트로 인해 다른 서비스가 정상적으로 운영되지 않을 수 있다.
  - 예) 데이터베이스가 1초에 10건의 데이터를 insert 할 수 있는 경우, 쿠폰 발행 100건의 insert가 발생한다면 약 10초 동안 다른 서비스는 기다려야 한다.
  - 예2) 데이터베이스의 부하로 인해 타임아웃이 발생하게 된다면, 다른 서비스뿐 아니라 쿠폰 100개도 정상적으로 발행하지 못 할 수 있다.
<br>
## [Kafka를 사용한 처리량 조절](https://github.com/GiJungPark/firstcome-firstserved-event/pull/3)
### Kafka 선택
- Kafka는 토픽에 데이터를 순차적으로 저장하고 처리할 수 있어, 요청량이 많을 때도 순서대로 데이터를 가져와 안정적으로 처리할 수 있다.
- 따라서, API 서버에서 Kafka Producer를 통해 Topic을 전송하고, Consumer에서 데이터를 처리하게 된다면 데이터베이스에 요청하는 양을 조절할 수 있게 된다.
### 상세
- API 서버는 쿠폰 발행 요청을 Kafka 토픽으로 보내고, Consumer 서버는 이 토픽을 받아 쿠폰 발행 이벤트를 순차적으로 처리한다.
### Kafka 적용 전 CPU 사용률
<img width="700" alt="스크린샷 2024-11-07 오후 4 13 16" src="https://github.com/user-attachments/assets/825e1d65-3744-4f87-a187-94eeeaad8b93"><br>
- 최대 16.53%의 CPU 사용률을 보인다.
### Kafka 적용 후 CPU 사용률
<img width="700" alt="스크린샷 2024-11-07 오후 4 15 18" src="https://github.com/user-attachments/assets/925b15b9-f4fc-4ea5-8749-3fe4643180b8"><br>
- 최대 4.8%의 CPU 사용률을 보인다.
- MySQL의 CPU 사용률이 약 4배 감소한 것을 확인할 수 있다.
<br>
## [쿠폰 1인 1매 발행](https://github.com/GiJungPark/firstcome-firstserved-event/pull/4)
### 상세
- Redis의 Set 자료 구조를 사용하여, UserId당 1개의 쿠폰만 발행한다.
<br>
## [Consumer에서 에러가 발생하는 경우](https://github.com/GiJungPark/firstcome-firstserved-event/pull/5)
### 문제점
- Consumer에서 에러가 발생한 경우, Redis의 발행된 쿠폰의 개수는 증가하지만 실제로 발행된 쿠폰의 개수는 100개보다 적을 수 있다.
### 상세
- Consumer에서 에러가 발생한 경우, 백업 데이터와 로그를 남긴다.
- 이후, 배치 프로그램에서 백업 데이터를 주기적으로 읽어서 쿠폰을 발행한다면, 100개의 쿠폰을 정상적으로 발행할 수 있게 된다.