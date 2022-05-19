package cn.octautumn.tsmasimulator.model.Sim;

import cn.octautumn.tsmasimulator.CoreResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class SimProcessor
{
    public class ProcessorRunnable implements Runnable
    {
        private Boolean keepRunning = true;

        @Override
        public void run()
        {
            while (keepRunning)
            {
                if (runningPID == null)
                {
                    //尝试调取一个新的进程运行
                    while (CoreResource.processService.readyPL.size() > 0)
                    {
                        int pid = CoreResource.processService.readyPL.get(0);
                        if (CoreResource.processService.tryToRunReadyProcess(pid))
                        {
                            CoreResource.processorService.runProcessOnProcessor(pid, processorID);
                            break;
                        }
                    }
                }
                //模拟运行过程
                synchronized (this)
                {
                    try
                    {
                        this.wait(CoreResource.simulatorConfig.getTimeStepLength());
                        if (runningPID == null) continue;
                        if (!keepRunning) break;

                        CoreResource.mainSceneController.refreshProcessorVis();
                        timeSliceEla += CoreResource.simulatorConfig.getTimeStepLength();
                        SimProcess simProcess = CoreResource.processService.findProcessByPID(runningPID);
                        simProcess.setElapsedTime(simProcess.getElapsedTime() + CoreResource.simulatorConfig.getTimeStepLength());
                        //时间片完成 或 进程完成运行
                        if (timeSliceEla == CoreResource.simulatorConfig.getTimeSliceLength() * 1000
                                || simProcess.getElapsedTime() >= simProcess.getTotalRunTime())
                        {
                            int priority = Math.max(simProcess.getPriority() - 1, 0);   //优先级降一级
                            simProcess.setPriority(priority);
                            CoreResource.processorService.removeProcessFromProcessor(simProcess.getPID());
                            CoreResource.processService.processRunOver(simProcess.getPID());
                        }
                    } catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }

            }
        }

        public void start()
        {
            this.keepRunning = true;
            new Thread(this).start();
        }

        public void stop()
        {
            this.keepRunning = false;
            synchronized (this)
            {
                this.notify();
            }
        }

        public void changeRunningPID(Integer pid)
        {
            if (pid == null)
            {
                status = Status.IDLE;
            }
            else
            {
                status = Status.USING;
            }
            timeSliceEla = 0;   //时间片计时清零
            runningPID = pid;
            synchronized (this)
            {
                this.notify();
            }
        }
    }

    /**
     * 处理机ID
     */
    private final int processorID;
    /**
     * 状态
     */
    private Status status;
    /**
     * 正在运行的进程id
     */
    private Integer runningPID;
    /**
     * 时间片经过时间（毫秒）
     */
    private int timeSliceEla;
    /**
     * 处理线程
     */
    private ProcessorRunnable processorRunnable;


    public SimProcessor(int processorID)
    {
        this.processorID = processorID;
        this.status = Status.IDLE;
        this.runningPID = null;
        this.timeSliceEla = 0;
        processorRunnable = new ProcessorRunnable();
    }

    public enum Status
    {
        IDLE,
        USING
    }
}
