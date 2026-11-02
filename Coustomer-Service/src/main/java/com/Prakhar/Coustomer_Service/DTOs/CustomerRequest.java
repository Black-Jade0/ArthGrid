package com.Prakhar.Coustomer_Service.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;


public class CustomerRequest {

    @NotBlank(message = "First Name is Required")
    private String firstName;

    @NotBlank(message = "Last Name iS Required")
    private String lastName;

    @Email(message="Invalid Email Format")
    @NotBlank(message = "Email is Required")
    private String email;

    private String phone;   // optional now - the frontend doesn't ask for it

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "External ID is required")
    private String externalId;

    // Optional so old clients (Postman, smoke test) still work. The frontend always sends it.
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

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


}
