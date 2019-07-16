app.controller("searchController", function ($scope, $controller, searchService) {
    $controller('baseController', {$scope: $scope});//继承
    //搜索
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
            }
        )
    }

    //搜索对象
    $scope.searchMap = {"keywords": "", "category": "", "brand": "", "spec": {}};

    //添加搜索项
    $scope.addSearchItem = function (key, value) {
        if (key == "category" || key == "brand"){ //如果点击的是分类或者是品牌
            $scope.searchMap[key] = value;
        }else {
            $scope.searchMap.spec[key] = value;
        }
        //执行查询
        $scope.search();
    }

    //删除搜索项
    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand"){ //如果点击的是分类或者是品牌
            $scope.searchMap[key] = "";
        }else {//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        //执行查询
        $scope.search();
    }
})