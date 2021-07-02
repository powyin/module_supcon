package com.supconit.hcmobile.plugins.debug.project;

import java.io.Serializable;
import java.util.List;

public class MiniRoot implements Serializable {
    String folderId;
    String name;
//    String parentFile;
    String icon;
    List<WorkSpace> children;
    int type;
    boolean isFile;
}

//[{
//    "folderId": 0,
//    "name": "离线任务",
//    "children": [{
//        "folderId": 0,
//        "name": "执行管理",
//        "children": [{
//            "folderId": 0,
//            "name": "历史日志",
//            "children": [],
//            "ud": false,
//            "icon": "../../img/deve_center/hist-log.png",
//            "type": -2,
//            "job": {
//                "projectName": null,
//                "name": null,
//                "type": 0,
//                "createTime": null,
//                "modifiedTime": null,
//                "description": null,
//                "param": null
//            }
//        },
//        {
//            "folderId": 0,
//            "name": "调度管理",
//            "children": [],
//            "ud": false,
//            "icon": "../../img/deve_center/schedule.png",
//            "type": -3,
//            "job": {
//                "projectName": null,
//                "name": null,
//                "type": 0,
//                "createTime": null,
//                "modifiedTime": null,
//                "description": null,
//                "param": null
//            }
//        }],
//        "ud": false,
//        "icon": "../../img/deve_center/folder.png",
//        "type": -1,
//        "job": {
//            "projectName": null,
//            "name": null,
//            "type": 0,
//            "createTime": null,
//            "modifiedTime": null,
//            "description": null,
//            "param": null
//        }
//    },
//    {
//        "folderId": 0,
//        "name": "任务管理",
//        "children": [{
//            "folderId": 1,
//            "name": "新建文件夹",
//            "children": [{
//                "folderId": 0,
//                "name": "asas",
//                "children": [{
//                    "folderId": 0,
//                    "name": "a",
//                    "children": [],
//                    "ud": true,
//                    "icon": "../../img/deve_center/shell.png",
//                    "type": 3,
//                    "job": {
//                        "projectName": "asas",
//                        "name": "a",
//                        "type": 1,
//                        "createTime": null,
//                        "modifiedTime": null,
//                        "description": null,
//                        "param": null
//                    }
//                },
//                {
//                    "folderId": 0,
//                    "name": "aS",
//                    "children": [],
//                    "ud": true,
//                    "icon": "../../img/deve_center/shell.png",
//                    "type": 3,
//                    "job": {
//                        "projectName": "asas",
//                        "name": "aS",
//                        "type": 1,
//                        "createTime": null,
//                        "modifiedTime": null,
//                        "description": null,
//                        "param": null
//                    }
//                },
//                {
//                    "folderId": 0,
//                    "name": "asda",
//                    "children": [],
//                    "ud": true,
//                    "icon": "../../img/deve_center/shell.png",
//                    "type": 3,
//                    "job": {
//                        "projectName": "asas",
//                        "name": "asda",
//                        "type": 1,
//                        "createTime": null,
//                        "modifiedTime": null,
//                        "description": null,
//                        "param": null
//                    }
//                },
//                {
//                    "folderId": 0,
//                    "name": "b",
//                    "children": [],
//                    "ud": true,
//                    "icon": "../../img/deve_center/shell.png",
//                    "type": 3,
//                    "job": {
//                        "projectName": "asas",
//                        "name": "b",
//                        "type": 1,
//                        "createTime": null,
//                        "modifiedTime": null,
//                        "description": null,
//                        "param": null
//                    }
//                },
//                {
//                    "folderId": 0,
//                    "name": "c",
//                    "children": [],
//                    "ud": true,
//                    "icon": "../../img/deve_center/shell.png",
//                    "type": 3,
//                    "job": {
//                        "projectName": "asas",
//                        "name": "c",
//                        "type": 1,
//                        "createTime": null,
//                        "modifiedTime": null,
//                        "description": null,
//                        "param": null
//                    }
//                }],
//                "ud": false,
//                "icon": "../../img/deve_center/workflow.png",
//                "type": 2,
//                "job": {
//                    "projectName": null,
//                    "name": null,
//                    "type": 0,
//                    "createTime": null,
//                    "modifiedTime": null,
//                    "description": null,
//                    "param": null
//                }
//            }],
//            "ud": true,
//            "icon": "../../img/deve_center/folder.png",
//            "type": 1,
//            "job": {
//                "projectName": null,
//                "name": null,
//                "type": 0,
//                "createTime": null,
//                "modifiedTime": null,
//                "description": null,
//                "param": null
//            }
//        }],
//        "ud": false,
//        "icon": "../../img/deve_center/folder.png",
//        "type": 1,
//        "job": {
//            "projectName": null,
//            "name": null,
//            "type": 0,
//            "createTime": null,
//            "modifiedTime": null,
//            "description": null,
//            "param": null
//        }
//    }],
//    "ud": false,
//    "icon": "../../img/deve_center/folder.png",
//    "type": -1,
//    "job": {
//        "projectName": null,
//        "name": null,
//        "type": 0,
//        "createTime": null,
//        "modifiedTime": null,
//        "description": null,
//        "param": null
//    }
//}]

