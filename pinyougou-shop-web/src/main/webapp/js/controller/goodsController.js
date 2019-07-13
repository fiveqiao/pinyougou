//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

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
        $scope.entity.goodsDesc.introduction = editor.html();
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert("保存成功！");
                    location.href = "goods.html";
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //增加
    /*    $scope.add = function () {
            $scope.entity.goodsDesc.introduction = editor.html();
            goodsService.add($scope.entity).success(
                function (response) {
                    if (response.success) {
                        alert("新增成功！")
                        $scope.entity = {};
                        //清空富文本编辑器
                        editor.html("");
                    } else {
                        alert(response.message);
                    }
                }
            );
        }*/
    /*
        //新增
        $scope.add=function(){
            goodsService.add( $scope.entity  ).success(
                function(response){
                    if(response.success){
                        //如果注册成功，跳转登录页面
                       location.href="shoplogin.html";
                    }else{
                        alert(response.message);
                    }
                }
            );
        }*/


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
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
    //上传图片
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    $scope.image_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }
        )
    }
    //定义页面实体结构
    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};

    //将当前上传的图片实体存入图片列表
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //查询一级商品分类列表
    $scope.selectItemCatList = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
                $scope.entity.goods.typeTemplateId = null;
            }, true
        )
    }

    //查询二级商品分类列表
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        if (newValue == null) {
            return;
        }
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
                $scope.itemCat3List = [];
                $scope.entity.goods.typeTemplateId = null;
                $scope.typeTemplate.brandIds = [{}];
                $scope.entity.goodsDesc.specificationItems = [];
                //列表初始化
                $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: "0", isDefault: "0"}];
            }, true
        )
    })

    //查询三级商品分类列表
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        if (newValue == null) {
            return;
        }
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
                $scope.entity.goods.typeTemplateId = null;
                $scope.typeTemplate.brandIds = [{}];
                $scope.entity.goodsDesc.specificationItems = [];
                //列表初始化
                $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: "0", isDefault: "0"}];
            }, true
        )
    })

    //读取模板id
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        if (newValue == null) {
            return;
        }
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        )
    }, true)

    //读取模板id后，读取对应的品牌列表 扩展属性，规格列表
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
        if (newValue == null) {
            return;
        }
        typeTemplateService.findOne(newValue).success(
            function (response) {
                //模板对象
                $scope.typeTemplate = response;
                //品牌列表对象类型转换
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                //扩展属性
                if ($location.search()["id"] == null) {//如果是增加商品
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                }
            }
        );
        $scope.specList = [];
        //读取规格
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList = response;
            }
        );
    }, true);

    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = $scope.searchObjectBykey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
        if (object != null) {
            if ($event.target.checked) {
                //push向集合中添加元素
                object.attributeValue.push(value);
            } else {
                //获取值对应的索引位置
                var idx = object.attributeValue.indexOf(value);
                //取消勾选，删除集合中对应的值
                object.attributeValue.splice(idx, 1);
                //如果选项都取消了，将此条记录移除
                if (object.attributeValue.length == 0) {
                    //$scope.entity.goodsDesc.specificationItems.indexOf(object)获取此条记录的索引位置
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }

        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }
    }

    //列表初始化
    $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: "0", isDefault: "0"}];

    //创建SKU列表
    $scope.createItemList = function () {
        //列表初始化
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: "0", isDefault: "0"}];

        //将这个对象赋给item
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }

    //定义一个局部方法，因为只有内部使用
    addColumn = function (list, columnName, columnValues) {
        var newList = [];
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnValues.length; j++) {
                //深克隆
                var newRow = JSON.parse(JSON.stringify(oldRow));
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    $scope.status = ["未审核", "已审核", "审核未通过", "已关闭"];

    $scope.isMarketable = ["未上架","已上架"];

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

    $scope.checkAttributeValue = function (specName, optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectBykey(items, "attributeName", specName);
        if (object != null) {
            if (object.attributeValue.indexOf(optionName) >= 0) {  //如果能够查询到规格选项
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    $scope.$watch("searchEntity.auditStatus", function (newValue, oldValue) {
        $scope.reloadList();
    },true);

    $scope.$watch("searchEntity.isMarketable", function (newValue, oldValue) {
        $scope.reloadList();
    },true)


    //修改上架状态
    $scope.updateIsMarketable = function (isMarketable) {
        if ($scope.selectIds.length > 0) {
            if (confirm("确定此操作吗？")) {
                goodsService.updateIsMarketable($scope.selectIds, isMarketable).success(
                    function (response) {
                        if (response.success) {
                            alert(response.message);
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
