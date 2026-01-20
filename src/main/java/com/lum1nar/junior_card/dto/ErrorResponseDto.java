package com.lum1nar.junior_card.dto;
import com.lum1nar.junior_card.model.JuniorCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    
    // HTTP статус код (200, 404, 500 и т.д.)
    private int status;
    
    // Понятное сообщение об ошибке для клиента
    private String message;
    
    // Ошибка код для программной обработки
    private String errorCode;
    
    // Время когда произошла ошибка
    private LocalDateTime timestamp;
    
    // Путь, который вызвал ошибку
    private String path;
    
    // Дополнительные детали (например, список field errors)
    private String details;

    // Для вывода карт на id родителя
    private List<JuniorCard> existingCards;
}
