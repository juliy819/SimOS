package com.juliy.simos.system.memory_manager.dpaa;

import com.juliy.simos.system.memory_manager.MemoryBlock;
import com.juliy.simos.system.memory_manager.MemoryException;
import com.juliy.simos.system.memory_manager.MemoryStatus;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 最坏适应算法
 * @author JuLiy
 * @date 2022/12/21 0:41
 */
public class WF extends MemoryAllocationAlgorithm {

    private static final Logger log = Logger.getLogger(WF.class);

    public WF(List<MemoryBlock> memoryList) {
        super(memoryList, log);
    }

    @Override
    public void allocateMemory(int size, int id) {
        MemoryBlock worstBlock = null;
        int max = 0;
        for (MemoryBlock block : memoryList) {
            if (block.getStatus() == MemoryStatus.FREE && block.getSize() >= size) {
                if (max < block.getSize() - size) {
                    max = block.getSize() - size;
                    worstBlock = block;
                }
            }
        }

        if (worstBlock == null) {
            throw new MemoryException("内存不足，无法向进程P" + id + "分配大小为" + size + "KB的内存块");
        }
        allocate(size, id, worstBlock);
        log.info("成功向进程P" + id + "分配大小为" + size + "KB的内存块");
    }
}
