package com.min.kafka.producer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Book {
    private Integer bookId;
    private String bookName;
    private String bookAuthor;
}