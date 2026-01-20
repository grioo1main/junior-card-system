package com.lum1nar.junior_card.repository;

import com.lum1nar.junior_card.model.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для CardRepository.
 * Проверяет корректность работы методов репозитория с БД.
 */
@Slf4j
@DataJpaTest
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ParentCardRepository parentCardRepository;

    /**
     * Тест: Подсчет карт когда их нет
     * Ожидается: возвращает 0
     */
    @Test
    void testCountByParentCard_Id_WhenNoCards_ReturnsZero() {
        log.info("Выполняется тест: подсчет карт когда их нет");

        // ARRANGE
        ParentCard parentCard = parentCardRepository.save(ParentCard.builder()
                .name("Родитель")
                .age(35)
                .status(UserStatus.PARENT)
                .cardStatus(CardStatus.ACTIVE)
                .build());

        // ACT
        long cardCount = cardRepository.countByParentCard_Id(parentCard.getId());

        // ASSERT
        assertEquals(0, cardCount, "Ожидалось 0 карт, но получено " + cardCount);
        log.info("Тест пройден: подсчитано {} карт", cardCount);
    }

    /**
     * Тест: Подсчет карт когда их две
     * Ожидается: возвращает 2
     */
    @Test
    void testCountByParentCard_Id_WhenTwoCards_ReturnsTwo() {
        log.info("Выполняется тест: подсчет двух карт");

        // ARRANGE
        ParentCard parentCard = parentCardRepository.save(ParentCard.builder()
                .name("Родитель")
                .age(35)
                .status(UserStatus.PARENT)
                .cardStatus(CardStatus.ACTIVE)
                .build());

        cardRepository.save(JuniorCard.builder()
                .name("Макс")
                .childAge(10)
                .parentCard(parentCard)
                .status(CardStatus.PENDING)
                .build());

        cardRepository.save(JuniorCard.builder()
                .name("Антон")
                .childAge(12)
                .parentCard(parentCard)
                .status(CardStatus.ACTIVE)
                .build());

        // ACT
        long cardCount = cardRepository.countByParentCard_Id(parentCard.getId());

        // ASSERT
        assertEquals(2, cardCount, "Ожидалось 2 карты, но получено " + cardCount);
        log.info("Тест пройден: подсчитано {} карт", cardCount);
    }

    /**
     * Тест: Получение всех детских карт родителя
     * Ожидается: возвращает все три карты
     */
    @Test
    void testFindByParentCard_Id_ReturnsAllChildren() {
        log.info("Выполняется тест: получение всех карт родителя");

        // ARRANGE
        ParentCard parentCard = parentCardRepository.save(ParentCard.builder()
                .name("Родитель")
                .age(35)
                .status(UserStatus.PARENT)
                .cardStatus(CardStatus.ACTIVE)
                .build());

        cardRepository.save(JuniorCard.builder()
                .name("Вася")
                .childAge(10)
                .parentCard(parentCard)
                .status(CardStatus.PENDING)
                .build());

        cardRepository.save(JuniorCard.builder()
                .name("Петя")
                .childAge(12)
                .parentCard(parentCard)
                .status(CardStatus.ACTIVE)
                .build());

        cardRepository.save(JuniorCard.builder()
                .name("Маша")
                .childAge(8)
                .parentCard(parentCard)
                .status(CardStatus.PENDING)
                .build());

        // ACT
        List<JuniorCard> childrenCards = cardRepository.findByParentCard_Id(parentCard.getId());

        // ASSERT
        assertEquals(3, childrenCards.size(), "Ожидалось 3 карты, но получено " + childrenCards.size());
        assertTrue(childrenCards.stream().anyMatch(card -> card.getName().equals("Вася")),
                "Карта Васи не найдена");
        assertTrue(childrenCards.stream().anyMatch(card -> card.getName().equals("Петя")),
                "Карта Пети не найдена");
        assertTrue(childrenCards.stream().anyMatch(card -> card.getName().equals("Маша")),
                "Карта Маши не найдена");

        log.info("Тест пройден: получено {} карт", childrenCards.size());
    }

    /**
     * Тест: Попытка сохранить карту без родителя
     * Ожидается: выбрасывается ConstraintViolationException
     */
    @Test
    void testSaveCard_WithoutParent_ThrowsException() {
        log.info("Выполняется тест: сохранение карты без родителя");

        // ARRANGE
        JuniorCard cardWithoutParent = JuniorCard.builder()
                .name("Ребенок")
                .childAge(10)
                .status(CardStatus.PENDING)
                .build();

        // ACT & ASSERT
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> {
                    cardRepository.save(cardWithoutParent);
                    cardRepository.flush();
                },
                "Ожидалось исключение при сохранении карты без родителя"
        );

        assertTrue(exception.getMessage().contains("parentCard"),
                "Ошибка должна содержать упоминание 'parentCard'");
        log.info("Тест пройден: выброшено исключение как ожидается");
    }
}