package com.juliy.simos.system.process_manager.psa;

import com.juliy.simos.system.process_manager.PCB;
import com.juliy.simos.system.process_manager.PStatus;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * 短作业优先算法
 * @author JuLiy
 * @date 2022/12/9 17:22
 */
public class SJF extends ProcessSchedulingAlgorithm {
    private static final Logger log = Logger.getLogger(SJF.class);

    public SJF(List<PCB> readyQueue) {
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

            //取出就绪队列中服务时间最短的进程
            Optional<PCB> min = readyQueue.stream()
                    .filter(pcb -> pcb.getStatus() == PStatus.ACTIVE_READY)
                    .min(Comparator.comparingInt(PCB::getServiceTime));
            if (min.isPresent()) {
                PCB pcb = min.get();
                removePCB(pcb);
                executeProcess(pcb);
            } else {
                log.error("未找到服务时间最短的进程");
            }
        }
    }
}
