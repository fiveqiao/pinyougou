//广告控制层（运营商后台）
app.controller("contentController", function ($scope,$controller, contentService) {
    $controller('baseController', {$scope: $scope});//继承
    $scope.contentList = [];//广告集合

    //根据分类 ID 查询广告列表
    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
                $scope.contentList[categoryId] = response;
            }
        )
    }
    //搜索框默认值
    $scope.keywords = '手机';
    //跳转搜索页 (传递参数)
    $scope.search = function () {
        location.href = "http://localhost:8087/search.html#?keywords=" + $scope.keywords;
    }
})