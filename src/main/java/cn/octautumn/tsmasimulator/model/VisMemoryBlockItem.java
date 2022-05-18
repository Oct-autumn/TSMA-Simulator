package cn.octautumn.tsmasimulator.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisMemoryBlockItem
{
    /**
     * 起始位置
     */
    private String startPos;
    /**
     * 终止位置
     */
    private String endPos;
    /**
     * 总大小
     */
    private String totalSize;
    /**
     * 状态
     */
    private String status;

    public VisMemoryBlockItem(SimMemoryBlock simMemoryBlock)
    {
        this.startPos = String.valueOf(simMemoryBlock.getStartPos());
        this.endPos = String.valueOf(simMemoryBlock.getEndPos());
        this.totalSize = String.valueOf(simMemoryBlock.getTotalSize());

        switch (simMemoryBlock.getStatus())
        {
            case IDLE -> this.status = "空闲";
            case INUSE -> this.status = "使用中";
            case RESERVED ->this.status = "保留";
        }
    }
}
