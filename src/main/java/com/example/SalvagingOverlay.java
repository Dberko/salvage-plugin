package com.example;

import javax.inject.Inject;
import java.awt.*;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class SalvagingOverlay extends Overlay
{
    private final SalvagingPlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public SalvagingOverlay(SalvagingPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text("Salvaging")
                        .color(Color.WHITE)
                        .build()
        );
        if (plugin.isSalvaging())
        {
            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Status:")
                            .right("Active")
                            .leftColor(Color.GRAY)
                            .rightColor(Color.GREEN)
                            .build()
            );

        } else if (plugin.isSorting()) {
            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Status:")
                            .right("Sorting")
                            .leftColor(Color.GRAY)
                            .rightColor(Color.GREEN)
                            .build()
            );

        } else {
            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Status:")
                            .right("Not Active")
                            .leftColor(Color.GRAY)
                            .rightColor(Color.RED)
                            .build()
            );
        }

        return panelComponent.render(graphics);
    }
}
