package com.juliy.simos.system.process_manager;

/**
 * @author JuLiy
 * @date 2022/12/9 9:49
 */
public class PCBNotFoundException extends RuntimeException {
    public PCBNotFoundException() {
        super("未找到相应的PCB");
    }
}
