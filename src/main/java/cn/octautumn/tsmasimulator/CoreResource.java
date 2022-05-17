package cn.octautumn.tsmasimulator;

import cn.octautumn.tsmasimulator.model.SimMemoryBlock;
import cn.octautumn.tsmasimulator.model.SimProcess;
import cn.octautumn.tsmasimulator.model.SimProcessor;

import java.util.ArrayList;

public class CoreResource
{
    public static final SimulatorConfig simulatorConfig =
            new SimulatorConfig(2, 4, 2048, 128);
    public static final ArrayList<SimProcessor> processorList = new ArrayList<>();
    public static final ArrayList<SimProcess> processList = new ArrayList<>();
    public static final ArrayList<SimMemoryBlock> memoryBlockList = new ArrayList<>();

    /**
     * 重置模拟器（将处理机列表、进程列表、内存分块列表重置为初始状态）
     */
    public static void resetSimulator()
    {
        //重置处理机列表
        processorList.clear();
        for (int i = 0; i < simulatorConfig.getProcessorCount(); i++)
            processorList.add(SimProcessor.builder()
                    .processorID(i)
                    .runningPID(-1)
                    .status(SimProcessor.Status.IDLE)
                    .build());

        //重置进程列表
        processList.clear();
        processList.add(SimProcess.builder()
                .PName("系统进程")
                .PID(0)
                .requireRunTime(-1)
                .priority(0)
                .status(SimProcess.Status.READY)
                .property(SimProcess.Property.INDEPENDENT)
                .requireMemSize(simulatorConfig.getSystemReservedMemSize())
                .memStartPos(0)
                .build());

        //重置内存分块列表
        memoryBlockList.clear();
        memoryBlockList.add(SimMemoryBlock.builder()
                .ID(0)
                .startPos(0)
                .stopPos(simulatorConfig.getSystemReservedMemSize() - 1)
                .totalSize(simulatorConfig.getSystemReservedMemSize())
                .status(SimMemoryBlock.Status.RESERVED)
                .build());
        memoryBlockList.add(SimMemoryBlock.builder()
                .ID(1)
                .startPos(simulatorConfig.getSystemReservedMemSize())
                .stopPos(simulatorConfig.getTotalMemorySize() - 1)
                .totalSize(simulatorConfig.getTotalMemorySize() - simulatorConfig.getSystemReservedMemSize())
                .status(SimMemoryBlock.Status.IDLE)
                .build());
    }
}
