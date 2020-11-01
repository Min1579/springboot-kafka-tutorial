package com.min.kafka.consumer.repository;

import com.min.kafka.consumer.model.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryEventRepository extends JpaRepository<LibraryEvent, Integer> {
}
