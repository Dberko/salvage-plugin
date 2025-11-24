package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = "Salvaging Indicator",
		description = "Shows whether the player is currently salvaging.",
		enabledByDefault = true,
		tags = {"salvage", "status", "overlay"}
)
public class SalvagingPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SalvagingOverlay overlay;

	@Inject
	private ExampleConfig config;

	// Replace this with the real animation ID once known
	private static final int SALVAGING_ANIMATION = 13577;
	private static final int SALVAGING_ANIMATION_PORT = 13584;
	private static final int SORTING_ANIMATION = 13599;


	private boolean isSalvaging = false;
	private boolean isSalvagingPort = false;
	private boolean isSorting = false;



	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	public boolean isSalvaging()
	{
		return isSalvaging || isSalvagingPort;
	}

	public boolean isSorting()
	{
		return isSorting;
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!(event.getActor() instanceof Player))
		{
			return;
		}

		Player p = (Player) event.getActor();
		if (p == client.getLocalPlayer())
		{
			int anim = p.getAnimation();

//			 Debug print to find correct animation ID
//			 System.out.println("Animation: " + anim);

			isSalvaging = (anim == SALVAGING_ANIMATION || anim == SALVAGING_ANIMATION_PORT);
			isSorting = (anim == SORTING_ANIMATION);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// If needed, add extra checks such as motionless state, item used, menu action, etc.
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
