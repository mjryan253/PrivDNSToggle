package com.privdnstoggle.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for [DnsManager.validateHostnameSyntax].
 * Run with: ./gradlew :app:testDebugUnitTest
 * or Run Tests on this class in Android Studio (green play icon next to class or method).
 */
@RunWith(JUnit4::class)
class DnsManagerTest {

    @Test
    fun validateHostnameSyntax_validHostnames_returnsNull() {
        assertNull(DnsManager.validateHostnameSyntax("dns.adguard.com"))
        assertNull(DnsManager.validateHostnameSyntax("dns.nextdns.io"))
        assertNull(DnsManager.validateHostnameSyntax("one.one.one.one"))
        assertNull(DnsManager.validateHostnameSyntax("dns.cloudflare.com"))
    }

    @Test
    fun validateHostnameSyntax_validIPv4_returnsNull() {
        assertNull(DnsManager.validateHostnameSyntax("1.1.1.1"))
        assertNull(DnsManager.validateHostnameSyntax("192.168.1.1"))
        assertNull(DnsManager.validateHostnameSyntax("8.8.8.8"))
    }

    @Test
    fun validateHostnameSyntax_validIPv6_returnsNull() {
        assertNull(DnsManager.validateHostnameSyntax("::1"))
        assertNull(DnsManager.validateHostnameSyntax("2001:4860:4860::8888"))
    }

    @Test
    fun validateHostnameSyntax_empty_returnsError() {
        val result = DnsManager.validateHostnameSyntax("")
        assertNotNull(result)
        assertEquals("Hostname cannot be empty", result)
    }

    @Test
    fun validateHostnameSyntax_blank_returnsError() {
        val result = DnsManager.validateHostnameSyntax("   ")
        assertNotNull(result)
        assertEquals("Hostname cannot be empty", result)
    }

    @Test
    fun validateHostnameSyntax_scheme_returnsError() {
        val result = DnsManager.validateHostnameSyntax("https://dns.example.com")
        assertNotNull(result)
        assertEquals("Do not include a scheme (e.g. https://). Enter the hostname only.", result)
    }

    @Test
    fun validateHostnameSyntax_pathOrSlash_returnsError() {
        val result = DnsManager.validateHostnameSyntax("dns.example.com/path")
        assertNotNull(result)
        assertEquals("Do not include paths or trailing slashes. Enter the hostname only.", result)
    }

    @Test
    fun validateHostnameSyntax_port_returnsError() {
        val result = DnsManager.validateHostnameSyntax("dns.example.com:853")
        assertNotNull(result)
        assertEquals("Do not include a port number. Enter the hostname only.", result)
    }

    @Test
    fun validateHostnameSyntax_invalidFormat_returnsError() {
        var result = DnsManager.validateHostnameSyntax("not valid..hostname")
        assertNotNull(result)
        assertEquals("Invalid hostname or IP address format", result)

        result = DnsManager.validateHostnameSyntax("-leading.com")
        assertNotNull(result)
        assertEquals("Invalid hostname or IP address format", result)
    }
}
