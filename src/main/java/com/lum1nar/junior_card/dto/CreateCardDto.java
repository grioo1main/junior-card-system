package com.lum1nar.junior_card.dto;

import jakarta.validation.constraints.*;
import lombok.*;
@Data
@NoArgsConstructor
public class CreateCardDto {

    @NotBlank(message = "Имя должно быть обязательно")
    @Size(min = 2 , max = 15 , message = "Имя от 2 до 15 символов")
    private String name;

    @NotNull(message = "Возраст обязателен")
    @Min(value = 6, message = "Минимальный возраст 6 лет")
    @Max(value = 17, message = "Максимальный возраст 17 лет")
    private Integer childAge;

    @NotNull(message = "ID родительской карты обязателен")
    @Min(value = 1, message = "ID должен быть положительным")
    private Long parentCardId;

    public CreateCardDto(String name, Integer childAge, Long parentCardId) {
        this.name = name;
        this.childAge = childAge;
        this.parentCardId = parentCardId;
    }
}
