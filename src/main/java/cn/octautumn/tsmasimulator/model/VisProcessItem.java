package cn.octautumn.tsmasimulator.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisProcessItem
{
    /**
     * 名称&ID
     */
    private String nameAndPID;
    /**
     * 已运行的时间&总时间
     */
    private String totalAndElapsedRunTime;
    /**
     * 优先级
     */
    private String priority;
    /**
     * 状态
     */
    private String status;
    /**
     * 进程属性&关联进程ID
     */
    private String propertyAndAssocPID;
    /**
     * 需要的内存大小
     */
    private String requireMemSize;
    /**
     * 占用内存的起始位置
     */
    private String memStartPos;

    public VisProcessItem(SimProcess simProcess)
    {
        this.nameAndPID = simProcess.getPName() + "/" + simProcess.getPID();
        this.totalAndElapsedRunTime = simProcess.getTotalRunTime() + "/" + simProcess.getElapsedTime();
        this.priority = String.valueOf(simProcess.getPriority());
        switch (simProcess.getStatus())
        {
            case BACK -> this.status = "后备";
            case READY -> this.status = "就绪";
            case RUNNING -> this.status = "正在运行";
            case BLOCK ->this.status = "阻塞";
            case SYS_HANGUP ->this.status = "阻塞挂起";
            case USER_HANGUP -> this.status = "挂起";
            case REVOKE -> this.status = "已完成";
        }
        switch (simProcess.getProperty())
        {
            case INDEPENDENT -> this.propertyAndAssocPID = "独立进程";
            case SYNCHRONIZE_PRE -> this.propertyAndAssocPID = "前驱进程-" + simProcess.getAssociatedPID();
            case SYNCHRONIZE_SUC -> this.propertyAndAssocPID = "后继进程-" + simProcess.getAssociatedPID();
        }
        this.requireMemSize = String.valueOf(simProcess.getRequireMemSize());
        if (simProcess.getMemStartPos() == -1)
            this.memStartPos = "未分配";
        else
            this.memStartPos = String.valueOf(simProcess.getMemStartPos());
    }
}
