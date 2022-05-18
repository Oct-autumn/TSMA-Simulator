package cn.octautumn.tsmasimulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SimProcessor
{
    /**
     * 处理机ID
     */
    private final int processorID;
    /**
     * 正在运行的进程id
     */
    private int runningPID;
    /**
     * 状态
     */
    private Status status;

    public enum Status
    {
        IDLE,
        USING
    }
}
