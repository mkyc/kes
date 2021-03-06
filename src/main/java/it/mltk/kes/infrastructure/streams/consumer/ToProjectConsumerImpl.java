package it.mltk.kes.infrastructure.streams.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.mltk.kes.domain.event.ProjectDomainEvent;
import it.mltk.kes.domain.model.Project;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.io.IOException;

import static it.mltk.kes.infrastructure.configuration.StreamsClientsConfig.PROJECTS_STORE;


@EnableBinding(ToProjectConsumerImpl.DomainEventConsumerBinding.class)
@Slf4j
public class ToProjectConsumerImpl implements ToProjectConsumer {

    private final Serde<ProjectDomainEvent> domainEventSerde;
    private final Serde<Project> projectSerde;
    ObjectMapper objectMapper;

    ToProjectConsumerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        domainEventSerde = new JsonSerde<>(ProjectDomainEvent.class, objectMapper);
        projectSerde = new JsonSerde<>(Project.class, objectMapper);
    }

    @SuppressWarnings("Duplicates")
    @StreamListener(DomainEventConsumerBinding.INPUT)
    public void process(KStream<Object, byte[]> input) {
        input
                .map((k, v) -> {
                    try {
                        ProjectDomainEvent projectDomainEvent = objectMapper.readValue(v, ProjectDomainEvent.class);
                        return new KeyValue<>(projectDomainEvent.getProjectUuid().toString(), projectDomainEvent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .peek((k, v) -> log.debug("key: " + k + " domainEvent: " + v))
                .groupBy((s, domainEvent) -> s, Serialized.with(Serdes.String(), domainEventSerde))
                .aggregate(
                        Project::new,
                        (key, domainEvent, project) -> project.handleEvent(domainEvent),
                        Materialized.<String, Project, KeyValueStore<Bytes, byte[]>>as(PROJECTS_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(projectSerde)
                );
    }

    interface DomainEventConsumerBinding {
        String INPUT = "domain-event-consumer";

        @Input("domain-event-consumer")
        KStream<?, ?> input();
    }
}
