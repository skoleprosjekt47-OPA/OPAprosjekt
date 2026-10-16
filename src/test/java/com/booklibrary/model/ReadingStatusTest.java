package com.booklibrary.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Author: Farid Rahmani
 * Contribution: Wrote the unit tests for the domain model classes.
 * Purpose: verifies each {@link ReadingStatus} value maps to the correct
 * human-readable label shown in the UI.
 */
class ReadingStatusTest {

    @Test
    void readMapsToReadLabel() {
        assertEquals("Read", ReadingStatus.READ.getDisplayLabel());
    }

    @Test
    void unreadMapsToUnreadLabel() {
        assertEquals("Unread", ReadingStatus.UNREAD.getDisplayLabel());
    }

    @Test
    void readingMapsToCurrentlyReadingLabel() {
        assertEquals("Currently reading", ReadingStatus.READING.getDisplayLabel());
    }
}
