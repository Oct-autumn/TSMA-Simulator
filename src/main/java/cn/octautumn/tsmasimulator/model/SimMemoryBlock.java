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
     * ID
     */
    private final int ID;
    /**
     * 起始位置
     */
    private int startPos;
    /**
     * 终止位置
     */
    private int stopPos;
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
        USING,
        RESERVED
    }
}
