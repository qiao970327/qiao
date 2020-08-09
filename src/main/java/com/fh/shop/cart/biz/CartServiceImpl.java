package com.fh.shop.cart.biz;

import com.alibaba.fastjson.JSONObject;
import com.fh.shop.cart.po.Cart;
import com.fh.shop.cart.po.CartItem;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.common.SystemConstant;
import com.fh.shop.product.mapper.ProductMapper;
import com.fh.shop.product.po.Product;
import com.fh.shop.utils.BigDecimalUtil;
import com.fh.shop.utils.KeyUtil;
import com.fh.shop.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

@Service("cartService")
public class CartServiceImpl implements CartService {


    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse addCart(Long memberId, Long goodsId, int num) {
        // 判断商品是否存在
        Product product = productMapper.selectById(goodsId);
        if(product == null){
            return ServerResponse.error(ResponseEnum.CART_PRODUCT_IS_NULL);
        }
        // 商品的状态是否正常上架中
        if(product.getStatus() == SystemConstant.PRODUCT_IS_DOWN){
            return ServerResponse.error(ResponseEnum.CART_PRODUCT_IS_DOWN);
        }
        // 如果会员已经有了对应的购物车
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        if(StringUtils.isNotEmpty(cartJson)){
            // 直接往购物车放入商品
            //          如果商品不存在
            Cart cart = JSONObject.parseObject(cartJson, Cart.class);
            List<CartItem> cartItemList = cart.getCartItemList();
            CartItem cartItem = null;
            for (CartItem item : cartItemList) {
                if(item.getGoodsId().longValue() == goodsId.longValue()){
                    cartItem = item;
                    break;
                }
            }
            if(cartItem != null){
                // 如果商品存在
                cartItem.setNum(cartItem.getNum()+num);
                // 从新获取cartItem中的商品数量便于计算小计
                Integer num1 = cartItem.getNum();
                // 如果此时商品为负数 或者0 则删除该条商品
                if(num1 <= 0){
                    // 删除整个商品,从cartItemList集合中删除整个商品
                    cartItemList.remove(cartItem);
                }else {
                    // 计算商品的小计
                    BigDecimal subPrice = BigDecimalUtil.mul(num1 + "", cartItem.getPrice().toString());
                    cartItem.setSubPrice(subPrice);
                }
                // 更新购物车
                updateCart(memberId, cart);
            }else {
                // 如果商品不存在添加商品，更新购物车[总个数，总计]
                // 判断传递的商品数量格式是否正确 不为0,不为负数
                if(num <= 0){
                    return ServerResponse.error(ResponseEnum.CART_NUM_IS_ERROR);
                }
                // 构建商品
                CartItem cartItemInfo = buildCartItem(num, product);
                // 加入购物车
                cart.getCartItemList().add(cartItemInfo);
                // 更新购物车
                updateCart(memberId, cart);
            }
        }else {
            // 判断传递的商品数量格式是否正确 不为0,不为负数
            if(num <= 0){
                return ServerResponse.error(ResponseEnum.CART_NUM_IS_ERROR);
            }
            // 如果会员没有对应的购物车：
            // 创建购物车：
            Cart cart = new Cart();
            // 构建商品
            CartItem cartItemInfo = buildCartItem(num, product);
            // 加入购物车
            cart.getCartItemList().add(cartItemInfo);
            updateCart(memberId, cart);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse findItemList(Long memberId) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        List<CartItem> cartItemList = cart.getCartItemList();
        // 遍历购物车中的商品集合，计算总价格和总个数
        int totalCount = 0;
        BigDecimal totalPrice = new BigDecimal("0");
        for (CartItem item : cartItemList){
            // 如果当前遍历的商品被选中
            if(item.getChecked()){
                totalCount += item.getNum();
                totalPrice = BigDecimalUtil.add(totalPrice.toString(),item.getSubPrice().toString());
            }
        }
        cart.setTotalCount(totalCount);
        cart.setTotalPrice(totalPrice);
        cart.setCartItemList(cartItemList);
        return ServerResponse.success(cart);
    }

    @Override
    public ServerResponse getCartTotalCount(Long memberId) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        if(cart == null){
            return ServerResponse.success(0);
        }
        int totalCount = cart.getTotalCount();
        return ServerResponse.success(totalCount);
    }

