/*
 * Author: Matthew Randall
 * Course: CS 320 - Software Test, Automation QA
 * Date: May 24, 2026
 * Description: JUnit tests for the ContactService class. Tests verify that
 *              contacts can be added, deleted, and updated correctly, and
 *              that all error conditions are handled properly.
 */

package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import contact.Contact;
import contact.ContactService;

class ContactServiceTest {

    // ContactService and a valid Contact reused across tests
    private ContactService service;
    private Contact contact;

    @BeforeEach
    void setUp() {
        // Give us a fresh service and contact before every test
        service = new ContactService();
        contact = new Contact("1234567890", "Matthew", "Randall",
                              "5085550123", "123 Main St, Plymouth MA");
    }

    // -----------------------------------------------------------------------
    // Add Contact Tests
    // -----------------------------------------------------------------------

    @Test
    // Verify a valid contact can be added successfully
    void testAddContact() {
        service.addContact(contact);
        assertNotNull(contact);
    }

    @Test
    // Verify adding two contacts with the same ID throws an exception
    void testAddDuplicateContact() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.addContact(contact));
    }

    @Test
    // Verify two contacts with different IDs can both be added
    void testAddMultipleContacts() {
        Contact contact2 = new Contact("0987654321", "John", "Smith",
                                       "5085550199", "456 Elm St, Boston MA");
        service.addContact(contact);
        service.addContact(contact2);
        assertNotNull(contact2);
    }

    // -----------------------------------------------------------------------
    // Delete Contact Tests
    // -----------------------------------------------------------------------

    @Test
    // Verify a contact can be deleted by ID
    void testDeleteContact() {
        service.addContact(contact);
        assertDoesNotThrow(() -> service.deleteContact("1234567890"));
    }

    @Test
    // Verify deleting a contact ID that doesn't exist throws an exception
    void testDeleteContactNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
            service.deleteContact("9999999999"));
    }

    @Test
    // Verify that after deletion, adding the same ID again works
    void testDeleteThenReAdd() {
        service.addContact(contact);
        service.deleteContact("1234567890");
        assertDoesNotThrow(() -> service.addContact(contact));
    }

    // -----------------------------------------------------------------------
    // Update firstName Tests
    // -----------------------------------------------------------------------

    @Test
    // Verify firstName can be updated successfully
    void testUpdateFirstName() {
        service.addContact(contact);
        service.updateFirstName("1234567890", "John");
        assertEquals("John", contact.getFirstName());
    }

    @Test
    // Verify updating firstName with a null value throws an exception
    void testUpdateFirstNameNull() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.updateFirstName("1234567890", null));
    }

    @Test
    // Verify updating firstName with a value too long throws an exception
    void testUpdateFirstNameTooLong() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.updateFirstName("1234567890", "Matthew1234"));
    }

    @Test
    // Verify updating firstName with an ID that doesn't exist throws an exception
    void testUpdateFirstNameNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
            service.updateFirstName("9999999999", "John"));
    }

    // -----------------------------------------------------------------------
    // Update lastName Tests
    // -----------------------------------------------------------------------

    @Test
    // Verify lastName can be updated successfully
    void testUpdateLastName() {
        service.addContact(contact);
        service.updateLastName("1234567890", "Smith");
        assertEquals("Smith", contact.getLastName());
    }

    @Test
    // Verify updating lastName with a null value throws an exception
    void testUpdateLastNameNull() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.updateLastName("1234567890", null));
    }

    @Test
    // Verify updating lastName with a value too long throws an exception
    void testUpdateLastNameTooLong() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.updateLastName("1234567890", "Randall1234"));
    }

    @Test
    // Verify updating lastName with an ID that doesn't exist throws an exception
    void testUpdateLastNameNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
            service.updateLastName("9999999999", "Smith"));
    }

    // -----------------------------------------------------------------------
    // Update Phone Tests
    // -----------------------------------------------------------------------

    @Test
    // Verify phone can be updated successfully
    void testUpdatePhone() {
        service.addContact(contact);
        service.updatePhone("1234567890", "5085550199");
        assertEquals("5085550199", contact.getPhone());
    }

    @Test
    // Verify updating phone with a null value throws an exception
    void testUpdatePhoneNull() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.updatePhone("1234567890", null));
    }

    @Test
    // Verify updating phone with letters throws an exception
    void testUpdatePhoneWithLetters() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.updatePhone("1234567890", "508555ABCD"));
    }

    @Test
    // Verify updating phone with wrong length throws an exception
    void testUpdatePhoneWrongLength() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.updatePhone("1234567890", "508555"));
    }

    @Test
    // Verify updating phone with an ID that doesn't exist throws an exception
    void testUpdatePhoneNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
            service.updatePhone("9999999999", "5085550199"));
    }

    // -----------------------------------------------------------------------
    // Update Address Tests
    // -----------------------------------------------------------------------

    @Test
    // Verify address can be updated successfully
    void testUpdateAddress() {
        service.addContact(contact);
        service.updateAddress("1234567890", "456 Elm St, Boston MA");
        assertEquals("456 Elm St, Boston MA", contact.getAddress());
    }

    @Test
    // Verify updating address with a null value throws an exception
    void testUpdateAddressNull() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.updateAddress("1234567890", null));
    }

    @Test
    // Verify updating address with a value too long throws an exception
    void testUpdateAddressTooLong() {
        service.addContact(contact);
        assertThrows(IllegalArgumentException.class, () ->
            service.updateAddress("1234567890",
                                  "1234 This Address Is Way Too Long Street"));
    }

    @Test
    // Verify updating address with an ID that doesn't exist throws an exception
    void testUpdateAddressNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
            service.updateAddress("9999999999", "456 Elm St, Boston MA"));
    }

}
