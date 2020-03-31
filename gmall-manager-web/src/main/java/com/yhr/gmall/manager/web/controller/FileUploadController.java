package com.yhr.gmall.manager.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
public class FileUploadController {

    // @Value注解的使用条件是当前类必须在spring中
    //服务器的ip地址需要作为一个配置文件放入项目中  软编码
    @Value("${fileServer.url}")
    private String fileUrl;

    @RequestMapping("/fileUpload")
    public String fileUpload(MultipartFile file) throws IOException, MyException {

        String imgUrl=fileUrl;

        //当文件不为空上传
        if(file!=null){

            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);

            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getTrackerServer();
            StorageClient storageClient=new StorageClient(trackerServer,null);

            String orginalFilename=file.getOriginalFilename();

            //获取文件后缀名
            String extName = StringUtils.substringAfterLast(orginalFilename, ".");


            //String[] upload_file = storageClient.upload_file(orginalFilename, "jpg", null);获取本地文件
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            for (int i = 0; i < upload_file.length; i++) {

                String path = upload_file[i];

                imgUrl+="/"+path;

            }

        }




        return imgUrl;
    }
}
