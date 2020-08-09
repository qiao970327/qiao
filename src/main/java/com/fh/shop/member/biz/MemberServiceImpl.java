package com.fh.shop.member.biz;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.member.mapper.MemberMapper;
import com.fh.shop.member.po.Member;
import com.fh.shop.member.vo.MemberVo;
import com.fh.shop.rabbitmq.MQsender;
import com.fh.shop.rabbitmq.MailMessage;
import com.fh.shop.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class MemberServiceImpl implements MemberService {
    @Autowired
    MemberMapper memberMapper;
    @Autowired
    MailUtil mailUtil;
    @Autowired
    MQsender mqSender;

    @Override
    public ServerResponse add(Member member) throws Exception {

        String memberName = member.getMemberName();
        String password = member.getPassword();
        String phone = member.getPhone();
        String mail = member.getMail();
        if (StringUtils.isEmpty(memberName) || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(mail)
                || StringUtils.isEmpty(phone)) {
            return ServerResponse.error(ResponseEnum.REG_Member_Is_NULL);

        }
        QueryWrapper<Member> memberWapper = new QueryWrapper<>();
        memberWapper.eq("memberName", memberName);
        Member member1 = memberMapper.selectOne(memberWapper);


        if (member1 !=null) {
            return ServerResponse.error(ResponseEnum.REG_MemberName_Exist);
        }
        QueryWrapper<Member> mailWapper = new QueryWrapper<>();
        mailWapper.eq("mail", mail);
        Member member2 = memberMapper.selectOne(mailWapper);


        if (member2 !=null) {
            return ServerResponse.error(ResponseEnum.REG_MemberMail_Exist);
        }
        QueryWrapper<Member> phoneWapper = new QueryWrapper<>();
        phoneWapper.eq("phone", phone);
        Member member3 = memberMapper.selectOne(phoneWapper);


        if (member3 !=null) {
            return ServerResponse.error(ResponseEnum.REG_MemberPhone_Exist);
        }


        memberMapper.add(member);
        return ServerResponse.success();
    }
    @Override
    public ServerResponse validaterMemName(String memberName) {
        if(StringUtils.isEmpty(memberName)){
            return ServerResponse.error(ResponseEnum.REG_Member_Is_NULL);
        }
        QueryWrapper<Member> objectQueryWrapper = new QueryWrapper();
        objectQueryWrapper.eq("memberName",memberName);
        Member member1 = memberMapper.selectOne(objectQueryWrapper);
        if(member1!=null){
            return ServerResponse.error(ResponseEnum.REG_MemberName_Exist);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse validaterEmail(String mail) {
        if(StringUtils.isEmpty(mail)){
            return ServerResponse.error(ResponseEnum.REG_Member_Is_NULL);
        }
        QueryWrapper<Member> objectQueryWrapper1 = new QueryWrapper();
        objectQueryWrapper1.eq("mail",mail);
        Member member2 = memberMapper.selectOne(objectQueryWrapper1);
        if(member2!=null){
            return ServerResponse.error(ResponseEnum.REG_MemberMail_Exist);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse validaterPhone(String phone) {
        if(StringUtils.isEmpty(phone)){
            return ServerResponse.error(ResponseEnum.REG_Member_Is_NULL);
        }
        QueryWrapper<Member> objectQueryWrapper2 = new QueryWrapper();
        objectQueryWrapper2.eq("phone",phone);
        Member member3 = memberMapper.selectOne(objectQueryWrapper2);
        if(member3!=null){
            return ServerResponse.error(ResponseEnum.REG_MemberPhone_Exist);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse login(String userName, String password) {
        //判断用户信息是否为空
        if(StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)){
            return ServerResponse.error(ResponseEnum.LOGIN_INFO_IS_NULL);
        }
        //通过用户名去查找登陆用户的相关信息
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("memberName",userName);
        Member member = memberMapper.selectOne(queryWrapper);
        if(member == null){
            return ServerResponse.error(ResponseEnum.LOGIN_MEMBER_NAME_IS_NOT_EXIT);
        }
        //判断用户密码是否正确
        if(!password.equals(member.getPassword())){
            return ServerResponse.error(ResponseEnum.LOGIN_PASSWORD_IS_ERROR);
        }
        // ===============生成token================
        // 模拟JWT[json web token]
        // 生成token样子类似于xxx.yyy 用户信息.对用户信息的签名
        // 签名的目的：保证用户信息不被篡改
        // 怎么生成签名：md5(用户信息 结合 密钥)
        // sign代表签名 secret/secretKey代表密钥
        // 密钥是在服务端保存的 黑客 攻击者 他们获取不到
        // =======================================
        // 生成用户信息对应的json
        MemberVo memberVo = new MemberVo();
        Long memberId = member.getId();
        memberVo.setId(memberId);
        memberVo.setMemberName(member.getMemberName());
        memberVo.setRealName(member.getRealName());
        String uuid = UUID.randomUUID().toString();
        memberVo.setUuid(uuid);
        // 转换Java对象到Json
        String memberJson = JSONObject.toJSONString(memberVo);
        // 对用户信息进行base64编码[]
        try {
            String memberJsonBase64 = Base64.getEncoder().encodeToString(memberJson.getBytes("UTF-8"));
            // 生成用户信息所对应的签名
            String sign = Md5Util.sign(memberJsonBase64, Md5Util.SECRET);
            // 对签名也进行Base64编码
            String signBase64 = Base64.getEncoder().encodeToString(sign.getBytes("UTF-8"));

            // 处理超时
            RedisUtil.setEX(KeyUtil.buildMemberKey(uuid,memberId) ,"A",KeyUtil.MEMBER_KEY_EXPIRE);
            // 登陆成功后 给用户邮箱发送邮件
            MailMessage mailMessage = new MailMessage();
            mailMessage.setTo(member.getMail());
            mailMessage.setRealName(member.getRealName());
            mailMessage.setSubject("恭喜"+member.getRealName()+"登陆成功");
            mailMessage.setContent("尊敬的会员"+member.getRealName()+"在"+DateUtil.date2str(new Date(),DateUtil.S_F_M)+"登陆成功！！！");
            String mail = JSONObject.toJSONString(mailMessage);
            // 交由生产者进行生成消息
            mqSender.sendMailMessage(mail);
            // 响应数据给客户端
            return ServerResponse.success(memberJsonBase64+"."+signBase64);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // throw new RuntimeException(e);
            return ServerResponse.error();
        }


    }


}
