app.service("uploadService", function ($http) {
//    angularJS的文件上传
    this.uploadFile = function () {
//        html5的对象
        var formData = new FormData();
        formData.append("file", file.files[0]); //file.files[0] js的取值方式  取到页面的第一个type=”file“  "file"与MultipartFile file 名字一致
        return $http({
            method: 'post',
            url: '../upload.do',
            data: formData,
            headers: {'Content-Type': undefined},  //写了这一行即声明enctype类型一定是 multipart/form-data
            transformRequest: angular.identity  //序列化上传的文件
        });
    }
})