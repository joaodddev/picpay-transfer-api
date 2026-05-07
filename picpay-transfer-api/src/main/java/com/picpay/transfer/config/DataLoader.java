package com.picpay.transfer.config;

import com.picpay.transfer.domain.entity.User;
import com.picpay.transfer.domain.entity.Wallet;
import com.picpay.transfer.domain.enums.UserType;
import com.picpay.transfer.repository.UserRepository;
import com.picpay.transfer.repository.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(UserRepository userRepository, WalletRepository walletRepository) {
        return args -> {

            User alice = User.builder()
                    .fullName("Alice Souza")
                    .document("111.111.111-11")
                    .email("alice@email.com")
                    .password("senha123")
                    .userType(UserType.COMMON)
                    .build();

            User bruno = User.builder()
                    .fullName("Bruno Lima")
                    .document("222.222.222-22")
                    .email("bruno@email.com")
                    .password("senha123")
                    .userType(UserType.COMMON)
                    .build();

            User loja = User.builder()
                    .fullName("Loja do Zé")
                    .document("33.333.333/0001-33")
                    .email("loja@email.com")
                    .password("senha123")
                    .userType(UserType.MERCHANT)
                    .build();

            userRepository.save(alice);
            userRepository.save(bruno);
            userRepository.save(loja);

            Wallet walletAlice = Wallet.builder()
                    .balance(new BigDecimal("1000.00"))
                    .user(alice)
                    .build();

            Wallet walletBruno = Wallet.builder()
                    .balance(new BigDecimal("500.00"))
                    .user(bruno)
                    .build();

            Wallet walletLoja = Wallet.builder()
                    .balance(new BigDecimal("0.00"))
                    .user(loja)
                    .build();

            walletRepository.save(walletAlice);
            walletRepository.save(walletBruno);
            walletRepository.save(walletLoja);
        };
    }
}