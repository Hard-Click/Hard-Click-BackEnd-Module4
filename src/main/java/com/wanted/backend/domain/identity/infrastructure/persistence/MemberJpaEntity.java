package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.Role;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_member_email", columnNames = "email")
})
public class MemberJpaEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 20)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String gender;

    private LocalDate birthDate;

    @Column(length = 20)
    private String phoneNumber;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    private boolean isPasswordChangeRequired;
    private int loginFailCount;
    private boolean isLocked;
    private LocalDateTime lockedAt;
    private LocalDateTime lastLoginAt;
    private boolean optionalTermsAgreed;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // JPA가 리플렉션을 통해 엔티티를 생성할 때 사용하는 기본 생성자
    protected MemberJpaEntity() {
    }

    // 신규 생성을 위한 생성자
    public MemberJpaEntity(String username, String email, String password, String name,
                           String gender, LocalDate birthDate, String phoneNumber, String profileImageUrl,
                           Role role, MemberStatus status, boolean isPasswordChangeRequired,
                           int loginFailCount, boolean isLocked, LocalDateTime lockedAt,
                           LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt,boolean optionalTermsAgreed) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.status = status != null ? status : MemberStatus.ACTIVE;
        this.isPasswordChangeRequired = isPasswordChangeRequired;
        this.loginFailCount = loginFailCount;
        this.isLocked = isLocked;
        this.lockedAt = lockedAt;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.optionalTermsAgreed = optionalTermsAgreed;
    }

    public void updateFromDomain(Member domain) {
        this.password = domain.getPassword();
        this.name = domain.getName();
        this.gender = domain.getGender();
        this.birthDate = domain.getBirthDate();
        this.phoneNumber = domain.getPhoneNumber();
        this.profileImageUrl = domain.getProfileImageUrl();
        this.role = domain.getRole();
        this.status = domain.getStatus();
        this.isPasswordChangeRequired = domain.isPasswordChangeRequired();
        this.loginFailCount = domain.getLoginFailCount();
        this.isLocked = domain.isLocked();
        this.lockedAt = domain.getLockedAt();
        this.lastLoginAt = domain.getLastLoginAt();
        this.updatedAt = domain.getUpdatedAt();
        this.optionalTermsAgreed = domain.isOptionalTermsAgreed();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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
    public boolean isOptionalTermsAgreed() {
        return optionalTermsAgreed;
    }
}