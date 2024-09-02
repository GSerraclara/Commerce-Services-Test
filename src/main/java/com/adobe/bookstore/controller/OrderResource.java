package com.adobe.bookstore.controller;

import com.adobe.bookstore.entities.BookStock;
import com.adobe.bookstore.entities.Order;
import com.adobe.bookstore.repository.BookStockRepository;
import com.adobe.bookstore.repository.OrderRepository;
import com.adobe.bookstore.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/orders")
public class OrderResource {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Autowired
    public OrderResource(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody Order order) {
        Long orderId = orderService.processOrder(order);
        System.out.println(order);
        return ResponseEntity.ok(orderId);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }
}
