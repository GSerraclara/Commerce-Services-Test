package com.adobe.bookstore.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "book_order")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private BookStock book;

    private int quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BookStock getBook() {
        return book;
    }

    public void setBook(BookStock book) {
        this.book = book;
    }
}