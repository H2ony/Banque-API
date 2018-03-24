package org.m2.oauthservice;

import java.util.stream.Stream;
import org.m2.oauthservice.boundary.AccountRepository;
import org.m2.oauthservice.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableResourceServer
public class OauthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OauthServiceApplication.class, args);
	}
}

@Component
class AccountCLR implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountCLR(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... strings) throws Exception {
        Stream.of("olivier,sesame", "toto,azerty", "titi,grosminet")
            .map(x -> x.split(","))
            .forEach(tpl -> accountRepository.save(new Account(tpl[0], tpl[1], true)));
    }
}

