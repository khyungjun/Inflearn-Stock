package com.inflearn.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.inflearn.stock.domain.Stock;

import jakarta.persistence.LockModeType;

public interface StockRepository extends JpaRepository<Stock, Long> {
	
	@Lock(LockModeType.PESSIMISTIC_WRITE) // 스프링 데이터 JPA에서는 Lock이라는 어노테이션을 통해서 손쉽게 Pessimistic Lock을 구현할 수 있다.
	@Query("select s from Stock s where s.id = :id")
	Stock findByIdWithPessimisticLock(Long id);
	
	@Lock(LockModeType.OPTIMISTIC) // 스프링 데이터 JPA에서는 Lock이라는 어노테이션을 통해서 손쉽게 Optimistic Lock을 구현할 수 있다.
	@Query("select s from Stock s where s.id = :id")
	Stock findByIdWithOptimisticLock(Long id);
}
