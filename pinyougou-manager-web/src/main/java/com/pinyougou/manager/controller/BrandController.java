package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    //使用随机某一个服务挂了还可以正常引用，不会报错
    @Reference(loadbalance = "random")
    private BrandService brandService;

    @RequestMapping("/findAll")
//    public List<TbBrand> findAll() {
    public PageInfo<TbBrand> findAll() {
//        List<TbBrand> tbBrandList = brandService.findAll();
        PageInfo<TbBrand> pageInfo = brandService.findAll();
        return pageInfo;
    }

    @RequestMapping("/findPage")  //@RequestBody(required = false)括号内的作用是可以不传入对象
    public PageResult<TbBrand> findPage(@RequestBody(required = false) TbBrand brand,int page, int size) {
//    public Page<TbBrand> findPage(int page, int size) {
        PageResult<TbBrand> pageResult = brandService.findPage(brand,page, size);
//        Page<TbBrand> pageResult = brandService.findPage(page, size);
        return pageResult;
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand) {
        try {
            brandService.add(brand);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
@RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
    @RequestMapping("/search") //@RequestBody(required = false)括号内的作用是可以不传入对象
    public PageResult<TbBrand> search(@RequestBody(required = false) TbBrand brand, int page, int size) {
//    public Page<TbBrand> findPage(int page, int size) {
        PageResult<TbBrand> pageResult = brandService.findPage(brand,page, size);
//        Page<TbBrand> pageResult = brandService.findPage(page, size);
        return pageResult;
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
