package com.lfey.lfenuserservice.rabbit;

import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.entity.UserVerification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisherService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${fanout.user_fanout}")
    private String fanout;

    @Value("${queue.notification}")
    private String notification_code;

    @Autowired
    public UserEventPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publisherUserEvent(User user, String header) {
        rabbitTemplate.convertAndSend(fanout, "", user,message -> {
            message.getMessageProperties().setHeader("USER_ACTION", header);
            return message;
                }
        );
    }

    public void publisherCondeEvent(UserVerification codeDTO) {
        rabbitTemplate.convertAndSend(notification_code, codeDTO);
    }
}
