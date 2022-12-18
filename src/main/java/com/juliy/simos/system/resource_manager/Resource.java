package com.juliy.simos.system.resource_manager;

import lombok.Data;

/**
 * 临界资源
 * @author JuLiy
 * @date 2022/12/12 22:26
 */
@Data
public abstract class Resource {
    /** 资源状态 */
    private ResourceStatus status = ResourceStatus.FREE;
    /** 被占有的进程id */
    private int id = -1;
}
