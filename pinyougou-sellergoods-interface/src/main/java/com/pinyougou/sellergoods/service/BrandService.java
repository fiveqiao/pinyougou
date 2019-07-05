package com.pinyougou.sellergoods.service;

import com.github.pagehelper.Page;
import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

/**
 * 品牌接口
 * @author fiveqiao
 */
public interface BrandService {
    List<TbBrand> findAll();

    /**
     * 品牌分页
     * @param pageNum 当前页面
     * @param pageSize  每页记录数
     * @return
     */
    PageResult<TbBrand> findPage(TbBrand brand,int pageNum,int pageSize);
//    Page<TbBrand> findPage(int pageNum, int pageSize);

    /**
     * 添加 brand
     * @param brand
     */
    void add(TbBrand brand);

    /**
     * 根据id查询实体
     * @param id
     * @return
     */
    TbBrand findOne(long id);

    /**
     *修改brand
     * @param brand
     */
    void update(TbBrand brand);

    /**
     * 删除
     * @param ids
     */
    void delete(Long[] ids);

}
