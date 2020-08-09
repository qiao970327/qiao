package com.fh.shop.rabbitmq;

import lombok.Data;

@Data
public class MailMessage {

    private String to;

    private String realName;

    private String content;

    private String subject;
}
