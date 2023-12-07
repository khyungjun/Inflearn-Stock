package com.inflearn.stock.facade;

import org.springframework.stereotype.Component;

import com.inflearn.stock.repository.LockRepository;
import com.inflearn.stock.service.NamedLockStockService;

import jakarta.transaction.Transactional;

@Component
public class NamedLockStockFacade {

	private final LockRepository lockRepository; 

	private final NamedLockStockService namedLockStockService;
	
	public NamedLockStockFacade(LockRepository lockRepository, NamedLockStockService namedLockStockService) {
		this.lockRepository = lockRepository;
		this.namedLockStockService = namedLockStockService;
	}

	@Transactional
	public void decrease(Long id, Long quantity) {
		try {
			lockRepository.getLock(id.toString());
			namedLockStockService.decrease(id, quantity);
		} finally {
			lockRepository.releaseLock(id.toString());
		}
		
	}
}
