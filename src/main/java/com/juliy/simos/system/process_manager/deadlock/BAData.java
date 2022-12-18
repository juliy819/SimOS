package com.juliy.simos.system.process_manager.deadlock;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 银行家算法中的数据结构
 * @author JuLiy
 * @date 2022/12/15 20:55
 */
@Data
public class BAData {
    private List<Integer> max = new ArrayList<>();
    private List<Integer> need = new ArrayList<>();
    private List<Integer> allocation = new ArrayList<>();

}
