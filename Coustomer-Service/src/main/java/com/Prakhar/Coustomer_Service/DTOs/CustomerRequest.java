package com.Prakhar.Coustomer_Service.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;


public class CustomerRequest {

    @NotBlank(message = "First Name is Required")
    private String firstName;

    @NotBlank(message = "Last Name iS Required")
    private String lastName;

    @Email(message="Invalid Email Format")
    @NotBlank(message = "Email is Required")
    private String email;

    private String phone;   // optional now - the frontend doesn't ask for it

    private String address;   // optional

    @NotBlank(message = "External ID is required")
    private String externalId;

    // Optional so old clients (Postman, smoke test) still work. The frontend always sends it.
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // 4-digit transaction PIN (optional for old clients; the website always sends it)
    @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
    private String pin;

    private String dateOfBirth;
    private String city;
    private String state;
    private String postalCode;
    private String occupation;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }   // used for checking ki Purani request toh nhi aa gyi phir se;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }



    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
}
