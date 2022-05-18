package cn.octautumn.tsmasimulator.service;

import cn.octautumn.tsmasimulator.model.SimProcess;
import cn.octautumn.tsmasimulator.model.SimProcessor;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessorService
{
    public static final ArrayList<SimProcessor> processorList = new ArrayList<>();

    public static final ConcurrentHashMap<Integer, SimProcess> runningProcessMap = new ConcurrentHashMap<>();

    public void runProcess()
    {

    }
}
