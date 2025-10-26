package com.example.hirelink.Repositories;

import com.example.hirelink.Application.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
