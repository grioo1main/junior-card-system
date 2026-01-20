package com.lum1nar.junior_card.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateParentCardDto {

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 15, message = "Имя должно быть от 2 до 15 символов")
    private String name;

    @Min(value = 18, message = "Родителю должно быть минимум 18 лет")
    @Max(value = 99, message = "Недопустимый возраст")
    private Integer age;
}
