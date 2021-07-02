package com.supconit.hcmobile.plugins.debug.data;

import java.io.Serializable;
import java.util.List;

public class FileDes implements Serializable {
    String path;
    String name;
    String parentFile;
    boolean isFile;
    List<FileDes> listFiles;
    long length;
}

// {
//    "path":"a/b/c",
//    "name":"c",
//    "parentFile":"a/b",
//    "isFile":false,
//    "listFiles":[
//        {
//            "path":"a/b/c/tt.txt",
//            "name":"tt.txt",
//            "parentFile":"a/b/c",
//            "isFile":true,
//            "listFiles":[
//
//            ],
//            "length":23344
//        },
//        {
//            "path":"a/b/c/d",
//            "name":"d",
//            "parentFile":"a/b/c",
//            "isFile":false,
//            "listFiles":[
//                {
//                    "path":"a/b/c/tt.txt",
//                    "name":"tt.txt",
//                    "parentFile":"a/b/c",
//                    "isFile":true,
//                    "listFiles":[
//
//                    ],
//                    "length":23344
//                }
//            ],
//            "length":-1
//        }
//    ],
//    "length":-1
// }

