package com.fh.shop.cart.biz;

import com.fh.shop.common.ServerResponse;

public interface CartService {

    ServerResponse addCart(Long memberId, Long goodsId, int num);

    ServerResponse findItemList(Long memberId);

    ServerResponse getCartTotalCount(Long memberId);

    ServerResponse changeCartItemCheckedStatus(Long goodsId, Long memberId);

    ServerResponse changeAllCartItemCheckedStatus(Boolean checked, Long memberId);

    ServerResponse deleteCartItem(Long goodsId, Long memberId);

    ServerResponse batchDeleteCartItem(Long memberId);
}
