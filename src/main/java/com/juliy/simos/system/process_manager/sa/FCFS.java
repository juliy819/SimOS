package com.juliy.simos.system.process_manager.sa;

import com.juliy.simos.entity.PCB;
import com.juliy.simos.entity.PStatus;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 先来先服务算法
 * @author JuLiy
 * @date 2022/12/8 23:03
 */
public class FCFS extends ProcessSchedulingAlgorithm {

    private static final Logger log = Logger.getLogger(FCFS.class);

    public FCFS(List<PCB> readyQueue) {
        super(readyQueue, log);
    }


    @Override
    public void run() {
        while (!readyQueue.isEmpty()) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //取出就绪队列的第一个进程
            PCB pcb = removePCB(0);
            //若该进程被挂起，则移到就绪队列队尾
            if (pcb.getStatus() == PStatus.STATIC_READY) {
                addPCB(pcb);
                continue;
            }
            executeProcess(pcb);
        }
    }
}
