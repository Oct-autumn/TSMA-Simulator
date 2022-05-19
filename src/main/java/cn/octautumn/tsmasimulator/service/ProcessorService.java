package cn.octautumn.tsmasimulator.service;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.model.Sim.SimProcess;
import cn.octautumn.tsmasimulator.model.Sim.SimProcessor;

import java.util.concurrent.ConcurrentHashMap;

public class ProcessorService
{
    public final ConcurrentHashMap<Integer, SimProcessor> processorMap = new ConcurrentHashMap<>();

    public final ConcurrentHashMap<Integer, SimProcessor> runningProcessMap = new ConcurrentHashMap<>();

    public void reset()
    {
        processorMap.clear();
        for (int i = 0; i < CoreResource.simulatorConfig.getProcessorCount(); i++)
            processorMap.put(i, new SimProcessor(i));
    }

    public void start()
    {
        processorMap.forEach((key, simProcessor) -> {
            try
            {
                Thread.sleep(2);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            simProcessor.getProcessorRunnable().start();
        });
    }

    public void stop()
    {
        processorMap.forEach((key, simProcessor) -> simProcessor.getProcessorRunnable().stop());
    }

    public void runProcessOnProcessor(Integer pid, Integer processorId)
    {
        if (runningProcessMap.containsKey(pid))
            return;
        //如果有正在运行的进程，将其撤销掉
        if (processorMap.get(processorId).getRunningPID() != null)
            runningProcessMap.remove(processorMap.get(processorId).getRunningPID());
        processorMap.get(processorId).getProcessorRunnable().changeRunningPID(pid);
        if (pid != null)
            runningProcessMap.put(pid, processorMap.get(processorId));
    }

    public void removeProcessFromProcessor(Integer pid)
    {
        runningProcessMap.get(pid).getProcessorRunnable().changeRunningPID(null);
        runningProcessMap.remove(pid);
    }

    /**
     * 尝试抢占处理机
     *
     * @param simProcess 进程实例
     * @return 是否成功
     */
    public boolean tryToPreemptProcessor(SimProcess simProcess)
    {
        if (CoreResource.processService.runningPL.size() < CoreResource.simulatorConfig.getProcessorCount())
        {
            for (SimProcessor it : processorMap.values())
            {
                if (it.getStatus() == SimProcessor.Status.IDLE
                        && CoreResource.processService.tryToRunReadyProcess(simProcess.getPID()))
                {
                    CoreResource.processorService.runProcessOnProcessor(simProcess.getPID(), it.getProcessorID());
                    return true;
                }
            }
        } else
        {
            int prePID = CoreResource.processService.runningPL.lastElement();
            if (CoreResource.processService.findProcessByPID(prePID).getPriority() >= simProcess.getPriority())
                return false;

            int processorID = CoreResource.processorService.runningProcessMap.get(prePID).getProcessorID();
            if (CoreResource.processService.tryToRunReadyProcess(simProcess.getPID()))
            {
                CoreResource.processorService.runProcessOnProcessor(simProcess.getPID(), processorID);
                CoreResource.processService.processRunOver(prePID);
                return true;
            }
        }
        return false;
    }
}
