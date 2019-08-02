package com.pinyougou.seckill.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillGoods> findAll() {
        return seckillGoodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillGoods seckillGoods) {
        seckillGoodsMapper.insert(seckillGoods);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillGoods seckillGoods) {
        seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillGoods findOne(Long id) {
        return seckillGoodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillGoodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        Criteria criteria = example.createCriteria();

        if (seckillGoods != null) {
            if (seckillGoods.getTitle() != null && seckillGoods.getTitle().length() > 0) {
                criteria.andTitleLike("%" + seckillGoods.getTitle() + "%");
            }
            if (seckillGoods.getSmallPic() != null && seckillGoods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + seckillGoods.getSmallPic() + "%");
            }
            if (seckillGoods.getSellerId() != null && seckillGoods.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillGoods.getSellerId() + "%");
            }
            if (seckillGoods.getStatus() != null && seckillGoods.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillGoods.getStatus() + "%");
            }
            if (seckillGoods.getIntroduction() != null && seckillGoods.getIntroduction().length() > 0) {
                criteria.andIntroductionLike("%" + seckillGoods.getIntroduction() + "%");
            }

        }

        Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 返回当前正在参与秒杀的商品
     *
     * @return
     */
    @Override
    public List<TbSeckillGoods> findList() {
        //获取秒杀商品列表
        List<String> seckillGoodsListString = redisTemplate.boundHashOps("seckillGoods").values();
        List<TbSeckillGoods> seckillGoodsList = new ArrayList<>();
        for (String seckillGoodsString : seckillGoodsListString) {
            seckillGoodsList.add(JSON.parseObject(seckillGoodsString,TbSeckillGoods.class));
        }
        if (seckillGoodsList == null || seckillGoodsList.size() == 0) {
            TbSeckillGoodsExample example = new TbSeckillGoodsExample();
            Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//审核通过
            criteria.andStockCountGreaterThan(0);//剩余库存大于 0
            criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
            criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
            seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            //将商品列表装入缓存
            System.out.println("将秒杀商品列表装入缓存");
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId().toString(), JSON.toJSONString(seckillGoods));
            }
        }
        return seckillGoodsList;
    }

    /**
     * 根据 ID 获取实体(从缓存中读取)
     * @param id
     * @return
     */
    @Override
    public TbSeckillGoods findOneFromRedis(Long id) {
        String  seckillGoodsString = (String) redisTemplate.boundHashOps("seckillGoods").get(id.toString());
        return JSON.parseObject(seckillGoodsString,TbSeckillGoods.class);
    }


}
