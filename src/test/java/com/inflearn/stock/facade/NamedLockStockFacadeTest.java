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
import com.inflearn.stock.repository.StockRepository;

/**
 * Named Lock은 주로 분산락을 구현할 때 사용한다.
 * 그리고 Pessimistic Lock은 Time out을 구현하기 힘들지만 Named Lock은 Time out을 손쉽게 구현할 수 있다.
 * 그 이외에도 데이터 삽입시에 정합성을 맞춰야 하는 경우에도 Named Lock을 사용할 수 있다.
 * 하지만 이 방법은 트랜잭션 종료시에 락 해제 세션 관리를 잘 해줘야 하기 때문에 주의해서 사용해야 하고 실제로 사용할 때는 구현 방법이 복잡할 수 있다. 
 */
@SpringBootTest
public class NamedLockStockFacadeTest {

	@Autowired
	private NamedLockStockFacade namedLockStockFacade;
	
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
					namedLockStockFacade.decrease(1L, 1L);
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
