package com.lum1nar.junior_card.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.lum1nar.junior_card.dto.CreateCardDto;
import lombok.*;



import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "junior_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JuniorCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 15)
    @Column(nullable = false)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_card_id" , nullable = false)
    @JsonBackReference
    private ParentCard parentCard;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private CardStatus status = CardStatus.PENDING;

    @NotNull
    @Min(6)
    @Max(17)
    @Column(name = "child_age", nullable = false)
    private Integer childAge;




}
