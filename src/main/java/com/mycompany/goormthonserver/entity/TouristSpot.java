package com.mycompany.goormthonserver.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tourist_spots")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TouristSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "tag", length = 200)
    private String tag;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "imgpath", length = 500)
    private String imgPath;

    @Column(name = "script", columnDefinition = "TEXT")
    private String script;

    @Column(name = "audioUrl", length = 500)
    private String audioUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 생성자
    public TouristSpot(String externalId, String name, String address,
                       BigDecimal latitude, BigDecimal longitude) {
        this.externalId = externalId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Builder 패턴을 위한 정적 메서드
    public static TouristSpot create(String externalId, String name, String address,
                                     BigDecimal latitude, BigDecimal longitude) {
        return new TouristSpot(externalId, name, address, latitude, longitude);
    }

    // 비즈니스 로직 메서드
    public void updateBasicInfo(String name, String address, String description) {
        this.name = name;
        this.address = address;
        this.description = description;
    }

    public void updateLocation(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateContent(String introduction, String script, String audioUrl) {
        this.introduction = introduction;
        this.script = script;
        this.audioUrl = audioUrl;
    }

    public void updateCategory(String category, String tag) {
        this.category = category;
        this.tag = tag;
    }

    public void updateImagePath(String imgPath) {
        this.imgPath = imgPath;
    }

    // 유틸리티 메서드
    public boolean hasAudioContent() {
        return audioUrl != null && !audioUrl.trim().isEmpty();
    }

    public boolean hasImage() {
        return imgPath != null && !imgPath.trim().isEmpty();
    }

    public boolean isComplete() {
        return name != null && !name.trim().isEmpty() &&
                latitude != null && longitude != null &&
                hasAudioContent();
    }

    @Override
    public String toString() {
        return "TouristSpot{" +
                "id=" + id +
                ", externalId='" + externalId + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}