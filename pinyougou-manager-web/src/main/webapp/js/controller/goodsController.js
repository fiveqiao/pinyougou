//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, itemCatService, brandService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        //获取参数值
        var id = $location.search()["id"];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //把富文本的值赋给editor，商品介绍
                editor.html($scope.entity.goodsDesc.introduction);
                //商品图片
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格选择
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                //转换sku列表中的规格对象
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    // alert($scope.entity.itemList[i].spec);
                    //弹出是什么类型，用于验证返回的是否JSON对象。和字符串
                    // alert(typeof $scope.entity.itemList[i].spec);
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        if ($scope.selectIds.length > 0) {
            if (confirm("您确定删除吗？")) {
                //获取选中的复选框
                goodsService.dele($scope.selectIds).success(
                    function (response) {
                        if (response.success) {
                            $scope.reloadList();//刷新列表
                            $scope.selectIds = [];
                        } else {
                            alert(response.message);
                        }
                    }
                );
            }
        } else {
            alert("您还没有选择！");
        }
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    $scope.status = ["未审核", "已审核", "审核未通过", "已关闭"];

    //查询商品分类列表
    $scope.itemCatList = [];
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    //数组索引为ID，值为name
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        )
    }

    //查询品牌分类列表
    $scope.brandList = [];
    $scope.findBrandList = function () {
        brandService.findAll().success(
            function (response) {
                for (var i = 0; i < response.list.length; i++) {
                    //数组索引为ID，值为name
                    $scope.brandList[response.list[i].id] = response.list[i].name;
                }
            }
        )
    }

    //图片链接
    $scope.imgurl = {};
    $scope.imgtupian = function (url) {
        $scope.imgurl = url;
    }

    //修改状态
    $scope.updateStatus = function (status) {
        if ($scope.selectIds.length > 0) {
            if (confirm("确定此操作吗？")) {
                goodsService.updateStatus($scope.selectIds, status).success(
                    function (response) {
                        if (response.success) {
                            $scope.reloadList();//刷新列表
                            $scope.selectIds = [];
                        } else {
                            alert(response.message);
                        }
                    })
            }
        } else {
            alert("您还没有选择！")
        }
    }

    $scope.checkAll = function ($event) {
        var idAll = document.getElementsByName("idAll");
        $scope.selectIds = [];
        for (i = 0; i < idAll.length; i++) {
            idAll[i].checked = $event.target.checked;
            $scope.selectIds.push(parseInt(idAll[i].value));
        }
        if (!$event.target.checked) {
            $scope.selectIds = [];
        }
    }
});	
