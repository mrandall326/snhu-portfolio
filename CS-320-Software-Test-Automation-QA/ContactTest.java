/*
 * Author: Matthew Randall
 * Course: CS 320 - Software Test, Automation QA
 * Date: May 24, 2026
 * Description: JUnit tests for the Contact class. Tests verify that all
 *              field validation rules are enforced correctly, including
 *              null checks, length limits, and phone format requirements.
 */

package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import contact.Contact;

class ContactTest {

    // A valid Contact object reused across tests
    private Contact contact;

    @BeforeEach
    void setUp() {
        // This runs before every single test, giving us a fresh contact each time
        contact = new Contact("1234567890", "Matthew", "Randall",
                              "5085550123", "123 Main St, Plymouth MA");
    }

    // -----------------------------------------------------------------------
    // Constructor Tests - Valid Contact
    // -----------------------------------------------------------------------

    @Test
    // Verify a valid contact is created successfully
    void testValidContactCreation() {
        assertNotNull(contact);
    }

    @Test
    // Verify all fields are stored correctly after construction
    void testContactFieldsStoredCorrectly() {
        assertEquals("1234567890", contact.getContactId());
        assertEquals("Matthew", contact.getFirstName());
        assertEquals("Randall", contact.getLastName());
        assertEquals("5085550123", contact.getPhone());
        assertEquals("123 Main St, Plymouth MA", contact.getAddress());
    }

    // -----------------------------------------------------------------------
    // contactId Tests
    // -----------------------------------------------------------------------

    @Test
    // contactId cannot be null
    void testContactIdNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact(null, "Matthew", "Randall", "5085550123",
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // contactId cannot be longer than 10 characters
    void testContactIdTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("12345678901", "Matthew", "Randall", "5085550123",
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // contactId at exactly 10 characters should be valid
    void testContactIdExactlyTenChars() {
        Contact c = new Contact("1234567890", "Matthew", "Randall",
                                "5085550123", "123 Main St, Plymouth MA");
        assertNotNull(c);
    }

    // -----------------------------------------------------------------------
    // firstName Tests
    // -----------------------------------------------------------------------

    @Test
    // firstName cannot be null
    void testFirstNameNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", null, "Randall", "5085550123",
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // firstName cannot be longer than 10 characters
    void testFirstNameTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", "Matthew1234", "Randall", "5085550123",
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // firstName at exactly 10 characters should be valid
    void testFirstNameExactlyTenChars() {
        contact.setFirstName("Matthew123");
        assertEquals("Matthew123", contact.getFirstName());
    }

    @Test
    // setFirstName cannot be null
    void testSetFirstNameNull() {
        assertThrows(IllegalArgumentException.class, () ->
            contact.setFirstName(null));
    }

    @Test
    // setFirstName cannot be longer than 10 characters
    void testSetFirstNameTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
            contact.setFirstName("Matthew1234"));
    }

    // -----------------------------------------------------------------------
    // lastName Tests
    // -----------------------------------------------------------------------

    @Test
    // lastName cannot be null
    void testLastNameNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", "Matthew", null, "5085550123",
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // lastName cannot be longer than 10 characters
    void testLastNameTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", "Matthew", "Randall1234", "5085550123",
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // lastName at exactly 10 characters should be valid
    void testLastNameExactlyTenChars() {
        contact.setLastName("Randall123");
        assertEquals("Randall123", contact.getLastName());
    }

    @Test
    // setLastName cannot be null
    void testSetLastNameNull() {
        assertThrows(IllegalArgumentException.class, () ->
            contact.setLastName(null));
    }

    @Test
    // setLastName cannot be longer than 10 characters
    void testSetLastNameTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
            contact.setLastName("Randall1234"));
    }

    // -----------------------------------------------------------------------
    // phone Tests
    // -----------------------------------------------------------------------

    @Test
    // phone cannot be null
    void testPhoneNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", "Matthew", "Randall", null,
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // phone cannot be fewer than 10 digits
    void testPhoneTooShort() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", "Matthew", "Randall", "508555012",
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // phone cannot be more than 10 digits
    void testPhoneTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", "Matthew", "Randall", "50855501234",
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // phone cannot contain letters
    void testPhoneWithLetters() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", "Matthew", "Randall", "508555ABCD",
                        "123 Main St, Plymouth MA"));
    }

    @Test
    // phone at exactly 10 digits should be valid
    void testPhoneExactlyTenDigits() {
        contact.setPhone("5085550199");
        assertEquals("5085550199", contact.getPhone());
    }

    @Test
    // setPhone cannot be null
    void testSetPhoneNull() {
        assertThrows(IllegalArgumentException.class, () ->
            contact.setPhone(null));
    }

    // -----------------------------------------------------------------------
    // address Tests
    // -----------------------------------------------------------------------

    @Test
    // address cannot be null
    void testAddressNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", "Matthew", "Randall", "5085550123",
                        null));
    }

    @Test
    // address cannot be longer than 30 characters
    void testAddressTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
            new Contact("1234567890", "Matthew", "Randall", "5085550123",
                        "1234 This Address Is Way Too Long Street"));
    }

    @Test
    // address at exactly 30 characters should be valid
    void testAddressExactlyThirtyChars() {
        contact.setAddress("123456789012345678901234567890");
        assertEquals("123456789012345678901234567890", contact.getAddress());
    }

    @Test
    // setAddress cannot be null
    void testSetAddressNull() {
        assertThrows(IllegalArgumentException.class, () ->
            contact.setAddress(null));
    }

    @Test
    // setAddress cannot be longer than 30 characters
    void testSetAddressTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
            contact.setAddress("1234 This Address Is Way Too Long Street"));
    }

}