package com.juliy.simos.system.memory_manager.dpaa;

import com.juliy.simos.system.memory_manager.MemoryBlock;
import com.juliy.simos.system.memory_manager.MemoryException;
import com.juliy.simos.system.memory_manager.MemoryStatus;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 循环首次适应算法
 * @author JuLiy
 * @date 2022/12/21 0:40
 */
public class NF extends MemoryAllocationAlgorithm {

    private static final Logger log = Logger.getLogger(FF.class);

    private int lastIndex = 0;

    public NF(List<MemoryBlock> memoryList) {
        super(memoryList, log);
    }

    @Override
    public void allocateMemory(int size, int id) {
        if (lastIndex >= memoryList.size()) {
            lastIndex = 0;
        }
        for (int i = lastIndex; i < memoryList.size(); i++) {
            MemoryBlock block = memoryList.get(i);
            if (block.getStatus() == MemoryStatus.FREE && block.getSize() >= size) {
                allocate(size, id, block);
                isAllocated = true;
                lastIndex = i;
                log.info("成功向进程P" + id + "分配大小为" + size + "KB的内存块");
                break;
            }
        }
        if (!isAllocated) {
            throw new MemoryException("内存不足，无法向进程P" + id + "分配大小为" + size + "KB的内存块");
        }
    }
}
