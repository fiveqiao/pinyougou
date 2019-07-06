app.controller("baseController", function ($scope) {
    /**
     * 分页控件配置
     * currentPage: 当前页
     * totalItems: 总记录数,
     * itemsPerPage: 每页记录数,
     * perPageOptions: 分页选项,
     * onChange: 当页码变更后自动触发的方法
     */
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
        //切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

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
});