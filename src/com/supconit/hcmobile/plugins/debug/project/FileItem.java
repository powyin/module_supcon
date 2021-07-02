package com.supconit.hcmobile.plugins.debug.project;

import java.io.Serializable;
import java.util.List;

public class FileItem implements Serializable {
    String spaceId;
    String folderId;
    String name;
//    String parentFile;
    String icon;
    List<FileItem> children;
    int type;
    boolean isFile;
    double lenght;
    long createTime;
}

//{
//    "result":{
//        "id":132,
//        "uniqueCode":"2157615050563",
//        "name":"666",
//        "type":1,
//        "indexUrl":"index.html",
//        "logoUrl":"http://10.10.77.106:8888/group1/M00/00/16/CgpNal2mbguAZyRSAAATiWrsjQ4884.png",
//        "remark":null,
//        "scope":"购物",
//        "offlineUrl":"http://10.10.77.106:8888/group1/M01/00/28/CgpNal3yJdqAaWdYAAAFP00QeVw368.zip",
//        "remoteUrl":"http://10.10.77.104:8888/www/applet/2157615050563/",
//        "updateTime":1576150506000,
//        "file":[
//            {
//                "file":"index.html",
//                "hash":"881c9532efb5f9161634406f2556fcd3"
//            }
//        ],
//        "hotUpdateUrl":"http://10.10.77.106:8888/group1/M01/00/28/CgpNal3yJeqAIjGtAAAAVBt2SN8.manife"
//    },
//    "code":100,
//    "resultDes":"获取小程序详情成功!"
//}
