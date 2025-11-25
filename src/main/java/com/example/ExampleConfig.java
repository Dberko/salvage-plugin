package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Salvage")
public interface ExampleConfig extends Config
{
    @ConfigItem(
            keyName = "trackJim",
            name = "Track Jobless Jim",
            description = "Whether to track this crewmate",
            position = 1
    )
    default boolean trackJim()
    {
        return false;
    }

    @ConfigItem(
            keyName = "trackSiad",
            name = "Track Ex-Captain Siad",
            description = "Whether to track this crewmate",
            position = 2
    )
    default boolean trackSiad()
    {
        return true;
    }

    @ConfigItem(
            keyName = "idleThresholdTicks",
            name = "Idle threshold (ticks)",
            description = "How many ticks of no activity counts as idle.",
            position = 3
    )
    default int idleThresholdTicks()
    {
        return 10; // ~6 seconds
    }

    @ConfigItem(
            keyName = "notifyIdle",
            name = "Notify when idle",
            description = "Send a chat message when a crewmate becomes idle.",
            position = 4
    )
    default boolean notifyIdle()
    {
        return true;
    }
}
