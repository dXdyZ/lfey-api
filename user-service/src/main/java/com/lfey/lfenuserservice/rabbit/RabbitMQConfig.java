package com.lfey.lfenuserservice.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    //Имя Fanout Exchange
    @Value("${fanout.user_fanout}")
    private String FANOUT_EXCHANGE_NAME = "user_events_fanout";

    //Имя очередей
    @Value("${queue.messenger}")
    private String MESSENGER_QUEUE;

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE_NAME);
    }

    @Bean
    public Queue messengerQueue() {
        return new Queue(MESSENGER_QUEUE);
    }

    //Привязываем очередь к Fanout Exchange
    @Bean
    public Binding bindMessengerQueue(Queue messengerQueue, FanoutExchange fanoutExchange) {
        //Fanout Exchange не использует routing key, поэтому он игнорируется
        return BindingBuilder.bind(messengerQueue).to(fanoutExchange);
    }
}



