package com.lum1nar.junior_card.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lum1nar.junior_card.controller.CardController;
import com.lum1nar.junior_card.dto.*;
import com.lum1nar.junior_card.exception.AccountLimitCards;
import com.lum1nar.junior_card.exception.CardNotFoundException;
import com.lum1nar.junior_card.model.CardStatus;
import com.lum1nar.junior_card.model.JuniorCard;
import com.lum1nar.junior_card.service.CardService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-тесты для CardController.
 * Проверяет корректность работы всех REST endpoints.
 */
@Slf4j
@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== ДЕТСКИЕ КАРТЫ (JUNIOR CARD TESTS) ==========

    /**
     * Тест: Успешное создание детской карты с корректными данными
     * Ожидается: HTTP 201 CREATED
     */
    @Test
    void testCreateJuniorCard_ValidRequest_Returns201() throws Exception {
        log.info("Выполняется тест: создание детской карты с корректными данными");

        // ARRANGE
        CreateCardDto createCardRequest = new CreateCardDto("Вася", 14, 12L);
        JuniorCard createdCard = JuniorCard.builder()
                .id(5L)
                .name("Вася")
                .childAge(10)
                .status(CardStatus.PENDING)
                .build();

        when(cardService.createJuniorCard(createCardRequest)).thenReturn(createdCard);

        // ACT & ASSERT
        mockMvc.perform(post("/api/cards/junior")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Вася"));

        verify(cardService, times(1)).createJuniorCard(any());
    }

    /**
     * Тест: Попытка создания карты с невалидными данными
     * Ожидается: HTTP 400 BAD REQUEST
     */
    @Test
    void testCreateJuniorCard_InvalidRequest_Returns400() throws Exception {
        log.info("Выполняется тест: создание карты с невалидными данными");

        // ARRANGE
        CreateCardDto invalidCardRequest = new CreateCardDto("о", 3, 100L);

        // ACT & ASSERT
        mockMvc.perform(post("/api/cards/junior")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCardRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(cardService, never()).createJuniorCard(any());
    }

    /**
     * Тест: Получение существующей детской карты по ID
     * Ожидается: HTTP 200 OK
     */
    @Test
    void testGetJuniorCard_ValidRequest_Returns200() throws Exception {
        log.info("Выполняется тест: получение детской карты по ID");

        // ARRANGE
        JuniorCard existingCard = JuniorCard.builder()
                .id(5L)
                .name("Макс")
                .childAge(10)
                .status(CardStatus.PENDING)
                .build();

        when(cardService.getJuniorCardById(5L)).thenReturn(existingCard);

        // ACT & ASSERT
        mockMvc.perform(get("/api/cards/junior/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Макс"));

        verify(cardService, times(1)).getJuniorCardById(5L);
    }

    /**
     * Тест: Попытка получения несуществующей карты
     * Ожидается: HTTP 404 NOT FOUND
     */
    @Test
    void testGetJuniorCard_NonExistent_Returns404() throws Exception {
        log.info("Выполняется тест: получение несуществующей карты");

        // ARRANGE
        when(cardService.getJuniorCardById(999L))
                .thenThrow(new CardNotFoundException(999L));

        // ACT & ASSERT
        mockMvc.perform(get("/api/cards/junior/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CARD_NOT_FOUND"));
    }

    /**
     * Тест: Успешное удаление детской карты
     * Ожидается: HTTP 204 NO CONTENT
     */
    @Test
    void testDeleteJuniorCard_Success_Returns204() throws Exception {
        log.info("Выполняется тест: удаление детской карты");

        // ARRANGE
        doNothing().when(cardService).deleteJuniorCard(5L);

        // ACT & ASSERT
        mockMvc.perform(delete("/api/cards/junior/5"))
                .andExpect(status().isNoContent());

        verify(cardService, times(1)).deleteJuniorCard(5L);
    }

    /**
     * Тест: Успешное обновление данных детской карты
     * Ожидается: HTTP 200 OK
     */
    @Test
    void testUpdateJuniorCard_Success_Returns200() throws Exception {
        log.info("Выполняется тест: обновление детской карты");

        // ARRANGE
        UpdateCardDto updateCardRequest = new UpdateCardDto(12, "Новое имя");
        JuniorCard updatedCard = JuniorCard.builder()
                .id(5L)
                .name("Новое имя")
                .childAge(12)
                .build();

        when(cardService.updateJuniorCard(eq(5L), any())).thenReturn(updatedCard);

        // ACT & ASSERT
        mockMvc.perform(put("/api/cards/junior/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"))
                .andExpect(jsonPath("$.childAge").value(12));
    }

    /**
     * Тест: Попытка превышения лимита карт (более 3)
     * Ожидается: HTTP 409 CONFLICT с списком существующих карт
     */
    @Test
    void testCreateJuniorCard_LimitExceeded_Returns409() throws Exception {
        log.info("Выполняется тест: превышение лимита карт");

        // ARRANGE
        CreateCardDto createCardRequest = new CreateCardDto("Вася", 10, 1L);

        List<JuniorCard> existingCards = List.of(
                JuniorCard.builder().id(1L).name("Карта1").build(),
                JuniorCard.builder().id(2L).name("Карта2").build(),
                JuniorCard.builder().id(3L).name("Карта3").build()
        );

        when(cardService.createJuniorCard(any()))
                .thenThrow(new AccountLimitCards(
                        "Лимит превышен",
                        "ACCOUNT_LIMIT_EXCEEDED",
                        existingCards
                ));

        // ACT & ASSERT
        mockMvc.perform(post("/api/cards/junior")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.existingCards", hasSize(3)));
    }
}