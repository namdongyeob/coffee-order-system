package com.example.coffeeordersystem.point.repository;

import com.example.coffeeordersystem.point.domain.UserPoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

	Optional<UserPoint> findByUserId(Long userId);
}
