package com.Prakhar.Coustomer_Service.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;


public class CustomerRequest {

    @NotBlank(message = "First Name is Required")
    private String firstName;

    @NotBlank(message = "Last Name iS Required")
    private String lastName;

    @Email(message="Invalid Email Format")
    @NotBlank(message = "Email is Required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "External ID is required")
    private String externalId;   // used for checking ki Purani request toh nhi aa gyi phir se;

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


}
