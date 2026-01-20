package com.lum1nar.junior_card.controller;

import com.lum1nar.junior_card.dto.*;
import com.lum1nar.junior_card.model.JuniorCard;
import com.lum1nar.junior_card.model.ParentCard;
import com.lum1nar.junior_card.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления детскими и родительскими картами.
 * Предоставляет API endpoints для CRUD операций.
 */
@Slf4j
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // ==================== ДЕТСКИЕ КАРТЫ (JUNIOR CARD CRUD) ====================

    /**
     * POST /api/cards/junior - Создание новой детской карты
     *
     * @param createCardRequest DTO с данными для создания карты
     * @return созданная детская карта (HTTP 201 CREATED)
     */
    @PostMapping("/junior")
    public ResponseEntity<JuniorCard> createJuniorCard(@Valid @RequestBody CreateCardDto createCardRequest) {
        log.info("Получен запрос на создание детской карты");
        JuniorCard createdCard = cardService.createJuniorCard(createCardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    /**
     * GET /api/cards/junior - Получение всех детских карт
     *
     * @return список всех детских карт (HTTP 200 OK)
     */
    @GetMapping("/junior")
    public ResponseEntity<List<JuniorCard>> getAllJuniorCards() {
        log.info("Получен запрос на получение всех детских карт");
        List<JuniorCard> allCards = cardService.getAllJuniorCards();
        return ResponseEntity.ok(allCards);
    }

    /**
     * GET /api/cards/junior/{id} - Получение детской карты по ID
     *
     * @param cardId ID карты
     * @return найденная детская карта (HTTP 200 OK)
     */
    @GetMapping("/junior/{cardId}")
    public ResponseEntity<JuniorCard> getJuniorCardById(@PathVariable Long cardId) {
        log.info("Получен запрос на получение детской карты с ID: {}", cardId);
        JuniorCard foundCard = cardService.getJuniorCardById(cardId);
        return ResponseEntity.ok(foundCard);
    }

    /**
     * PUT /api/cards/junior/{id} - Обновление данных детской карты
     *
     * @param cardId            ID карты для обновления
     * @param updateCardRequest DTO с новыми данными
     * @return обновленная детская карта (HTTP 200 OK)
     */
    @PutMapping("/junior/{cardId}")
    public ResponseEntity<JuniorCard> updateJuniorCard(
            @PathVariable Long cardId,
            @Valid @RequestBody UpdateCardDto updateCardRequest) {
        log.info("Получен запрос на обновление детской карты с ID: {}", cardId);
        JuniorCard updatedCard = cardService.updateJuniorCard(cardId, updateCardRequest);
        return ResponseEntity.ok(updatedCard);
    }

    /**
     * PUT /api/cards/junior/{id}/status - Изменение статуса детской карты
     *
     * @param id              ID карты
     * @param changeStatusRequest DTO с новым статусом
     * @return карта с обновленным статусом (HTTP 200 OK)
     */
    @PutMapping("/junior/{id}/status")
    public ResponseEntity<JuniorCard> changeJuniorStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusDto changeStatusRequest) {
        log.info("Получен запрос на изменение статуса детской карты с ID: {}", id);
        JuniorCard updatedCard = cardService.changeJuniorStatus(id, changeStatusRequest);
        return ResponseEntity.ok(updatedCard);
    }

    /**
     * DELETE /api/cards/junior/{id} - Удаление детской карты
     *
     * @param cardId ID карты для удаления
     * @return пусто (HTTP 204 NO CONTENT)
     */
    @DeleteMapping("/junior/{cardId}")
    public ResponseEntity<Void> deleteJuniorCard(@PathVariable Long cardId) {
        log.info("Получен запрос на удаление детской карты с ID: {}", cardId);
        cardService.deleteJuniorCard(cardId);
        return ResponseEntity.noContent().build();
    }

    // ==================== РОДИТЕЛЬСКИЕ КАРТЫ (PARENT CARD CRUD) ====================

    /**
     * POST /api/cards/parent - Создание новой родительской карты
     *
     * @param createParentCardRequest DTO с данными для создания карты
     * @return созданная родительская карта (HTTP 201 CREATED)
     */
    @PostMapping("/parent")
    public ResponseEntity<ParentCard> createParentCard(@Valid @RequestBody CreateParentCardDto createParentCardRequest) {
        log.info("Получен запрос на создание родительской карты");
        ParentCard createdCard = cardService.createParentCard(createParentCardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    /**
     * GET /api/cards/parent - Получение всех родительских карт
     *
     * @return список всех родительских карт (HTTP 200 OK)
     */
    @GetMapping("/parent")
    public ResponseEntity<List<ParentCard>> getAllParentCards() {
        log.info("Получен запрос на получение всех родительских карт");
        List<ParentCard> allCards = cardService.getAllParentCards();
        return ResponseEntity.ok(allCards);
    }

    /**
     * GET /api/cards/parent/{id} - Получение родительской карты по ID
     *
     * @param parentId ID родительской карты
     * @return найденная родительская карта (HTTP 200 OK)
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ParentCard> getParentCardById(@PathVariable Long parentId) {
        log.info("Получен запрос на получение родительской карты с ID: {}", parentId);
        ParentCard foundCard = cardService.getParentCardById(parentId);
        return ResponseEntity.ok(foundCard);
    }

    /**
     * PUT /api/cards/parent/{id} - Обновление данных родительской карты
     *
     * @param parentId                ID карты для обновления
     * @param updateParentCardRequest DTO с новыми данными
     * @return обновленная родительская карта (HTTP 200 OK)
     */
    @PutMapping("/parent/{parentId}")
    public ResponseEntity<ParentCard> updateParentCard(
            @PathVariable Long parentId,
            @Valid @RequestBody UpdateParentCardDto updateParentCardRequest) {
        log.info("Получен запрос на обновление родительской карты с ID: {}", parentId);
        ParentCard updatedCard = cardService.updateParentCard(parentId, updateParentCardRequest);
        return ResponseEntity.ok(updatedCard);
    }

    /**
     * PUT /api/cards/parent/{id}/status - Изменение статуса родительской карты
     *
     * @param parentId            ID карты
     * @param changeStatusRequest DTO с новым статусом
     * @return карта с обновленным статусом (HTTP 200 OK)
     */
    @PutMapping("/parent/{parentId}/status")
    public ResponseEntity<ParentCard> changeParentStatus(
            @PathVariable Long parentId,
            @Valid @RequestBody ChangeStatusDto changeStatusRequest) {
        log.info("Получен запрос на изменение статуса родительской карты с ID: {}", parentId);
        ParentCard updatedCard = cardService.changeParentStatus(parentId, changeStatusRequest);
        return ResponseEntity.ok(updatedCard);
    }

    /**
     * DELETE /api/cards/parent/{id} - Удаление родительской карты
     *
     * @param parentId ID карты для удаления
     * @return пусто (HTTP 204 NO CONTENT)
     */
    @DeleteMapping("/parent/{parentId}")
    public ResponseEntity<Void> deleteParentCard(@PathVariable Long parentId) {
        log.info("Получен запрос на удаление родительской карты с ID: {}", parentId);
        cardService.deleteParentCard(parentId);
        return ResponseEntity.noContent().build();
    }
}