package com.aircargo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "uld_type_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UldTypeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Airline airline;

    @Enumerated(EnumType.STRING)
    @Column(name = "uld_type", nullable = false, columnDefinition = "uld_type")
    private UldType uldType;

    @Column(name = "default_tare_lbs", nullable = false, precision = 8, scale = 2)
    private BigDecimal defaultTareLbs;

    @Column(name = "max_gross_lbs", precision = 10, scale = 2)
    private BigDecimal maxGrossLbs;

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
