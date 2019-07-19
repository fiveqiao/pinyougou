package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    public Map<String, Object> search(Map searchMap);

    /**
     * 导入数据
     * @param itemList
     */
    void importList(List itemList);

    /**
     * 根据商品sku ID 删除索引库的数据
     * @param goodsId
     */
    void deleteByGoodsIds(List goodsId);
}
