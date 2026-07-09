package com.example.coffeeordersystem.point.repository;

import com.example.coffeeordersystem.point.domain.UserPoint;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

	Optional<UserPoint> findByUserId(Long userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select up from UserPoint up where up.userId = :userId")
	Optional<UserPoint> findByUserIdForUpdate(Long userId);
}
