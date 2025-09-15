package com.example.AIHealth.entity;

import com.example.AIHealth.dto.MemberDTO;
import jakarta.persistence.*;
import lombok.*; // 추가

import java.util.List;

@Entity
@Setter
@Getter
@Builder              // ✨ 추가
@AllArgsConstructor   // ✨ 추가
@NoArgsConstructor    // ✨ 추가
@Table(name = "member_table")
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String memberEmail;

    @Column
    private String memberPassword;

    @Column
    private String memberName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InbodyEntity> inbodyList;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RecommendationEntity> recommendations;

    public static MemberEntity toMemberEntity(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(memberDTO.getId());
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());
        memberEntity.setMemberName(memberDTO.getMemberName());
        if (memberDTO.getRole() != null) {
            memberEntity.setRole(memberDTO.getRole());
        }
        return memberEntity;
    }

    public static MemberEntity toUpdateMemberEntity(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(memberDTO.getId());
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());
        memberEntity.setMemberName(memberDTO.getMemberName());
        if (memberDTO.getRole() != null) {
            memberEntity.setRole(memberDTO.getRole());
        }
        return memberEntity;
    }

    // ✨ 소셜 로그인 시 닉네임(이름) 업데이트를 위한 메소드 추가
    public MemberEntity updateName(String name) {
        this.memberName = name;
        return this;
    }
}