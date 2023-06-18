package com.juliy.simos.system.memory_manager;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内存块
 * @author JuLiy
 * @date 2022/12/20 21:54
 */
@Data
@NoArgsConstructor
public class MemoryBlock {

    private int startAddress = 0;
    private int size = 0;
    private MemoryStatus status = MemoryStatus.FREE;
    private int id = -1;

    public MemoryBlock(int startAddress, int size) {
        this.startAddress = startAddress;
        this.size = size;
    }
}
