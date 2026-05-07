package com.picpay.transfer.repository;

import com.picpay.transfer.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByDocument(String document);
    boolean existsByEmail(String email);
}
