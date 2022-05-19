package cn.octautumn.tsmasimulator.model.Vis;

import cn.octautumn.tsmasimulator.CoreResource;
import cn.octautumn.tsmasimulator.model.Sim.SimProcess;
import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

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
    private Integer requireMemSize;
    /**
     * 占用内存的起始位置
     */
    private Integer memStartPos;

    private Button toggleHangButton;

    public VisProcessItem(SimProcess simProcess)
    {
        this.nameAndPID = simProcess.getPName() + "/" + simProcess.getPID();
        this.totalAndElapsedRunTime = String.format("%.3f/%.3f", simProcess.getElapsedTime() / 1000.0d, simProcess.getTotalRunTime() / 1000.0d);
        this.priority = String.valueOf(simProcess.getPriority());
        switch (simProcess.getStatus())
        {
            case BACK -> this.status = "后备";
            case READY -> this.status = "就绪";
            case RUNNING -> this.status = "正在运行";
            case BLOCKED -> this.status = "阻塞";
            case SYS_HANGUP -> this.status = "系统挂起";
            case USER_HANGUP -> this.status = "用户挂起";
            case REVOKE -> this.status = "已完成";
        }
        switch (simProcess.getProperty())
        {
            case INDEPENDENT -> this.propertyAndAssocPID = "独立进程";
            case SYNCHRONIZE_SUC ->
                    this.propertyAndAssocPID = "前驱进程-" + Arrays.toString(simProcess.getAssociatedPidList().toArray());
        }
        this.requireMemSize = simProcess.getRequireMemSize();
        this.memStartPos = simProcess.getMemStartPos();

        switch (simProcess.getStatus())
        {
            case READY, RUNNING, BLOCKED, SYS_HANGUP ->
            {
                toggleHangButton = new Button("挂起");
                toggleHangButton.setPrefHeight(20);
                toggleHangButton.setOnAction(actionEvent -> CoreResource.processService.userHangupProcess(simProcess.getPID()));
            }
            case USER_HANGUP ->
            {
                toggleHangButton = new Button("解挂");
                toggleHangButton.setPrefHeight(20);
                toggleHangButton.setOnAction(actionEvent -> CoreResource.processService.tryToUnHangingProcess(simProcess.getPID()));
            }
            default -> toggleHangButton = null;
        }
    }
}
