package com.lum1nar.junior_card.repository;

import com.lum1nar.junior_card.model.JuniorCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с детскими картами (JuniorCard).
 * Предоставляет методы для CRUD операций и специализированные запросы.
 */
@Repository
public interface CardRepository extends JpaRepository<JuniorCard, Long> {

    /**
     * Подсчитывает количество детских карт у родителя.
     *
     * @param parentCardId ID родительской карты
     * @return количество детских карт
     */
    long countByParentCard_Id(Long parentCardId);

    /**
     * Получает все детские карты принадлежащие родителю.
     *
     * @param parentCardId ID родительской карты
     * @return список детских карт родителя
     */
    List<JuniorCard> findByParentCard_Id(Long parentCardId);

    /**
     * Подсчитывает количество детских карт у родителя с пессимистической блокировкой.
     * Используется для потокобезопасного подсчета при создании новой карты.
     *
     * @param parentId ID родительской карты
     * @return количество детских карт (с блокировкой)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(c) FROM JuniorCard c WHERE c.parentCard.id = :parentId")
    long countByParentCardIdWithLock(@Param("parentId") Long parentId);
}