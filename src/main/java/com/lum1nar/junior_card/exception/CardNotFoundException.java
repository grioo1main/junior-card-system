package com.lum1nar.junior_card.exception;

public class CardNotFoundException extends ApplicationException {
    public CardNotFoundException(Long id){
        super("Card with ID " + id + " not found", "CARD_NOT_FOUND");
    }

}