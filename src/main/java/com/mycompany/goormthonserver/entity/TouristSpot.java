package com.mycompany.goormthonserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * 관광지 엔티티
 * 제주도 주요 관광지 정보를 저장
 */
@Entity
@Table(name = "tourist_spots", indexes = {
        @Index(name = "idx_location", columnList = "latitude, longitude"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_name", columnList = "name")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, length = 100)
    private String externalId; // 제주 Visit API ID

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "operating_hours", length = 200)
    private String operatingHours;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계 매핑
    @OneToMany(mappedBy = "touristSpot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AudioContent> audioContents;

    @OneToMany(mappedBy = "touristSpot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QrMapping> qrMappings;

    @OneToMany(mappedBy = "touristSpot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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

    // 비즈니스 메서드
    /**
     * 두 좌표 간의 거리 계산 (km)
     */
    public double calculateDistance(BigDecimal lat, BigDecimal lng) {
        double R = 6371; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat.subtract(latitude).doubleValue());
        double dLng = Math.toRadians(lng.subtract(longitude).doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latitude.doubleValue())) *
                        Math.cos(Math.toRadians(lat.doubleValue())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 활성화된 오디오 콘텐츠 개수
     */
    public long getActiveAudioContentCount() {
        if (audioContents == null) return 0;
        return audioContents.stream()
                .filter(content -> "COMPLETED".equals(content.getGenerationStatus()))
                .count();
    }
}