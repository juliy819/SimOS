package com.juliy.simos.system;

import com.juliy.simos.system.file_system.FileSystem;
import com.juliy.simos.system.memory_manager.MemoryManager;
import com.juliy.simos.system.process_manager.ProcessManager;
import com.juliy.simos.system.resource_manager.ResourceManager;
import lombok.Data;

/**
 * 系统内核
 * @author JuLiy
 * @date 2022/10/24
 */
@Data
public class SystemKernel {
    /** 进程管理器 */
    private final ProcessManager processManager = new ProcessManager();
    /** 内存管理器 */
    private final MemoryManager memoryManager = new MemoryManager();
    /** 设备管理器 */
    private final ResourceManager resourceManager = new ResourceManager();
    /** 文件系统 */
    private final FileSystem fileSystem = new FileSystem();


    /** 启动各管理器 */
    public void start() {
        this.processManager.start();
    }

    public void stop() {
        this.processManager.setRun(false);
    }
}
