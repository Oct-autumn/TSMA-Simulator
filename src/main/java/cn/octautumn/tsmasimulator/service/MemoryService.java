package cn.octautumn.tsmasimulator.service;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.model.Sim.SimMemoryBlock;

import java.util.ArrayList;

public class MemoryService
{

    public static final ArrayList<SimMemoryBlock> memoryBlockList = new ArrayList<>();

    public void reset()
    {
        memoryBlockList.clear();
        memoryBlockList.add(SimMemoryBlock.builder()
                .startPos(0)
                .endPos(CoreResource.simulatorConfig.getTotalMemorySize() - 1)
                .totalSize(CoreResource.simulatorConfig.getTotalMemorySize())
                .status(SimMemoryBlock.Status.IDLE)
                .build());
        allocMemBlock(CoreResource.simulatorConfig.getSystemReservedMemSize(), SimMemoryBlock.Status.RESERVED);
    }

    /**
     * 释放内存块（会合并空闲内存块）
     *
     * @param startPos 要释放的内存块的起始位置
     */
    public void freeMemBlock(Integer startPos)
    {
        SimMemoryBlock thisMemoryBlock = findMemBlock(startPos);
        int i = memoryBlockList.indexOf(thisMemoryBlock);
        SimMemoryBlock preMergeBlock = null, nextMergeBlock = null;
        if (i != 0)
        {
            //前向查找一个块，空闲则合并
            SimMemoryBlock preMemoryBlock = memoryBlockList.get(i - 1);
            if (preMemoryBlock.getStatus() == SimMemoryBlock.Status.IDLE)
            {
                thisMemoryBlock.setStartPos(preMemoryBlock.getStartPos());
                thisMemoryBlock.setTotalSize(preMemoryBlock.getTotalSize() + thisMemoryBlock.getTotalSize());
                preMergeBlock = preMemoryBlock;
            }
        }
        if (i != (memoryBlockList.size() - 1))
        {
            //后向查找一个块，空闲则合并
            SimMemoryBlock nextMemoryBlock = memoryBlockList.get(i + 1);
            if (nextMemoryBlock.getStatus() == SimMemoryBlock.Status.IDLE)
            {
                thisMemoryBlock.setEndPos(nextMemoryBlock.getEndPos());
                thisMemoryBlock.setTotalSize(nextMemoryBlock.getTotalSize() + thisMemoryBlock.getTotalSize());
                nextMergeBlock = nextMemoryBlock;
            }
        }

        if (preMergeBlock != null) memoryBlockList.remove(preMergeBlock);
        if (nextMergeBlock != null) memoryBlockList.remove(nextMergeBlock);
        thisMemoryBlock.setStatus(SimMemoryBlock.Status.IDLE);

        CoreResource.mainSceneController.refreshMemBlockVis();
    }

    /**
     * 分配内存块
     *
     * @param requireSize 要分配的内存块大小
     * @return 分配的内存块起始位置（-1则为分配失败）
     */
    public int allocMemBlock(Integer requireSize, SimMemoryBlock.Status status)
    {
        int memoryBlockCount = memoryBlockList.size();
        for (int i = 0; i < memoryBlockCount; i++)
        {
            SimMemoryBlock thisMemoryBlock = memoryBlockList.get(i);
            if (thisMemoryBlock.getStatus() == SimMemoryBlock.Status.IDLE
                    && thisMemoryBlock.getTotalSize() >= requireSize)
            {
                int ret;
                if (thisMemoryBlock.getTotalSize() == requireSize)
                {
                    thisMemoryBlock.setStatus(SimMemoryBlock.Status.ACTIVE);
                    ret = thisMemoryBlock.getStartPos();
                } else
                {
                    memoryBlockList.add(i, new SimMemoryBlock(
                            thisMemoryBlock.getStartPos(),
                            thisMemoryBlock.getStartPos() + requireSize - 1,
                            requireSize,
                            status));
                    thisMemoryBlock.setStartPos(thisMemoryBlock.getStartPos() + requireSize);
                    thisMemoryBlock.setTotalSize(thisMemoryBlock.getTotalSize() - requireSize);
                    ret = memoryBlockList.get(i).getStartPos();
                }
                CoreResource.mainSceneController.refreshMemBlockVis();
                return ret;
            }
        }
        return -1;
    }

    /**
     * 通过起始位置查找内存块
     * @param startPos 内存块的起始位置
     * @return 内存块实例
     */
    public SimMemoryBlock findMemBlock(Integer startPos)
    {
        for (SimMemoryBlock it : memoryBlockList)
        {
            if (it.getStartPos() == startPos)
                return it;
        }
        return null;
    }

    /**
     * 查找第一块非活动内存
     * @return 该内存块实例（未找到则为null）
     */
    public SimMemoryBlock findFirstInactiveMemBlock()
    {
        for (SimMemoryBlock it : memoryBlockList)
        {
            if (it.getStatus() == SimMemoryBlock.Status.INACTIVE)
                return it;
        }
        return null;
    }
}
