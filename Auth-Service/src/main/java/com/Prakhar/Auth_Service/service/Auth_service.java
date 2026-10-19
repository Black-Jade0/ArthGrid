package com.Prakhar.Auth_Service.service;

import com.Prakhar.Auth_Service.DTO.UserEvent;

import com.Prakhar.Auth_Service.DTO.UserRequest;
import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.tickets.PasswordChangeTicket;
import com.auth0.net.TokenRequest;
import com.auth0.json.mgmt.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class Auth_service {

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.clientId}")
    private String clientId;

    @Value("${auth0.clientSecret}")
    private String clientSecret;

    @Autowired
    @Lazy
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    private static final String CONNECTION_NAME = "Username-Password-Authentication";

    /** Where the "Set password" link in the welcome email sends the user afterwards (the frontend). */
    @Value("${app.frontend-url:http://localhost:5173/}")
    private String frontendUrl;

    /**
     * LOCAL MODE: if no Auth0 client secret is configured (e.g. on your laptop),
     * we skip Auth0 completely and just make up a local user id.
     * Everything else (Kafka welcome email, account creation) still runs.
     */
    private boolean isLocalMode() {
        return clientSecret == null || clientSecret.isBlank();
    }

    private String createLocalUser(UserRequest userRequest) {
        // No "|" in the id (unlike Auth0's "auth0|..."), so it can be used directly in URLs
        String localUserId = "local-" + UUID.randomUUID();
        System.out.println("[LOCAL MODE] Auth0 skipped. Created local user " + localUserId + " for " + userRequest.getEmail());

        try {
            UserEvent event = new UserEvent();
            event.setEmail(userRequest.getEmail());
            event.setUsername(userRequest.getFirstName() + " " + userRequest.getLastName());
            event.setUserId(localUserId);
            event.setResetLink(frontendUrl);
            event.setEventType("USER_CREATED");
            kafkaTemplate.send("user-events", event);
        } catch (Exception e) {
            System.err.println("[LOCAL MODE] User created but Kafka message failed: " + e.getMessage());
        }
        return localUserId;
    }

    public String createUserInAuth0(UserRequest userRequest) {
        System.err.println("AUTH-SERVICE CALLED FOR EMAIL: " + userRequest.getEmail());
        if (isLocalMode()) {
            return createLocalUser(userRequest);
        }
        try {
            // 1. Get Admin Token
            AuthAPI authAPI = new AuthAPI(domain, clientId, clientSecret);
            TokenRequest tokenRequest = authAPI.requestToken("https://" + domain + "/api/v2/");
            TokenHolder tokenHolder = tokenRequest.execute().getBody();
            String accessToken = tokenHolder.getAccessToken();

            // 2. Setup Management API
            ManagementAPI managementAPI = new ManagementAPI(domain, accessToken);

            // 3. Prepare User Object
            User user = new User();
            user.setEmail(userRequest.getEmail());
            user.setName(userRequest.getFirstName() + " " + userRequest.getLastName());
            user.setConnection(CONNECTION_NAME);
            user.setPassword("Bank@123");
            user.setEmailVerified(true);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("bank_external_id", userRequest.getExternalId());
            user.setAppMetadata(metadata);

            // 4. Create User aur uska Response body nikal lo
            // YAHAN BADLAV HAI
            User createdUser = managementAPI.users().create(user).execute().getBody();
            String auth0UserId = createdUser.getId(); // Ye return karega "auth0|..."
            String resetLink = generatePasswordTicket(auth0UserId);

            System.err.println(" RESET LINK GENERATED: " + resetLink);

            System.out.println("Successfully Created user in Auth0: " + auth0UserId);

            // SEND KAFKA EVENT (Taaki Notification Service mail bhej sake)
            try {
                UserEvent event = new UserEvent();
                        event.setEmail(userRequest.getEmail());
                        event.setUsername(userRequest.getFirstName() + " " + userRequest.getLastName());
                        event.setUserId(auth0UserId);// Yahan bhi asli ID bhej sakte ho agar chaho
                        event.setResetLink(resetLink);
                        event.setEventType("USER_CREATED");


                kafkaTemplate.send("user-events", event);
            } catch (Exception e) {
                System.err.println("User ban gaya par Kafka message fail ho gaya: " + e.getMessage());
            }

            //  RETURN THE REAL ID
            return auth0UserId;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create User in Auth0: " + e.getMessage());
        }
    }

    public String generatePasswordTicket(String userId) {
        try {
            // 1. Admin Token लो (बिल्कुल पुराने मेथड की तरह)
            AuthAPI authAPI = new AuthAPI(domain, clientId, clientSecret);
            TokenRequest tokenRequest = authAPI.requestToken("https://" + domain + "/api/v2/");
            TokenHolder tokenHolder = tokenRequest.execute().getBody();
            String accessToken = tokenHolder.getAccessToken();

            // 2. Setup Management API
            ManagementAPI managementAPI = new ManagementAPI(domain, accessToken);

            // 3. Ticket का ऑब्जेक्ट बनाओ
            // ये Auth0 को बताता है कि पासवर्ड बदलने के बाद यूजर को वापस कहाँ भेजना है
            PasswordChangeTicket ticketRequest =
                    new PasswordChangeTicket(userId);

            ticketRequest.setResultUrl(frontendUrl); // वापस डैशबोर्ड पर आए
            ticketRequest.setTTLSeconds(86400); // 24 घंटे की वैलिडिटी

            // 4. Auth0 से Ticket मांगो
            PasswordChangeTicket ticketResponse =
                    managementAPI.tickets().requestPasswordChange(ticketRequest).execute().getBody();
            return ticketResponse.getTicket();

        } catch (Exception e) {
            System.err.println("Password Ticket बनाने में गड़बड़: " + e.getMessage());
            return null;
        }
    }
}

