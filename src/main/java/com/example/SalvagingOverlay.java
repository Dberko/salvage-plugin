package com.example;

import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class SalvagingOverlay extends Overlay
{
    private final SalvagingPlugin plugin;
    private final ExampleConfig config;

    private final PanelComponent panel = new PanelComponent();

    @Inject
    public SalvagingOverlay(SalvagingPlugin plugin, ExampleConfig config)
    {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panel.setPreferredSize(new Dimension(170, 100));
        panel.getChildren().clear();

        panel.getChildren().add(
                TitleComponent.builder()
                        .text("Salvaging Tracker")
                        .color(Color.WHITE)
                        .build()
        );



        boolean isPlayerIdle = plugin.isPlayerIdle();
        boolean isPlayerSalvaging = plugin.isPlayerSalvaging();
        boolean isPlayerSorting = plugin.isPlayerSorting();

        String playerStatus = isPlayerIdle ? "Idle" : "";
        if (isPlayerSalvaging) playerStatus += "Salvaging";
        if (isPlayerSorting)   playerStatus += "Sorting";

        panel.getChildren().add(
                LineComponent.builder()
                        .left("Status ")
                        .right(playerStatus)
                        .leftColor(isPlayerIdle ? Color.RED : Color.GREEN)
                        .rightColor(Color.WHITE)
                        .build()
        );

        if (plugin.getTrackedInstances().isEmpty()) return panel.render(graphics);

        for (var npc : plugin.getTrackedInstances().values())
        {
            NPC n = npc.getLeft();
            Boolean show = npc.getRight();
            if (!show) continue;
            int idx = n.getIndex();
            boolean idle = plugin.isNpcIdle(idx);
            var state = plugin.getActivityState(idx);
            boolean isSalvaging = state != null && state.getLeft();
            boolean isSorting = state != null && state.getRight();

            String status = idle ? "Idle" : "";
            if (isSalvaging) status += "Salvaging";
            if (isSorting)   status += "Sorting";



            panel.getChildren().add(
                    LineComponent.builder()
                            .left(n.getName())
                            .right(status)
                            .leftColor(idle ? Color.RED : Color.GREEN)
                            .rightColor(Color.WHITE)
                            .build()
            );
        }

        return panel.render(graphics);
    }
}
