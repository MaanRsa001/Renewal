package com.insurer.renewal.policy;

import com.insurer.renewal.product.Country;
import com.insurer.renewal.product.LineOfBusiness;
import com.insurer.renewal.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "policy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_no", nullable = false, unique = true, length = 50)
    private String policyNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lob_id")
    private LineOfBusiness lineOfBusiness;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, CANCELLED, LAPSED, EXPIRED

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "outstanding_premium", nullable = false)
    private BigDecimal outstandingPremium;

    @Column(name = "total_premium", nullable = false)
    private BigDecimal totalPremium;

    @Column(name = "sum_insured")
    private BigDecimal sumInsured;

    @Column(name = "fraud_flag", nullable = false)
    private boolean fraudFlag;

    @Column(name = "already_renewed", nullable = false)
    private boolean alreadyRenewed;

    /**
     * Product-specific attributes that don't warrant their own column
     * (vehicle age, occupancy, contract value, construction period, etc.).
     * The rule engine reads these via "extra.<key>" field paths.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_attributes", columnDefinition = "json")
    @Builder.Default
    private Map<String, Object> extraAttributes = new HashMap<>();
}
