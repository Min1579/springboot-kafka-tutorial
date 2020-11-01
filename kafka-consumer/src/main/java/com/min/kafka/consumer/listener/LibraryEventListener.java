package com.min.kafka.consumer.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.min.kafka.consumer.service.LibraryEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LibraryEventListener {
    private final LibraryEventService libraryEventsService;

    @KafkaListener(topics = {"library-events"})
    public void onMessage(final ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {
        log.info("ConsumerRecord : {} ", consumerRecord );
        libraryEventsService.processLibraryEvent(consumerRecord);
    }
}
