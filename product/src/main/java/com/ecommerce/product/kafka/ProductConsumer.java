package com.ecommerce.product.kafka;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.security.oauthbearer.internals.unsecured.OAuthBearerUnsecuredJws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductConsumer {

    @Autowired
    ProductService service;

    @KafkaListener(topics = "tqumy56i-inventory")
    public void processPayload(String payload){
        ObjectMapper mapper = new ObjectMapper();
        try{
            Product product = mapper.readValue(payload, Product.class);
            service.updateProductInventory(product);
        }catch (Exception e){
            System.out.println("======error convering Json string to object");
        }
    }

}
