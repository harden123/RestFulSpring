package restfulspring.utils;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TextUtil {

	public static boolean isTextEqualKey(String text1,String text2) {
		Set<String> allKeys = getAllKeys(text1);
		Set<String> allKeys2 = getAllKeys(text2);
		return allKeys.containsAll(allKeys2)&&allKeys.size()==allKeys2.size();
	}
	

    /**
     * 递归获取 JSON 对象中的所有 key
     */
    private static Set<String> getAllKeys(Object obj) {
        Set<String> keys = new HashSet<>();
        if (obj instanceof JSONObject) {
            // 如果当前对象是 JSONObject，则遍历其中所有的子节点
            JSONObject jsonObj = (JSONObject) obj;
            for (String key : jsonObj.keySet()) {
                keys.add(key);
                keys.addAll(getAllKeys(jsonObj.get(key)));
            }
        } else if (obj instanceof JSONArray) {
            // 如果当前对象是 JSONArray，则遍历其中所有的元素
            JSONArray jsonArray = (JSONArray) obj;
            for (Object subObj : jsonArray) {
                keys.addAll(getAllKeys(subObj));
            }
        }
        // 其它类型的对象不包含 key，直接返回空 Set
        return keys;
    }
}
