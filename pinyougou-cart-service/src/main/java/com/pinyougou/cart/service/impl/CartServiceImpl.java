package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现类
 *
 * @author fiveqiao
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品 SKU ID 查询 SKU 商品信息
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        if (tbItem == null) {
            throw new RuntimeException("");
        }
        if (!"1".equals(tbItem.getStatus())) {
            throw new RuntimeException("商品状态不合法");
        }
        //2.获取商家 ID
        String sellerId = tbItem.getSellerId(); //商家ID
        //3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart == null) {//4.如果购物车列表中不存在该商家的购物车
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);//商家ID
            cart.setSellerName(tbItem.getSeller());//商家名称
            //创建购物车明细列表
            List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();
            TbOrderItem orderItem = createOrderItem(tbItem, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        } else {  //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem tbOrderItem = searchOrderItemByItemId(orderItemList, itemId);
            if (tbOrderItem == null) {//5.1. 如果没有，新增购物车明细
                tbOrderItem = createOrderItem(tbItem, num);
                orderItemList.add(tbOrderItem);
            } else {//5.2. 如果有，在原购物车明细上添加数量，更改金额
                //如果存在更新数量
                Integer newNum = num - tbOrderItem.getNum();
                tbOrderItem.setNum(tbOrderItem.getNum() + newNum);
                //更新金额
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getPrice().doubleValue() * tbOrderItem.getNum()));
                //当明细小于等于0，移除该明细
                if (tbOrderItem.getNum() <= 0) {
                    orderItemList.remove(tbOrderItem);
                }
                //如果移除后 cart 的明细数量为 0，则将 cart 移除
                if (orderItemList.size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 根据商家 ID 查询购物车对象
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 创建购物车明细对象
     *
     * @param tbItem
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem tbItem, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量不合法");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(tbItem.getGoodsId());//SPU_ID
        orderItem.setItemId(tbItem.getId()); //商品id
        orderItem.setNum(num);//商品购买数量
        orderItem.setTitle(tbItem.getTitle());//商品标题
        orderItem.setPrice(tbItem.getPrice());//商品单价
        orderItem.setTotalFee(new BigDecimal(tbItem.getPrice().doubleValue() * num));//商品总金额
        orderItem.setPicPath(tbItem.getImage());//商品图片地址
        orderItem.setSellerId(tbItem.getSellerId());
        return orderItem;
    }

    /**
     * 根据商品明细 ID 查询
     * 根据skuId在购物车明细列表中查询购物车明细对象
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem tbOrderItem : orderItemList) {
            if (itemId.equals(tbOrderItem.getItemId())) {
                return tbOrderItem;
            }
        }
        return null;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从 redis 中提取购物车数据....." + username);
        String cartListString = (String) redisTemplate.boundHashOps("cartList").get(username);
        List<Cart> cartList = JSON.parseArray(cartListString, Cart.class);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向 redis 存入购物车数据....." + username);
        redisTemplate.boundHashOps("cartList").put(username, JSON.toJSONString(cartList));
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }
}
