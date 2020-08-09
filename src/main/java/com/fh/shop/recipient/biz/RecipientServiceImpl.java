package com.fh.shop.recipient.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.recipient.mapper.RecipientMapper;
import com.fh.shop.recipient.po.Recipient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("recipientService")
public class RecipientServiceImpl implements RecipientService {


    @Autowired
    private RecipientMapper recipientMapper;

    @Override
    public List<Recipient> findList(Long memberId) {
        QueryWrapper recipientWrapper = new QueryWrapper();
        recipientWrapper.eq("memberId",memberId);
        List recipientList = recipientMapper.selectList(recipientWrapper);
        return recipientList;
    }
}
