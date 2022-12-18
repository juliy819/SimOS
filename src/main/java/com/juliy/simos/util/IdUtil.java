package com.juliy.simos.util;

/**
 * @author JuLiy
 * @date 2022/10/21 10:20
 */
public class IdUtil {

    private static int count = 0;

    /** 生成进程编号 */
    public static int createId() {
        return count++;
    }
}
