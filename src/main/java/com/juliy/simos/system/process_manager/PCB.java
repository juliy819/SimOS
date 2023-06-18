package com.juliy.simos.system.process_manager;


import com.juliy.simos.controller.MainController;
import com.juliy.simos.system.process_manager.deadlock.BankerAlgorithm;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 进程控制块
 * @author JuLiy
 * @date 2022/10/21 8:21
 */
public class PCB {
    /** 进程标识符 */
    private final IntegerProperty pid;
    /** 用户标识符 */
    private final StringProperty uid;
    /** 进程优先级，数字越大优先级越高 */
    private final IntegerProperty priority;
    /** 进程状态 */
    private final ObjectProperty<PStatus> status;
    private final IntegerProperty arrivalTime;
    /** 进程总计所需时间 */
    private final IntegerProperty serviceTime;
    /** 进程已运行时间 */
    private final IntegerProperty usedTime;
    /** 进程剩余时间 */
    private final IntegerProperty remainingTime;
    /** 进程已等待时间 */
    private final IntegerProperty waitedTime;
    /** 进程运行进度 */
    private final DoubleProperty progress;
    /** 所需资源总数 */
    private final List<Integer> maxR;
    /** 仍需资源数 */
    private final List<Integer> needR;
    /** 已分配资源数 */
    private final List<Integer> alocR;
    /** 所需内存 */
    private int memorySize;

    public PCB() {
        this.pid = new SimpleIntegerProperty();
        this.uid = new SimpleStringProperty();
        this.priority = new SimpleIntegerProperty(0);
        this.status = new SimpleObjectProperty<>(PStatus.CREATE);
        this.arrivalTime = new SimpleIntegerProperty(0);
        this.serviceTime = new SimpleIntegerProperty(1);
        this.usedTime = new SimpleIntegerProperty(0);
        this.remainingTime = new SimpleIntegerProperty(0);
        this.waitedTime = new SimpleIntegerProperty(0);
        this.progress = new SimpleDoubleProperty(0.0);
        this.maxR = new ArrayList<>();
        this.needR = new ArrayList<>();
        this.alocR = new ArrayList<>();
        this.memorySize = new Random().nextInt(450) + 50;

        //数据绑定，设置剩余时间、进程进度为动态更新
        this.remainingTime.bind(this.serviceTime.subtract(this.usedTime));
        this.progress.bind(this.usedTime.multiply(1.0).divide(this.serviceTime));
    }

    /** 初始化进程所需资源 */
    public void initResources(List<Integer> list) {
        maxR.addAll(list);
        needR.addAll(list);
        Collections.addAll(alocR, 0, 0, 0);
    }

    /** 分配资源后，更新仍需资源数和已分配资源数 */
    public void updateResources(List<Integer> list) {
        needR.set(0, needR.get(0) - list.get(0));
        needR.set(1, needR.get(1) - list.get(1));
        needR.set(2, needR.get(2) - list.get(2));

        alocR.set(0, alocR.get(0) + list.get(0));
        alocR.set(1, alocR.get(1) + list.get(1));
        alocR.set(2, alocR.get(2) + list.get(2));
    }

    public void releaseAllResources() {
        //释放资源
        MainController.systemKernel.getResourceManager().release(pid.get());
        //更新银行家算法中的数据
        BankerAlgorithm ba = ProcessManager.getBA();
        ba.getData().remove(pid.get());
        ba.release(maxR);
        //释放资源后检查是否有进程可以申请资源
        Platform.runLater(() -> {
            MainController.systemKernel.getProcessManager().checkBlock();
            MainController.systemKernel.getProcessManager().checkUnReady();
        });
    }

    /** 判断是否所有资源均已获得 */
    public boolean isCompleteAloc() {
        return needR.get(0) == 0 &&
                needR.get(1) == 0 &&
                needR.get(2) == 0;
    }


    public int getPid() {
        return pid.get();
    }

    public void setPid(int pid) {
        this.pid.set(pid);
    }

    public IntegerProperty pidProperty() {
        return pid;
    }

    public String getUid() {
        return uid.get();
    }

    public void setUid(String uid) {
        this.uid.set(uid);
    }

    public StringProperty uidProperty() {
        return uid;
    }

    public int getPriority() {
        return priority.get();
    }

    public void setPriority(int priority) {
        this.priority.set(priority);
    }

    public IntegerProperty priorityProperty() {
        return priority;
    }

    public PStatus getStatus() {
        return status.get();
    }

    public void setStatus(PStatus status) {
        this.status.set(status);
    }

    public ObjectProperty<PStatus> statusProperty() {
        return status;
    }

    public int getArrivalTime() {
        return arrivalTime.get();
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime.set(arrivalTime);
    }

    public IntegerProperty arrivalTimeProperty() {
        return arrivalTime;
    }

    public int getServiceTime() {
        return serviceTime.get();
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime.set(serviceTime);
    }

    public IntegerProperty serviceTimeProperty() {
        return serviceTime;
    }

    public int getUsedTime() {
        return usedTime.get();
    }

    public void setUsedTime(int usedTime) {
        this.usedTime.set(usedTime);
    }

    public IntegerProperty usedTimeProperty() {
        return usedTime;
    }

    public int getRemainingTime() {
        return remainingTime.get();
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime.set(remainingTime);
    }

    public IntegerProperty remainingTimeProperty() {
        return remainingTime;
    }

    public int getWaitedTime() {
        return waitedTime.get();
    }

    public void setWaitedTime(int waitedTime) {
        this.waitedTime.set(waitedTime);
    }

    public IntegerProperty waitedTimeProperty() {
        return waitedTime;
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public List<Integer> getMaxR() {
        return maxR;
    }

    public List<Integer> getNeedR() {
        return needR;
    }

    public List<Integer> getAlocR() {
        return alocR;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }

    @Override
    public String toString() {
        return "Pcb{" +
                "pid=" + pid +
                ", uid=" + uid +
                ", priority=" + priority +
                ", status=" + status +
                ", arriveTime=" + arrivalTime +
                ", serviceTime=" + serviceTime +
                ", usedTime=" + usedTime +
                ", remainingTime=" + remainingTime +
                ", waitedTime=" + waitedTime +
                ", progress=" + progress +
                '}';
    }
}
