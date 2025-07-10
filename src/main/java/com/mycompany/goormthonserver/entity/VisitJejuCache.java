package com.mycompany.goormthonserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 비짓제주 API 캐시 엔티티 (MySQL 백업용)
 * 7개 핵심 필드만 저장
 */
@Entity
@Table(name = "visitjeju_cache", indexes = {
        @Index(name = "idx_external_id", columnList = "external_id", unique = true),
        @Index(name = "idx_tourist_spot", columnList = "tourist_spot_id"),
        @Index(name = "idx_cached_at", columnList = "cached_at"),
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_active", columnList = "is_active")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitJejuCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_spot_id", nullable = false)
    private TouristSpot touristSpot;

    @Column(name = "external_id", nullable = false, unique = true, length = 100)
    private String externalId; // 비짓제주 콘텐츠 ID

    // 핵심 7개 필드
    @Column(name = "title", nullable = false, length = 500)
    private String title; // 관광지명

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction; // 관광지 간단 소개

    @Column(name = "tag", columnDefinition = "TEXT")
    private String tag; // 주요 키워드

    @Column(name = "address", length = 500)
    private String address; // 지번 주소

    @Column(name = "photo_id")
    private Long photoId; // 이미지 고유번호

    @Column(name = "img_path", length = 1000)
    private String imgPath; // 원본 이미지 URL

    // 캐시 관리
    @Column(name = "cached_at", nullable = false, updatable = false)
    private LocalDateTime cachedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // 24시간 후

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        cachedAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = cachedAt.plusHours(24); // 24시간 후 만료
        }
    }

    // 비즈니스 메서드
    /**
     * 캐시가 만료되었는지 확인
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 캐시가 유효한지 확인 (활성 상태 + 만료되지 않음)
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) && !isExpired();
    }

    /**
     * 캐시 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 캐시 갱신 (만료시간 연장)
     */
    public void refresh() {
        this.cachedAt = LocalDateTime.now();
        this.expiresAt = this.cachedAt.plusHours(24);
        this.isActive = true;
    }

    /**
     * VisitJejuResponseDto로 변환
     */
    public com.mycompany.goormthonserver.dto.VisitJejuResponseDto toDto() {
        return com.mycompany.goormthonserver.dto.VisitJejuResponseDto.builder()
                .contentsId(externalId)
                .title(title)
                .introduction(introduction)
                .tag(tag)
                .address(address)
                .photoId(photoId)
                .imgPath(imgPath)
                .source(com.mycompany.goormthonserver.dto.VisitJejuResponseDto.CacheSource.MYSQL.name())
                .build();
    }

    /**
     * VisitJejuResponseDto에서 생성
     */
    public static VisitJejuCache fromDto(TouristSpot touristSpot,
                                         com.mycompany.goormthonserver.dto.VisitJejuResponseDto dto) {
        return VisitJejuCache.builder()
                .touristSpot(touristSpot)
                .externalId(dto.getContentsId())
                .title(dto.getTitle())
                .introduction(dto.getIntroduction())
                .tag(dto.getTag())
                .address(dto.getAddress())
                .photoId(dto.getPhotoId())
                .imgPath(dto.getImgPath())
                .isActive(true)
                .build();
    }
}