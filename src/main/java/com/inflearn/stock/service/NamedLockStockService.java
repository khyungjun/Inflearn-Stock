package com.inflearn.stock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.inflearn.stock.domain.Stock;
import com.inflearn.stock.repository.StockRepository;

@Service
public class NamedLockStockService {

	private final StockRepository stockRepository;

	public NamedLockStockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void decrease(Long id, Long quantity) { 
		// Stock 조회
		// 재고를 감소시킨 뒤
		// 갱신된 값을 저장
		
		Stock stock = stockRepository.findById(id).orElseThrow();
		stock.decrease(quantity);
		stockRepository.saveAndFlush(stock);
	}
}
