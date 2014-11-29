/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loanbroker.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import loanbroker.dto.LoanResponse;

/**
 *
 * @author Kaboka
 */
public class Messaging {
    
    private static final String IN_QUEUE = "webservice"; //dummy name
    private static final String OUT_QUEUE = "enricher_creditScore";;
    private Channel channel;
    private QueueingConsumer consumer;        
   
    public Messaging(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("student");
        factory.setPassword("cph");
        factory.setHost("datdb.cphbusiness.dk");
        
        Connection connection;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(IN_QUEUE, false, false, false, null);
            channel.queueDeclare(OUT_QUEUE, false, false, false, null);
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(IN_QUEUE,false, consumer);
        } catch (IOException ex) {
            Logger.getLogger(Messaging.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public LoanResponse getLoanRequest(String message){
       LoanResponse response = null;
        try {
            publishMessage(message);
            response = consumeMessage();
        } catch (IOException ex) {
            Logger.getLogger(Messaging.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Messaging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    private void publishMessage(String message) throws IOException{
        channel.basicPublish("", OUT_QUEUE, null, message.getBytes());
    }
    
    private LoanResponse consumeMessage() throws InterruptedException, IOException{
        Delivery delivery = consumer.nextDelivery();
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        LoanResponse response = new LoanResponse();
        response.bankName = new String(delivery.getBody());
        return response;
    }
}
