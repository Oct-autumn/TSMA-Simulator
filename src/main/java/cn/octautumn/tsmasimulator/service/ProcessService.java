package cn.octautumn.tsmasimulator.service;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.model.Sim.SimMemoryBlock;
import cn.octautumn.tsmasimulator.model.Sim.SimProcess;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Synchronized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessService
{
    @Getter
    private final ConcurrentHashMap<Integer, SimProcess> processMap = new ConcurrentHashMap<>();

    @AllArgsConstructor
    public class ProcessIdList extends Vector<Integer>
    {
        @Override
        public synchronized boolean add(Integer pid)
        {
            if (this.isEmpty())
                return super.add(pid);
            int priority0 = processMap.get(pid).getPriority();

            for (int i = 0; i < this.size(); i++)
            {
                int priority = processMap.get(this.get(i)).getPriority();
                if (priority0 > priority)
                {
                    super.add(i, pid);
                    return true;
                } else if (priority0 == priority)
                    if (pid > this.get(i))
                    {
                        super.add(i, pid);
                        return true;
                    }
            }
            return super.add(pid);
        }
    }

    @Getter
    public final ProcessIdList backPL = new ProcessIdList();    //后备进程队列
    @Getter
    public final ProcessIdList blockPL = new ProcessIdList();   //阻塞进程队列
    @Getter
    public final ProcessIdList hangPL = new ProcessIdList();    //挂起进程队列
    @Getter
    public final ProcessIdList finishPL = new ProcessIdList();  //完成进程队列
    @Getter
    public final ProcessIdList readyPL = new ProcessIdList();   //就绪进程队列
    @Getter
    public final ProcessIdList runningPL = new ProcessIdList(); //运行中进程队列

    public int pidNum = 0;

    public void reset()
    {
        pidNum = 0;
        processMap.clear();
        backPL.clear();
        blockPL.clear();
        hangPL.clear();
        finishPL.clear();
        readyPL.clear();
        runningPL.clear();
        //测试进程
        createNewProcess("测试进程",
                10000, 20,
                SimProcess.Property.INDEPENDENT,
                null,
                CoreResource.simulatorConfig.getSystemReservedMemSize()
        );
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

        if (readyPL.size() + runningPL.size() < CoreResource.simulatorConfig.getMaxReadyProcess())
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
                {
                    newProcess.setStatus(SimProcess.Status.BACK);   //仍没有成功分配内存，进入后备队列
                    newProcess.setMemStartPos(-1);
                }
            }
        } else
        {
            newProcess.setStatus(SimProcess.Status.BACK);   //就绪队列已满，进入后备队列
            newProcess.setMemStartPos(-1);
        }

        processMap.put(newProcess.getPID(), newProcess);

        if (newProcess.getStatus() == SimProcess.Status.READY)
        {
            readyPL.add(newProcess.getPID());
            //尝试抢占处理机
            CoreResource.processorService.tryToPreemptProcessor(newProcess);
        } else if (newProcess.getStatus() == SimProcess.Status.BACK)
            backPL.add(newProcess.getPID());


        CoreResource.mainSceneController.refreshTViewThreadTable();
        return this.pidNum - 1;
    }

    /**
     * 检查就绪进程是否符合运行条件
     *
     * @param pid 进程id
     * @return 是否符合
     */
    public boolean tryToRunReadyProcess(Integer pid)
    {
        SimProcess thisProcess = processMap.get(pid);
        if (thisProcess.getStatus() != SimProcess.Status.READY || runningPL.contains(pid))
            return false;

        //检查进程属性,若为后继进程则检查前驱进程是否均已完成
        if (thisProcess.getProperty() == SimProcess.Property.SYNCHRONIZE_SUC)
        {
            for (int assocPid : thisProcess.getAssociatedPidList())
                if (processMap.get(assocPid).getStatus() != SimProcess.Status.REVOKE)
                {
                    //有前驱进程未完成，该进程阻塞
                    thisProcess.setStatus(SimProcess.Status.BLOCKED);
                    CoreResource.memoryService.findMemBlock(thisProcess.getMemStartPos()).setStatus(SimMemoryBlock.Status.INACTIVE);
                    readyPL.remove((Integer) thisProcess.getPID());
                    blockPL.add(thisProcess.getPID());
                    CoreResource.mainSceneController.refreshTViewThreadTable();
                    CoreResource.mainSceneController.refreshMemBlockVis();
                    return false;
                }
        }

        thisProcess.setStatus(SimProcess.Status.RUNNING);
        readyPL.remove((Integer) thisProcess.getPID());
        runningPL.add(thisProcess.getPID());

        CoreResource.mainSceneController.refreshTViewThreadTable();
        return true;
    }

    /**
     * 进程运行完毕，回到就绪状态（已完成的进程将标记为已完成）
     *
     * @param pid 进程id
     */
    public void processRunOver(Integer pid)
    {
        SimProcess thisProcess = processMap.get(pid);
        if (thisProcess.getStatus() != SimProcess.Status.RUNNING)
            return;

        if (thisProcess.getElapsedTime() >= thisProcess.getTotalRunTime())
        {
            //运行完成
            thisProcess.setStatus(SimProcess.Status.REVOKE);
            CoreResource.memoryService.freeMemBlock(thisProcess.getMemStartPos());
            runningPL.remove((Integer) thisProcess.getPID());
            finishPL.add(thisProcess.getPID());

            for (int i = 0; i < blockPL.size(); i++)
            {
                if (readyPL.size() + runningPL.size() >= CoreResource.simulatorConfig.getMaxReadyProcess())
                    break;
                if (processMap.get(blockPL.get(i)).getStatus() == SimProcess.Status.BLOCKED)
                    if (tryToReadyBlockedProcess(blockPL.get(i))) i = 0;
            }

            for (int i = 0; i < hangPL.size(); i++)
            {
                if (readyPL.size() + runningPL.size() >= CoreResource.simulatorConfig.getMaxReadyProcess())
                    break;
                if (processMap.get(hangPL.get(i)).getStatus() == SimProcess.Status.SYS_HANGUP)
                    if (tryToUnHangingProcess(hangPL.get(i))) i = 0;
            }

            CoreResource.mainSceneController.refreshTViewThreadTable();
            return;
        }

        thisProcess.setStatus(SimProcess.Status.READY);
        runningPL.remove((Integer) thisProcess.getPID());
        readyPL.add(thisProcess.getPID());
        CoreResource.mainSceneController.refreshTViewThreadTable();
    }

    /**
     * 尝试就绪一个阻塞进程
     *
     * @param pid 进程id
     * @return 是否成功
     */
    public boolean tryToReadyBlockedProcess(Integer pid)
    {
        SimProcess thisProcess = processMap.get(pid);
        if (thisProcess.getStatus() != SimProcess.Status.BLOCKED)
            return false;

        //检查进程属性,若为后继进程则检查前驱进程是否均已完成
        if (thisProcess.getProperty() == SimProcess.Property.SYNCHRONIZE_SUC)
        {
            for (int assocPid : thisProcess.getAssociatedPidList())
                if (processMap.get(assocPid).getStatus() != SimProcess.Status.REVOKE)
                    return false;   //有前驱进程未完成，维持阻塞
        }

        thisProcess.setStatus(SimProcess.Status.READY);
        CoreResource.memoryService.findMemBlock(thisProcess.getMemStartPos()).setStatus(SimMemoryBlock.Status.ACTIVE);
        blockPL.remove((Integer) thisProcess.getPID());
        readyPL.add(thisProcess.getPID());
        //尝试抢占处理机
        CoreResource.processorService.tryToPreemptProcessor(thisProcess);
        CoreResource.mainSceneController.refreshTViewThreadTable();
        return true;
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
            case BLOCKED ->
            {
                thisProcess.setStatus(SimProcess.Status.SYS_HANGUP);
                if (thisProcess.getMemStartPos() != -1)
                {//回收分配的内存
                    CoreResource.memoryService.freeMemBlock(thisProcess.getMemStartPos());
                    thisProcess.setMemStartPos(-1);
                }
                blockPL.remove((Integer) thisProcess.getPID());
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
                CoreResource.processorService.removeProcessFromProcessor(thisProcess.getPID()); //从处理机上撤除
                hangPL.add(thisProcess.getPID());
            }
            case BLOCKED ->
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
                //检查进程属性,若为后继进程则检查前驱进程是否均已完成
                if (thisProcess.getProperty() == SimProcess.Property.SYNCHRONIZE_SUC)
                {
                    for (int assocPid : thisProcess.getAssociatedPidList())
                    {
                        if (processMap.get(assocPid).getStatus() != SimProcess.Status.REVOKE)
                            return false;
                    }
                }

                if (readyPL.size() + runningPL.size() >= CoreResource.simulatorConfig.getMaxReadyProcess())
                {
                    sysHangupProcess(thisProcess.getPID());  //转为系统挂起
                    return false;
                }

                int memStartPos = CoreResource.memoryService.allocMemBlock(thisProcess.getRequireMemSize(), SimMemoryBlock.Status.ACTIVE);
                if (memStartPos == -1)
                    if (thisProcess.getStatus() == SimProcess.Status.USER_HANGUP)
                    {
                        sysHangupProcess(thisProcess.getPID());  //转为系统挂起
                        return false;
                    } else
                        return false;   //解挂失败

                thisProcess.setStatus(SimProcess.Status.READY);
                thisProcess.setMemStartPos(memStartPos);
                hangPL.remove((Integer) thisProcess.getPID());
                readyPL.add(thisProcess.getPID());
                //尝试抢占处理机
                CoreResource.processorService.tryToPreemptProcessor(thisProcess);
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

    /**
     * 获取pid列表
     *
     * @return pid列表
     */
    public ObservableList<Integer> getOptionalPidList()
    {
        ObservableList<Integer> optionalPidList = FXCollections.observableArrayList();
        for (SimProcess it : processMap.values())
            optionalPidList.add(it.getPID());
        Collections.sort(optionalPidList);

        return optionalPidList;
    }


}
