package com.aircargo.authservice.repository;

import com.aircargo.authservice.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {
    Optional<Site> findByCode(String code);
    List<Site> findByIsActiveTrue();
    boolean existsByCode(String code);
}
