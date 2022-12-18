package com.juliy.simos.system.process_manager.deadlock;

import com.juliy.simos.entity.PCB;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 资源请求
 * @author JuLiy
 * @date 2022/12/15 18:10
 */
@Data
public class ResourceRequest {
    //请求资源的进程编号
    private int id;

    //请求的资源列表
    private List<Integer> source;

    /**
     * 生成资源请求，每种资源请求数量均为1
     * @param id 进程编号
     * @return 资源请求
     */
    public static ResourceRequest generateBaseRequest(int id) {
        ResourceRequest r = new ResourceRequest();
        r.setId(id);
        List<Integer> list = new ArrayList<>();
        Collections.addAll(list, 1, 1, 1);
        r.setSource(list);
        return r;
    }

    /** 生成申请剩余资源的请求 */
    public static ResourceRequest generateRemainRequest(PCB pcb) {
        ResourceRequest r = new ResourceRequest();
        r.setId(pcb.getPid());
        List<Integer> list = pcb.getNeedR();
        //int aNum = pcb.getNeedR().get(0);
        //int bNum = pcb.getNeedR().get(1);
        //int cNum = pcb.getNeedR().get(2);
        //Collections.addAll(list, aNum, bNum, cNum);
        r.setSource(list);
        return r;
    }
}
