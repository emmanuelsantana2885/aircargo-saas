package com.aircargo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Airline airline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Flight flight;

    // mawb_id es nullable — el booking puede existir sin MAWB asignado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mawb_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Mawb mawb;

    @Column(name = "client_name", nullable = false, length = 150)
    private String clientName;

    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @Column(name = "cnee", length = 150)
    private String cnee;

    @Column(name = "shipper_name", length = 150)
    private String shipperName;

    @Column(name = "awb_number", length = 20)
    private String awbNumber;

    @Column(name = "skids")
    private Integer skids = 0;

    @Column(name = "units")
    private Integer units = 0;

    @Column(name = "reserved_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal reservedKg;

    @Column(name = "confirmed_kg", precision = 10, scale = 3)
    private BigDecimal confirmedKg = BigDecimal.ZERO;

    @Column(name = "received_kg", precision = 10, scale = 3)
    private BigDecimal receivedKg = BigDecimal.ZERO;

    @Column(name = "fulfillment_pct", precision = 6, scale = 4)
    private BigDecimal fulfillmentPct = BigDecimal.ZERO;

    @Column(name = "destination", columnDefinition = "bpchar(3)")
    private String destination;

    @Column(name = "priority")
    private Integer priority = 0;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "commodity_type", nullable = false, columnDefinition = "commodity_type")
    private CommodityType commodityType = CommodityType.DRY_CARGO;

    @Column(name = "day_received", length = 5)
    private String dayReceived;

    @Column(name = "time_hours", precision = 6, scale = 4)
    private BigDecimal timeHours = BigDecimal.ZERO;

    @Column(name = "positions", precision = 8, scale = 4)
    private BigDecimal positions = BigDecimal.ZERO;

    @Column(name = "real_positions", precision = 8, scale = 4)
    private BigDecimal realPositions = BigDecimal.ZERO;

    @Column(name = "last_week_kg", precision = 10, scale = 3)
    private BigDecimal lastWeekKg = BigDecimal.ZERO;

    @Column(name = "last_week_positions", precision = 8, scale = 4)
    private BigDecimal lastWeekPositions = BigDecimal.ZERO;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed = false;

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}