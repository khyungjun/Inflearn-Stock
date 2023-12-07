package com.inflearn.stock.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.inflearn.stock.domain.Stock;
import com.inflearn.stock.facade.OptimisticLockStockFacade;
import com.inflearn.stock.repository.StockRepository;

/**
 * Optimistic Lock
 * 
 * 장점
 * - 별도의 Lock을 잡지 않으므로 Pessimistic Lock보다 성능상의 이점이 있다.
 * 단점
 * - update가 실패했을 때 재시도 로직을 개발자가 직접 작성해주어야 하는 번거로움이 존재한다.
 * 
 * 충돌이 빈번하게 일어난다면 혹은 충돌이 빈번하게 일어날 것이라고 예상된다면 Pessimistic Lock을 추천하고 빈번하게 일어나지 않을 것이라고 예상된다면 Optimistic Lock을 추천한다.
 */
@SpringBootTest
public class OptimisticLockStockFacadeTest {

	@Autowired
	private OptimisticLockStockFacade optimisticLockStockFacade;
	
	@Autowired
	private StockRepository stockRepository;
	
	@BeforeEach // 테스트가 실행되기 전
	public void before() {
		stockRepository.saveAndFlush(new Stock(1L, 100L));
	}

	@AfterEach
	public void after() {
		stockRepository.deleteAll();
	}
	
	@Test
	public void 동시에_100개의_요청() throws InterruptedException {
		int threadCount = 100; // 동시에 여러개의 요청을 보내야 하기 때문에 멀티 스레드를 사용해야 한다.(100개의 요청을 보낼 것이다)

		// 멀티스레드를 이용해야 하기 때문에 Executors 서비스를 사용한다. Executors 서비스는 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와주는 자바의 API이다.
		ExecutorService executorService = Executors.newFixedThreadPool(32); 
		
		// 100개의 요청이 모두 끝날 때까지 기다려야 하므로 CountDownLatch를 활용한다.
		CountDownLatch latch = new CountDownLatch(threadCount);
		
		// Optimistic Lock을 활용할 떄는 실패했을 때 재시도하는 로직이 있어서 이전(Pessimistic Lock)보다는 오래걸리기는 했지만 테스트 케이스가 정상적으로 동작하는 것을 확인 할 수 있다.
		for(int i=0; i<threadCount; i++) {
			executorService.submit(() -> {
				try {
					optimisticLockStockFacade.decrease(1L, 1L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await(); // CountDownLatch는 다른 스레드에서 수행 중인 작업이 완료될 때까지 대기할 수 있도록 도와주는 클래스다.
		
		// 모든 요청이 완료가 된다면 stockRepository를 활용해서 stock을 가지고 온 이후에 실행을 비교해주도록 한다.
		Stock stock = stockRepository.findById(1L).orElseThrow();
		
		// 예상 재고 : 100 - (1 * 100) = 0
		System.out.println("stock.getQuantity() : " + stock.getQuantity());
		assertEquals(0, stock.getQuantity());
	}
}
