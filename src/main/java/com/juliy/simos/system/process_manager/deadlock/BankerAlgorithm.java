package com.juliy.simos.system.process_manager.deadlock;

import com.juliy.simos.system.resource_manager.ResourceManager;
import lombok.Data;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 银行家算法
 * @author JuLiy
 * @date 2022/12/12 20:55
 */
@Data
public class BankerAlgorithm {

    private static final Logger log = Logger.getLogger(BankerAlgorithm.class);

    private final int RESOURCE_NUM = 3;
    private final List<Integer> available = new ArrayList<>();
    private final Map<Integer, BAData> data = new HashMap<>();
    private ResourceManager resourceManager;

    public void initResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        available.add(resourceManager.getTotalANum());
        available.add(resourceManager.getTotalBNum());
        available.add(resourceManager.getTotalCNum());
    }

    /**
     * 银行家算法
     * @param request 资源请求
     */
    public void bankerAlgorithm(ResourceRequest request) {
        int id = request.getId();
        log.info("进程P" + id + "请求分配资源：" + request.getSource());

        //判断请求资源是否超过进程所声明的最大需求数
        for (int i = 0; i < RESOURCE_NUM; i++) {
            if (request.getSource().get(i) > data.get(id).getNeed().get(i)) {
                throw new BAException("进程P" + id + "请求的资源超过其申明的最大值");
            }
        }
        //判断是否可以满足进程此次的资源请求
        for (int i = 0; i < RESOURCE_NUM; i++) {
            if (request.getSource().get(i) > available.get(i)) {
                throw new BAException("可用资源数不足，无法满足进程P" + id + "的请求");
            }
        }
        //进行资源的试探分配
        resourceAllocation(request);
        //进行安全性检查，通过则分配资源，否则进行回滚
        if (securityCheck()) {
            log.info("进程P" + id + "的资源请求：" + request.getSource() + " 通过");
            resourceManager.allocate(request);
        } else {
            rollbackAllocation(request);
            throw new BAException("进程P" + id + "的资源请求：" + request.getSource() + " 不通过");
        }
    }

    /**
     * 试探性资源分配
     * @param request 资源请求
     */
    private void resourceAllocation(ResourceRequest request) {
        int id = request.getId();
        List<Integer> requestSource = request.getSource();
        List<Integer> currentAllocation = data.get(id).getAllocation();
        List<Integer> currentNeed = data.get(id).getNeed();

        //修改可利用资源数量、已分配资源、仍需求资源
        for (int i = 0; i < RESOURCE_NUM; i++) {
            available.set(i, available.get(i) - requestSource.get(i));
            currentAllocation.set(i, currentAllocation.get(i) + requestSource.get(i));
            currentNeed.set(i, currentNeed.get(i) - requestSource.get(i));
        }
        //更新数据
        data.get(id).setAllocation(currentAllocation);
        data.get(id).setNeed(currentNeed);
    }

    /**
     * 安全性检查
     * @return 通过安全性检查返回true，否则返回false
     */
    private boolean securityCheck() {
        log.info("进行安全性检查...");
        List<Integer> work = new ArrayList<>(available);
        int processCount = data.size();
        Map<Integer, Boolean> finish = new HashMap<>();
        data.forEach((id, baData) -> finish.put(id, false));

        boolean flag;
        //从进程集合中找到满足条件的进程
        for (int i = 0; i < processCount; i++) {
            flag = false;

            for (Map.Entry<Integer, BAData> entry : data.entrySet()) {
                int id = entry.getKey();
                //finish为true表示可以获得所需资源，顺利执行完毕
                if (finish.get(id)) {
                    continue;
                }
                List<Integer> currentNeed = entry.getValue().getNeed();
                //j表示资源的类型
                int j;
                //判断需求量是否大于可用资源量
                for (j = 0; j < RESOURCE_NUM; j++) {
                    if (currentNeed.get(j) > work.get(j)) {
                        break;
                    }
                }
                if (j == RESOURCE_NUM) {
                    List<Integer> currentAllocation = entry.getValue().getAllocation();
                    for (j = 0; j < RESOURCE_NUM; j++) {
                        //进程可以顺利完成，释放资源
                        work.set(j, work.get(j) + currentAllocation.get(j));
                    }
                    //更改标志位
                    finish.put(id, true);
                    flag = true;
                    log.info("进程P" + id + "执行完毕");
                    break;
                }
            }
            //flag为false表示现有资源不满足任意进程的需求
            if (!flag) {
                break;
            }
        }

        //判断是否所有进程的finish标志位是否全为true
        for (Map.Entry<Integer, Boolean> entry : finish.entrySet()) {
            if (!entry.getValue()) {
                log.info("安全性检查不通过");
                return false;
            }
        }
        log.info("安全性检查通过");
        return true;
    }

    /**
     * 回滚试探性分配的资源
     * @param request 资源请求
     */
    private void rollbackAllocation(ResourceRequest request) {
        int id = request.getId();
        List<Integer> requestSource = request.getSource();
        List<Integer> currentAllocation = data.get(id).getAllocation();
        List<Integer> currentNeed = data.get(id).getNeed();

        //修改可利用资源数量、已分配资源、仍需求资源
        for (int i = 0; i < RESOURCE_NUM; i++) {
            available.set(i, available.get(i) + requestSource.get(i));
            currentAllocation.set(i, currentAllocation.get(i) - requestSource.get(i));
            currentNeed.set(i, currentNeed.get(i) + requestSource.get(i));
        }
        //更新数据
        data.get(id).setAllocation(currentAllocation);
        data.get(id).setNeed(currentNeed);
    }

    /**
     * 释放资源后更新可用资源数量
     * @param list 资源量列表
     */
    public void release(List<Integer> list) {
        available.set(0, available.get(0) + list.get(0));
        available.set(1, available.get(1) + list.get(1));
        available.set(2, available.get(2) + list.get(2));
    }
}
