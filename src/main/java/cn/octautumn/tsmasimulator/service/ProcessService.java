package cn.octautumn.tsmasimulator.service;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.model.SimProcess;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.ConcurrentHashMap;

public class ProcessService
{
    public static final ConcurrentHashMap<Integer, SimProcess> processMap = new ConcurrentHashMap<>();
    public static final ObservableList<Integer> backPL = FXCollections.observableArrayList();           //后备进程队列
    public static final ObservableList<Integer> blockPL = FXCollections.observableArrayList();          //阻塞进程队列
    public static final ObservableList<Integer> hangPL = FXCollections.observableArrayList();           //挂起进程队列
    public static final ObservableList<Integer> finishPL = FXCollections.observableArrayList();         //完成进程队列
    public static final ObservableList<Integer> readyPL = FXCollections.observableArrayList();          //就绪进程队列
    public static int pidNum = 0;

    public int addNewProcessIntoList(String name, int totalRunTime, int priority, SimProcess.Status status, SimProcess.Property property, int associatedPID, int requireMemSize)
    {
        SimProcess newProcess = SimProcess.builder()
                .PName(name)
                .PID(pidNum)
                .totalRunTime(totalRunTime)
                .priority(priority)
                .status(status)
                .property(property)
                .associatedPID(associatedPID)
                .requireMemSize(requireMemSize)
                .build();
        pidNum++;

        processMap.put(newProcess.getPID(), newProcess);
        backPL.add(newProcess.getPID());

        // TODO 刷新进程列表视图
        return ProcessService.pidNum - 1;
    }

    /**
     * 挂起进程-系统
     * <p>
     * （只能对阻塞进程/用户挂起进程使用，否则挂起失败）
     *
     * @param pid 进程id
     * @return 是否成功
     */
    public boolean sysHangupProcess(Integer pid)
    {
        SimProcess thisProcess = processMap.get(pid);
        switch (thisProcess.getStatus())
        {
            case BLOCK ->
            {
                thisProcess.setStatus(SimProcess.Status.SYS_HANGUP);
                CoreResource.memoryService.freeMemBlock(thisProcess.getMemStartPos());
                thisProcess.setMemStartPos(-1);
                blockPL.remove(thisProcess.getPID());
                hangPL.add(thisProcess.getPID());
            }
            case USER_HANGUP -> thisProcess.setStatus(SimProcess.Status.SYS_HANGUP);

            default ->
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 挂起进程-用户
     * <p>
     * （只能对阻塞进程/用户挂起进程使用，否则挂起失败）
     *
     * @param pid 进程id
     * @return 是否成功
     */
    public boolean userHangupProcess(Integer pid)
    {
        SimProcess thisProcess = processMap.get(pid);
        if (thisProcess == null)
            return false;
        switch (thisProcess.getStatus())
        {
            case READY ->
            {
                thisProcess.setStatus(SimProcess.Status.USER_HANGUP);
                CoreResource.memoryService.freeMemBlock(thisProcess.getMemStartPos());
                thisProcess.setMemStartPos(-1);
                readyPL.remove((Integer) thisProcess.getPID());
                hangPL.add(thisProcess.getPID());
            }
            case RUNNING ->
            {
                thisProcess.setStatus(SimProcess.Status.USER_HANGUP);
                CoreResource.memoryService.freeMemBlock(thisProcess.getMemStartPos());
                thisProcess.setMemStartPos(-1);
                readyPL.remove((Integer) thisProcess.getPID());
                // TODO
                hangPL.add(thisProcess.getPID());
            }
            case BLOCK ->
            {
                thisProcess.setStatus(SimProcess.Status.USER_HANGUP);
                CoreResource.memoryService.freeMemBlock(thisProcess.getMemStartPos());
                thisProcess.setMemStartPos(-1);
                blockPL.remove((Integer) thisProcess.getPID());
                hangPL.add(thisProcess.getPID());
            }

            case SYS_HANGUP -> thisProcess.setStatus(SimProcess.Status.USER_HANGUP);
            default ->
            {
                return false;
            }
        }
        return true;
    }
}
