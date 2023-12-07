package com.inflearn.stock.facade;

import org.springframework.stereotype.Component;

import com.inflearn.stock.service.OptimisticLockStockService;

@Component
public class OptimisticLockStockFacade { // Optimistic Lock은 실패했을 때 재시도를 해야하므로 Facade라는 패키지를 만들고 하위에 OptimisticLockFacade라는 클래스를 생성하도록 한다.

	private final OptimisticLockStockService optimisticLockStockService; // OptimisticLockStockService를 필드로 추가한다.

	public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) { // 생성자도 추가한다.
		this.optimisticLockStockService = optimisticLockStockService;
	}

	public void decrease(Long id, Long quantity) throws InterruptedException {
		while(true) { // update에 실패했을 때 재시도를 해야하므로 while문으로 감싸준다.
			try {
				optimisticLockStockService.decrease(id, quantity); // OptimisticLockStockService의 decrease 메소드를 호출한다.
				
				break; // 정상적으로 update가 된다면 break를 활용하여 while문을 빠져나오도록 한다.
			} catch(Exception e) {
				Thread.sleep(50); // 수량 감소에 실패하게 된다면 50밀리초 있다가 재시도를 하도록 한다. 
			}
		}
		
	}
}
