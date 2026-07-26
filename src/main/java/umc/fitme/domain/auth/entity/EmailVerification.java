package umc.fitme.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "is_used")
    @Builder.Default
    private boolean isUsed = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /***
     * 함수 기능: 인증 코드 발송 시점의 EmailVerification 생성
     * @param email 이메일
     * @param verificationCode 인증문자 6자리
     * @param ttlSeconds 만료시간 (5분)
     * @return
     */
    public static EmailVerification create(String email, String verificationCode, long ttlSeconds){
        LocalDateTime now = LocalDateTime.now();
        return EmailVerification.builder()
                .email(email)
                .verificationCode(verificationCode)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .createdAt(now)
                .build();
    }

    /***
     * 함수 기능: 인증 코드 만료 여부
     * @return t/f
     */
    public boolean isExpired() {

        return !LocalDateTime.now().isBefore(expiresAt); // 딱 시간이 같을때도 false 반환
    }

    /***
     * 함수 기능: 인증 코드 진위 여부
     * @param inputCode 전달받은 인증코드
     * @return t/f
     */
    public boolean matches(String inputCode){
        return this.verificationCode.equals(inputCode);
    }

    /***
     * 함수 기능: 이메일 인증 완료된 시각
     */
    public void verify(){
        this.verifiedAt = LocalDateTime.now();
    }

    /***
     * 함수 기능: 인증 번호 사용 처리
     */
    public void consume() {
        this.isUsed = true;
    }
}
