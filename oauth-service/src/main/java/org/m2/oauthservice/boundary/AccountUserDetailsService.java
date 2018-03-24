package org.m2.oauthservice.boundary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return this.accountRepository.findByUsername(s)
                .map(account -> new User(
                        account.getUsername(),
                        account.getPassword(),
                        account.isActive(), 
                        account.isActive(), 
                        account.isActive(), 
                        account.isActive(),
                        AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Pb!"));
    }
}