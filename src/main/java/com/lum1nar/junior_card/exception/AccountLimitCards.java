package com.lum1nar.junior_card.exception;

import com.lum1nar.junior_card.model.JuniorCard;
import lombok.Getter;

import java.util.List;

@Getter
public class AccountLimitCards extends ApplicationException {

    private String errorCode;

    private List<JuniorCard> existingCards;  // ← ДОБАВЬ

    public AccountLimitCards(String message, String errorCode, List<JuniorCard> existingCards) {
        super(message , errorCode);
        this.errorCode = errorCode;
        this.existingCards = existingCards;
    }

}

