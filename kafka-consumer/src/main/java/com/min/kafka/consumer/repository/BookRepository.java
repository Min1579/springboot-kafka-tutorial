package com.min.kafka.consumer.repository;

import com.min.kafka.consumer.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {
}
