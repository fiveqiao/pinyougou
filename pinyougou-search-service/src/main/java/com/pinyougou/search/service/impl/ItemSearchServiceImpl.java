package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 50000) //超时时间
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String, Object> map = new HashMap<>();
        //空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replaceAll(" ", ""));
/*        Query query = new SimpleQuery();
        //添加查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows",page.getContent());*/

        //1.查询列表
        map.putAll(searchList(searchMap));
        //2.分组查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        //3.查询品牌和规格列表
        String category = (String) searchMap.get("category");
        //如果有模板，就根据选择的模板去查找品牌和规格
        if (!"".equals(category)) {
            map.putAll(searchBrandAndSpecList(category));
        } else {
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    /**
     * 查询分类列表
     *
     * @param searchMap
     * @return
     */
    public List<String> searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<String>();
        //创建query对象
        Query query = new SimpleQuery("*:*");
        //设置查询条件
        //查询solr中自定义的复制域，匹配传过来的关键字
        if (searchMap.get("keywords") != null && !"".equals(searchMap.get("keywords"))) {
            Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));  //where
            query.addCriteria(criteria);
        }

        //构建分组选项对象
        GroupOptions groupOptions = new GroupOptions();
        //设置要进行分组的域名，可以设置多个，在后面接.addField("")即可
        groupOptions.addGroupByField("item_category");  //group by
        //给query设置分组选项
        query.setGroupOptions(groupOptions);
        //分组查询，获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据域名，获取分页结果对象（以下得到的数据，都是针对于这里设置的域的结果）
        //这个域名，一定要是在上面进行分组过的域名
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //从分组入口页groupEntries中获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        //从entryList中取出每一个分组结果
        for (GroupEntry<TbItem> entry : entryList) {
            String groupValue = entry.getGroupValue();  //将分组的结果添加到集合中
            list.add(groupValue);
        }
        return list;
    }

    /**
     * 查询品牌和规格列表
     *
     * @param categroy
     * @return
     */
    public Map<String, Object> searchBrandAndSpecList(String categroy) {
        Map<String, Object> map = new HashMap<String, Object>();
        //获取模板 ID
        String typeId = (String) redisTemplate.boundHashOps("itemCat").get(categroy);

        if (typeId != null) {
            //根据模板 ID 查询品牌列表
            String brandListString = (String) redisTemplate.boundHashOps("brandList").get(typeId);
            List<Map> brandList = JSON.parseArray(brandListString, Map.class);
            //返回值添加品牌列表
            map.put("brandList", brandList);
            //根据模板 ID 查询规格列表
            String specListString = (String) redisTemplate.boundHashOps("specList").get(typeId);
            List<Map> specList = JSON.parseArray(specListString, Map.class);
            map.put("specList", specList);
        }
        return map;
    }

    //查询列表抽离方法
    public Map<String, Object> searchList(Map searchMap) {
        Map<String, Object> map = new HashMap<>();

        //查询高亮显示
        HighlightQuery query = new SimpleHighlightQuery();
        //构建高亮选项对象,封装高亮条件
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");  //高亮域，可以add多个高亮域
        highlightOptions.setSimplePrefix("<em style='color: red'>");//设置前缀
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);//为查询对象设置高亮选项
        //1.1关键字查询
        if (searchMap.get("keywords") != null && !"".equals(searchMap.get("keywords"))) {
            Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
            query.addCriteria(criteria);
        }
        //1.2按商品分类过滤
        if (!"".equals(searchMap.get("category"))) {  //如果用户选择了分类
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3 按品牌筛选
        if (!"".equals(searchMap.get("brand"))) {  //如果用户选择了品牌
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4 过滤规格
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                //添加动态域条件
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.5过滤价格
        if (!"".equals(searchMap.get("price"))) {
            String[] price = searchMap.get("price").toString().split("-");
            if (!"0".equals(price[0])) {//如果区间起点
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

            //如果区间终点不等于*
            if (!"*".equals(price[1])) {
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

            //第二种方法
            /*
            String[] split = searchMap.get("price").toString().split("-");
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria itemPrice = new Criteria("item_price");
            //如果有* 语法是不支持的
            if (!split[1].equals("*")) {
                itemPrice.between(split[0], split[1], true, true);
            } else {
                itemPrice.greaterThanEqual(split[0]);
            }
            filterQuery.addCriteria(itemPrice);
            query.addFilterQuery(filterQuery);*/
        }

        //1.6 分页查询
        //提取页码
        Integer pageNum = (Integer) searchMap.get("pageNum");
        if (pageNum == null) {
            //如果没有传递，默认为第一页
            pageNum = 1;
        }
        //每页记录数
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null) {
            //默认为20
            pageSize = 20;
        }
        //从第几天记录开始查
        query.setOffset((pageNum - 1) * pageSize);
        //查询多少条
        query.setRows(pageSize);

        //1.7排序
        String sortValue = (String) searchMap.get("sort"); //ASC  DESC
        String sortField = (String) searchMap.get("sortField");  //排序字段
        //判断是否为空
        if (sortValue != null && !"".equals(sortValue)) {
            if ("ASC".equals(sortValue)) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if ("DESC".equals(sortValue)) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }


        //***********************   获取高亮结果集   *******************
        //高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //高亮入口集合（可以设置多个高亮域，每个域对应一个高亮入口）
        List<HighlightEntry<TbItem>> entityList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entityList) {
            //获取高亮列表，获取所有高亮（高亮域的个数）
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();

            //这里明确知道只有一个域，并且只有一个列值，所以下面可以注释掉
            /*
            for (HighlightEntry.Highlight highlight : highlightList) {
                //每个域有可能存储多个值
                List<String> snipplets = highlight.getSnipplets();
            }
            */
            //先做判断是否为空
            if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
                TbItem entity = entry.getEntity();
                entity.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());
        //返回总页数
        map.put("totalPages", page.getTotalPages());
        //返回总记录数
        map.put("total", page.getTotalElements());
        return map;
    }

    /**
     * 导入数据
     * @param itemList
     */
    @Override
    public void importList(List itemList) {
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    /**
     * 根据SKU商品品删除索引库中的数据
     * @param goodsId
     */
    @Override
    public void deleteByGoodsIds(List goodsId) {
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_goodsid").in(goodsId);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
