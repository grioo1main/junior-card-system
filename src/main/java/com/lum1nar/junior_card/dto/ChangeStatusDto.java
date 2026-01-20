package com.lum1nar.junior_card.dto;

import com.lum1nar.junior_card.model.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusDto {
    @NotNull(message =  "Статус обязателен")
    private CardStatus status;
}
