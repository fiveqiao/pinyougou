package com.pinyougou.page.service;

public interface ItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId
     * @return
     */
    boolean genItemHtml(Long goodsId);
}
