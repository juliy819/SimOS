package com.juliy.simos.system.process_manager.deadlock;

/**
 * 银行家算法异常
 * @author JuLiy
 * @date 2022/12/15 18:31
 */
public class BAException extends RuntimeException {
    public BAException() {
        super();
    }

    public BAException(String msg) {
        super(msg);
    }
}
