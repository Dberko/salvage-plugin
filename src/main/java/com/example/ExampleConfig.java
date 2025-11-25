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
        return true;
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
            keyName = "trackAda",
            name = "Track Adventurer Ada",
            description = "Whether to track this crewmate",
            position = 3
    )
    default boolean trackAda()
    {
        return false;
    }

    @ConfigItem(
            keyName = "trackJenkins",
            name = "Track Cabin Boy Jenkins",
            description = "Whether to track this crewmate",
            position = 4
    )
    default boolean trackJenkins()
    {
        return false;
    }

    @ConfigItem(
            keyName = "trackJitteryJim",
            name = "Track Jittery Jim",
            description = "Whether to track this crewmate",
            position = 5
    )
    default boolean trackJitteryJim()
    {
        return false;
    }

    @ConfigItem(
            keyName = "trackJollyJim",
            name = "Track Jolly Jim",
            description = "Whether to track this crewmate",
            position = 6
    )
    default boolean trackJollyJim()
    {
        return false;
    }

    @ConfigItem(
            keyName = "trackOlga",
            name = "Track Oarswoman Olga",
            description = "Whether to track this crewmate",
            position = 7
    )
    default boolean trackOlga()
    {
        return false;
    }
    @ConfigItem(
            keyName = "trackJakob",
            name = "Track Sailor Jakob",
            description = "Whether to track this crewmate",
            position = 8
    )
    default boolean trackJakob()
    {
        return false;
    }

    @ConfigItem(
            keyName = "trackVirginia",
            name = "Track Spotter Virginia",
            description = "Whether to track this crewmate",
            position = 9
    )
    default boolean trackVirginia()
    {
        return false;
    }

    @ConfigItem(
            keyName = "idleThresholdTicks",
            name = "Idle threshold (ticks)",
            description = "How many ticks of no activity counts as idle.",
            position = 10
    )
    default int idleThresholdTicks()
    {
        return 10; // ~6 seconds
    }

    @ConfigItem(
            keyName = "notifyIdle",
            name = "Notify when idle",
            description = "Send a chat message when a crewmate becomes idle.",
            position = 11
    )
    default boolean notifyIdle()
    {
        return true;
    }

}
