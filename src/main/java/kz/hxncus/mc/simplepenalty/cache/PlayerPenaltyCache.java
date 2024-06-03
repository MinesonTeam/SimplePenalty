package kz.hxncus.mc.simplepenalty.cache;

import lombok.Data;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class PlayerPenaltyCache {
    private final UUID uuid;
    private final List<PenaltyCache> penalties = new ArrayList<>();
    private BukkitTask task;

    public void setTask(BukkitTask task) {
        this.task.cancel();
        this.task = task;
    }
}
