package com.adobe.bookstore;

import com.adobe.bookstore.entities.BookStock;
import com.adobe.bookstore.entities.Order;
import com.adobe.bookstore.entities.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderResourceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Sql(statements = "INSERT INTO book_stock (id, name, quantity) VALUES ('123-67', 'some book', 10)")
    public void testCreateOrderSuccess() {
        String orderJson = "{ \"orderItems\": [{ \"book\": { \"id\": \"123-67\" }, \"quantity\": 10 }] }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> orderRequest = new HttpEntity<>(orderJson, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity("http://localhost:" + port + "/orders", orderRequest, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // assert stock has been reduced
        ResponseEntity<BookStock> verifyResponse = restTemplate.getForEntity("http://localhost:" + port + "/books_stock/123-67", BookStock.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookStock bookStock = verifyResponse.getBody();
        assertThat(bookStock).isNotNull();
        assertThat(bookStock.getQuantity()).isEqualTo(0);
    }

    @Test
    @Sql(statements = "INSERT INTO book_stock (id, name, quantity) VALUES ('1', 'TEST', 7)")
    public void testCreateOrderInsufficientStock() throws Exception {
        String orderJson = "{ \"orderItems\": [{ \"book\": { \"id\": 1 }, \"quantity\": 10 }] }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(orderJson, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity("http://localhost:" + port + "/orders", request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        // assert stock remains unchanged
        ResponseEntity<BookStock> bookResponse = restTemplate.getForEntity("http://localhost:" + port + "/books_stock/1", BookStock.class);
        assertThat(bookResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookStock bookStock = bookResponse.getBody();
        assertThat(bookStock).isNotNull();
        assertThat(bookStock.getQuantity()).isEqualTo(7);
    }

    @Test
    @Sql(statements = {
            "INSERT INTO book_stock (id, name, quantity) VALUES ('12345-67890', 'some book', 10)",
            "INSERT INTO book_stock (id, name, quantity) VALUES ('09876-54321', 'another book', 5)"
    })
    public void testRetrieveOrders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String firstOrderJson = "{ \"orderItems\": [{ \"book\": { \"id\": \"12345-67890\" }, \"quantity\": 10 }] }";
        HttpEntity<String> orderRequest1 = new HttpEntity<>(firstOrderJson, headers);
        ResponseEntity<Void> response1 = restTemplate.postForEntity("http://localhost:" + port + "/orders", orderRequest1, Void.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);


        String secondOrderJson = "{ \"orderItems\": [{ \"book\": { \"id\": \"09876-54321\" }, \"quantity\": 1 }] }";

        HttpEntity<String> orderRequest2 = new HttpEntity<>(secondOrderJson, headers);
        ResponseEntity<Void> response2 = restTemplate.postForEntity("http://localhost:" + port + "/orders", orderRequest2, Void.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);


        ResponseEntity<Order[]> response = restTemplate.getForEntity("http://localhost:" + port + "/orders", Order[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Order[] orders = response.getBody();

        assertThat(orders).isNotNull();
        assertThat(orders).hasSizeBetween(2,3);
    }
}