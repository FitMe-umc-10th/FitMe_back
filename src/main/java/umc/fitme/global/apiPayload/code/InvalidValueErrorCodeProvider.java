package umc.fitme.global.apiPayload.code;

/***
 * 요청 파라미터 바인딩에 실패했을 때 어떤 에러 코드를 내려줄지 enum 스스로 정하게 하는 인터페이스.
 *
 * 이 인터페이스가 없으면 global 예외 핸들러가 도메인 enum을 직접 import 해서
 * if-else 로 분기해야 하고(global -> domain 역방향 의존), enum이 늘 때마다 핸들러를 고쳐야 한다.
 *
 * 구현체는 enum 이어야 한다. 핸들러가 상수 하나를 꺼내 이 메서드를 호출하는 방식으로 코드를 얻는다.
 */
public interface InvalidValueErrorCodeProvider {

    BaseErrorCode invalidValueErrorCode();
}
