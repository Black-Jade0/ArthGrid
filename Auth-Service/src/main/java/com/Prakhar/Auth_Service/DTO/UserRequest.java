package com.Prakhar.Auth_Service.DTO;





public class UserRequest {

    private Long id;
    private String externalId;
    private String firstName;
    private String lastName;
    private String email;

    public UserRequest(Long id, String externalId, String firstName, String lastName, String email) {
        this.id = id;
        this.externalId = externalId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public UserRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

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

    // password yha nhi denge hum set karege baad mein yeh
}
