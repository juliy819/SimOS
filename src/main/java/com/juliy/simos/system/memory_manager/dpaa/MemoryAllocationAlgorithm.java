package com.juliy.simos.system.memory_manager.dpaa;

import com.juliy.simos.common.Context;
import com.juliy.simos.controller.MemoryController;
import com.juliy.simos.system.memory_manager.MemoryBlock;
import com.juliy.simos.system.memory_manager.MemoryStatus;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author JuLiy
 * @date 2022/12/20 22:33
 */
public abstract class MemoryAllocationAlgorithm {

    /** 不再分割的剩余分区的大小 */
    private static final int MIN_SIZE = 50;
    Logger log;
    List<MemoryBlock> memoryList;

    boolean isAllocated = false;

    public MemoryAllocationAlgorithm(List<MemoryBlock> memoryList, Logger log) {
        this.memoryList = memoryList;
        this.log = log;
    }

    /**
     * 分配内存
     * @param size 所需内存大小
     * @param id   进程pid
     */
    public abstract void allocateMemory(int size, int id);

    /**
     * 分配内存的底层实现
     * @param size  所需内存大小
     * @param id    进程pid
     * @param block 要分配的内存块
     */
    void allocate(int size, int id, MemoryBlock block) {
        //若剩余分区大小大于不再分割下限，则进行分割
        if (block.getSize() - size >= MIN_SIZE) {
            MemoryBlock newBlock = new MemoryBlock(block.getStartAddress() + size, block.getSize() - size);
            block.setSize(size);
            memoryList.add(memoryList.indexOf(block) + 1, newBlock);
        }
        block.setStatus(MemoryStatus.USED);
        block.setId(id);
        update();
    }

    public void release(int id) {
        for (int i = 0; i < memoryList.size(); i++) {
            MemoryBlock block = memoryList.get(i);
            if (block.getId() == id) {
                block.setStatus(MemoryStatus.FREE);
                block.setId(-1);

                //与前后两个空闲分区相邻
                if (i != 0 && i != memoryList.size() - 1 &&
                        memoryList.get(i - 1).getStatus() == MemoryStatus.FREE &&
                        memoryList.get(i + 1).getStatus() == MemoryStatus.FREE) {
                    memoryList.get(i - 1).setSize(memoryList.get(i - 1).getSize() + block.getSize() + memoryList.get(i + 1).getSize());
                    memoryList.remove(i);
                    memoryList.remove(i);
                    update();
                    log.info("回收的内存分区与前后分区合并，新空闲分区起址为：" + memoryList.get(i - 1).getStartAddress() +
                                     "，大小为：" + memoryList.get(i - 1).getSize());
                    break;
                }
                //与前一个空闲分区相邻
                else if (i != 0 && memoryList.get(i - 1).getStatus() == MemoryStatus.FREE) {
                    memoryList.get(i - 1).setSize(memoryList.get(i - 1).getSize() + block.getSize());
                    memoryList.remove(i);
                    update();
                    log.info("回收的内存分区与前分区合并，新空闲分区起址为：" + memoryList.get(i - 1).getStartAddress() +
                                     "，大小为：" + memoryList.get(i - 1).getSize());
                    break;
                }
                //与后一个空闲分区相邻
                else if (i != memoryList.size() - 1 && memoryList.get(i + 1).getStatus() == MemoryStatus.FREE) {
                    block.setSize(block.getSize() + memoryList.get(i + 1).getSize());
                    log.info(block);
                    memoryList.remove(i + 1);
                    update();
                    log.info("回收的内存分区与后分区合并，新空闲分区起址为：" + block.getStartAddress() +
                                     "，大小为：" + block.getSize());
                    break;
                }
                //不与空闲分区相邻就不用再处理了
                else {
                    log.info("回收的内存分区无法合并");
                }

            }
        }
    }

    private void update() {
        MemoryController controller = (MemoryController) Context.controllerMap.get("Memory");
        controller.update();
    }
}
