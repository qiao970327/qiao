package com.fh.shop.token.biz;

import com.fh.shop.common.ServerResponse;
import com.fh.shop.utils.RedisUtil;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("tokenService")
public class TokenServiceImpl implements TokenService {

    @Override
    public ServerResponse createToken() {
        // 生成token
        String token = UUID.randomUUID().toString();
        // 将token存入Redis
        RedisUtil.set(token,"1");
        // 相应给客户端
        return ServerResponse.success(token);
    }
}
