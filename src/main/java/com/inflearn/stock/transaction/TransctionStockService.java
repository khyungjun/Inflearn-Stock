package com.inflearn.stock.transaction;

import com.inflearn.stock.service.StockService;

public class TransctionStockService {

	private StockService stockService;
	
	public TransctionStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public void decrease(Long id, Long quantity) {
		startTransction();
		
		stockService.decrease(id, quantity);
		
		endTransction();
	}
	
	private void startTransction() {
		System.out.println("Transction Start");
	}
	
	private void endTransction() {
		System.out.println("Commit");
	}
}
