app.controller("searchController", function ($scope, $controller,$location, searchService) {
    $controller('baseController', {$scope: $scope});//继承
    //搜索
    $scope.search = function () {
        if ($scope.searchMap.keywords.length > 0) {
            searchService.search($scope.searchMap).success(
                function (response) {
                    $scope.resultMap = response; //搜索返回的结果
                    buildPageLabel();//调用分页
                }
            )
        }
    }

    //搜索对象
    $scope.searchMap = {
        "keywords": "",
        "category": "",
        "brand": "",
        "spec": {},
        "price": "",
        "pageNum": 1,
        "pageSize": 20,
        "sort": "",
        "sortField": ""
    };

    //添加搜索项
    $scope.addSearchItem = function (key, value) {
        if (key == "category" || key == "brand" || key == "price") { //如果点击的是分类或者是品牌
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        //执行查询
        $scope.search();
    }

    //删除搜索项
    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand" || key == "price") { //如果点击的是分类或者是品牌
            $scope.searchMap[key] = "";
        } else {//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        //执行查询
        $scope.search();
    }

    //构建分页标签(totalPages 为总页数)
    buildPageLabel = function () {
        //新增分页栏属性
        $scope.pageLabel = [];
        //得到最后页码，也就是总页数
        var maxPageNum = $scope.resultMap.totalPages;
        //开始页码
        var firstPage = 1;
        //截至页码
        var lastPage = maxPageNum;
        $scope.firstDot = true; //前面有点
        $scope.lastDot = true; //后面有点
        //如果总页数大于 5 页,显示部分页码
        if (maxPageNum > 5) {
            //如果当前页小于3
            if ($scope.searchMap.pageNum < 3) {
                lastPage = 5; //前 5 页
                $scope.firstDot = false;//前面没点
            } else if ($scope.searchMap.pageNum >= maxPageNum - 2) { //如果当前页大于等于最大页码-2
                firstPage = maxPageNum - 4;//后 5 页
                $scope.lastDot = false; //后面没点
            } else { //显示当前页为中心的 5 页
                firstPage = $scope.searchMap.pageNum - 2;
                lastPage = $scope.searchMap.pageNum + 2;
            }
        } else {
            $scope.firstDot = false; //前面没点
            $scope.lastDot = false; //后面没点
        }
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    //根据页码查询
    $scope.queryByPage = function (pageNum) {
        //页码验证
        if (pageNum < 1 || pageNum > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNum = parseInt(pageNum);
        //执行查询
        $scope.search();
    }

    //搜索页码鼠标回车事件
    $scope.enterEventPageNum = function (index, pageNum) {
        if (pageNum <= $scope.resultMap.totalPages) {
            var keycode = window.event ? index.keyCode : index.which;
            if (keycode == 13) {
                $scope.searchMap.pageNum = parseInt(pageNum);
                $scope.search();
            }
        } else {
            $scope.searchMap.pageNum = 1;
            alert("没有您要的页数！");
        }
    }
    //判断当前页码是否第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNum == 1) {
            return true;
        } else {
            return false;
        }
    }
    //判断当前页码是否最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNum == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    //设置排序规则
    $scope.sortSearch = function (sort, sortField) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        //搜索
        $scope.search();
    }

    //搜索关键字中是否有品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
                //如果包含
                return true;
            }
        }
        return false;
    }

    //加载关键字
    $scope.loadkeywords = function () {
        $scope.searchMap.keywords = $location.search()["keywords"];
        $scope.search(); //查询
    }
})