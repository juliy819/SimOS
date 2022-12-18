package com.juliy.simos.entity;

import com.juliy.simos.controller.MainController;
import com.juliy.simos.system.process_manager.ProcessManager;
import com.juliy.simos.system.process_manager.deadlock.BAException;
import com.juliy.simos.system.process_manager.deadlock.BankerAlgorithm;
import com.juliy.simos.system.process_manager.deadlock.ResourceRequest;
import javafx.application.Platform;
import org.apache.log4j.Logger;

/**
 * 进程
 * @author JuLiy
 * @date 2022/10/21 8:21
 */
public class Process {

    private static final Logger log = Logger.getLogger(Process.class);
    private final PCB pcb;

    public Process(PCB pcb) {
        this.pcb = pcb;
    }

    public void run(int time) {
        if ((pcb.getUsedTime() + time) < pcb.getServiceTime()) {
            pcb.setUsedTime(pcb.getUsedTime() + time);
            //若时间过半，且未获得所有资源，则申请剩余资源
            if (pcb.getUsedTime() > pcb.getRemainingTime() && !pcb.isCompleteAloc()) {
                ResourceRequest req = ResourceRequest.generateRemainRequest(pcb);
                BankerAlgorithm ba = ProcessManager.getBA();
                try {
                    ba.bankerAlgorithm(req);
                    pcb.updateResources(req.getSource());
                } catch (BAException e) {
                    log.info("进程P" + pcb.getPid() + "申请资源失败，陷入阻塞");
                    pcb.setStatus(PStatus.ACTIVE_BLOCK);
                }
            }
        } else {
            pcb.setUsedTime(pcb.getServiceTime());
            pcb.setStatus(PStatus.DESTROY);

            //释放资源
            MainController.systemKernel.getResourceManager().release(pcb.getPid());
            //更新银行家算法中的数据
            BankerAlgorithm ba = ProcessManager.getBA();
            ba.getData().remove(pcb.getPid());
            ba.release(pcb.getMaxR());
            //释放资源后检查是否有进程可以申请资源
            Platform.runLater(() -> {
                MainController.systemKernel.getProcessManager().checkBlock();
                MainController.systemKernel.getProcessManager().checkUnReady();
            });

        }
    }

}