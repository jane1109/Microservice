package com.ecommerce.shoppingportal.service;

import com.ecommerce.shoppingportal.domain.Order;
import com.ecommerce.shoppingportal.kafka.ShoppingProducer;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;


@Service
public class ShoppingService {

    public static final double TAX_RATE = 6.25;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ShoppingProducer producer;

    @HystrixCommand(fallbackMethod = "recommendationFallback")
    public String recommendProduct(String category) {
        //TODO:: use restTemplate to call product service to get recommended product
        String url = "http://PRODUCT-SERVICE/recommend/" + category;
        String recommended = restTemplate.getForObject(url, String.class);
        return recommended;
    }

    public String recommendationFallback(String category){
        return "sorry " + category;
    }

    public String getAccountServer() {
        //TODO:: use restTemplate to call account service to get account info
        String url = "http://ACCOUNT-SERVICE/account_server_info";
        String account_server_info = restTemplate.getForObject(url, String.class);
        return account_server_info;
    }


    /**
     * Logic
     * step1: call product service to check if the product is available with amount, if so, return unit price
     * step2: call account to reduce that total cost
     * step3: call Kafka to reduce product service inventory
     * step4: call Kafka to add new order into order history
     * */
    public String orderProduct(String name, int amount) {
        //TODO:: Order product logic
        //step 1: call product service to check if the product is available with amount
        String eligibilityUrl = "http://PRODUCT-SERVICE/price/" + name + "/" + amount;
        double unitPrice = 0;
        try{
            unitPrice = restTemplate.getForObject(eligibilityUrl, Double.class);
            if(unitPrice == -1){
                throw new Exception();
            }
        }catch (Exception e){
            return "the product " + name + "is not avalilable for the amount:" + amount;
        }

        //step 2: call account to reduce that total cost
        try{
            String costUrl = "http://ACCOUNT-SERVICE/payment/" + unitPrice * amount;
            ResponseEntity<String> response = restTemplate.exchange(costUrl, HttpMethod.PUT, null, String.class);
            if (!"SUCCESS".equalsIgnoreCase(response.getBody())){
                return "error processing the payment! Please try again later";
            }
        }catch (Exception e){
            return "error processing the payment! Please try again later";
        }

        //Step3
        //place an order
        Order order = new Order();
        order.setProductName(name);
        order.setProductAmount(amount);
        //call kafka to product service to reduce inventory
        producer.sendProductInventoryReduction(order);

        //Step 4: call kafka to account service to update order history
        order.setProductUnitPrice(unitPrice);
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        order.setOrderDate(timeStamp);
        order.setTaxRate(TAX_RATE);
        order.setTotalPrice(unitPrice * amount * (1 + TAX_RATE));
        order.setOrderId(timeStamp.toString());

        producer.sendAccountOrderHistory(order);
        return "Thanks for your Order ";
    }

}
