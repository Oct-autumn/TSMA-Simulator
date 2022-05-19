package cn.octautumn.tsmasimulator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SimulatorConfig
{
    /**
     * 处理机数量
     */
    public int processorCount;
    /**
     * 最大就绪队列（道数）
     */
    public int maxReadyProcess;
    /**
     * 总内存大小
     */
    public int totalMemorySize;
    /**
     * 系统保留内存大小
     */
    public int systemReservedMemSize;
    /**
     * 时间片长度（单位：秒）
     */
    public int timeSliceLength;
    /**
     * 模拟步长（单位：毫秒）
     */
    public int timeStepLength;
}
