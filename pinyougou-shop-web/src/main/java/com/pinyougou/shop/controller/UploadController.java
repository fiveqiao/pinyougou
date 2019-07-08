package com.pinyougou.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

@RestController
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String file_service_url;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        //获取文件全名称
        String fileName = file.getOriginalFilename();
        //取最后一个点的索引加1 截取得到扩展名
        String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
        try {
            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
            String fileId = client.uploadFile(file.getBytes(), extName);
            //图片完整地址
            String url = file_service_url + fileId;
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");

        }
    }
}
