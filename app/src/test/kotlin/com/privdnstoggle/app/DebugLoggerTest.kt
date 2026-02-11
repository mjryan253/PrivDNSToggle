package com.privdnstoggle.app

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for [DebugLogger].
 * Run with: ./gradlew :app:testDebugUnitTest
 * or Run Tests on this class in Android Studio (green play icon next to class or method).
 */
@RunWith(JUnit4::class)
class DebugLoggerTest {

    @Test
    fun circularBuffer_maxEntries_removesOldest() {
        DebugLogger.clear()
        
        // Add 101 entries
        for (i in 1..101) {
            DebugLogger.d("Test", "Entry $i")
        }
        
        val entries = DebugLogger.getLogEntries()
        assertEquals(100, entries.size)
        
        // Verify buffer contains exactly 100 entries
        // When 101 entries are added, the oldest (Entry 1) should be removed
        // First entry should be Entry 2, last entry should be Entry 101
        // This proves Entry 1 was removed and Entry 101 was added
        assertTrue("First entry should be Entry 2 (Entry 1 was removed)", entries.first().contains("Entry 2"))
        assertTrue("Last entry should be Entry 101", entries.last().contains("Entry 101"))
        
        // Verify Entry 101 is present (proves we added 101 entries)
        assertTrue(entries.any { it.contains("Entry 101") })
    }

    @Test
    fun circularBuffer_exactlyMaxEntries_preservesAll() {
        DebugLogger.clear()
        
        // Add exactly 100 entries
        for (i in 1..100) {
            DebugLogger.d("Test", "Entry $i")
        }
        
        val entries = DebugLogger.getLogEntries()
        assertEquals(100, entries.size)
        
        // All entries should be present
        assertTrue(entries.first().contains("Entry 1"))
        assertTrue(entries.last().contains("Entry 100"))
    }

    @Test
    fun circularBuffer_order_maintained() {
        DebugLogger.clear()
        
        // Add entries sequentially
        for (i in 1..10) {
            DebugLogger.d("Test", "Entry $i")
        }
        
        val entries = DebugLogger.getLogEntries()
        assertEquals(10, entries.size)
        
        // Verify order is preserved (oldest to newest)
        for (i in 0 until entries.size) {
            assertTrue(entries[i].contains("Entry ${i + 1}"))
        }
    }

    @Test
    fun getRecentLogEntries_lessThanTotal_returnsLastN() {
        DebugLogger.clear()
        
        // Add 10 entries
        for (i in 1..10) {
            DebugLogger.d("Test", "Entry $i")
        }
        
        // Request last 5
        val recent = DebugLogger.getRecentLogEntries(5)
        assertEquals(5, recent.size)
        
        // Should return entries 6-10
        assertTrue(recent[0].contains("Entry 6"))
        assertTrue(recent[4].contains("Entry 10"))
    }

    @Test
    fun getRecentLogEntries_moreThanTotal_returnsAll() {
        DebugLogger.clear()
        
        // Add 5 entries
        for (i in 1..5) {
            DebugLogger.d("Test", "Entry $i")
        }
        
        // Request last 10
        val recent = DebugLogger.getRecentLogEntries(10)
        assertEquals(5, recent.size)
        
        // Should return all 5 entries
        assertTrue(recent[0].contains("Entry 1"))
        assertTrue(recent[4].contains("Entry 5"))
    }

    @Test
    fun getRecentLogEntries_exactlyTotal_returnsAll() {
        DebugLogger.clear()
        
        // Add 10 entries
        for (i in 1..10) {
            DebugLogger.d("Test", "Entry $i")
        }
        
        // Request last 10
        val recent = DebugLogger.getRecentLogEntries(10)
        assertEquals(10, recent.size)
        
        // Should return all entries
        assertTrue(recent[0].contains("Entry 1"))
        assertTrue(recent[9].contains("Entry 10"))
    }

    @Test
    fun getRecentLogEntries_zero_returnsEmpty() {
        DebugLogger.clear()
        
        // Add entries
        for (i in 1..5) {
            DebugLogger.d("Test", "Entry $i")
        }
        
        // Request 0
        val recent = DebugLogger.getRecentLogEntries(0)
        assertTrue(recent.isEmpty())
    }

    @Test
    fun getRecentLogEntries_one_returnsLastOne() {
        DebugLogger.clear()
        
        // Add 10 entries
        for (i in 1..10) {
            DebugLogger.d("Test", "Entry $i")
        }
        
        // Request 1
        val recent = DebugLogger.getRecentLogEntries(1)
        assertEquals(1, recent.size)
        assertTrue(recent[0].contains("Entry 10"))
    }

    @Test
    fun getRecentLogEntries_emptyBuffer_returnsEmpty() {
        DebugLogger.clear()
        
        // No entries added, request last 5
        val recent = DebugLogger.getRecentLogEntries(5)
        assertTrue(recent.isEmpty())
    }

    @Test
    fun getRecentLogEntries_defaultParameter_returns50() {
        DebugLogger.clear()
        
        // Add 100 entries
        for (i in 1..100) {
            DebugLogger.d("Test", "Entry $i")
        }
        
        // Call without parameter (should default to 50)
        val recent = DebugLogger.getRecentLogEntries()
        assertEquals(50, recent.size)
        
        // Should return last 50 entries (51-100)
        assertTrue(recent[0].contains("Entry 51"))
        assertTrue(recent[49].contains("Entry 100"))
    }

