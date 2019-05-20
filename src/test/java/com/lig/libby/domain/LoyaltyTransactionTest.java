package com.lig.libby.domain;

import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class LoyaltyTransactionTest {

    @Test
    void equalsAndHashCode() {
        EqualsVerifier
                .forClass(LoyaltyTransaction.class)
                .withPrefabValues(Task.class, new Task(), new Task())
                .withPrefabValues(Book.class, new Book(), new Book())
                .withPrefabValues(Work.class, new Work(), new Work())
                .withOnlyTheseFields("id")
                .verify();
    }
}