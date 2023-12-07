package com.inflearn.stock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inflearn.stock.domain.Stock;
import com.inflearn.stock.repository.StockRepository;

@Service
public class OptimisticLockStockService {

	private final StockRepository stockRepository;

	public OptimisticLockStockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	@Transactional
	public void decrease(Long id, Long quantity) {
		Stock stock = stockRepository.findByIdWithOptimisticLock(id);
		
		stock.decrease(quantity); // Optimistic Lock은 실패했을 때 재시도를 해야하므로 Facade라는 패키지를 만들고 하위에 OptimisticLockFacade라는 클래스를 생성하도록 한다.
		
		stockRepository.save(stock);
	}
	
}
