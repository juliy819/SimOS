package com.juliy.simos.system.process_manager;

import com.juliy.simos.common.Operation;
import com.juliy.simos.controller.MainController;
import com.juliy.simos.entity.PCB;
import com.juliy.simos.entity.PStatus;
import com.juliy.simos.system.process_manager.deadlock.BAData;
import com.juliy.simos.system.process_manager.deadlock.BAException;
import com.juliy.simos.system.process_manager.deadlock.BankerAlgorithm;
import com.juliy.simos.system.process_manager.deadlock.ResourceRequest;
import com.juliy.simos.system.process_manager.sa.ProcessSchedulingAlgorithm;
import com.juliy.simos.util.IdUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 进程管理器
 * @author JuLiy
 * @date 2022/10/21 8:18
 */
public class ProcessManager extends Thread {
    public static final Logger log = Logger.getLogger(ProcessManager.class.getName());
    private static BankerAlgorithm ba = new BankerAlgorithm();
    private final Random random = new Random();
    /** 就绪队列 */
    private final ObservableList<PCB> readyQueue = FXCollections.observableArrayList();
    /** 阻塞队列 */
    private final ObservableList<PCB> blockQueue = FXCollections.observableArrayList();
    /** PCB表，便于检索PCB */
    private final List<PCB> PCBList = new ArrayList<>();
    private boolean run = true;
    /** 进程调度算法 */
    private ProcessSchedulingAlgorithm psa;
    private ProcessSchedulingAlgorithm tempPsa;
    /** 调度算法执行线程 */
    private Thread saThread;

    private StringProperty cpuTime = new SimpleStringProperty("0");

    public static BankerAlgorithm getBA() {
        return ba;
    }

