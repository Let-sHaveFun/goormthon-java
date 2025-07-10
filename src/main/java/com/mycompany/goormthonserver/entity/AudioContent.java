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
 * 오디오 콘텐츠 엔티티
 * AI 생성 스크립트 및 TTS 오디오 정보
 */
@Entity
@Table(name = "audio_contents", indexes = {
        @Index(name = "idx_spot_persona", columnList = "spot_id, persona_id"),
        @Index(name = "idx_status", columnList = "generation_status")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private TouristSpot touristSpot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(name = "script_standard", nullable = false, columnDefinition = "TEXT")
    private String scriptStandard;

    @Column(name = "script_dialect", columnDefinition = "TEXT")
    private String scriptDialect;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "audio_duration")
    private Integer audioDuration; // 초 단위

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status")
    @Builder.Default
    private GenerationStatus generationStatus = GenerationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계 매핑
    @OneToMany(mappedBy = "audioContent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCollection> userCollections;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 생성 상태 enum
    public enum GenerationStatus {
        PENDING("대기중"),
        GENERATING("생성중"),
        COMPLETED("완료"),
        FAILED("실패");

        private final String description;

        GenerationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 비즈니스 메서드
    /**
     * 오디오 생성이 완료되었는지 확인
     */
    public boolean isCompleted() {
        return GenerationStatus.COMPLETED.equals(generationStatus);
    }

    /**
     * 오디오 생성이 실패했는지 확인
     */
    public boolean isFailed() {
        return GenerationStatus.FAILED.equals(generationStatus);
    }

    /**
     * 오디오 생성 중인지 확인
     */
    public boolean isGenerating() {
        return GenerationStatus.GENERATING.equals(generationStatus);
    }

    /**
     * 오디오 재생 시간을 분:초 형식으로 반환
     */
    public String getFormattedDuration() {
        if (audioDuration == null || audioDuration <= 0) {
            return "00:00";
        }
        int minutes = audioDuration / 60;
        int seconds = audioDuration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 생성 상태를 업데이트
     */
    public void updateStatus(GenerationStatus status, String errorMessage) {
        this.generationStatus = status;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 오디오 URL 설정 및 완료 상태로 변경
     */
    public void completeGeneration(String audioUrl, Integer duration) {
        this.audioUrl = audioUrl;
        this.audioDuration = duration;
        this.generationStatus = GenerationStatus.COMPLETED;
        this.errorMessage = null;
        this.updatedAt = LocalDateTime.now();
    }
}