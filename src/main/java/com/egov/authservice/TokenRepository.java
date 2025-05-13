package com.egov.authservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TokenRepository extends JpaRepository<Token, String> {
    boolean existsByToken(String token);

    @Transactional
    @Modifying
    @Query("update Token t set t.status = ?1 where t.token = ?2")
    int updateStatusByToken(String status, String token);
}
