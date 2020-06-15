package com.ecommerce.account.kafka;

import com.ecommerce.account.domain.Order;
import com.ecommerce.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Properties;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Component
public class AccountConsumer implements Runnable {

    @Autowired
    AccountService accountService;

    private KafkaConsumer kafkaConsumer;
    private static final String ACCOUNT_TOPIC = "tqumy56i-account";

    /*
     * method 2 : write kafka manually
     * 1. we need to manually config the kafka server connection properties  (application.properties)
     * 2.setup a thread to keep listening to the brokder (@KafkaListener)
     * 3.process payload once the consumer gets message  -- processPayLoad()
     *
     */
    @Override
    public void run() {

        try {
            while (true) {
                //TODO:: fetch from partition every given seconds
                ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    String order = record.value();
                    //convert String to Order.class
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Order newOrder = mapper.readValue(order, Order.class);
                        accountService.updateOrderHistory(newOrder);
                    } catch (Exception e) {
                        System.out.println("======error parsing orde info");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("error polling mesg from partition");
        } finally {
            kafkaConsumer.close();
        }

    }

    //TODO:: kafkaConsumer -- connection,topic
    private void createConsumer() {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG,"moped-01.srvs.cloudkafka.com:9094,moped-02.srvs.cloudkafka.com:9094,moped-03.srvs.cloudkafka.com:9094");
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        props.put("security.protocol","SASL_SSL");
        props.put("sasl.mechanism","SCRAM-SHA-256");

        String JASS_FORMAT = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\"; ";
        String USERNAME = "tqumy56i";
        String PASSWORD = "n_B0NehNYDpAEtc2KmgnUiVzLoQwKLB2";
        String JASSCONFIG = String.format(JASS_FORMAT,USERNAME,PASSWORD);
        props.put("sasl.jaas.config",JASSCONFIG);
        props.put(GROUP_ID_CONFIG,"account-consumer");
        props.put(AUTO_OFFSET_RESET_CONFIG,"latest");
        props.put(ENABLE_AUTO_COMMIT_CONFIG,false);
        props.put(MAX_POLL_RECORDS_CONFIG,1);

        kafkaConsumer = new KafkaConsumer<>(props);
        //Listening jian ting
        kafkaConsumer.subscribe(Arrays.asList(ACCOUNT_TOPIC));

        //make AccountConsumer as sepeate thread
        Thread thread = new Thread(this);
        thread.start();
    }

    //constructor, will be called constructor when Spring start running
    public AccountConsumer(){
        this.createConsumer();
    }

    /*
    * # Kafka configuration
        spring.kafka.bootstrap-servers=moped-01.srvs.cloudkafka.com:9094,moped-02.srvs.cloudkafka.com:9094,moped-03.srvs.cloudkafka.com:9094
        spring.kafka.properties.security.protocol=SASL_SSL
        spring.kafka.properties.sasl.mechanism=SCRAM-SHA-256
        #Replace username and password with your account
        spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="tqumy56i" password="n_B0NehNYDpAEtc2KmgnUiVzLoQwKLB2";
        spring.kafka.consumer.group-id=product-consumer
        spring.kafka.consumer.auto-offset-reset=latest
        spring.kafka.consumer.key-serializer=org.apache.kafka.common.serialization.StringDeSerializer
        spring.kafka.consumer.value-serializer=org.apache.kafka.common.serialization.StringDeSerializer
        spring.kafka.consumer.properties.spring.json.trusted.packages=*
        spring.kafka.consumer.enable-auto-commit=false
        spring.kafka.consumer.max-poll-records=1
    * */
}


