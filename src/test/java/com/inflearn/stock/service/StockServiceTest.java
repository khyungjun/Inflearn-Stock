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

@SpringBootTest
public class StockServiceTest {

	@Autowired
	private StockService stockService;
	
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
	public void 재고감소() {
		stockService.decrease(1L, 1L);
		
		// 예상 재고 : 100 - 1 = 99
		Stock stock = stockRepository.findById(1L).orElseThrow();
		
		System.out.println("stock.getQuantity() : " + stock.getQuantity());
		assertEquals(99, stock.getQuantity());
	}

	/**
	 * 테스트 케이스가 실패하는 것을 확인 할 수 있다. 우리가 예상한 것은 0개인데 실제로는 그렇지 않을 것을 확인 할 수 있다. 이유를 생각해보도록 하자.
	 * 우리의 예상과 다른 이유는 바로 Race Condition이 일어났기 때문이다. Race Condition이란 둘 이상의 스레드가 동류 데이터에 엑세스 할 수 있고 동시에 변경을 하려고 할 때 발생하는 문제이다.
	 * 예를 들면, 우리는 스레드1이 데이터를 가져가서 갱신을 한 값을 스레드2가 가져가서 갱신을 하는 것을 예상하지만 실제로는 스레드1이 데이터를 가져가서 갱신을 하기 이전에 스레드2가 데이터를 가져가서 갱신되기 이전의 값을 가져가게 된다. 
	 * 그리고 스레드1이 갱신을 하고 스레드2도 갱신을 하지만 둘 다 재고가 같은 상태에서 1을 줄인 값을 갱신하기 때문에 갱신이 누락되게 된다.
	 * 이런 문제를 해결하기 위해서는 우리의 예상대로 하나의 스레드가 작업을 완료한 이후에 다른 스레드가 데이터에 접근할 수 있도록 하면 된다. 
	 */
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
