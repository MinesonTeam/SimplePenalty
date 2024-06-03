package kz.hxncus.mc.simplepenalty.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PenaltyCache {
    private final long id;
    private final String officer;
    private final String offender;
    private final String description;
    private int count;
    private long time;
}