    @Override
    // 修改选中商品的状态
    public ServerResponse changeCartItemCheckedStatus(Long goodsId, Long memberId) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        // 获取直接取到的总件数
        int oldTotalCount = cart.getTotalCount();
        // 获取直接取到的总金额
        BigDecimal oldTotalPrice = cart.getTotalPrice();
        List<CartItem> cartItemList = cart.getCartItemList();
        CartItem cartItem = null;
        int count = 0;
        // 找到要更改复选框的商品
        for (CartItem item : cartItemList){
            if(item.getGoodsId().longValue() == goodsId.longValue()){
                cartItem = item;
                break;
            }
            count ++;
        }
        if(cartItem != null){
            // 对选中的checked 进行取反修改
            cartItem.setGoodsId(cartItem.getGoodsId());
            cartItem.setGoodsName(cartItem.getGoodsName());
            cartItem.setPrice(cartItem.getPrice());
            cartItem.setNum(cartItem.getNum());
            int num1 = cartItem.getNum();
            cartItem.setImage(cartItem.getImage());
            BigDecimal subPrice = BigDecimalUtil.mul(num1+"",cartItem.getPrice().toString());
            cartItem.setSubPrice(subPrice);
            cartItem.setChecked(!cartItem.getChecked());
            cart.getCartItemList().set(count,cartItem);
            String cartNewJson = JSONObject.toJSONString(cart);
            RedisUtil.set(cartKey, cartNewJson);
        }
        // 如果这个商品不是选中状态就重新计算总计，总金额
        if(cartItem.getChecked() != true){
            // 总件数减去 未选中商品的件数 存放到redis中
            int num = cartItem.getNum();
            cart.setTotalCount(oldTotalCount-num);
            // 总金额减去 未选中商品的小计
            BigDecimal price = cartItem.getSubPrice();
            BigDecimal newTotalPrice =  BigDecimalUtil.sub(oldTotalPrice.toString(),price.toString());
            cart.setTotalPrice(newTotalPrice);
            // 最终往redis里更新
            String cartNewJson = JSONObject.toJSONString(cart);
            RedisUtil.set(cartKey, cartNewJson);
        }else {
            updateCart(memberId,cart);
        }
        return ServerResponse.success();
    }

    @Override
    // 修改选中所有商品的状态
    public ServerResponse changeAllCartItemCheckedStatus(Boolean checked, Long memberId) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        Cart cart = JSONObject.parseObject(cartJson,Cart.class);
        List<CartItem> cartItemList = cart.getCartItemList();
        for (CartItem item : cartItemList) {
            item.setChecked(checked);
        }
        cart.setTotalCount(cart.getTotalCount());
        cart.setTotalPrice(cart.getTotalPrice());
        // 最终往redis里更新
        String cartNewJson = JSONObject.toJSONString(cart);
        RedisUtil.set(cartKey, cartNewJson);
        return ServerResponse.success();
    }

    @Override
    public ServerResponse deleteCartItem(Long goodsId, Long memberId) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        Cart cart = JSONObject.parseObject(cartJson,Cart.class);
        List<CartItem> cartItemList = cart.getCartItemList();
        // 新建一个cartItem
        CartItem cartItem = null;
        for(CartItem item : cartItemList){
            if(item.getGoodsId().longValue() == goodsId.longValue()){
                cartItem = item;
                break;
            }
        }
        if(cartItem != null){
            // 删掉这个cartItem
            cartItemList.remove(cartItem);
            if(cartItemList != null){
                updateCart(memberId,cart);
            }else {
                RedisUtil.del(cartKey);
            }
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse batchDeleteCartItem(Long memberId) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        Cart cart = JSONObject.parseObject(cartJson,Cart.class);
        List<CartItem> cartItemList = cart.getCartItemList();
        Iterator<CartItem> cartItemIterator = cartItemList.iterator();
        // hashNext检查序列中是否还有元素
        while (cartItemIterator.hasNext()){
            // 获取集合下一个元素
            CartItem next = cartItemIterator.next();
            if(next.getChecked()){
                cartItemIterator.remove();
            }
        }
        if(cart != null){
            cart.setTotalCount(cart.getTotalCount());
            cart.setTotalPrice(cart.getTotalPrice());
            // 最终往redis里更新
            String cartNewJson = JSONObject.toJSONString(cart);
            RedisUtil.set(cartKey, cartNewJson);
        }else {
            RedisUtil.del(cartKey);
        }
        return ServerResponse.success();
    }


    // 封装构建商品
    private CartItem buildCartItem(int num, Product product) {
        // 构建商品
        CartItem cartItemInfo = new CartItem();
        cartItemInfo.setGoodsId(product.getId());
        cartItemInfo.setPrice(product.getPrice());
        cartItemInfo.setImage(product.getImage());
        cartItemInfo.setGoodsName(product.getProductName());
        cartItemInfo.setNum(num);
        BigDecimal subPrice = BigDecimalUtil.mul(num + "", product.getPrice().toString());
        cartItemInfo.setSubPrice(subPrice);
        cartItemInfo.setChecked(true);
        return cartItemInfo;
    }


    // 封装更新购物车
    private void updateCart(Long memberId, Cart cart) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        List<CartItem> cartItemList = cart.getCartItemList();
        if(cartItemList.size() == 0){
            // 删除整个购物车
            RedisUtil.del(cartKey);
            return;
        }
        int totalCount = 0;
        BigDecimal totalPrice = new BigDecimal(0);
        // 更新购物车
        for (CartItem item : cartItemList) {
            totalCount += item.getNum();
            totalPrice = BigDecimalUtil.add(totalPrice.toString(), item.getSubPrice().toString());
        }
        cart.setTotalCount(totalCount);
        cart.setTotalPrice(totalPrice);
        // 最终往redis里更新
        String cartNewJson = JSONObject.toJSONString(cart);
        RedisUtil.set(cartKey, cartNewJson);
    }

}
