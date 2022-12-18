package com.juliy.simos.system.resource_manager;

import com.juliy.simos.system.process_manager.deadlock.ResourceRequest;
import lombok.Data;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备管理器
 * @author JuLiy
 * @date 2022/10/24
 */
@Data
public class ResourceManager {

    private static final Logger log = Logger.getLogger(ResourceManager.class);

    private final List<ResourceA> resourceA = new ArrayList<>();
    private final List<ResourceB> resourceB = new ArrayList<>();
    private final List<ResourceC> resourceC = new ArrayList<>();

    private int totalANum = 15;
    private int totalBNum = 12;
    private int totalCNum = 6;
    private int freeANum = totalANum;
    private int freeBNum = totalBNum;
    private int freeCNum = totalCNum;


    public ResourceManager() {
        for (int i = 0; i < totalANum; i++) {
            resourceA.add(new ResourceA());
        }
        for (int i = 0; i < totalBNum; i++) {
            resourceB.add(new ResourceB());
        }
        for (int i = 0; i < totalCNum; i++) {
            resourceC.add(new ResourceC());
        }
    }

    /**
     * 资源分配
     * @param request 分配请求
     */
    public void allocate(ResourceRequest request) {
        int id = request.getId();
        List<Integer> list = request.getSource();

        //寻找空闲的资源进行分配
        allocate(id, list.get(0), resourceA);
        allocate(id, list.get(1), resourceB);
        allocate(id, list.get(2), resourceC);

        freeANum -= list.get(0);
        freeBNum -= list.get(1);
        freeCNum -= list.get(2);

        log.info("进程P" + id + "成功分配资源  " +
                         "当前可用资源数：" + freeANum + "-" + freeBNum + "-" + freeCNum);
    }

    /**
     * 资源分配
     * @param id        进程编号
     * @param num       所需资源数量
     * @param resources 资源类型
     */
    private void allocate(int id, int num, List<? extends Resource> resources) {
        for (int i = 0; i < num; i++) {
            for (Resource r : resources) {
                if (r.getStatus() == ResourceStatus.FREE) {
                    r.setId(id);
                    r.setStatus(ResourceStatus.USED);
                    break;
                }
            }
        }
    }

    /**
     * 释放进程占有的所有资源
     * @param id 进程编号
     */
    public void release(int id) {
        for (Resource a : resourceA) {
            if (a.getId() == id) {
                a.setId(-1);
                a.setStatus(ResourceStatus.FREE);
                freeANum++;
            }
        }
        for (Resource b : resourceB) {
            if (b.getId() == id) {
                b.setId(-1);
                b.setStatus(ResourceStatus.FREE);
                freeBNum++;
            }
        }
        for (Resource c : resourceC) {
            if (c.getId() == id) {
                c.setId(-1);
                c.setStatus(ResourceStatus.FREE);
                freeCNum++;
            }
        }
        log.info("进程P" + id + "成功释放资源  " +
                         "当前可用资源数：" + freeANum + "-" + freeBNum + "-" + freeCNum);
    }
}
