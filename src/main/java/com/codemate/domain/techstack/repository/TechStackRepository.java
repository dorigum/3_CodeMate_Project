package com.codemate.domain.techstack.repository;

import com.codemate.domain.techstack.entity.TechStack;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {

    Optional<TechStack> findByName(String name);

    boolean existsByName(String name);
}
