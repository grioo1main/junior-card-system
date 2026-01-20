package com.lum1nar.junior_card.Service;

import com.lum1nar.junior_card.dto.*;
import com.lum1nar.junior_card.exception.*;
import com.lum1nar.junior_card.model.*;
import com.lum1nar.junior_card.repository.*;
import com.lum1nar.junior_card.service.CardService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ParentCardRepository parentCardRepository;

    @InjectMocks
    private CardService cardService;

    private ParentCard testParent;
    private CreateCardDto validRequest;
    private JuniorCard testCard;

    @BeforeEach
    void setUp() {
        testParent = ParentCard.builder()
                .id(1L).name("Родитель").age(35)
                .status(UserStatus.PARENT)
                .cardStatus(CardStatus.ACTIVE)
                .build();

        validRequest = new CreateCardDto("Вася", 10, 1L);

        testCard = JuniorCard.builder()
                .id(5L).name("Вася").childAge(10)
                .parentCard(testParent)
                .status(CardStatus.PENDING)
                .build();
    }

    // ========== СОЗДАНИЕ КАРТЫ ==========

    @Test
    void createJuniorCard_ValidRequest_Success() {
        log.info("Тест: создание карты (успех)");

        when(parentCardRepository.findById(1L)).thenReturn(Optional.of(testParent));
        when(cardRepository.countByParentCard_Id(1L)).thenReturn(0L);
        when(cardRepository.save(any(JuniorCard.class))).thenReturn(testCard);

        JuniorCard result = cardService.createJuniorCard(validRequest);

        assertNotNull(result);
        assertEquals("Вася", result.getName());
        assertEquals(10, result.getChildAge());
        assertEquals(CardStatus.PENDING, result.getStatus());

        verify(parentCardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).countByParentCard_Id(1L);
        verify(cardRepository, times(1)).save(any(JuniorCard.class));
    }

    @Test
    void createJuniorCard_ParentNotFound_ThrowsException() {
        log.info("Тест: родитель не найден");

        CreateCardDto request = new CreateCardDto("Вася", 10, 999L);
        when(parentCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.createJuniorCard(request));

        verify(cardRepository, never()).save(any());
    }

    @Test
    void createJuniorCard_LimitExceeded_ThrowsException() {
        log.info("Тест: превышен лимит карт");

        List<JuniorCard> existingCards = List.of(
                JuniorCard.builder().id(1L).name("Карта1").build(),
                JuniorCard.builder().id(2L).name("Карта2").build(),
                JuniorCard.builder().id(3L).name("Карта3").build()
        );

        when(parentCardRepository.findById(1L)).thenReturn(Optional.of(testParent));
        when(cardRepository.countByParentCard_Id(1L)).thenReturn(3L);
        when(cardRepository.findByParentCard_Id(1L)).thenReturn(existingCards);

        AccountLimitCards exception = assertThrows(AccountLimitCards.class,
                () -> cardService.createJuniorCard(validRequest));

        assertEquals("ACCOUNT_LIMIT_EXCEEDED", exception.getErrorCode());
        verify(cardRepository, never()).save(any());
    }

    // ========== ПОЛУЧЕНИЕ КАРТЫ ==========

    @Test
    void getJuniorCardById_CardExists_Success() {
        log.info("Тест: получение карты по ID");

        when(cardRepository.findById(5L)).thenReturn(Optional.of(testCard));

        JuniorCard result = cardService.getJuniorCardById(5L);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Вася", result.getName());

        verify(cardRepository, times(1)).findById(5L);
    }

    @Test
    void getJuniorCardById_CardNotFound_ThrowsException() {
        log.info("Тест: карта не найдена");

        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.getJuniorCardById(999L));

        verify(cardRepository, times(1)).findById(999L);
    }

    // ========== ОБНОВЛЕНИЕ КАРТЫ ==========

    @Test
    void updateJuniorCard_Success() {
        log.info("Тест: обновление карты");

        UpdateCardDto updateRequest = new UpdateCardDto(12, "Новое Имя");

        JuniorCard updatedCard = JuniorCard.builder()
                .id(5L).name("Новое Имя").childAge(12)
                .status(CardStatus.PENDING).build();

        when(cardRepository.findById(5L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(JuniorCard.class))).thenReturn(updatedCard);

        JuniorCard result = cardService.updateJuniorCard(5L, updateRequest);

        assertEquals("Новое Имя", result.getName());
        assertEquals(12, result.getChildAge());

        verify(cardRepository, times(1)).save(any(JuniorCard.class));
    }

    // ========== ИЗМЕНЕНИЕ СТАТУСА ==========

    @Test
    void changeJuniorStatus_ValidTransition_Success() {
        log.info("Тест: изменение статуса PENDING → ACTIVE");

        ChangeStatusDto statusRequest = new ChangeStatusDto(CardStatus.ACTIVE);

        JuniorCard activatedCard = JuniorCard.builder()
                .id(5L).name("Вася").status(CardStatus.ACTIVE).build();

        when(cardRepository.findById(5L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(JuniorCard.class))).thenReturn(activatedCard);

        JuniorCard result = cardService.changeJuniorStatus(5L, statusRequest);

        assertEquals(CardStatus.ACTIVE, result.getStatus());
        verify(cardRepository, times(1)).save(any(JuniorCard.class));
    }

    @Test
    void changeJuniorStatus_InvalidTransition_ThrowsException() {
        log.info("Тест: невалидный переход статуса");

        JuniorCard cancelledCard = JuniorCard.builder()
                .id(5L).status(CardStatus.CANCELLED).build();

        ChangeStatusDto statusRequest = new ChangeStatusDto(CardStatus.ACTIVE);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(cancelledCard));

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> cardService.changeJuniorStatus(5L, statusRequest));

        assertEquals("INVALID_STATUS_TRANSITION", exception.getErrorCode());
        verify(cardRepository, never()).save(any());
    }

    // ========== УДАЛЕНИЕ КАРТЫ ==========

    @Test
    void deleteJuniorCard_Success() {
        log.info("Тест: удаление карты");

        when(cardRepository.findById(5L)).thenReturn(Optional.of(testCard));
        doNothing().when(cardRepository).delete(testCard);

        assertDoesNotThrow(() -> cardService.deleteJuniorCard(5L));

        verify(cardRepository, times(1)).findById(5L);
        verify(cardRepository, times(1)).delete(testCard);
    }

    @Test
    void getAllJuniorCards_ReturnsAllCards() {
        log.info("Тест: получение всех карт");

        List<JuniorCard> allCards = List.of(
                JuniorCard.builder().id(1L).name("Карта1").build(),
                JuniorCard.builder().id(2L).name("Карта2").build()
        );

        when(cardRepository.findAll()).thenReturn(allCards);

        List<JuniorCard> result = cardService.getAllJuniorCards();

        assertEquals(2, result.size());
        verify(cardRepository, times(1)).findAll();
    }
}