package com.example;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
		name = "Salvaging Tracker",
		description = "Tracks crewmates salvaging/sorting and idle status on your ship",
		enabledByDefault = false
)
public class SalvagingPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SalvagingOverlay panelOverlay;

	/** NPC index → last active tick */
	private final Map<Integer, Integer> lastActiveTick = new HashMap<>();

	/** NPC index → (isSalvaging, isSorting) */
	private final Map<Integer, Pair<Boolean, Boolean>> activityMap = new HashMap<>();

	/** Currently tracked NPCs on your ship */
	private final Map<Integer, NPC> trackedInstances = new HashMap<Integer, NPC>();

	/** NPC index → last known world position */
	private final Map<Integer, WorldPoint> lastPosition = new HashMap<>();

	private static final int SALVAGING_ANIMATION = 13577;
	private static final int SALVAGING_ANIMATION_PORT = 13584;
	private static final int SORTING_ANIMATION = 13599;


	private boolean shipSnapshotTaken = false;
	private boolean isPlayerSalvaging;
	private boolean isPlayerSorting;

	@Override
	protected void startUp()
	{
		lastActiveTick.clear();
		activityMap.clear();
		trackedInstances.clear();
		lastPosition.clear();
		shipSnapshotTaken = false;
		isPlayerSalvaging = false;
		isPlayerSorting = false;

		overlayManager.add(panelOverlay);

		log.info("SalvagingPlugin started");
	}

	@Override
	protected void shutDown()
	{
		lastActiveTick.clear();
		activityMap.clear();
		trackedInstances.clear();
		lastPosition.clear();
		shipSnapshotTaken = false;

		overlayManager.remove(panelOverlay);
	}

	// ------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------

	public boolean isTrackedNpcId(int npcId)
	{
		if (npcId == 15256 && config.trackJim()) return true;
		if (npcId == 15334 && config.trackSiad()) return true;
		return false;
	}

	public Map<Integer, NPC> getTrackedInstances() {
		return trackedInstances;
	}

	/** Returns true if the NPC is in the same WorldView as the player */
	private boolean isNpcOnMyShip(NPC npc)
	{
		return activityMap.containsKey(npc.getIndex());
	}

	/** Combine config filter + world-view filter */
	private boolean isMyCrewmate(NPC npc)
	{
		return isTrackedNpcId(npc.getId()) && isNpcOnMyShip(npc);
	}

	private void ensureInstanceExists(NPC npc)
	{
		int idx = npc.getIndex();
		activityMap.putIfAbsent(idx, Pair.of(false, false));
		trackedInstances.putIfAbsent(idx, npc);
	}

	private void markActive(NPC npc)
	{
		lastActiveTick.put(npc.getIndex(), client.getTickCount());
	}

	public boolean isNpcIdle(int instanceIndex)
	{
		return !activityMap.get(instanceIndex).getLeft();
	}

	public Pair<Boolean, Boolean> getActivityState(int instanceIndex)
	{
		return activityMap.get(instanceIndex);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event) {
		Actor actor = event.getActor();
		NPC npc = event.getNpc();
		if (!(actor instanceof NPC))
		{
			return;
		}

		if (actor.getWorldView().isTopLevel())
		{
			return;
		}

		if (actor.getWorldView() != client.getLocalPlayer().getWorldView())
		{
			return;
		}
		if (!isTrackedNpcId(npc.getId())) {
			return;
		}
		ensureInstanceExists(npc);
//		markActive(npc);

//		actor.setOverheadCycle(-1);
//		actor.setOverheadText("");actor.setOverheadCycle(-1);
//		actor.setOverheadText("");
		log.trace("Spawning npc {}={} in wv {}", ((NPC) npc).getId(), npc.getName(), npc.getWorldView().getId());
	}
	@Subscribe
	public void onGameTick(GameTick event)
	{
//		Player player = client.getLocalPlayer();
//		if (player == null) return;
//
//		for (NPC npc : client.getTopLevelWorldView().npcs())
//		{
//			if (!isMyCrewmate(npc)) continue;
//
//			if (npc.getWorldView() != client.getLocalPlayer().getWorldView())
//			{
//				continue;
//			}
//			if (!isTrackedNpcId(npc.getId())) {
//				continue;
//			}
//
////			ensureInstanceExists(npc);
//
//
//		}
	}

	// ------------------------------------------------------------
	// Event Listeners
	// ------------------------------------------------------------

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if ((event.getActor() instanceof Player))
		{
			Player p = (Player) event.getActor();
			if (p == client.getLocalPlayer())
			{
				int anim = p.getAnimation();

			 	System.out.println("Animation: " + anim);

				isPlayerSalvaging = (anim == SALVAGING_ANIMATION || anim == SALVAGING_ANIMATION_PORT);
				isPlayerSorting = (anim == SORTING_ANIMATION);
			}
			return;
		}


		if (!(event.getActor() instanceof NPC)) return;
		NPC npc = (NPC) event.getActor();
		if (!isMyCrewmate(npc)) return;

		int anim = npc.getAnimation();
		if (anim <= 0) return;
		activityMap.put(npc.getIndex(), Pair.of(anim == SALVAGING_ANIMATION, anim == SORTING_ANIMATION));

		markActive(npc);
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (!(event.getSource() instanceof NPC)) return;
		NPC npc = (NPC) event.getSource();
		if (!isMyCrewmate(npc)) return;

		markActive(npc);
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager manager)
	{
		return manager.getConfig(ExampleConfig.class);
	}

	public boolean isPlayerSalvaging() {
		return isPlayerSalvaging;
	}

	public boolean isPlayerSorting() {
		return isPlayerSorting;
	}

	public boolean isPlayerIdle() {
		return !isPlayerSalvaging && !isPlayerSorting;
	}
}
