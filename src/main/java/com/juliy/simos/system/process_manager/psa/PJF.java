package com.juliy.simos.system.process_manager.psa;

import com.juliy.simos.system.process_manager.PCB;
import com.juliy.simos.system.process_manager.PStatus;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 优先级调度算法
 * @author JuLiy
 * @date 2022/12/11 23:28
 */
public class PJF extends ProcessSchedulingAlgorithm {
    private static final Logger log = Logger.getLogger(PJF.class);

    public PJF(List<PCB> readyQueue) {
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

            //取出就绪队列中优先级最高的进程
            Optional<PCB> maxPriority = readyQueue.stream()
                    .filter(pcb -> pcb.getStatus() == PStatus.ACTIVE_READY)
                    .max(Comparator.comparingInt(PCB::getPriority));
            if (maxPriority.isPresent()) {
                PCB pcb = maxPriority.get();
                removePCB(pcb);
                executeProcess(pcb);
            } else {
                log.error("未找到优先级最高的的进程");
            }
        }
    }
}
