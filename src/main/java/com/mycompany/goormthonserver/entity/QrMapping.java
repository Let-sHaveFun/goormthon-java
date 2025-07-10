package com.mycompany.goormthonserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * QR 코드 매핑 엔티티
 * QR 코드와 관광지 연결 정보
 */
@Entity
@Table(name = "qr_mappings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qr_code", nullable = false, unique = true, length = 255)
    private String qrCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private TouristSpot touristSpot;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    /**
     * QR 코드가 활성화되어 있는지 확인
     */
    public boolean isAvailable() {
        return isActive != null && isActive;
    }

    /**
     * QR 코드 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * QR 코드 활성화
     */
    public void activate() {
        this.isActive = true;
    }
}