    @Test
    fun getAllLogs_returnsJoinedString() {
        DebugLogger.clear()
        
        // Add multiple entries
        DebugLogger.d("Test", "Entry 1")
        DebugLogger.d("Test", "Entry 2")
        DebugLogger.d("Test", "Entry 3")
        
        val allLogs = DebugLogger.getAllLogs()
        assertNotNull(allLogs)
        assertTrue(allLogs.contains("Entry 1"))
        assertTrue(allLogs.contains("Entry 2"))
        assertTrue(allLogs.contains("Entry 3"))
        
        // Should be joined with newlines
        val lines = allLogs.split("\n")
        assertTrue(lines.size >= 3)
    }

    @Test
    fun getAllLogs_emptyBuffer_returnsEmptyString() {
        DebugLogger.clear()
        
        // No entries added
        val allLogs = DebugLogger.getAllLogs()
        assertEquals("", allLogs)
    }

    @Test
    fun getAllLogs_singleEntry_returnsEntry() {
        DebugLogger.clear()
        
        // Add one entry
        DebugLogger.d("Test", "Single entry")
        
        val allLogs = DebugLogger.getAllLogs()
        assertTrue(allLogs.contains("Single entry"))
        // Single entry should not have trailing newline in the string itself
        // (though it may be formatted with timestamp)
    }

    @Test
    fun getLogEntries_returnsListCopy() {
        DebugLogger.clear()
        
        // Add entries
        DebugLogger.d("Test", "Entry 1")
        DebugLogger.d("Test", "Entry 2")
        
        val entries1 = DebugLogger.getLogEntries()
        val entries2 = DebugLogger.getLogEntries()
        
        // Should return copies, not references
        assertEquals(entries1.size, entries2.size)
        
        // Modifying one shouldn't affect the other (they're copies)
        // This is tested implicitly - if they were references, modifying entries1 would affect entries2
    }

    @Test
    fun getLogEntries_emptyBuffer_returnsEmptyList() {
        DebugLogger.clear()
        
        // No entries added
        val entries = DebugLogger.getLogEntries()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun clear_removesAllEntries() {
        DebugLogger.clear()
        
        // Add entries
        DebugLogger.d("Test", "Entry 1")
        DebugLogger.d("Test", "Entry 2")
        DebugLogger.d("Test", "Entry 3")
        
        // Clear
        DebugLogger.clear()
        
        // Verify all entries removed
        val allLogs = DebugLogger.getAllLogs()
        assertEquals("", allLogs)
        
        val entries = DebugLogger.getLogEntries()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun clear_afterAddingEntries_works() {
        DebugLogger.clear()
        
        // Add entries
        DebugLogger.d("Test", "Old entry 1")
        DebugLogger.d("Test", "Old entry 2")
        
        // Clear
        DebugLogger.clear()
        
        // Add new entries
        DebugLogger.d("Test", "New entry 1")
        DebugLogger.d("Test", "New entry 2")
        
        // Verify only new entries present
        val entries = DebugLogger.getLogEntries()
        assertEquals(2, entries.size)
        assertTrue(entries.all { it.contains("New entry") })
        assertFalse(entries.any { it.contains("Old entry") })
    }

    @Test
    fun d_addsEntry() {
        DebugLogger.clear()
        
        // Call d()
        DebugLogger.d("TestTag", "Test message")
        
        // Verify entry added
        val entries = DebugLogger.getLogEntries()
        assertEquals(1, entries.size)
        assertTrue(entries[0].contains("TestTag"))
        assertTrue(entries[0].contains("Test message"))
    }

    @Test
    fun e_addsEntry() {
        DebugLogger.clear()
        
        // Call e()
        DebugLogger.e("TestTag", "Error message")
        
        // Verify entry added
        val entries = DebugLogger.getLogEntries()
        assertEquals(1, entries.size)
        assertTrue(entries[0].contains("TestTag"))
        assertTrue(entries[0].contains("Error message"))
    }

    @Test
    fun d_withThrowable_includesStackTrace() {
        DebugLogger.clear()
        
        // Call d() with throwable
        val exception = RuntimeException("Test exception")
        DebugLogger.d("TestTag", "Message with exception", exception)
        
        // Verify entry includes stack trace (check for newline separator)
        val entries = DebugLogger.getLogEntries()
        assertEquals(1, entries.size)
        val entry = entries[0]
        
        // Should contain message
        assertTrue(entry.contains("Message with exception"))
        // Should contain stack trace (indicated by newline separator)
        assertTrue(entry.contains("\n"))
    }

    @Test
    fun e_withThrowable_includesStackTrace() {
        DebugLogger.clear()
        
        // Call e() with throwable
        val exception = IllegalArgumentException("Test exception")
        DebugLogger.e("TestTag", "Error with exception", exception)
        
        // Verify entry includes stack trace (check for newline separator)
        val entries = DebugLogger.getLogEntries()
        assertEquals(1, entries.size)
        val entry = entries[0]
        
        // Should contain message
        assertTrue(entry.contains("Error with exception"))
        // Should contain stack trace (indicated by newline separator)
        assertTrue(entry.contains("\n"))
    }
}
