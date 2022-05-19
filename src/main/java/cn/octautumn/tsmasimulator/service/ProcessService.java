package cn.octautumn.tsmasimulator.service;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.model.SimMemoryBlock;
import cn.octautumn.tsmasimulator.model.SimProcess;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessService
{
    @Getter
    private static final ConcurrentHashMap<Integer, SimProcess> processMap = new ConcurrentHashMap<>();
    public static final ObservableList<Integer> backPL = FXCollections.observableArrayList();           //后备进程队列
    public static final ObservableList<Integer> blockPL = FXCollections.observableArrayList();          //阻塞进程队列
    public static final ObservableList<Integer> hangPL = FXCollections.observableArrayList();           //挂起进程队列
    public static final ObservableList<Integer> finishPL = FXCollections.observableArrayList();         //完成进程队列
    public static final ObservableList<Integer> readyPL = FXCollections.observableArrayList();          //就绪进程队列
    public static final ObservableList<Integer> runningPL = FXCollections.observableArrayList();        //运行中进程队列
    public static int pidNum = 0;

    public void reset()
    {
        processMap.clear();
        backPL.clear();
        blockPL.clear();
        hangPL.clear();
        finishPL.clear();
        readyPL.clear();
        runningPL.clear();
        //测试进程
        createNewProcess("测试进程",
                30, 20,
                SimProcess.Property.INDEPENDENT,
                null,
                CoreResource.simulatorConfig.getSystemReservedMemSize()
        );
        CoreResource.mainSceneController.refreshTViewThreadTable();
    }

    /**
     * 通过PID查找进程
     *
     * @param pid 进程的PID
     * @return 进程实例（未找到则为null）
     */
    public SimProcess findProcessByPID(Integer pid)
    {
        return processMap.get(pid);
    }

    /**
     * 通过分配的内存起始的位置查找进程
     *
     * @param memStartPos 进程分配到的内存的起始位置
     * @return 进程实例（未找到则为null）
     */
    public SimProcess findProcessByMemStartPos(Integer memStartPos)
    {
        for (SimProcess it : processMap.values())
        {
            if (it.getMemStartPos() == memStartPos)
                return it;
        }
        return null;
    }

    /**
     * 创建新进程
     * <p>
     * （创建的进程会优先尝试进入就绪队列，若就绪队列已满或无法分配内存，则进入后备队列）
     *
     * @param name              进程名（可留空）
     * @param totalRunTime      总运行时长
     * @param priority          优先级（不能低于0）
     * @param property          进程属性
     * @param associatedPidList 关联进程PID列表（独立进程则留空）
     * @param requireMemSize    需要的内存大小
     * @return 新建进程的pid
     */
    public int createNewProcess(String name, int totalRunTime, int priority, SimProcess.Property property, ArrayList<Integer> associatedPidList, int requireMemSize)
    {
        SimProcess newProcess = SimProcess.builder()
                .PName(name)
                .PID(pidNum)
                .totalRunTime(totalRunTime)
                .elapsedTime(0)
                .priority(priority)
                .property(property)
                .associatedPidList(associatedPidList)
                .requireMemSize(requireMemSize)
                .build();
        pidNum++;

        if (readyPL.size() < CoreResource.simulatorConfig.getMaxReadyProcess())
        {
            //就绪队列不满，尝试为其分配内存
            int memStartPos = CoreResource.memoryService.allocMemBlock(requireMemSize, SimMemoryBlock.Status.ACTIVE);
            if (memStartPos != -1)
            {
                //成功分配内存，进入就绪队列
                newProcess.setStatus(SimProcess.Status.READY);
                newProcess.setMemStartPos(memStartPos);
            } else
            {//内存分配失败，触发内存回收，挂起阻塞中的进程来释放内存
                while (tryToHangupFirstBlockedProcess())
                {
                    memStartPos = CoreResource.memoryService.allocMemBlock(requireMemSize, SimMemoryBlock.Status.ACTIVE);
                    if (memStartPos != -1)
                    {
                        //成功分配内存，进入就绪队列
                        newProcess.setStatus(SimProcess.Status.READY);
                        newProcess.setMemStartPos(memStartPos);
                        break;
                    }
                }
                if (newProcess.getStatus() != SimProcess.Status.READY)
                    newProcess.setStatus(SimProcess.Status.BACK);   //仍没有成功分配内存，进入后备队列
            }
        } else
            newProcess.setStatus(SimProcess.Status.BACK);   //就绪队列已满，进入后备队列

        processMap.put(newProcess.getPID(), newProcess);

        if (newProcess.getStatus() == SimProcess.Status.READY)
            readyPL.add(newProcess.getPID());
        else if (newProcess.getStatus() == SimProcess.Status.BACK)
            backPL.add(newProcess.getPID());


        CoreResource.mainSceneController.refreshTViewThreadTable();
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
        CoreResource.mainSceneController.refreshTViewThreadTable();
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
                runningPL.remove((Integer) thisProcess.getPID());
                // TODO 从处理机上撤下
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
        CoreResource.mainSceneController.refreshTViewThreadTable();
        return true;
    }

    /**
     * 尝试解挂进程
     *
     * @param pid 进程id
     * @return 是否成功解挂
     */
    public boolean tryToUnHangingProcess(Integer pid)
    {
        SimProcess thisProcess = processMap.get(pid);
        if (thisProcess == null)
            return false;
        switch (thisProcess.getStatus())
        {
            case SYS_HANGUP, USER_HANGUP ->
            {
                switch (thisProcess.getProperty())
                {

                    case INDEPENDENT ->
                    {
                    }
                    case SYNCHRONIZE_SUC ->
                    {
                        //检查前驱进程是否均已完成
                        for (int assocPid : thisProcess.getAssociatedPidList())
                        {
                            if (processMap.get(assocPid).getStatus() != SimProcess.Status.REVOKE)
                                return false;
                        }
                    }
                }

                int memStartPos = CoreResource.memoryService.allocMemBlock(thisProcess.getRequireMemSize(), SimMemoryBlock.Status.ACTIVE);
                if (memStartPos == -1)
                    if (thisProcess.getStatus() == SimProcess.Status.USER_HANGUP)
                        return sysHangupProcess(thisProcess.getPID());  //用户挂起转为系统挂起
                    else
                        return false;   //解挂失败

                thisProcess.setStatus(SimProcess.Status.READY);
                thisProcess.setMemStartPos(memStartPos);
                hangPL.remove((Integer) thisProcess.getPID());
                readyPL.add(thisProcess.getPID());
            }

            default ->
            {
                return false;
            }
        }
        CoreResource.mainSceneController.refreshTViewThreadTable();
        return true;
    }

    /**
     * 尝试挂起内存中的第一个阻塞进程以回收内存
     *
     * @return 是否成功挂起阻塞进程
     */
    public boolean tryToHangupFirstBlockedProcess()
    {
        SimMemoryBlock aInactiveMemBlock = CoreResource.memoryService.findFirstInactiveMemBlock();

        if (aInactiveMemBlock == null)
            return false;

        SimProcess aBlockedProcess = findProcessByMemStartPos(aInactiveMemBlock.getStartPos());
        sysHangupProcess(aBlockedProcess.getPID());
        CoreResource.mainSceneController.refreshTViewThreadTable();
        return true;
    }

    public ObservableList<Integer> getOptionalPidList()
    {
        ObservableList<Integer> optionalPidList = FXCollections.observableArrayList();
        for (SimProcess it : processMap.values())
            optionalPidList.add(it.getPID());
        Collections.sort(optionalPidList);

        return optionalPidList;
    }
}
