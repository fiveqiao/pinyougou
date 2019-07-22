package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //初始状态为0，未审核
        goods.getGoods().setAuditStatus("0");
        //插入商品基本信息
        goodsMapper.insert(goods.getGoods());
        //给goodsDesc表的goodsId注入值
        //因为在mybatis文件中配置了返回主键，他会自动给goods的id属性注入值，将商品表的基本ID给商品扩展表
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //插入商品表扩展数据
        goodsDescMapper.insert(goods.getGoodsDesc());
        saveItemList(goods);

    }

    //插入sku列表数据
    private void saveItemList(Goods goods){
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            for (TbItem tbItem : goods.getItemList()) {
                //构建标题 SPU名称 + 规格选项值
                String title = goods.getGoods().getGoodsName();//SPU名称
                Map<String, Object> map = JSON.parseObject(tbItem.getSpec());
                for (String key : map.keySet()) {
                    title += " " + map.get(key);
                }
                tbItem.setTitle(title);
                setValues(goods, tbItem);
                itemMapper.insert(tbItem);
            }
        } else {
            //没有启用规格
            TbItem tbItem = new TbItem();
            //标题
            tbItem.setTitle(goods.getGoods().getGoodsName());
            //价格
            tbItem.setPrice(goods.getGoods().getPrice());
            //数量
            tbItem.setNum(999);
            //状态
            tbItem.setStatus("1");
            //默认
            tbItem.setIsDefault("1");
            //规格
            tbItem.setSpec("{}");
            setValues(goods, tbItem);
            itemMapper.insert(tbItem);
        }
    }

    private void setValues(Goods goods, TbItem tbItem) {
        //商品分类
        tbItem.setCategoryid(goods.getGoods().getCategory3Id()); //三级分类
        //添加日期
        tbItem.setCreateTime(new Date());
        //修改日期
        tbItem.setUpdateTime(new Date());
        //商品ID
        tbItem.setGoodsId(goods.getGoods().getId());
        //商家ID
        tbItem.setSellerId(goods.getGoods().getSellerId());
        //分类名称
        TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        tbItem.setCategory(tbItemCat.getName());
        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        tbItem.setBrand(brand.getName());
        //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        tbItem.setSeller(seller.getNickName());
        //图片地址（取SPU的第一个图片）
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList != null && imageList.size() > 0) {
            tbItem.setImage((String) imageList.get(0).get("url"));
        }
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //跟新基本表数据
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //更新扩展数据
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //先删除原有SKU列表数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(example);
        //插入SKU列表数据
        saveItemList(goods);

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        //商品基本表
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        //商品扩展
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(tbGoods.getId());
        goods.setGoodsDesc(tbGoodsDesc);
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(tbGoods.getId());
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        goods.setItemList(tbItems);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            //物理删除，把该字段上的字改为1
            tbGoods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();
        //指定条件为未逻辑删除
//        criteria.andIsDeleteIsNull();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
//                criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            //首先根据id查询对应的商品信息表
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            //修改状态
            tbGoods.setAuditStatus(status);
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(tbGoods.getId());
            List<TbItem> tbItems = itemMapper.selectByExample(example);
            if(tbItems.size() > 0){
                for (TbItem tbItem : tbItems) {
                    tbItem.setStatus(status);
                    itemMapper.updateByPrimaryKey(tbItem);
                }
            }
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    @Override
    public void updateIsMarketable(Long[] ids, String isMarketable) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsMarketable(isMarketable);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    @Override
    public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsId, String status) {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo(status);
        //数组转集合
        criteria.andGoodsIdIn(Arrays.asList(goodsId));
        return itemMapper.selectByExample(example);
    }
}
