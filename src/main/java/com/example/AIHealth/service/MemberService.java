package com.example.AIHealth.service;

import com.example.AIHealth.dto.MemberDTO;
import com.example.AIHealth.entity.MemberEntity;
import com.example.AIHealth.entity.Role;
import com.example.AIHealth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void save(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberName(memberDTO.getMemberName());
        memberEntity.setMemberPassword(passwordEncoder.encode(memberDTO.getMemberPassword()));
        memberEntity.setRole(Role.USER);
        memberRepository.save(memberEntity);
    }

    // Spring Security가 로그인 처리를 대신하므로 이 메소드는 이제 사용되지 않습니다.
    // 다른 곳에서 사용하지 않는다면 삭제해도 무방합니다. 여기서는 유지하겠습니다.
    public MemberDTO login(MemberDTO memberDTO) {
        Optional<MemberEntity> byMemberEmail = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());
        if (byMemberEmail.isPresent()) {
            MemberEntity memberEntity = byMemberEmail.get();
            if (passwordEncoder.matches(memberDTO.getMemberPassword(), memberEntity.getMemberPassword())) {
                return MemberDTO.toMemberDTO(memberEntity);
            }
        }
        return null;
    }

    public List<MemberDTO> findAll() {
        List<MemberEntity> memberEntityList = memberRepository.findAll();
        // stream을 사용한 코드로 간결하게 변경
        return memberEntityList.stream()
                .map(MemberDTO::toMemberDTO)
                .collect(Collectors.toList());
    }

    public MemberDTO findById(Long id) {
        return memberRepository.findById(id)
                .map(MemberDTO::toMemberDTO)
                .orElse(null);
    }

    // MainController에서 사용할 이메일로 회원 정보 조회 메소드
    public MemberDTO findByMemberEmail(String memberEmail) {
        return memberRepository.findByMemberEmail(memberEmail)
                .map(MemberDTO::toMemberDTO)
                .orElse(null);
    }

    public MemberDTO updateForm(String myEmail) {
        return memberRepository.findByMemberEmail(myEmail)
                .map(MemberDTO::toMemberDTO)
                .orElse(null);
    }

    public void update(MemberDTO memberDTO) {
        MemberEntity existingMember = memberRepository.findById(memberDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("ID에 해당하는 회원이 없습니다."));

        existingMember.setMemberEmail(memberDTO.getMemberEmail());
        existingMember.setMemberName(memberDTO.getMemberName());

        if (memberDTO.getMemberPassword() != null && !memberDTO.getMemberPassword().isEmpty()) {
            existingMember.setMemberPassword(passwordEncoder.encode(memberDTO.getMemberPassword()));
        }

        memberRepository.save(existingMember);
    }

    public void deleteById(Long id) {
        memberRepository.deleteById(id);
    }

    public String emailCheck(String memberEmail) {
        return memberRepository.findByMemberEmail(memberEmail).isPresent() ? null : "ok";
    }

    public String nameCheck(String memberName) {
        return memberRepository.findByMemberName(memberName).isPresent() ? null : "ok";
    }
}