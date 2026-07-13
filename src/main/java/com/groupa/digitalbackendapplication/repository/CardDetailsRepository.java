package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;
import com.groupa.digitalbackendapplication.domain.entities.CardDetails;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

@Repository
public class CardDetailsRepository {
    private final Map<String, CardDetails> cardDetails;

    public CardDetailsRepository(){
        CardDetails card1 = new CardDetails("7893234572819472","SOLOMON GRUNDY", YearMonth.of(2029,1),324, TransactionStatus.SUCCESSFUL);
        CardDetails card2 = new CardDetails("1234567893824913", "CHIOMA PRECIOUS", YearMonth.of(2027,8),372, TransactionStatus.PENDING);

        cardDetails = Map.of(
                card1.cardNumber(), card1,
                card2.cardNumber(),card2
        );
    }

    public Optional<CardDetails> findByCardNumber(String cardNumber){
        return Optional.ofNullable(cardDetails.get(cardNumber));
    }
}
