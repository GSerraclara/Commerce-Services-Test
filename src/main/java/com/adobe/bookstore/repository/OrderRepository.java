package com.adobe.bookstore.repository;

import com.adobe.bookstore.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Long> {
}
