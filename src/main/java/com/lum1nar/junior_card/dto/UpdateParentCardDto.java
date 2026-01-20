package com.lum1nar.junior_card.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateParentCardDto {
    @Min(value = 18 , message = "Для открытия карты взрослого должно быть минимум 18 лет")
    @Max(value = 99, message = "Максимальный возраст 99 лет")
    Integer age;

    @Size(min = 2 , max = 15 , message = "Имя должно быть от 2 до 15 символов")
    String name;

}
