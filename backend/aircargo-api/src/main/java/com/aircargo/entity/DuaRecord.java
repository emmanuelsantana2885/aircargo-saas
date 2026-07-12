package com.aircargo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "dua_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mawb_id", nullable = false)
    private Mawb mawb;

    @Column(name = "dua_number", nullable = false, length = 50)
    private String duaNumber;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DuaStatus status = DuaStatus.PENDING;

    @Column(name = "dua_date")
    private LocalDate duaDate;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "customs_broker", length = 150)
    private String customsBroker;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
