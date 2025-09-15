package com.example.AIHealth.config;

import com.example.AIHealth.entity.MemberEntity;
import com.example.AIHealth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder import
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // PasswordEncoder 주입

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<MemberEntity> optionalMember = memberRepository.findByMemberEmail(username);

        if (optionalMember.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        MemberEntity memberEntity = optionalMember.get();
        String dbPassword = memberEntity.getMemberPassword();

        // ▼▼▼ [추가] DB 비밀번호가 암호화되지 않았다면, 암호화해서 다시 저장 ▼▼▼
        if (dbPassword != null && !dbPassword.startsWith("$2a$")) {
            memberEntity.setMemberPassword(passwordEncoder.encode(dbPassword));
            memberRepository.save(memberEntity);
        }

        return User.builder()
                .username(memberEntity.getMemberEmail())
                .password(memberEntity.getMemberPassword()) // DB에 저장된 (이제 암호화된) 비밀번호
                .roles(memberEntity.getRole().name())
                .build();
    }
}