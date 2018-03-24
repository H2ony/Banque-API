package org.m2.oauthservice.boundary;

import java.util.Optional;
import org.m2.oauthservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
}