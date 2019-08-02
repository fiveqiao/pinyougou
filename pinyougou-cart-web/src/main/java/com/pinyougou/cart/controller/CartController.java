package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(name);
        //首先读取cooke中的值
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || "".equals(cartListString)) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if ("anonymousUser".equals(name)) {
            //读取本地购物车//
            return cartList_cookie;
        } else {
            //如果已登录则从redis合并cookie
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
            if (cartList_cookie.size() > 0) {
                List<Cart> mergeCartList = cartService.mergeCartList(cartList_redis, cartList_cookie);
                cartService.saveCartListToRedis(name, mergeCartList);
                CookieUtil.deleteCookie(request, response, "cartList");
                return mergeCartList;
            }
            return cartList_redis;
        }
    }

    /**
     * 添加商品到购物车
     *
     * @param itemId
     * @param num
     * @return
     */
    @CrossOrigin(origins = "http://localhost:8089", allowCredentials = "true")
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //4.12版本hou也可以使用注解
/*        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8089");
        response.setHeader("Access-Control-Allow-Credentials", "true");*/
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(name);
        //获取购物车列表
        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if ("anonymousUser".equals(name)) {
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
                System.out.println("向 cookie 存入数据");
            } else {
                //如果是已登录，保存到 redis
                cartService.saveCartListToRedis(name, cartList);
            }
            return new Result(true, "添加成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败！");
        }
    }
}
