package com.Prakhar.Coustomer_Service.Entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="customers")
  // for getter setter
public class Customer {

    public Customer() {
    }

    public Customer(Long id, String externalId, String firstName, String lastName, String email, String phone, String address, KycStatus kycStatus, LocalDateTime createdAt, LocalDateTime updatedAt, Integer version, boolean active, String requestFingerprint, String accountNumber) {
        this.id = id;
        this.externalId = externalId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.kycStatus = kycStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
        this.active = active;
        this.requestFingerprint = requestFingerprint;
        this.accountNumber = accountNumber;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String externalId; // external Id apn bahar Dikhayege;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true) // Email sabka alag hona chahiye
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus;


    @CreationTimestamp // Jab row banegi, time apne aap aa jayega
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Jab bhi update hoga, time change ho jayega
    private LocalDateTime updatedAt;

    @Version // Ye data overwrite problem solve kr dega bankings mein use hota hai bhai
    private Integer version;

    private boolean active = true;  // ye softDelete karega data permanent delete nhi hoga

    private String requestFingerprint;// double touch se bacayega har request ka ek hi fingerprint hoga


    @Column(name="account_number")
    private String accountNumber;

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

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRequestFingerprint() {
        return requestFingerprint;
    }

    public void setRequestFingerprint(String requestFingerprint) {
        this.requestFingerprint = requestFingerprint;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
