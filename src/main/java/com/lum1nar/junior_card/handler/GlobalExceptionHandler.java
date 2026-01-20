package com.lum1nar.junior_card.handler;

import com.lum1nar.junior_card.dto.ErrorResponseDto;
import com.lum1nar.junior_card.exception.AccountLimitCards;
import com.lum1nar.junior_card.exception.ApplicationException;
import com.lum1nar.junior_card.exception.CardNotFoundException;
import com.lum1nar.junior_card.exception.InsufficientPermissionsException;
import com.lum1nar.junior_card.model.JuniorCard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Глобальный обработчик исключений для всего приложения.
 * Перехватывает исключения и возвращает унифицированный формат ошибки.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Вспомогательный метод для создания ошибки без списка карт.
     *
     * @param status HTTP статус
     * @param message сообщение об ошибке
     * @param errorCode код ошибки
     * @param request веб-запрос
     * @return объект ошибки
     */
    private ErrorResponseDto buildError(
            HttpStatus status,
            String message,
            String errorCode,
            WebRequest request) {
        return buildError(status, message, errorCode, request, null);
    }

    /**
     * Универсальный builder для ErrorResponseDto.
     * Используется во всех обработчиках для единообразия.
     *
     * @param status HTTP статус
     * @param message сообщение об ошибке
     * @param errorCode код ошибки
     * @param request веб-запрос
     * @param existingCards список существующих карт (если применимо)
     * @return объект ошибки
     */
    private ErrorResponseDto buildError(
            HttpStatus status,
            String message,
            String errorCode,
            WebRequest request,
            List<JuniorCard> existingCards) {

        return ErrorResponseDto.builder()
                .status(status.value())
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .existingCards(existingCards)
                .build();
    }

    // ========== ПОЛЬЗОВАТЕЛЬСКИЕ ИСКЛЮЧЕНИЯ ==========

    /**
     * Обработчик для CardNotFoundException.
     * Возникает когда карта не найдена в БД по ID.
     *
     * @param exception исключение
     * @param request веб-запрос
     * @return ответ с ошибкой (HTTP 404)
     */
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCardNotFoundException(
            CardNotFoundException exception, WebRequest request) {
        log.warn("Карта не найдена: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, exception.getMessage(),
                        exception.getErrorCode(), request));
    }

    /**
     * Обработчик для AccountLimitCards.
     * Возникает при попытке создать 4-ю карту у родителя (лимит = 3).
     * В ответе возвращает список существующих карт.
     *
     * @param exception исключение
     * @param request веб-запрос
     * @return ответ с ошибкой (HTTP 409) и списком существующих карт
     */
    @ExceptionHandler(AccountLimitCards.class)
    public ResponseEntity<ErrorResponseDto> handleAccountLimitCards(
            AccountLimitCards exception, WebRequest request) {
        log.warn("Превышен лимит аккаунта: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, exception.getMessage(),
                        exception.getErrorCode(), request, exception.getExistingCards()));
    }

    /**
     * Обработчик для InsufficientPermissionsException.
     * Возникает при попытке выполнить операцию без прав доступа.
     *
     * @param exception исключение
     * @param request веб-запрос
     * @return ответ с ошибкой (HTTP 403)
     */
    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientPermissionsException(
            InsufficientPermissionsException exception, WebRequest request) {
        log.warn("Недостаточно прав: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, exception.getMessage(),
                        exception.getErrorCode(), request));
    }

    /**
     * Обработчик для ApplicationException.
     * Общий обработчик для всех кастомных бизнес-исключений.
     *
     * @param exception исключение
     * @param request веб-запрос
     * @return ответ с ошибкой (HTTP 400)
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponseDto> handleApplicationException(
            ApplicationException exception, WebRequest request) {
        log.warn("Ошибка приложения: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, exception.getMessage(),
                        exception.getErrorCode(), request));
    }

    // ========== SPRING VALIDATION ИСКЛЮЧЕНИЯ ==========

    /**
     * Обработчик для MethodArgumentNotValidException.
     * Возникает при провале @Valid валидации в DTO.
     * Возвращает ошибки валидации для каждого поля.
     *
     * @param exception исключение
     * @param request веб-запрос
     * @return ответ с ошибкой (HTTP 400) и детали ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
            MethodArgumentNotValidException exception, WebRequest request) {
        log.warn("Ошибка валидации для запроса: {}", request.getDescription(false));

        // Собираем все ошибки валидации в Map
        Map<String, String> fieldErrors = new HashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponseDto errorResponse = buildError(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации данных",
                "VALIDATION_ERROR",
                request
        );
        errorResponse.setDetails(fieldErrors.toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Обработчик для MethodArgumentTypeMismatchException.
     * Возникает когда тип параметра в URL не соответствует ожидаемому.
     *
     * @param exception исключение
     * @param request веб-запрос
     * @return ответ с ошибкой (HTTP 400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, WebRequest request) {
        log.warn("Несоответствие типа для параметра '{}': получено '{}', ожидается '{}'",
                exception.getName(), exception.getValue(),
                exception.getRequiredType().getSimpleName());

        String message = String.format(
                "Неверный формат параметра '%s'. Получено: '%s', ожидается: %s",
                exception.getName(), exception.getValue(),
                exception.getRequiredType().getSimpleName()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, message, "TYPE_MISMATCH", request));
    }

    /**
     * Обработчик для HttpMessageNotReadableException.
     * Возникает когда JSON в теле запроса невалидный или не парсится.
     *
     * @param exception исключение
     * @param request веб-запрос
     * @return ответ с ошибкой (HTTP 400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, WebRequest request) {
        log.warn("Некорректный JSON в запросе: {}", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(
                        HttpStatus.BAD_REQUEST,
                        "Неверный формат JSON или отсутствует тело запроса",
                        "INVALID_JSON",
                        request
                ));
    }

    /**
     * Обработчик для NoHandlerFoundException.
     * Возникает когда endpoint не существует.
     *
     * @param exception исключение
     * @param request веб-запрос
     * @return ответ с ошибкой (HTTP 404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoHandlerFound(
            NoHandlerFoundException exception, WebRequest request) {
        log.warn("Эндпоинт не найден: {} {}", exception.getHttpMethod(), exception.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(
                        HttpStatus.NOT_FOUND,
                        "Эндпоинт не найден: " + exception.getHttpMethod() + " " + exception.getRequestURL(),
                        "ENDPOINT_NOT_FOUND",
                        request
                ));
    }

    /**
     * CATCH-ALL обработчик - перехватывает ВСЕ необработанные исключения.
     * Срабатывает только если ни один другой @ExceptionHandler не подошёл.
     *
     * @param exception исключение
     * @param request веб-запрос
     * @return ответ с ошибкой (HTTP 500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception exception, WebRequest request) {
        log.error("Неожиданная ошибка произошла в {}: {}",
                request.getDescription(false), exception.getMessage(), exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Внутренняя ошибка сервера. Обратитесь к администратору",
                        "INTERNAL_SERVER_ERROR",
                        request
                ));
    }
}