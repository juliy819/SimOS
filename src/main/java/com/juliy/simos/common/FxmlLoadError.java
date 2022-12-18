package com.juliy.simos.common;

/**
 * @author JuLiy
 * @date 2022/12/9 14:01
 */
public class FxmlLoadError extends RuntimeException {
    public FxmlLoadError(Throwable e) {
        super("fxml文件加载失败", e);
    }
}
