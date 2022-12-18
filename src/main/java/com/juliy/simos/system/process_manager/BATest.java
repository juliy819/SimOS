package com.juliy.simos.system.process_manager;

import com.juliy.simos.system.process_manager.deadlock.BAException;
import com.juliy.simos.system.process_manager.deadlock.ResourceRequest;
import com.juliy.simos.system.resource_manager.ResourceManager;
import lombok.Data;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 银行家算法
 * @author JuLiy
 * @date 2022/12/12 13:50
 */
@Data
public class BATest {

    private static final Logger log = Logger.getLogger(BATest.class);

    //可利用资源变量
    private final List<Integer> available = new ArrayList<>();

    //最大资源需求矩阵
    private final List<List<Integer>> max = new ArrayList<>();

    //资源分配矩阵
    private final List<List<Integer>> allocation = new ArrayList<>();

    //仍需资源需求矩阵
    private final List<List<Integer>> need = new ArrayList<>();

    private ResourceManager resourceManager;

    public void initResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        available.add(resourceManager.getTotalANum());
        available.add(resourceManager.getTotalBNum());
        available.add(resourceManager.getTotalCNum());
    }

    //银行家算法
    public void bankerAlgorithm(ResourceRequest request) {
        //请求资源进程的编号
        int id = request.getId();
        //判断请求资源是否超过进程所声明的最大需求数
        for (int i = 0; i < available.size(); i++) {
            if (request.getSource().get(i) > need.get(id).get(i)) {
                throw new BAException("进程P" + id + "请求资源超过其申明的最大值，发生异常");
            }
        }
        //判断OS当前是否可以满足进程此次的资源请求
        for (int i = 0; i < available.size(); i++) {
            if (request.getSource().get(i) > available.get(i)) {
                throw new BAException("当前OS尚无足够的资源满足进程P" + id + "请求资源，发生异常");
            }
        }
        //进行资源的试探分配
        resourceAllocation(request);
        //进行安全性检查
        if (securityCheck()) {
            resourceManager.allocate(request);
            log.info("进程" + id + "申请资源" + request.getSource() + "分配成功！");
        } else {
            rollbackAllocation(request);
            log.info("进程" + id + "申请资源" + request.getSource() + "不可分配！");
        }
    }

    //资源分配，直接分配，如果安全性检查不通过，进行资源回滚释放
    private void resourceAllocation(ResourceRequest request) {
        //请求资源进程的编号
        int id = request.getId();
        List<Integer> requestSource = request.getSource();
        //当前进程已经分配的资源
        List<Integer> currentAllocation = allocation.get(id);
        //当前进程仍需的资源
        List<Integer> currentNeed = need.get(id);

        //修改可利用资源数量、已分配资源、仍需求资源
        //注：因为在前面已经判断过request<=need和available,所以资源不会变成负,不需要处理异常
        for (int i = 0; i < available.size(); i++) {
            available.set(i, available.get(i) - requestSource.get(i));
            currentAllocation.set(i, currentAllocation.get(i) + requestSource.get(i));
            currentNeed.set(i, currentNeed.get(i) - requestSource.get(i));
        }
        //更新总的分配矩阵
        allocation.set(id, currentAllocation);
        //更新总的需求矩阵
        need.set(id, currentNeed);
    }

    //安全性检查算法
    private boolean securityCheck() {
        //步骤1：初始化临时变量work和
        List<Integer> work = new ArrayList<>(available);
        int processCount = max.size();
        List<Boolean> finish = new ArrayList<>();
        for (int i = 0; i < processCount; i++) {
            finish.add(false);
        }
        //i表示已执行多少个进程，id表示可执行的进程号，j表示资源的类型
        int i, j, id;
        //在步骤1中是否找到一个能满足条件的进程，即是否有进程可以执行完毕，释放资源
        boolean flag;
        //步骤1：从进程集合中找到满足条件的进程
        for (i = 0; i < processCount; i++) {
            flag = false;
            for (id = 0; id < processCount; id++) {
                //finish为true表示可以获得所需资源，顺利执行完毕
                if (finish.get(id)) {
                    continue;
                }
                List<Integer> currentNeed = need.get(id);
                //j表示资源的类型
                for (j = 0; j < work.size(); j++) {
                    if (currentNeed.get(j) > work.get(j)) {
                        break;
                    }
                }
                //当前进程id所需的资源可以得到满足，转而执行步骤2
                if (j == work.size()) {
                    //步骤2
                    List<Integer> currentAllocation = allocation.get(id);
                    for (j = 0; j < work.size(); j++) {
                        //进程可以顺利完成，释放资源
                        work.set(j, work.get(j) + currentAllocation.get(j));

                    }
                    //更改标志位
                    finish.set(id, true);
                    flag = true;
                    //跳出循环，执行步骤1
                    log.info("此进程执行完毕：P" + id);
                    break;
                }
            }
            //上接break
            //判断是否进程分配资源，扫描进程集合一遍，如未分配资源，则表示没有进程可以继续执行，
            //立刻跳出循环，转而执行步骤3
            if (!flag) {
                break;
            }
        }

        //步骤3：判断是否所有进程的finish标志位是否全为true
        for (id = 0; id < processCount; id++) {
            //如果有一个为false，则处于不安全状态
            if (!finish.get(id)) {
                return false;
            }
        }
        return true;
    }

    //安全性检查不通过，回滚刚才分配的资源
    private void rollbackAllocation(ResourceRequest request) {
        //请求资源进程的编号
        int id = request.getId();
        List<Integer> requestSource = request.getSource();
        //当前进程已经分配的资源
        List<Integer> currentAllocation = allocation.get(id);
        //当前进程仍需的资源
        List<Integer> currentNeed = need.get(id);

        //修改可利用资源数量、已分配资源、仍需求资源
        //注：因为在前面已经判断过request<=need和available,所以资源不会变成负,不需要处理异常
        for (int i = 0; i < available.size(); i++) {
            available.set(i, available.get(i) + requestSource.get(i));
            currentAllocation.set(i, currentAllocation.get(i) - requestSource.get(i));
            currentNeed.set(i, currentNeed.get(i) + requestSource.get(i));
        }
        //更新总的分配矩阵
        allocation.set(id, currentAllocation);
        //更新总的需求矩阵
        need.set(id, currentNeed);
    }

    private void printResourceAllocationPic() {
        for (int i = 0; i < max.size(); i++) {
            log.info(max.get(i) + " " + allocation.get(i) + " " + need.get(i));
        }
        log.info("当前可用资源：" + available);
    }


}
