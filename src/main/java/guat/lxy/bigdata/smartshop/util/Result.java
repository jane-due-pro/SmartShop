package guat.lxy.bigdata.smartshop.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 控制器统一返回结果工具，消除各 Controller 中重复的 Map 创建代码。
 */
public final class Result {

    private Result() {
    }

    public static Map<String, Object> success(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", message);
        return map;
    }

    public static Map<String, Object> fail(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", message);
        return map;
    }

    public static Map<String, Object> of(boolean ok, String successMsg, String failMsg) {
        return ok ? success(successMsg) : fail(failMsg);
    }
}