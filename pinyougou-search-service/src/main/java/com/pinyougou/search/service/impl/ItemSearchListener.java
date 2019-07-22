package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String itemListString = textMessage.getText();
            System.out.println("监听到消息:" + itemListString);
            List<TbItem> itemList = JSON.parseArray(itemListString, TbItem.class);
            for (TbItem item : itemList) {
                System.out.println(item.getId()+" "+item.getTitle());
                //将 spec 字段中的 json字符串转换为 map ,因为索引库中是动态域
                Map specMap = JSON.parseObject(item.getSpec(), Map.class);
                item.setSpecMap(specMap);//给带注解的字段赋值
            }
            itemSearchService.importList(itemList);
            System.out.println("导入到solr索引库");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
