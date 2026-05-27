package com.wanted.backend.domain.identity.domain.model;

import com.wanted.backend.domain.identity.domain.event.MemberLoggedInEvent;
import com.wanted.backend.global.domain.DomainEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Member {
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    private final Long id;
    private final String username;
    private final String email;
    private String password;
    private final String name;
    private String gender;
    private LocalDate birthDate;
    private String phoneNumber;
    private String profileImageUrl;
    private final Role role;
    private MemberStatus status;
    private boolean isPasswordChangeRequired;
    private int loginFailCount;
    private boolean isLocked;
    private LocalDateTime lockedAt;
    private LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 아키텍처 가이드라인에 따른 도메인 이벤트 리스트 (순수 자바)
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Member(Long id, String username, String email, String password, String name, String gender,
                   LocalDate birthDate, String phoneNumber, String profileImageUrl, Role role, MemberStatus status,
                   boolean isPasswordChangeRequired, int loginFailCount, boolean isLocked,
                   LocalDateTime lockedAt, LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.status = status;
        this.isPasswordChangeRequired = isPasswordChangeRequired;
        this.loginFailCount = loginFailCount;
        this.isLocked = isLocked;
        this.lockedAt = lockedAt;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Member create(String username, String email, String password, String name,
                                String gender, LocalDate birthDate, String phoneNumber,
                                String profileImageUrl, Role role) {
        LocalDateTime now = LocalDateTime.now();
        return new Member(
                null, username, email, password, name, gender, birthDate, phoneNumber,
                profileImageUrl, role, MemberStatus.ACTIVE, false, 0, false, null, null,
                now, now
        );
    }
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.isPasswordChangeRequired = false; // 임시비번 상태 해제
        this.updatedAt = LocalDateTime.now();
    }


    public static Member restore(Long id, String username, String email, String password, String name, String gender,
                                 LocalDate birthDate, String phoneNumber, String profileImageUrl, Role role,
                                 MemberStatus status, boolean isPasswordChangeRequired, int loginFailCount,
                                 boolean isLocked, LocalDateTime lockedAt, LocalDateTime lastLoginAt,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Member(id, username, email, password, name, gender, birthDate, phoneNumber,
                profileImageUrl, role, status, isPasswordChangeRequired, loginFailCount,
                isLocked, lockedAt, lastLoginAt, createdAt, updatedAt);
    }

    // 도메인 행위: 로그인 성공 처리
    public void loginSuccess(LocalDateTime now) {
        this.loginFailCount = 0;
        this.isLocked = false;
        this.lockedAt = null;
        this.lastLoginAt = now;
        this.updatedAt = now;

        // [Record Event] 도메인 내부에서 이벤트를 생성하여 기록합니다.
        registerEvent(new MemberLoggedInEvent(this.id, now));
    }
    public void loginFailed(LocalDateTime now) {
        if (this.isLocked) {
            return;
        }

        this.loginFailCount++;
        this.updatedAt = now;

        if (this.loginFailCount >= MAX_LOGIN_FAIL_COUNT) {
            this.isLocked = true;
            this.lockedAt = now;
        }
    }


    // UI에서 수정 가능한 프로필 이미지와 비밀번호만 변경합니다.
    public void updateProfile(String profileImageUrl, String encodedPassword, LocalDateTime now) {
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
        if (encodedPassword != null) {
            this.password = encodedPassword;
            this.isPasswordChangeRequired = false;
        }
        this.updatedAt = now;
    }

    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }


    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    public void changePasswordAndUnlock(String encodedPassword, LocalDateTime now) {
        this.password = encodedPassword;
        this.isPasswordChangeRequired = false;
        this.loginFailCount = 0;
        this.isLocked = false;
        this.lockedAt = null;
        this.updatedAt = now;
    }

    public void withdraw(LocalDateTime now) {
        this.status = MemberStatus.WITHDRAWN;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getGender() { return gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public Role getRole() { return role; }
    public MemberStatus getStatus() { return status; }
    public boolean isPasswordChangeRequired() { return isPasswordChangeRequired; }
    public int getLoginFailCount() { return loginFailCount; }
    public boolean isLocked() { return isLocked; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
