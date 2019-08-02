//定义controller
app.controller("brandController", function ($scope, $controller, brandService) {

    //继承，其实这里是伪继承
    $controller("baseController",{$scope:$scope})

    //查询品牌列表
    $scope.findAll = function () {
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }
/*
    /!**
     * 分页控件配置
     * currentPage: 当前页
     * totalItems: 总记录数,
     * itemsPerPage: 每页记录数,
     * perPageOptions: 分页选项,
     * onChange: 当页码变更后自动触发的方法
     *!/
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            //重新加载
            $scope.reloadList();
        }
    };

    //刷新列表
    $scope.reloadList = function () {
        $scope.selectIds = [];
        //切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };
*/

//分页
    $scope.findPage = function (page, size) {
        brandService.findPage(page, size).success(
            function (PageResult) {
                //显示当前页数据
                $scope.list = PageResult.rows;
                //更新总记录数
                $scope.paginationConf.totalItems = PageResult.total;
            }
        )
    };
    //新增
    $scope.save = function () {
        var object = null;
        if ($scope.entity.id == null) {
            object = brandService.add($scope.entity);
        } else {
            object = brandService.update($scope.entity);
        }
        object.success(
            function (response) {
                if (response.success) {
                    //刷新
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            }
        )
    };

    //查询实体
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        )
    };
/*

    //用户勾选的ID集合
    $scope.selectIds = [];

    //用户点击勾选复选框
    $scope.updateSelection = function ($event, id) {
        //$event.target  获得触发事件的元素
        if ($event.target.checked) {
            //push向集合中添加元素
            $scope.selectIds.push(id);
        } else {
            //获取要删除的id下标,就是值的位置
            var idx = $scope.selectIds.indexOf(id);
            //参数1：idx:要删除的下标, 参数2：移除的个数
            $scope.selectIds.splice(idx, 1);
        }
    };
*/

    //删除选中
    $scope.dele = function () {
        if ($scope.selectIds.length > 0) {
            if (confirm("您确定要删除吗？")) {
                brandService.dele($scope.selectIds).success(
                    function (response) {
                        if (response.success) {
                            $scope.selectIds = [];
                            //刷新
                            $scope.reloadList();
                        } else {
                            alert(response.message);
                        }
                    }
                )
            }
        } else {
            alert("您还没有选择！");
        }
    };
    //先初始化$scope.searchEntity防止上面调用为空
    $scope.searchEntity = {};
    //条件查询
    $scope.search = function (page, size) {
        brandService.search(page, size, $scope.searchEntity).success(
            function (PageResult) {
                //显示当前页数据
                $scope.list = PageResult.rows;
                //更新总记录数
                $scope.paginationConf.totalItems = PageResult.total;
            }
        )
    }

});