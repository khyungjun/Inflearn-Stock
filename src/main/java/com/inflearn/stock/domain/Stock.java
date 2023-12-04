package com.inflearn.stock.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;

@Entity
public class Stock {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long productId;
	
	@Version // javax.persistence.Version 어노테이션 추가한다. (java17 이후로는 jakarta.persistence.Version 추가)
	private Long version; // Optimistic Lock을 사용하기 위해 version 컬럼 추가 
	
	@Getter
	private Long quantity;

	public Stock() {
	}

	public Stock(Long productId, Long quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}
	
	public void decrease(Long quantity) {
		if(this.quantity - quantity < 0) {
			throw new RuntimeException("재고는 0개 미만이 될 수 없습니다.");
		}
		
		this.quantity -= quantity;
	}
}
