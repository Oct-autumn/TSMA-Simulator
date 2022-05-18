package cn.octautumn.tsmasimulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SimProcess
{
    /**
     * 名称
     */
    private final String PName;
    /**
     * ID
     */
    private final int PID;
    /**
     * 需要运行的总时间（设为 -1 则一直运行）
     */
    private int totalRunTime;
    /**
     * 已运行的时间
     */
    private int elapsedTime;
    /**
     * 优先级
     */
    private int priority;
    /**
     * 状态
     */
    private Status status;
    /**
     * 属性（独立/同步）
     */
    private Property property;
    /**
     * 关联进程ID（设为 -1 置空）
     */
    private int associatedPID;
    /**
     * 需要的内存大小
     */
    private final int requireMemSize;
    /**
     * 占用内存的起始位置（未分配则设为 -1）
     */
    private int memStartPos;

    public enum Status
    {
        BACK,
        READY,
        RUNNING,
        BLOCK,
        SYS_HANGUP,
        USER_HANGUP,
        REVOKE
    }

    public enum Property
    {
        INDEPENDENT,
        SYNCHRONIZE_PRE,
        SYNCHRONIZE_SUC
    }
}
