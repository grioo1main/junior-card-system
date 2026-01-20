package com.lum1nar.junior_card.repository;

import com.lum1nar.junior_card.model.ParentCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Репозиторий для работы с родительскими картами (ParentCard).
 * Предоставляет методы для CRUD операций и специализированные запросы.
 */
public interface ParentCardRepository extends JpaRepository<ParentCard, Long> {
    // Пустой интерфейс - наследует все CRUD методы от JpaRepository
}