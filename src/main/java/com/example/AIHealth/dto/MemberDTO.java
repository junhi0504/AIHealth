package com.example.AIHealth.dto;

import com.example.AIHealth.entity.MemberEntity;
import com.example.AIHealth.entity.Role; // Role import 추가
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDTO {
    private Long id;
    private String memberEmail;
    private String memberPassword;
    private String memberName;
    private Role role; // ✅ Role 필드 추가

    public static MemberDTO toMemberDTO(MemberEntity memberEntity) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(memberEntity.getId());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberPassword(memberEntity.getMemberPassword());
        memberDTO.setMemberName(memberEntity.getMemberName());
        memberDTO.setRole(memberEntity.getRole()); // ✅ Entity의 role 정보를 DTO로 복사
        return memberDTO;
    }
}