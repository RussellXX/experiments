package com.njuse.llmeval.repository;

import com.njuse.llmeval.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByPhone(String phone);

    boolean existsByPhone(String phone);
}
