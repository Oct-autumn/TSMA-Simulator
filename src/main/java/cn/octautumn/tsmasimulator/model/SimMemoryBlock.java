package cn.octautumn.tsmasimulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SimMemoryBlock
{
    /**
     * 起始位置
     */
    private int startPos;
    /**
     * 终止位置
     */
    private int endPos;
    /**
     * 总大小
     */
    private int totalSize;
    /**
     * 状态
     */
    private Status status;

    public enum Status
    {
        IDLE,
        ACTIVE,
        INACTIVE,
        RESERVED
    }
}
