package com.inflearn.stock.service;

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
import com.inflearn.stock.repository.StockRepository;

/**
 * Pessimistic Lock
 * 
 * 장점
 * - 충돌이 빈번하게 일어난다면 Optimistic Lock보다 성능이 좋을 수 있다.
 * - Lock을 통해 업데이트를 제어하기 때문에 데이터 정합성이 보장된다.
 *
 * 단점
 * - 별도의 Lock을 잡기 때문에 성능 감소가 있을 수 있다.
 * 
 *  충돌이 빈번하게 일어난다면 혹은 충돌이 빈번하게 일어날 것이라고 예상된다면 Pessimistic Lock을 추천하고 빈번하게 일어나지 않을 것이라고 예상된다면 Optimistic Lock을 추천한다.
 */
@SpringBootTest
public class PessimisticLockStockServiceTest {

	@Autowired
	private PessimisticLockStockService stockService; // Pessimistic Lock을 사용 -> 성공
	
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
		
		for(int i=0; i<threadCount; i++) {
			executorService.submit(() -> {
				try {
					stockService.decrease(1L, 1L);
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