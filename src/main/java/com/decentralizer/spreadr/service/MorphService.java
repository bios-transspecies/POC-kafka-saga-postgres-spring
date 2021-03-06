package com.decentralizer.spreadr.service;

import com.decentralizer.spreadr.data.MorphRepository;
import com.decentralizer.spreadr.data.entities.Morph;
import com.decentralizer.spreadr.data.kafkaDTO.KafkaMessage;
import com.decentralizer.spreadr.data.kafkaDTO.MorphDTOK;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MorphService {

    public static final String MAIN_TOPIC = "morph";
    private final MorphRepository morphRepository;
    private final KafkaTemplate<String, KafkaMessage> morphDTOKKafkaTemplate;
    private final ModelMapper modelMapper;

    public void saveToDb(MorphDTOK morphDTOK) {
        Morph morph = modelMapper.map(morphDTOK, Morph.class);
        saveToDb(morph);
    }

    public void saveToDb(Morph morph) {
        morphRepository.save(morph);
    }

    public void sendOnKafka(MorphDTOK morphDTOK) {
        ListenableFuture<SendResult<String, KafkaMessage>> handler
                = morphDTOKKafkaTemplate.send(MAIN_TOPIC, morphDTOK);
        addCallback(handler);
    }


    private void addCallback(ListenableFuture<SendResult<String, KafkaMessage>> oo) {
        oo.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.info(" sending ... [{}]", throwable);
            }

            @Override
            public void onSuccess(SendResult<String, KafkaMessage> stringStringSendResult) {
                log.info(" success ... produced [{}], \nmetadata [{}]", stringStringSendResult.getProducerRecord(), stringStringSendResult.getRecordMetadata());
            }
        });
    }
}
