package cn.octautumn.tsmasimulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SimProcessor
{
    private final int processorID;
    private int runningPID;
    private Status status;

    public enum Status
    {
        IDLE,
        USING
    }
}
