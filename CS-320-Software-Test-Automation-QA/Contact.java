/*
 * Author: Matthew Randall
 * Course: CS 320 - Software Test, Automation QA
 * Date: May 24, 2026
 * Description: Contact class that defines the Contact object and enforces
 *              all field validation requirements for the contact service.
 */

package contact;

public class Contact {

    // Fields - contactId is final because it cannot be updated after creation
    private final String contactId;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;

    // Constructor - validates all fields before storing them
    public Contact(String contactId, String firstName, String lastName,
                   String phone, String address) {

        // contactId must not be null and must be 10 characters or fewer
        if (contactId == null || contactId.length() > 10) {
            throw new IllegalArgumentException("Invalid contact ID");
        }
        // firstName must not be null and must be 10 characters or fewer
        if (firstName == null || firstName.length() > 10) {
            throw new IllegalArgumentException("Invalid first name");
        }
        // lastName must not be null and must be 10 characters or fewer
        if (lastName == null || lastName.length() > 10) {
            throw new IllegalArgumentException("Invalid last name");
        }
        // phone must not be null and must be exactly 10 digits
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        // address must not be null and must be 30 characters or fewer
        if (address == null || address.length() > 30) {
            throw new IllegalArgumentException("Invalid address");
        }

        // All validation passed, store the values
        this.contactId = contactId;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.phone     = phone;
        this.address   = address;
    }

    // Getter for contactId - no setter provided because contactId cannot be updated
    public String getContactId() {
        return contactId;
    }

    // Getter and setter for firstName
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        // firstName must not be null and must be 10 characters or fewer
        if (firstName == null || firstName.length() > 10) {
            throw new IllegalArgumentException("Invalid first name");
        }
        this.firstName = firstName;
    }

    // Getter and setter for lastName
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        // lastName must not be null and must be 10 characters or fewer
        if (lastName == null || lastName.length() > 10) {
            throw new IllegalArgumentException("Invalid last name");
        }
        this.lastName = lastName;
    }

    // Getter and setter for phone
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        // phone must not be null and must be exactly 10 digits
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        this.phone = phone;
    }

    // Getter and setter for address
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        // address must not be null and must be 30 characters or fewer
        if (address == null || address.length() > 30) {
            throw new IllegalArgumentException("Invalid address");
        }
        this.address = address;
    }

}