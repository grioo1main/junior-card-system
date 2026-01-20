package com.lum1nar.junior_card.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "parent_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Size(min = 2, max = 15)
    @Column(nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    UserStatus status;

    @Min(18)
    @Max(99)
    @Column(nullable = false)
    int age;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    CardStatus cardStatus;

    @OneToMany(mappedBy = "parentCard", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    List<JuniorCard> juniorCards;


}
