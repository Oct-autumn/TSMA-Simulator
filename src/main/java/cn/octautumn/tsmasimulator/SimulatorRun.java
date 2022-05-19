package cn.octautumn.tsmasimulator;

import cn.octautumn.tsmasimulator.model.SimProcess;
import cn.octautumn.tsmasimulator.service.ProcessService;

import java.util.Comparator;

import static cn.octautumn.tsmasimulator.CoreResource.processService;

public class SimulatorRun implements Runnable
{
    private static Boolean keepRunning = true;

    @Override
    public void run()
    {
        while (keepRunning)
        {
            try
            {
                Thread.sleep(100);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            nextTimeStep();
        }
    }

    public void nextTimeStep()
    {
        for (Integer it : processService.getHangPL())
        {
            SimProcess thisProcess = processService.findProcessByPID(it);
            if (thisProcess.getStatus() == SimProcess.Status.SYS_HANGUP)
                processService.tryToUnHangingProcess(thisProcess.getPID());
        }
    }

    public void stop()
    {
        keepRunning = false;
    }

    public void start()
    {
        keepRunning = true;
        new Thread(this).start();
    }
}