    @Override
    public void run() {
        ba.initResourceManager(MainController.systemKernel.getResourceManager());
        while (run) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error("进程管理器模拟时间流逝时被打断");
            }
            cpuTime.set(String.valueOf(Integer.parseInt(cpuTime.get()) + 1));
            setSA();
        }
    }

    /** 设置调度算法 */
    private void setSA() {
        //初次启动时，默认调度算法为复选框的默认第一个选项
        if (psa == null) {
            psa = tempPsa;
        }

        //第一次启动或上次的调度算法执行完毕后，再次启动
        if (saThread == null ||
                saThread.getState() == State.TERMINATED) {

            if (psa != tempPsa) {
                psa = tempPsa;
                log.info("切换算法:" + psa.getClass().getSimpleName());
            }
            saThread = new Thread(psa);
            saThread.start();
        }
    }

    /**
     * 创建原语
     * @return 创建进程的PCB
     */
    public PCB create() {
        //申请空白PCB
        PCB pcb = new PCB();
        int pid = IdUtil.createId();

        //随机生成进程所需资源总数
        int ra = random.nextInt(4) + 1;
        int rb = random.nextInt(3) + 1;
        int rc = random.nextInt(2) + 1;
        //Collections.addAll(pcb.getMaxR(), ra, rb, rc);


        //更新银行家算法中的数据
        List<Integer> maxList = new ArrayList<>();
        Collections.addAll(maxList, ra, rb, rc);

        pcb.initResources(maxList);

        List<Integer> needList = new ArrayList<>();
        Collections.addAll(needList, ra, rb, rc);

        List<Integer> alocList = new ArrayList<>();
        Collections.addAll(alocList, 0, 0, 0);

        BAData data = new BAData();
        data.setMax(maxList);
        data.setNeed(needList);
        data.setAllocation(alocList);
        ba.getData().put(pid, data);

        ResourceRequest req = ResourceRequest.generateBaseRequest(pid);

        try {
            ba.bankerAlgorithm(req);
            pcb.updateResources(req.getSource());
            pcb.setStatus(PStatus.ACTIVE_READY);
        } catch (BAException e) {
            log.error(e.getMessage());
        }


        //初始化PCB
        pcb.setPid(pid);
        pcb.setServiceTime(random.nextInt(30) + 20);
        pcb.setPriority(random.nextInt(10));

        //是否能加入就绪队列
        if (pcb.getStatus().equals(PStatus.ACTIVE_READY)) {
            readyQueue.add(pcb);
        }
        PCBList.add(pcb);
        return pcb;
    }

    /** 检查是否有创建时请求资源分配失败的进程，有则重新申请资源 */
    public void checkUnReady() {
        for (PCB pcb : PCBList) {
            if (pcb.getStatus() == PStatus.CREATE) {
                ResourceRequest req = ResourceRequest.generateBaseRequest(pcb.getPid());
                try {
                    ba.bankerAlgorithm(req);
                    pcb.updateResources(req.getSource());
                    pcb.setStatus(PStatus.ACTIVE_READY);
                    readyQueue.add(pcb);
                } catch (BAException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    /** 检查是否有陷入阻塞的进程，有则重新申请资源 */
    public void checkBlock() {
        for (PCB pcb : PCBList) {
            if (pcb.getStatus() == PStatus.ACTIVE_BLOCK) {
                ResourceRequest req = ResourceRequest.generateRemainRequest(pcb);
                try {
                    ba.bankerAlgorithm(req);
                    pcb.updateResources(req.getSource());
                    pcb.setStatus(PStatus.ACTIVE_READY);
                    readyQueue.add(pcb);
                } catch (BAException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    /**
     * 创建指定uid的进程
     * @param uid 用户编号
     * @return 创建后的pcb
     */
    public PCB create(String uid) {
        PCB pcb = this.create();
        pcb.setUid(uid);
        return pcb;
    }

    /**
     * 终止原语
     * @param id 进程id
     */
    public void destroy(int id) {
        PCB pcb = getPCB(id);

        if (pcb == null) {
            return;
        }

        pcb.setStatus(PStatus.DESTROY);
        readyQueue.remove(pcb);
    }

    /**
     * 阻塞原语
     * @param id 进程id
     */
    public void block(int id) {
        PCB pcb = getPCB(id);

        if (pcb == null) {
            return;
        }

        if (pcb.getStatus() == PStatus.RUNNING) {
            pcb.setStatus(PStatus.ACTIVE_BLOCK);
            readyQueue.remove(pcb);
            blockQueue.add(pcb);
        } else {
            Operation.showErrorAlert("只有运行中的进程才可手动阻塞");
        }
    }

    /**
     * 唤醒原语
     * @param id 进程id
     */
    public void wakeup(int id) {
        PCB pcb = getPCB(id);

        if (pcb == null) {
            return;
        }

        pcb.setStatus(PStatus.ACTIVE_READY);
    }

    /**
     * 挂起原语
     * @param id 进程id
     */
    public void suspend(int id) {
        PCB pcb = getPCB(id);

        if (pcb == null) {
            return;
        }

        //活动阻塞->静止阻塞
        if (pcb.getStatus().equals(PStatus.ACTIVE_BLOCK)) {
            pcb.setStatus(PStatus.STATIC_BLOCK);
        }
        //活动就绪->静止就绪
        else if (pcb.getStatus().equals(PStatus.ACTIVE_READY)) {
            pcb.setStatus(PStatus.STATIC_READY);
        }
        //执行->静止就绪
        else if (pcb.getStatus().equals(PStatus.RUNNING)) {
            pcb.setStatus(PStatus.STATIC_READY);
        } else {
            Operation.showErrorAlert("进程状态有误，无法挂起");
        }
    }

    /**
     * 激活原语
     * @param id 进程id
     */
    public void active(int id) {
        PCB pcb = getPCB(id);

        if (pcb == null) {
            return;
        }

        //静止阻塞->活动阻塞
        if (pcb.getStatus().equals(PStatus.STATIC_BLOCK)) {
            pcb.setStatus(PStatus.ACTIVE_BLOCK);
        }
        //静止就绪->活动就绪
        if (pcb.getStatus().equals(PStatus.STATIC_READY)) {
            pcb.setStatus(PStatus.ACTIVE_READY);
        } else {
            Operation.showErrorAlert("进程状态有误，无法激活");
        }
    }

    /** 根据pid获取对应的PCB */
    public PCB getPCB(int pid) {
        for (PCB pcb : PCBList) {
            if (pcb.getPid() == pid) {
                return pcb;
            }
        }
        log.error("未找到pid为:" + pid + " 的进程");
        throw new PCBNotFoundException();
    }

    /** 根据uid获取对应的PCB */
    public PCB getPCB(String uid) {
        for (PCB pcb : PCBList) {
            if (pcb.getUid().equals(uid)) {
                return pcb;
            }
        }
        log.error("未找到uid为:" + uid + " 的进程");
        throw new PCBNotFoundException();
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public void setPsa(ProcessSchedulingAlgorithm psa) {
        this.tempPsa = psa;
    }

    public ObservableList<PCB> getReadyQueue() {
        return this.readyQueue;
    }

    public ObservableList<PCB> getBlockQueue() {
        return this.blockQueue;
    }

    public StringProperty cpuTimeProperty() {
        return cpuTime;
    }
}
