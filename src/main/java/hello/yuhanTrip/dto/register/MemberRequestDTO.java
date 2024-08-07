package hello.yuhanTrip.dto.register;

import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class MemberRequestDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String checkPassword;


    @NotBlank
    private String certificationNumber; // 추가: 인증번호 필드

    @NotBlank
    private MemberRole memberRole;



    public Member toMember(PasswordEncoder passwordEncoder) {
        // 회원 객체를 생성하고 반환
        return Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .memberRole(MemberRole.MEMBER)
                .build();
    }

    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(email, password);
        // 사용자 로그인 기능 처리하기 위한 메서드
    }

    // 추가: 인증번호를 DTO에 포함시키는 메서드
    public void setCertificationNumber(String certificationNumber) {
        this.certificationNumber = certificationNumber;
    }

}
