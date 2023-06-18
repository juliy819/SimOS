package com.juliy.simos.system.memory_manager;

/**
 * 内存分配异常
 * @author JuLiy
 * @date 2022/12/20 23:42
 */
public class MemoryException extends RuntimeException {
    public MemoryException(String msg) {
        super(msg);
    }
}
