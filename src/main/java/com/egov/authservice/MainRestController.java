package com.egov.authservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping(value = "api/v1")
@CrossOrigin("*")
public class MainRestController
{
    private static final Logger log = LoggerFactory.getLogger(MainRestController.class);

    @Autowired
    CredentialRepository credentialRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    Producer producer;

    @PostMapping("signup")
    public ResponseEntity<String> signup(@RequestBody CredentialView credentialView) throws JsonProcessingException
    {
        // validate the credential [email and phone]
        // persist the credential in the database
        Credential credential = new Credential();
        credential.setId((long) (Math.random()*10000000));
        credential.setEmail(credentialView.getEmail());
        credential.setPhone(credentialView.getPhone());
        credential.setPassword(credentialView.getPassword());
        // persist credential into the database
        credentialRepository.save(credential);

        // send an event to the kafka topic
        AuthEvent authEvent = new AuthEvent();
        authEvent.setType("SIGNUP");
        authEvent.setPrincipal(credential.getEmail());
        producer.pubAuthEvent(authEvent); // publish the message to kafka

        return ResponseEntity.ok("Credential saved successfully!");
    }

    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody CredentialView credentialView,
                                        HttpServletResponse response) throws JsonProcessingException {
        if(credentialView.getEmail() == "")
        {
            Credential credential = credentialRepository.findByPhone(credentialView.getPhone()).stream().findFirst().get();
            if(credential.getPassword().equals(credentialView.getPassword()))
            {
                String tokenValue = String.valueOf(new Random().nextInt(900000000));
                response.addHeader("Authorization",tokenValue);
                Token token = new Token();
                token.setToken(tokenValue);
                token.setUserid(credential.getId());
                token.setStatus("ACTIVE");
                tokenRepository.save(token);

                // send an event to the kafka topic
                AuthEvent authEvent = new AuthEvent();
                authEvent.setType("LOGIN");
                authEvent.setPrincipal(credential.getEmail());
                producer.pubAuthEvent(authEvent); // publish the message to kafka

                return ResponseEntity.ok().body(credential.getId().toString());

            }
            else
            {
                // send an event to the kafka topic
                AuthEvent authEvent = new AuthEvent();
                authEvent.setType("LOGIN FAILED");
                authEvent.setPrincipal(credential.getEmail());
                producer.pubAuthEvent(authEvent); // publish the message to kafka
                return ResponseEntity.badRequest().body("Invalid credentials!");
            }
        }
        else if (credentialView.getPhone() == "")
        {
            Credential credential = credentialRepository.findByEmail(credentialView.getEmail());
            if(credential.getPassword().equals(credentialView.getPassword()))
            {
                String tokenValue = String.valueOf(new Random().nextInt(900000000));
                response.addHeader("Authorization",tokenValue);
                Token token = new Token();
                token.setToken(tokenValue);
                token.setUserid(credential.getId());
                token.setStatus("ACTIVE");
                tokenRepository.save(token);

                // send an event to the kafka topic
                AuthEvent authEvent = new AuthEvent();
                authEvent.setType("LOGIN");
                authEvent.setPrincipal(credential.getEmail());
                producer.pubAuthEvent(authEvent); // publish the message to kafka

                return ResponseEntity.ok().body(credential.getId().toString());
            }
            else
            {
                // send an event to the kafka topic
                AuthEvent authEvent = new AuthEvent();
                authEvent.setType("LOGIN FAILED");
                authEvent.setPrincipal(credential.getEmail());
                producer.pubAuthEvent(authEvent); // publish the message to kafka
                return ResponseEntity.badRequest().body("Invalid credentials!");
            }
        }
        else
        {
            return ResponseEntity.badRequest().body("Invalid credentials!");
        }
    }

    @GetMapping("validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token)
    {

        log.info("Validate Token request received with Token: {}", token);

        if(tokenRepository.existsByToken(token))
        {
            log.info("Token exists in the database");
            if(tokenRepository.findById(token).get().getStatus().equals("ACTIVE"))
            {
                log.info("Token is active");
                return ResponseEntity.ok("VALID");
            }
            else
            {
                log.info("Token is inactive");
                return ResponseEntity.badRequest().body("INVALID");
            }
        }
        else
        {
            log.info("Token does not exist in the database");
            return ResponseEntity.badRequest().body("INVALID");
        }

    }

    @PostMapping("logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token)
    {
        if(tokenRepository.existsByToken(token))
        {
            if(tokenRepository.findById(token).get().getStatus().equals("ACTIVE"))
            {
                tokenRepository.updateStatusByToken("INACTIVE", token);
            }
            else
            {
                return ResponseEntity.badRequest().body("INVALID");
            }
        }
        else
        {
            return ResponseEntity.badRequest().body("INVALID");
        }

        return ResponseEntity.ok("Logged out successfully!");
    }

    @GetMapping("get/customerid/{email}")
    public ResponseEntity<Long> getCustomerIdfromEmail(@PathVariable("email") String email)
    {
        Credential credential = credentialRepository.findByEmail(email);
        return ResponseEntity.ok(credential.getId());
    }




}
