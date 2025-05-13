package com.egov.authservice;

import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
   List<Credential> findByPhone(String phone);

    List<Credential> findByEmail(String email);
}
