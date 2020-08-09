package com.fh.shop.recipient.biz;

import com.fh.shop.recipient.po.Recipient;

import java.util.List;

public interface RecipientService{

    List<Recipient> findList(Long memberId);
}
