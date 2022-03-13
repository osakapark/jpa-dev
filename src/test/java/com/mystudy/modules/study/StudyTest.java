package com.mystudy.modules.study;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mystudy.modules.member.Member;
import com.mystudy.modules.member.UserMember;
import com.mystudy.modules.study.Study;

public class StudyTest {
    Study study;
    Member member;
    UserMember userMember;

    @BeforeEach
    void beforeEach() {
        study = new Study();
        member = new Member();
        member.setNickname("keesun");
        member.setPassword("123");
        userMember = new UserMember(member);

    }

    @DisplayName("스터디를 공개했고 인원 모집 중이고, 이미 멤버나 스터디 관리자가 아니라면 스터디 가입 가능")
    @Test
    void isJoinable() {
        study.setPublished(true);
        study.setRecruiting(true);

        assertTrue(study.isJoinable(userMember));
    }

    @DisplayName("스터디를 공개했고 인원 모집 중이더라도, 스터디 관리자는 스터디 가입이 불필요하다.")
    @Test
    void isJoinable_false_for_manager() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addManager(member);

        assertFalse(study.isJoinable(userMember));
    }

    @DisplayName("스터디를 공개했고 인원 모집 중이더라도, 스터디 멤버는 스터디 재가입이 불필요하다.")
    @Test
    void isJoinable_false_for_member() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addMember(member);

        assertFalse(study.isJoinable(userMember));
    }

    @DisplayName("스터디가 비공개거나 인원 모집 중이 아니면 스터디 가입이 불가능하다.")
    @Test
    void isJoinable_false_for_non_recruiting_study() {
        study.setPublished(true);
        study.setRecruiting(false);

        assertFalse(study.isJoinable(userMember));

        study.setPublished(false);
        study.setRecruiting(true);

        assertFalse(study.isJoinable(userMember));
    }

    @DisplayName("스터디 관리자인지 확인")
    @Test
    void isManager() {
        study.addManager(member);
        assertTrue(study.isManager(userMember));
    }

    @DisplayName("스터디 멤버인지 확인")
    @Test
    void isMember() {
        study.addMember(member);
        assertTrue(study.isMember(userMember));
    }


}
