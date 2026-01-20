package com.lum1nar.junior_card.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления данных детской карты.
 * Содержит данные, которые можно обновить: имя и возраст.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCardDto {

    /** Возраст ребенка (от 6 до 17 лет) */
    @Min(value = 6, message = "Для открытия карты ребенку должно быть минимум 6 лет")
    @Max(value = 17, message = "Максимальный возраст 17 лет")
    private Integer age;

    /** Имя ребенка (от 2 до 15 символов) */
    @Size(min = 2, max = 15, message = "Имя должно быть от 2 до 15 символов")
    private String name;
}