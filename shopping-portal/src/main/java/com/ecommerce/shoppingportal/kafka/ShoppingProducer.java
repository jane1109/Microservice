package com.ecommerce.shoppingportal.kafka;

import com.ecommerce.shoppingportal.domain.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShoppingProducer {

    //include kafka username and password and config properties
    private final KafkaTemplate kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(ShoppingProducer.class);

    @Autowired
    public ShoppingProducer(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String INVENTORY_TOPIC = "tqumy56i-inventory";
    private static final String ACCOUNT_TOPIC = "tqumy56i-account";

    //producer send to kafka , consumer side will receive
    public void sendProductInventoryReduction(Order order) {
        //send topic ,partition and mesg body to kafka
        String product = this.convertObjectToJsonString(order);
        this.kafkaTemplate.send(INVENTORY_TOPIC,product);
        System.out.println("========== send to PRODUCT SERVICE with purchased order: " + order);
    }

    public void sendAccountOrderHistory(Order order){
        String ord = this.convertObjectToJsonString(order);
        this.kafkaTemplate.send(ACCOUNT_TOPIC,ord);
        LOGGER.info("========== send to service account with purchased order");

    }

    //kafka prefer accept String message body
    private String convertObjectToJsonString(Object o){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        String result = "";
        try{
            result = objectMapper.writeValueAsString(o);
        }catch (Exception e){
            System.out.println("error convert object to String");
        }
        return result;
    }


    /* Spring autoconfig kafkatemplate - no customized initialization needed*/

    /*update inventory*/

    /*update order history*/

}
