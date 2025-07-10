package com.mycompany.goormthonserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 사용자 수집 엔티티
 * 사용자의 조각(관광지) 수집 현황
 */
@Entity
@Table(name = "user_collections",
        uniqueConstraints = @UniqueConstraint(name = "unique_user_spot", columnNames = {"user_id", "spot_id"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId; // 임시 사용자 식별자 (세션ID, 디바이스ID 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private TouristSpot touristSpot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_content_id")
    private AudioContent audioContent;

    @Column(name = "collected_at", nullable = false, updatable = false)
    private LocalDateTime collectedAt;

    @PrePersist
    protected void onCreate() {
        collectedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    /**
     * 수집한 지 며칠이 지났는지 계산
     */
    public long getDaysSinceCollection() {
        return java.time.temporal.ChronoUnit.DAYS.between(
                collectedAt.toLocalDate(),
                LocalDateTime.now().toLocalDate()
        );
    }

    /**
     * 오늘 수집했는지 확인
     */
    public boolean isCollectedToday() {
        return collectedAt.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    /**
     * 특정 오디오 콘텐츠로 수집했는지 확인
     */
    public boolean isCollectedWithAudio() {
        return audioContent != null;
    }
}