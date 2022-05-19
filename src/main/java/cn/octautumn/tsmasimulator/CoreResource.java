package cn.octautumn.tsmasimulator;

import cn.octautumn.tsmasimulator.SceneController.ConfigSceneController;
import cn.octautumn.tsmasimulator.SceneController.MainSceneController;
import cn.octautumn.tsmasimulator.model.SimMemoryBlock;
import cn.octautumn.tsmasimulator.model.SimProcess;
import cn.octautumn.tsmasimulator.model.SimProcessor;
import cn.octautumn.tsmasimulator.service.MemoryService;
import cn.octautumn.tsmasimulator.service.ProcessService;
import cn.octautumn.tsmasimulator.service.ProcessorService;
import javafx.stage.Stage;

public class CoreResource
{
    public static final SimulatorConfig simulatorConfig =
            new SimulatorConfig(2, 4, 2048, 128, 5);
    public static final MemoryService memoryService = new MemoryService();
    public static final ProcessService processService = new ProcessService();

    public static Stage mainStage;
    public static MainSceneController mainSceneController;
    public static Stage configStage;
    public static ConfigSceneController configSceneController;

    /**
     * 重置模拟器（将处理机列表、进程列表、内存分块列表重置为初始状态）
     */
    public static void resetSimulator()
    {
        //重置处理机列表
        ProcessorService.processorList.clear();
        for (int i = 0; i < simulatorConfig.getProcessorCount(); i++)
            ProcessorService.processorList.add(SimProcessor.builder()
                    .processorID(i)
                    .runningPID(-1)
                    .status(SimProcessor.Status.IDLE)
                    .timeSliceEla(0)
                    .build());

        //重置内存分块列表
        memoryService.reset();

        //重置进程列表
        processService.reset();
    }
}
