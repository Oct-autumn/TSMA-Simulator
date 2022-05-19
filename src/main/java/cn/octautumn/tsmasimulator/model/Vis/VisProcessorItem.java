package cn.octautumn.tsmasimulator.model.Vis;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.model.Sim.SimProcess;
import cn.octautumn.tsmasimulator.model.Sim.SimProcessor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisProcessorItem
{
    private String processorID;
    private String statusInfo;
    private double timeSlicePercentage;
    private double processRunTimePercentage;

    public VisProcessorItem(SimProcessor simProcessor)
    {
        this.processorID = String.valueOf(simProcessor.getProcessorID());
        switch (simProcessor.getStatus())
        {
            case IDLE ->
            {
                this.statusInfo = "（空闲）";
                timeSlicePercentage = 0d;
                processRunTimePercentage = 0d;
            }
            case USING ->
            {
                this.statusInfo = String.format("（运行进程PID：%d）", simProcessor.getRunningPID());
                timeSlicePercentage = (double) simProcessor.getTimeSliceEla() / (CoreResource.simulatorConfig.getTimeSliceLength() * 1000);
                SimProcess simProcess =  CoreResource.processService.findProcessByPID(simProcessor.getRunningPID());
                processRunTimePercentage = (double) simProcess.getElapsedTime() / simProcess.getTotalRunTime();
            }
        }
    }
}
