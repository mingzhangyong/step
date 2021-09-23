package utils;

import com.google.gson.Gson;

/**
 * @author: mingzhangyong
 * @create: 2021-08-24 16:53
 **/
public class GsonUtils {
    private static Gson gson;

    public static String toJsonString(Object object){
        if(null == gson){
            gson = new Gson();
        }
        return gson.toJson(object);
    }
}
