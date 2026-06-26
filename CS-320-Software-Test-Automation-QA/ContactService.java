/*
 * Author: Matthew Randall
 * Course: CS 320 - Software Test, Automation QA
 * Date: May 24, 2026
 * Description: ContactService class that manages a collection of Contact
 *              objects. Supports adding, deleting, and updating contacts
 *              stored in memory using a HashMap.
 */

package contact;

import java.util.HashMap;

public class ContactService {

    // HashMap stores contacts in memory, using contactId as the key
    private HashMap<String, Contact> contacts = new HashMap<>();

    // Add a new contact to the map
    // Throws an exception if the contact ID already exists
    public void addContact(Contact contact) {
        if (contacts.containsKey(contact.getContactId())) {
            throw new IllegalArgumentException("Contact ID already exists");
        }
        contacts.put(contact.getContactId(), contact);
    }

    // Delete a contact by ID
    // Throws an exception if the contact ID is not found
    public void deleteContact(String contactId) {
        if (!contacts.containsKey(contactId)) {
            throw new IllegalArgumentException("Contact ID not found");
        }
        contacts.remove(contactId);
    }

    // Update firstName by contact ID
    public void updateFirstName(String contactId, String firstName) {
        // Look up the contact, throws exception if not found
        Contact contact = getContact(contactId);
        contact.setFirstName(firstName);
    }

    // Update lastName by contact ID
    public void updateLastName(String contactId, String lastName) {
        Contact contact = getContact(contactId);
        contact.setLastName(lastName);
    }

    // Update phone by contact ID
    public void updatePhone(String contactId, String phone) {
        Contact contact = getContact(contactId);
        contact.setPhone(phone);
    }

    // Update address by contact ID
    public void updateAddress(String contactId, String address) {
        Contact contact = getContact(contactId);
        contact.setAddress(address);
    }

    // Helper method - looks up a contact by ID
    // Throws an exception if the contact ID is not found
    private Contact getContact(String contactId) {
        if (!contacts.containsKey(contactId)) {
            throw new IllegalArgumentException("Contact ID not found");
        }
        return contacts.get(contactId);
    }

}