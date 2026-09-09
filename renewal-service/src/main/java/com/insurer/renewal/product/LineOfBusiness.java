package com.insurer.renewal.product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "line_of_business")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LineOfBusiness {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;
}
