package com.juliy.simos.system.process_manager.psa;

import com.juliy.simos.common.Config;
import com.juliy.simos.system.process_manager.PCB;
import com.juliy.simos.system.process_manager.PStatus;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 轮转调度算法
 * @author JuLiy
 * @date 2022/12/11 23:28
 */
public class RR extends ProcessSchedulingAlgorithm {

    private static final Logger log = Logger.getLogger(RR.class);


    public RR(List<PCB> readyQueue) {
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
            executeProcess(pcb, Config.TIME_SLICE);
        }
    }
}
