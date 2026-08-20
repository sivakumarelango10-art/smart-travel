package com.smarttravel.modules.ticket;

import com.smarttravel.common.config.MongoIndexConfig;
import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.model.TicketStatus;
import com.smarttravel.modules.ticket.repository.TicketRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression test suite verifying resolution of MongoDB error 85 (IndexOptionsConflict).
 *
 * Scenarios tested:
 * 1. Existing ticketNumber_1 (or equivalent) index is recognized correctly.
 * 2. Equivalent index does not produce IndexOptionsConflict during ensureIndexes().
 * 3. Correct unique constraint remains enforced on ticketNumber.
 * 4. Duplicate ticketNumber insertion fails with DuplicateKeyException.
 * 5. Existing valid ticket records remain untouched after index initialization.
 * 6. Application startup / MongoIndexConfig does not fail.
 * 7. Index initializer is idempotent across repeated calls.
 */
@SpringBootTest
class TicketIndexOptionsConflictIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoIndexConfig mongoIndexConfig;

    @Autowired
    private TicketRepository ticketRepository;

    private String testTicketNumber;

    @BeforeEach
    void setUp() {
        testTicketNumber = "ST-REGRESS-" + System.currentTimeMillis();
    }

    @AfterEach
    void tearDown() {
        if (testTicketNumber != null) {
            mongoTemplate.remove(Query.query(Criteria.where("ticketNumber").is(testTicketNumber)), "tickets");
        }
    }

    @Test
    @DisplayName("TEST 1: MongoIndexConfig initializes cleanly without IndexOptionsConflict")
    void test1_MongoIndexConfigInitializesCleanly() {
        assertThatCode(() -> mongoIndexConfig.ensureIndexes())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("TEST 2: Existing unique ticketNumber index is detected and verified")
    void test2_ExistingUniqueIndexDetected() {
        boolean valid = mongoIndexConfig.isUniqueTicketNumberIndexValid();
        assertThat(valid).isTrue();

        List<IndexInfo> indexInfoList = mongoTemplate.indexOps("tickets").getIndexInfo();
        boolean hasUniqueTicketNumber = indexInfoList.stream()
                .anyMatch(idx -> idx.isUnique() && idx.getIndexFields().stream()
                        .anyMatch(f -> "ticketNumber".equals(f.getKey())));
        assertThat(hasUniqueTicketNumber).isTrue();
    }

    @Test
    @DisplayName("TEST 3: ensureIndexes is idempotent and produces no errors when run repeatedly")
    void test3_EnsureIndexesIsIdempotent() {
        // Run ensureIndexes multiple times consecutively
        for (int i = 0; i < 3; i++) {
            assertThatCode(() -> mongoIndexConfig.ensureIndexes())
                    .doesNotThrowAnyException();
        }
        assertThat(mongoIndexConfig.isUniqueTicketNumberIndexValid()).isTrue();
    }

    @Test
    @DisplayName("TEST 4: Unique constraint on ticketNumber is actively enforced by MongoDB")
    void test4_UniqueConstraintEnforcedOnTicketNumber() {
        Ticket ticket1 = Ticket.builder()
                .ticketNumber(testTicketNumber)
                .bookingId("bkg_regress_1_" + System.currentTimeMillis())
                .userId("user_regress")
                .flightId("fl_regress")
                .flightNumber("ST-101")
                .status(TicketStatus.ISSUED)
                .totalAmount(new BigDecimal("5000.00"))
                .currency("INR")
                .issuedAt(Instant.now())
                .build();

        Ticket ticket2 = Ticket.builder()
                .ticketNumber(testTicketNumber)
                .bookingId("bkg_regress_2_" + System.currentTimeMillis())
                .userId("user_regress")
                .flightId("fl_regress")
                .flightNumber("ST-101")
                .status(TicketStatus.ISSUED)
                .totalAmount(new BigDecimal("5000.00"))
                .currency("INR")
                .issuedAt(Instant.now())
                .build();

        mongoTemplate.insert(ticket1, "tickets");

        // Attempting to insert second ticket with identical ticketNumber must throw DuplicateKeyException
        assertThrows(DuplicateKeyException.class, () -> mongoTemplate.insert(ticket2, "tickets"));
    }

    @Test
    @DisplayName("TEST 5: Existing valid ticket data remains intact after ensureIndexes runs")
    void test5_ExistingTicketDataRemainsIntact() {
        Ticket ticket = Ticket.builder()
                .ticketNumber(testTicketNumber)
                .bookingId("bkg_preserve_" + System.currentTimeMillis())
                .bookingReference("REFPRESERVE")
                .userId("user_preserve")
                .flightId("fl_preserve")
                .flightNumber("ST-999")
                .status(TicketStatus.ISSUED)
                .totalAmount(new BigDecimal("4500.00"))
                .currency("INR")
                .issuedAt(Instant.now())
                .build();

        Ticket saved = mongoTemplate.insert(ticket, "tickets");

        // Re-run index configuration
        mongoIndexConfig.ensureIndexes();

        // Verify saved ticket document is completely unchanged
        Ticket fetched = mongoTemplate.findById(saved.getId(), Ticket.class);
        assertThat(fetched).isNotNull();
        assertThat(fetched.getTicketNumber()).isEqualTo(testTicketNumber);
        assertThat(fetched.getBookingReference()).isEqualTo("REFPRESERVE");
        assertThat(fetched.getStatus()).isEqualTo(TicketStatus.ISSUED);
    }
}
