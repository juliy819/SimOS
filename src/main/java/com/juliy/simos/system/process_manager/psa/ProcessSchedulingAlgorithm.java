package com.juliy.simos.system.process_manager.psa;

import com.juliy.simos.common.Config;
import com.juliy.simos.system.process_manager.PCB;
import com.juliy.simos.system.process_manager.PStatus;
import com.juliy.simos.system.process_manager.Process;
import javafx.application.Platform;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 进程调度算法
 * @author JuLiy
 * @date 2022/12/8 23:03
 */
public abstract class ProcessSchedulingAlgorithm implements Runnable {

    List<PCB> readyQueue;

    Logger log;

    public ProcessSchedulingAlgorithm(List<PCB> readyQueue, Logger log) {
        this.readyQueue = readyQueue;
        this.log = log;
    }


    /**
     * 执行算法<br>
     * 未停止时无法切换算法
     * 停止条件：就绪队列为空或其中只剩下被挂起的进程
     */
    @Override
    public abstract void run();

    //对readyQueue的更新操作必须执行在FX线程，否则无法更新显示
    //Platform.runLater正是运行在FX线程里的，因此需要将更新操作封装进去
    //以下三个方法都是如此
    public synchronized void removePCB(PCB pcb) {
        Platform.runLater(() -> readyQueue.remove(pcb));
    }

    public synchronized void addPCB(PCB pcb) {
        Platform.runLater(() -> readyQueue.add(pcb));
    }

    public synchronized PCB removePCB(int index) {
        PCB pcb = readyQueue.get(index);
        Platform.runLater(() -> readyQueue.remove(index));
        return pcb;
    }

    /** 进程执行到结束 */
    public void executeProcess(PCB pcb) {
        Process process = new Process(pcb);
        pcb.setStatus(PStatus.RUNNING);
        log.info("运行进程:" + pcb.getPid());
        String msg = "执行完毕";
        //进程一直运行到完成
        while (pcb.getStatus() != PStatus.DESTROY) {
            //运行中被挂起，则停止执行并移到队尾
            if (pcb.getStatus() == PStatus.STATIC_READY) {
                addPCB(pcb);
                msg = "被挂起";
                break;
            }
            //运行中被阻塞，则停止执行
            if (pcb.getStatus() == PStatus.ACTIVE_BLOCK) {
                msg = "被阻塞";
                break;
            }

            process.run(Config.TIME_SLICE);
            try {
                TimeUnit.MILLISECONDS.sleep(Config.WAIT_INTERVAL);
            } catch (InterruptedException e) {
                log.error("进程{" + pcb.getPid() + "}模拟运行时被打断");
            }
        }
        log.info("进程" + pcb.getPid() + msg);
    }

    /** 进程执行一个时间片 */
    public void executeProcess(PCB pcb, int timeSlice) {
        Process process = new Process(pcb);
        pcb.setStatus(PStatus.RUNNING);
        process.run(timeSlice);
        try {
            TimeUnit.MILLISECONDS.sleep(Config.WAIT_INTERVAL);
        } catch (InterruptedException e) {
            log.error("进程{" + pcb.getPid() + "}模拟运行时被打断");
        }

        //若进程执行完毕则输出,若进程阻塞则退出，否则加入到就绪队列队尾
        if (pcb.getUsedTime() == pcb.getServiceTime()) {
            log.info("进程" + pcb.getPid() + "执行完毕");
        } else if (pcb.getStatus() != PStatus.ACTIVE_BLOCK) {
            addPCB(pcb);
        }
    }
}
