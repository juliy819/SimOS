package com.juliy.simos.system.memory_manager.dpaa;

import com.juliy.simos.system.memory_manager.MemoryBlock;
import com.juliy.simos.system.memory_manager.MemoryException;
import com.juliy.simos.system.memory_manager.MemoryStatus;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 首次适应算法
 * @author JuLiy
 * @date 2022/12/20 22:39
 */
public class FF extends MemoryAllocationAlgorithm {

    private static final Logger log = Logger.getLogger(FF.class);

    public FF(List<MemoryBlock> memoryList) {
        super(memoryList, log);
    }

    @Override
    public void allocateMemory(int size, int id) {
        for (MemoryBlock block : memoryList) {
            if (block.getStatus() == MemoryStatus.FREE && block.getSize() >= size) {
                allocate(size, id, block);
                isAllocated = true;
                log.info("成功向进程P" + id + "分配大小为" + size + "KB的内存块");
                break;
            }
        }
        if (!isAllocated) {
            throw new MemoryException("内存不足，无法向进程P" + id + "分配大小为" + size + "KB的内存块");
        }
    }
}
