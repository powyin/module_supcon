package com.supconit.hcmobile.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

public class JsonUtil {

    // json
    private static final WeakHashMap<String, JsonHolder> mCache = new WeakHashMap<>();

    private static class JsonHolder {
        String key;
        Object value;
        HashMap<String, JsonHolder> link;
    }

    /**
     * 获取路径下面 json元素
     * {"a1":"value_a_1","a2":[["data1","data2","data3"],["data5"]],"a3":{"b1":"value_b_1"},"cc":{"b1":"value_cc_b1_1"}}
     * if(path == "a1")               return list:  "value_a_1"
     * if(path == "a2")               return list:  "data1"
     * if(path == "a3","b1")          return list:  "value_b_1"
     * if(path == null,"b1")          return list:  "value_b_1"
     *
     * @param json json 原数据
     * @param path 路径 特殊( null 为通配符)
     */
    public static String getJsonString(String json, String... path) {
        if (json == null || path == null) {
            Log.e("JsonGetValue", "error:" + json);
            return null;
        }

        JsonHolder rootHolder = mCache.get(json);
        if (rootHolder == null) {
            try {
                rootHolder = new JsonHolder();
                if (json.startsWith("{")) {
                    rootHolder.value = new JSONObject(json);
                }
                if (json.startsWith("[")) {
                    rootHolder.value = new JSONArray(json);
                }
                mCache.put(json, rootHolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Object objectSingle = _getJsonObjectAsSingle(rootHolder, path);

        if (objectSingle == null) {
            Log.e("JsonGetValue", "error: can not find object -> path:" + Arrays.toString(path) + " json -> " + json.replace("\n", "").replace("\t", "").replace(" ", ""));
        }
        return objectSingle == null ? null : objectSingle.toString();
    }

    public static Double getJsonDouble(String json, String... path) {
        String value = getJsonString(json, path);
        try {
            return value == null ? null : Double.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Float getJsonFloat(String json, String... path) {
        String value = getJsonString(json, path);
        try {
            return value == null ? null : Float.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getJsonInt(String json, String... path) {
        String value = getJsonString(json, path);
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Boolean getJsonBoolean(String json, String... path) {
        String value = getJsonString(json, path);
        try {
            return value == null ? null : Boolean.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取路径下面 json元素
     * {
     * "a1":"value_a_1","a2":[["data1","data2","data3"],["data5"]],"a3":{"b1":"value_b_1"},"cc":{"b1":"value_cc_b1_1"}}
     * if(path == "a1")               return list:  "value_a_1"
     * if(path == "a2")               return list:  "data1","data2","data3","data5"
     * if(path == "a3","b1")          return list:  "value_b_1"
     * if(path == null,"b1")          return list:  "value_b_1","value_cc_b1_1"
     *
     * @param json json 原数据
     * @param path 路径 特殊(null 为通配符)
     */
    public static List<String> getJsonStringList(String json, String... path) {
        if (json == null || path == null) {
            Log.e("JsonGetValue", "error:" + json);
            return null;
        }

        JsonHolder rootHolder = mCache.get(json);
        if (rootHolder == null) {
            try {
                rootHolder = new JsonHolder();
                if (json.startsWith("{")) {
                    rootHolder.value = new JSONObject(json);
                }
                if (json.startsWith("[")) {
                    rootHolder.value = new JSONArray(json);
                }
                mCache.put(json, rootHolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<Object> objectList = _getJsonObjectAsList(rootHolder, path);
        if (objectList != null && objectList.size() > 0) {
            ArrayList<String> ret = new ArrayList<>();
            for (Object object : objectList) {
                if (object != null) {
                    ret.add(object.toString());
                }
            }
            if (ret.size() > 0) {
                return ret;
            }
        }
        Log.e("JsonGetValue", "error: can not find object -> path:" + Arrays.toString(path) + " json -> " + json.replace("\n", "").replace("\t", "").replace(" ", ""));
        return null;
    }


    /**
     * 缓冲 节点遍历 获取json数据 and null 可以作为通配符（尽可能多的获取数据）
     */
    private static List<Object> _getJsonObjectAsList(JsonHolder rootHolder, String... path) {
        if (rootHolder == null || path == null || rootHolder.value == null) {
            return null;
        }

        if (rootHolder.value instanceof JSONArray) {         // todo case jsonArray
            try {
                JSONArray jsonArray = (JSONArray) rootHolder.value;
                List<Object> strings = new ArrayList<>();
                for (int j = 0; j < jsonArray.length(); j++) {
                    String key = "[" + j + "]";

                    JsonHolder subHolder = rootHolder.link != null ? rootHolder.link.get(key) : null;
                    if (subHolder == null) {   // 加入节点
                        if (rootHolder.link == null) {
                            rootHolder.link = new HashMap<>();
                        }
                        subHolder = new JsonHolder();
                        subHolder.key = key;
                        if (subHolder.value == null) {
                            subHolder.value = jsonArray.get(j);
                        }
                        rootHolder.link.put(key, subHolder);
                    }

                    List<Object> des = _getJsonObjectAsList(subHolder, path);
                    if (des != null && des.size() > 0) {
                        strings.addAll(des);
                    }
                }
                return strings.size() > 0 ? strings : null;
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        if (rootHolder.value instanceof JSONObject) {   // todo case jsonObject
            JSONObject rootHolderObject = (JSONObject) rootHolder.value;
            for (int i = 0; i < path.length - 1 && rootHolderObject != null; i++) {
                String key = path[i];
                if (key == null) {
                    Iterator<String> keys = rootHolderObject.keys();
                    List<Object> strings = new ArrayList<>();

                    while (keys != null && keys.hasNext()) {
                        String next = keys.next();
                        JsonHolder subHolder = rootHolder.link != null ? rootHolder.link.get(next) : null;

                        if (subHolder == null && rootHolderObject.has(next)) {   // 加入节点
                            if (rootHolder.link == null) {
                                rootHolder.link = new HashMap<>();
                            }
                            subHolder = new JsonHolder();
                            subHolder.key = next;
                            try {
                                subHolder.value = rootHolderObject.get(next);
                            } catch (Exception e) {
                                // e.printStackTrace();
                            }
                            rootHolder.link.put(next, subHolder);
                        }

                        if (subHolder != null) {
                            List<Object> stringList = _getJsonObjectAsList(subHolder, Arrays.copyOfRange(path, i + 1, path.length));
                            if (stringList != null) {
                                strings.addAll(stringList);
                            }
                        }
                    }

                    return strings.size() > 0 ? strings : null;
                } else {
                    JsonHolder subHolder = rootHolder.link != null ? rootHolder.link.get(key) : null;
                    if (subHolder == null && rootHolderObject.has(key)) {   // 加入节点
                        if (rootHolder.link == null) {
                            rootHolder.link = new HashMap<>();
                        }
                        subHolder = new JsonHolder();
                        subHolder.key = key;
                        try {
                            subHolder.value = rootHolderObject.get(key);
                        } catch (Exception e) {
                            // e.printStackTrace();
                        }
                        rootHolder.link.put(key, subHolder);
                    }

                    if (subHolder != null && subHolder.value instanceof JSONArray) {
                        return _getJsonObjectAsList(subHolder, Arrays.copyOfRange(path, i + 1, path.length));
                    }
                    if (subHolder != null && subHolder.value instanceof JSONObject) {
                        rootHolder = subHolder;
                        rootHolderObject = (JSONObject) subHolder.value;
                        continue;
                    }
                    return null;
                }
            }

            try {
                Object value = path.length == 0 || rootHolderObject == null ? rootHolderObject : rootHolderObject.get(path[path.length - 1]);
                return value == null ? null : Collections.singletonList(value);
            } catch (Exception e) {
                // e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    /**
     * 缓冲 节点遍历 获取json数据 and null 可以作为通配符（尽可能少的获取数据）
     */
    private static Object _getJsonObjectAsSingle(JsonHolder rootHolder, String... path) {
        if (rootHolder == null || path == null || rootHolder.value == null) {
            return null;
        }

        if (rootHolder.value instanceof JSONArray) {         // todo case jsonArray
            try {
                JSONArray jsonArray = (JSONArray) rootHolder.value;
                for (int j = 0; j < jsonArray.length(); j++) {
                    String key = "[" + j + "]";

                    JsonHolder subHolder = rootHolder.link != null ? rootHolder.link.get(key) : null;
                    if (subHolder == null) {   // 加入节点
                        if (rootHolder.link == null) {
                            rootHolder.link = new HashMap<>();
                        }
                        subHolder = new JsonHolder();
                        subHolder.key = key;
                        if (subHolder.value == null) {
                            subHolder.value = jsonArray.get(j);
                        }
                        rootHolder.link.put(key, subHolder);
                    }

                    // 找到一个就直接返回 尽量少的获取数据
                    Object des = _getJsonObjectAsSingle(subHolder, path);
                    if (des != null) {
                        return des;
                    }
                }
                return null;
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        if (rootHolder.value instanceof JSONObject) {   // todo case jsonObject
            JSONObject rootHolderObject = (JSONObject) rootHolder.value;
            for (int i = 0; i < path.length - 1 && rootHolderObject != null; i++) {
                String key = path[i];
                if (key == null) {
                    Iterator<String> keys = rootHolderObject.keys();
                    while (keys != null && keys.hasNext()) {
                        String next = keys.next();
                        JsonHolder subHolder = rootHolder.link != null ? rootHolder.link.get(next) : null;

                        if (subHolder == null && rootHolderObject.has(next)) {   // 加入节点
                            if (rootHolder.link == null) {
                                rootHolder.link = new HashMap<>();
                            }
                            subHolder = new JsonHolder();
                            subHolder.key = next;
                            try {
                                subHolder.value = rootHolderObject.get(next);
                            } catch (Exception e) {
                                // e.printStackTrace();
                            }
                            rootHolder.link.put(next, subHolder);
                        }

                        // 找到一个就直接返回 尽量少的获取数据
                        if (subHolder != null) {
                            Object objectSingle = _getJsonObjectAsSingle(subHolder, Arrays.copyOfRange(path, i + 1, path.length));
                            if (objectSingle != null) {
                                return objectSingle;
                            }
                        }
                    }

                    return null;
                } else {
                    JsonHolder subHolder = rootHolder.link != null ? rootHolder.link.get(key) : null;
                    if (subHolder == null && rootHolderObject.has(key)) {   // 加入节点
                        if (rootHolder.link == null) {
                            rootHolder.link = new HashMap<>();
                        }
                        subHolder = new JsonHolder();
                        subHolder.key = key;
                        try {
                            subHolder.value = rootHolderObject.get(key);
                        } catch (Exception e) {
                            // e.printStackTrace();
                        }
                        rootHolder.link.put(key, subHolder);
                    }

                    if (subHolder != null && subHolder.value instanceof JSONArray) {
                        return _getJsonObjectAsSingle(subHolder, Arrays.copyOfRange(path, i + 1, path.length));
                    }
                    if (subHolder != null && subHolder.value instanceof JSONObject) {
                        rootHolder = subHolder;
                        rootHolderObject = (JSONObject) subHolder.value;
                        continue;
                    }
                    return null;
                }
            }

            try {
                return path.length == 0 || rootHolderObject == null ? rootHolderObject : rootHolderObject.get(path[path.length - 1]);
            } catch (Exception e) {
                //e.printStackTrace();
                return null;
            }
        }
        return null;
    }


}
