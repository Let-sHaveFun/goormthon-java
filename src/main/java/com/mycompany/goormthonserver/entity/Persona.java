package com.mycompany.goormthonserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 페르소나 엔티티
 * AI 음성 생성을 위한 다양한 캐릭터 정보
 */
@Entity
@Table(name = "personas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "voice_style", length = 100)
    private String voiceStyle;

    @Column(name = "language_code", length = 10)
    @Builder.Default
    private String languageCode = "ko-KR";

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @Builder.Default
    private Gender gender = Gender.NEUTRAL;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 연관관계 매핑
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AudioContent> audioContents;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 성별 enum
    public enum Gender {
        MALE("남성"),
        FEMALE("여성"),
        NEUTRAL("중성");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 비즈니스 메서드
    /**
     * 활성화된 페르소나인지 확인
     */
    public boolean isAvailable() {
        return isActive != null && isActive;
    }

    /**
     * 페르소나에 연결된 오디오 콘텐츠 개수
     */
    public long getAudioContentCount() {
        if (audioContents == null) return 0;
        return audioContents.size();
    }

    /**
     * 완료된 오디오 콘텐츠 개수
     */
    public long getCompletedAudioContentCount() {
        if (audioContents == null) return 0;
        return audioContents.stream()
                .filter(content -> "COMPLETED".equals(content.getGenerationStatus()))
                .count();
    }
}