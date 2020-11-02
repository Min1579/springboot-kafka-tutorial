package com.min.kafka.producer.controller;

import com.min.kafka.producer.domain.Book;
import com.min.kafka.producer.domain.LibraryEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        final Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @Timeout(5) //with this code -> do not need Thread.sleep(3000);
    void postLibraryEvent() throws InterruptedException {
        //given
        final LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(Book.builder()
                        .bookId(123)
                        .bookAuthor("Dilip")
                        .bookName("Kafka using Spring Boot")
                        .build())
                .build();

        final HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());
        final HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        //when
        final ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST, request, LibraryEvent.class);

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        final ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        //Thread.sleep(3000);
        final String expectedRecord
                = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        final String value = consumerRecord.value();
        assertEquals(expectedRecord, value);
    }

    @Test
    @Timeout(5)
    void putLibraryEvent() throws InterruptedException {
        //given
        final LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .book(Book.builder()
                        .bookId(456)
                        .bookAuthor("Dilip")
                        .bookName("Kafka using Spring Boot")
                        .build())
                .build();

        final HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON.toString());
        final HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        //when
        final ResponseEntity<LibraryEvent> responseEntity
                = restTemplate.exchange("/v1/libraryevent", HttpMethod.PUT, request, LibraryEvent.class);

        //then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final ConsumerRecord<Integer, String> consumerRecord
                = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        Thread.sleep(3000);
        final String expectedRecord
                = "{\"libraryEventId\":123,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        final String value = consumerRecord.value();
        assertEquals(expectedRecord, value);
    }
}