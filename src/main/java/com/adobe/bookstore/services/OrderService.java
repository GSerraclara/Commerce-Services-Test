package com.adobe.bookstore.services;

import com.adobe.bookstore.entities.BookStock;
import com.adobe.bookstore.entities.Order;
import com.adobe.bookstore.entities.OrderItem;
import com.adobe.bookstore.exceptions.InsufficientStockException;
import com.adobe.bookstore.repository.BookStockRepository;
import com.adobe.bookstore.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private BookStockRepository bookStockRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Long processOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            BookStock stock = bookStockRepository.findById(item.getBook().getId()).orElseThrow();
            if (stock.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for book " + stock.getName());
            }
        }

        order.setStatus("SUCCESS");
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        updateStockAsync(order);

        return savedOrder.getId();
    }

    public void updateStockAsync(Order order) {
        try {
            for (OrderItem item : order.getOrderItems()) {
                BookStock stock = bookStockRepository.findById(item.getBook().getId()).orElseThrow();
                stock.setQuantity(stock.getQuantity() - item.getQuantity());
                bookStockRepository.save(stock);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
