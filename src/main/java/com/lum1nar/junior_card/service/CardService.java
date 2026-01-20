package com.lum1nar.junior_card.service;

import com.lum1nar.junior_card.dto.*;
import com.lum1nar.junior_card.exception.AccountLimitCards;
import com.lum1nar.junior_card.exception.ApplicationException;
import com.lum1nar.junior_card.exception.CardNotFoundException;
import com.lum1nar.junior_card.model.*;
import com.lum1nar.junior_card.repository.CardRepository;
import com.lum1nar.junior_card.repository.ParentCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления детскими и родительскими картами.
 * Содержит бизнес-логику создания, обновления, удаления карт и изменения их статусов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final ParentCardRepository parentCardRepository;


    /** Максимальное количество детских карт на одного родителя */
    private static final int MAX_CARDS_PER_PARENT = 3;

    // ========== ДЕТСКИЕ КАРТЫ (JUNIOR CARD CRUD) ==========

    /**
     * Создает новую детскую карту для родителя.
     * Проверяет существование родителя и лимит на количество карт.
     *
     * @param createCardDto данные для создания карты (имя, возраст, ID родителя)
     * @return созданная детская карта
     * @throws CardNotFoundException если родитель с заданным ID не найден
     * @throws AccountLimitCards если достигнут лимит карт (3 максимум)
     */
    @Transactional(
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED
    )
    public JuniorCard createJuniorCard(CreateCardDto createCardDto) {
        log.info("Попытка создания детской карты для родителя с ID: {}", createCardDto.getParentCardId());

        // Проверяем существование родительской карты
        ParentCard parentCard = parentCardRepository.findById(createCardDto.getParentCardId())
                .orElseThrow(() -> {
                    log.error("Родительская карта не найдена с ID: {}", createCardDto.getParentCardId());
                    return new CardNotFoundException(createCardDto.getParentCardId());
                });

        // Подсчитываем текущее количество детских карт у родителя
        long currentCardCount = cardRepository.countByParentCard_Id(createCardDto.getParentCardId());
        log.debug("Текущее количество карт у родителя {}: {}", createCardDto.getParentCardId(), currentCardCount);

        // Проверяем не превышен ли лимит
        if (currentCardCount >= MAX_CARDS_PER_PARENT) {
            List<JuniorCard> existingCards = cardRepository.findByParentCard_Id(createCardDto.getParentCardId());
            log.warn("Превышен лимит карт для родителя {}. Текущее количество: {}",
                    createCardDto.getParentCardId(), currentCardCount);
            throw new AccountLimitCards(
                    String.format("У родителя может быть максимум %d карт. Текущее количество: %d",
                            MAX_CARDS_PER_PARENT, currentCardCount),
                    "ACCOUNT_LIMIT_EXCEEDED",
                    existingCards
            );
        }

        // Создаем новую детскую карту
        JuniorCard newCard = JuniorCard.builder()
                .name(createCardDto.getName())
                .parentCard(parentCard)
                .childAge(createCardDto.getChildAge())
                .status(CardStatus.PENDING)
                .build();

        JuniorCard savedCard = cardRepository.save(newCard);
        log.info("Детская карта успешно создана с ID: {}, имя: {}", savedCard.getId(), savedCard.getName());

        return savedCard;
    }

    /**
     * Получает все детские карты.
     *
     * @return список всех детских карт
     */
    public List<JuniorCard> getAllJuniorCards() {
        log.debug("Получение всех детских карт");
        return cardRepository.findAll();
    }

    /**
     * Получает детскую карту по ID.
     *
     * @param cardId ID карты
     * @return найденная детская карта
     * @throws CardNotFoundException если карта не найдена
     */
    public JuniorCard getJuniorCardById(Long cardId) {
        log.debug("Получение детской карты с ID: {}", cardId);
        return cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Детская карта не найдена с ID: {}", cardId);
                    return new CardNotFoundException(cardId);
                });
    }

    /**
     * Обновляет данные детской карты (имя и возраст).
     *
     * @param cardId ID карты для обновления
     * @param updateCardDto новые данные (имя, возраст)
     * @return обновленная детская карта
     * @throws CardNotFoundException если карта не найдена
     */
    @Transactional
    public JuniorCard updateJuniorCard(Long cardId, UpdateCardDto updateCardDto) {
        log.info("Обновление детской карты с ID: {}", cardId);

        JuniorCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Детская карта не найдена с ID: {}", cardId);
                    return new CardNotFoundException(cardId);
                });

        // Обновляем имя если оно передано и отличается от текущего
        if (updateCardDto.getName() != null && !updateCardDto.getName().equals(card.getName())) {
            log.debug("Обновление имени карты {} с '{}' на '{}'", cardId, card.getName(), updateCardDto.getName());
            card.setName(updateCardDto.getName());
        }

        // Обновляем возраст если он передан и отличается от текущего
        if (updateCardDto.getAge() != null && !updateCardDto.getAge().equals(card.getChildAge())) {
            log.debug("Обновление возраста карты {} с '{}' на '{}'", cardId, card.getChildAge(), updateCardDto.getAge());
            card.setChildAge(updateCardDto.getAge());
        }

        JuniorCard updatedCard = cardRepository.save(card);
        log.info("Детская карта {} успешно обновлена", cardId);

        return updatedCard;
    }

    /**
     * Изменяет статус детской карты.
     * Проверяет валидность перехода статуса перед изменением.
     *
     * @param cardId ID карты
     * @param changeStatusDto новый статус
     * @return карта с обновленным статусом
     * @throws CardNotFoundException если карта не найдена
     * @throws ApplicationException если переход статуса невалидный
     */
    @Transactional
    public JuniorCard changeJuniorStatus(Long cardId, ChangeStatusDto changeStatusDto) {
        log.info("Изменение статуса детской карты с ID: {} на статус: {}", cardId, changeStatusDto.getStatus());

        JuniorCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Детская карта не найдена с ID: {}", cardId);
                    return new CardNotFoundException(cardId);
                });

        CardStatus oldStatus = card.getStatus();
        CardStatus newStatus = changeStatusDto.getStatus();

        // Проверяем валидность перехода статуса
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            log.error("Невалидный переход статуса для карты {}: {} -> {}", cardId, oldStatus, newStatus);
            throw new ApplicationException(
                    "Невозможно изменить статус с " + oldStatus + " на " + newStatus,
                    "INVALID_STATUS_TRANSITION");
        }

        card.setStatus(newStatus);
        JuniorCard updatedCard = cardRepository.save(card);
        log.info("Статус карты {} успешно изменен с {} на {}", cardId, oldStatus, newStatus);

        return updatedCard;
    }

    /**
     * Удаляет детскую карту по ID.
     *
     * @param cardId ID карты для удаления
     * @throws CardNotFoundException если карта не найдена
     */
    @Transactional
    public void deleteJuniorCard(Long cardId) {
        log.info("Удаление детской карты с ID: {}", cardId);

        JuniorCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Детская карта не найдена с ID: {}", cardId);
                    return new CardNotFoundException(cardId);
                });

        cardRepository.delete(card);
        log.info("Детская карта {} успешно удалена", cardId);
    }

    // ========== РОДИТЕЛЬСКИЕ КАРТЫ (PARENT CARD CRUD) ==========

    /**
     * Создает новую родительскую карту.
     *
     * @param createParentCardDto данные для создания (имя, возраст)
     * @return созданная родительская карта
     */
    @Transactional
    public ParentCard createParentCard(CreateParentCardDto createParentCardDto) {
        log.info("Создание родительской карты для: {}", createParentCardDto.getName());

        ParentCard newParentCard = ParentCard.builder()
                .age(createParentCardDto.getAge())
                .cardStatus(CardStatus.PENDING)
                .name(createParentCardDto.getName())
                .status(UserStatus.PARENT)
                .build();

        ParentCard savedParentCard = parentCardRepository.save(newParentCard);
        log.info("Родительская карта успешно создана с ID: {}, имя: {}",
                savedParentCard.getId(), savedParentCard.getName());

        return savedParentCard;
    }

    /**
     * Получает все родительские карты.
     *
     * @return список всех родительских карт
     */
    public List<ParentCard> getAllParentCards() {
        log.debug("Получение всех родительских карт");
        return parentCardRepository.findAll();
    }

    /**
     * Получает родительскую карту по ID.
     *
     * @param parentId ID родительской карты
     * @return найденная родительская карта
     * @throws CardNotFoundException если карта не найдена
     */
    public ParentCard getParentCardById(Long parentId) {
        log.debug("Получение родительской карты с ID: {}", parentId);
        return parentCardRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.error("Родительская карта не найдена с ID: {}", parentId);
                    return new CardNotFoundException(parentId);
                });
    }

    /**
     * Обновляет данные родительской карты (имя и возраст).
     *
     * @param parentId ID родительской карты для обновления
     * @param updateParentCardDto новые данные (имя, возраст)
     * @return обновленная родительская карта
     * @throws CardNotFoundException если карта не найдена
     */
    @Transactional
    public ParentCard updateParentCard(Long parentId, UpdateParentCardDto updateParentCardDto) {
        log.info("Обновление родительской карты с ID: {}", parentId);

        ParentCard parentCard = parentCardRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.error("Родительская карта не найдена с ID: {}", parentId);
                    return new CardNotFoundException(parentId);
                });

        // Обновляем имя если оно передано и отличается от текущего
        if (updateParentCardDto.getName() != null && !updateParentCardDto.getName().equals(parentCard.getName())) {
            log.debug("Обновление имени родителя {} с '{}' на '{}'",
                    parentId, parentCard.getName(), updateParentCardDto.getName());
            parentCard.setName(updateParentCardDto.getName());
        }

        // Обновляем возраст если он передан и отличается от текущего
        if (updateParentCardDto.getAge() != null && !updateParentCardDto.getAge().equals(parentCard.getAge())) {
            log.debug("Обновление возраста родителя {} с '{}' на '{}'",
                    parentId, parentCard.getAge(), updateParentCardDto.getAge());
            parentCard.setAge(updateParentCardDto.getAge());
        }

        ParentCard updatedParentCard = parentCardRepository.save(parentCard);
        log.info("Родительская карта {} успешно обновлена", parentId);

        return updatedParentCard;
    }

    /**
     * Изменяет статус родительской карты.
     * Проверяет валидность перехода статуса перед изменением.
     *
     * @param parentId ID родительской карты
     * @param changeStatusDto новый статус
     * @return карта с обновленным статусом
     * @throws CardNotFoundException если карта не найдена
     * @throws ApplicationException если переход статуса невалидный
     */
    @Transactional
    public ParentCard changeParentStatus(Long parentId, ChangeStatusDto changeStatusDto) {
        log.info("Изменение статуса родительской карты с ID: {} на статус: {}", parentId, changeStatusDto.getStatus());

        ParentCard parentCard = parentCardRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.error("Родительская карта не найдена с ID: {}", parentId);
                    return new CardNotFoundException(parentId);
                });

        CardStatus oldStatus = parentCard.getCardStatus();
        CardStatus newStatus = changeStatusDto.getStatus();

        // Проверяем валидность перехода статуса
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            log.error("Невалидный переход статуса для родителя {}: {} -> {}", parentId, oldStatus, newStatus);
            throw new ApplicationException(
                    "Невозможно изменить статус с " + oldStatus + " на " + newStatus,
                    "INVALID_STATUS_TRANSITION");
        }

        parentCard.setCardStatus(newStatus);
        ParentCard updatedParentCard = parentCardRepository.save(parentCard);
        log.info("Статус родительской карты {} успешно изменен с {} на {}", parentId, oldStatus, newStatus);

        return updatedParentCard;
    }

    /**
     * Удаляет родительскую карту по ID.
     * Предварительно проверяет, что у родителя нет активных детских карт.
     *
     * @param parentId ID родительской карты для удаления
     * @throws CardNotFoundException если карта не найдена
     * @throws ApplicationException если у родителя есть активные детские карты
     */
    @Transactional
    public void deleteParentCard(Long parentId) {
        log.info("Удаление родительской карты с ID: {}", parentId);

        ParentCard parentCard = parentCardRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.error("Родительская карта не найдена с ID: {}", parentId);
                    return new CardNotFoundException(parentId);
                });

        // Проверяем наличие активных детских карт
        long activeChildCardsCount = cardRepository.countByParentCard_Id(parentId);
        if (activeChildCardsCount > 0) {
            log.warn("Попытка удаления родителя {} с активными детскими картами. Количество: {}",
                    parentId, activeChildCardsCount);
            throw new ApplicationException(
                    "Невозможно удалить родительскую карту. У неё есть " + activeChildCardsCount + " активных детских карт.",
                    "PARENT_HAS_CHILDREN");
        }

        parentCardRepository.delete(parentCard);
        log.info("Родительская карта {} успешно удалена", parentId);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Проверяет валидность перехода статуса карты.
     * Определяет разрешенные переходы для каждого статуса.
     *
     * @param fromStatus текущий статус
     * @param toStatus целевой статус
     * @return true если переход разрешен, false в противном случае
     */
    private boolean isValidStatusTransition(CardStatus fromStatus, CardStatus toStatus) {
        // Из PENDING можно перейти в ACTIVE или CANCELLED
        if (fromStatus == CardStatus.PENDING &&
                (toStatus == CardStatus.ACTIVE || toStatus == CardStatus.CANCELLED)) {
            return true;
        }

        // Из ACTIVE можно перейти в SUSPENDED, CANCELLED или EXPIRED
        if (fromStatus == CardStatus.ACTIVE &&
                (toStatus == CardStatus.SUSPENDED || toStatus == CardStatus.CANCELLED || toStatus == CardStatus.EXPIRED)) {
            return true;
        }

        // Из SUSPENDED можно перейти в ACTIVE или CANCELLED
        if (fromStatus == CardStatus.SUSPENDED &&
                (toStatus == CardStatus.ACTIVE || toStatus == CardStatus.CANCELLED)) {
            return true;
        }

        return false;
    }
}