package com.example;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
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
		enabledByDefault = true
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

	@Inject
	private Notifier notifier;

	/** NPC index → last active tick */
	private final Map<Integer, Integer> lastActiveTick = new HashMap<>();

	/** NPC index → (isSalvaging, isSorting) */
	private final Map<Integer, Pair<Boolean, Boolean>> activityMap = new HashMap<>();

	/** Currently tracked NPCs on your ship */
	private final Map<Integer, Pair<NPC, Boolean>> trackedInstances = new HashMap<Integer, Pair<NPC, Boolean>>();

	/** NPC index → last known world position */
	private final Map<Integer, WorldPoint> lastPosition = new HashMap<>();

	private final Map<Integer, String> crewmateIdToName = new HashMap<>();

	private static final int SALVAGING_ANIMATION = 13577;
	private static final int SALVAGING_ANIMATION_PORT = 13584;
	private static final int SORTING_ANIMATION = 13599;
	private boolean isPlayerSalvaging;
	private boolean isPlayerSorting;
	private HashMap<Integer, Boolean> notified = new HashMap<>();

	@Override
	protected void startUp()
	{
		lastActiveTick.clear();
		activityMap.clear();
		trackedInstances.clear();
		lastPosition.clear();
		notified.clear();
		isPlayerSalvaging = false;
		isPlayerSorting = false;

		crewmateIdToName.put(15256, "Jobless Jim");
		crewmateIdToName.put(15334, "Ex-Captain Siad");
		crewmateIdToName.put(15265, "Adventurer Ada");
		crewmateIdToName.put(15344, "Cabin Boy Jenkins");
		crewmateIdToName.put(15275, "Jittery Jim");
		crewmateIdToName.put(15285, "Jolly Jim");
		crewmateIdToName.put(15305, "Oarswoman Olga");
		crewmateIdToName.put(15295, "Sailor Jakob");
		crewmateIdToName.put(15325, "Spotter Virginia");


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
		notified.clear();

		overlayManager.remove(panelOverlay);
	}

	// ------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------

	public boolean isTrackedNpcId(int npcId)
	{
		if (npcId == 15256 && config.trackJim()) return true;
		if (npcId == 15334 && config.trackSiad()) return true;
		if (npcId == 15265 && config.trackAda()) return true;
		if (npcId == 15344 && config.trackJenkins()) return true;
		if (npcId == 15275 && config.trackJitteryJim()) return true;
		if (npcId == 15285 && config.trackJollyJim()) return true;
		if (npcId == 15305 && config.trackOlga()) return true;
		if (npcId == 15295 && config.trackJakob()) return true;
		if (npcId == 15325 && config.trackVirginia()) return true;
		return false;
	}

	public Map<Integer, Pair<NPC, Boolean>> getTrackedInstances() {
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

	private boolean isACrewmate(Integer index) {return crewmateIdToName.containsKey(index);}

	private void ensureInstanceExists(NPC npc)
	{
		int idx = npc.getIndex();
		activityMap.putIfAbsent(idx, Pair.of(false, false));
		trackedInstances.putIfAbsent(idx, Pair.of(npc, true));
		lastPosition.putIfAbsent(idx, npc.getWorldLocation());
		notified.putIfAbsent(idx, false);
	}

	private void markActive(NPC npc)
	{
		notified.put(npc.getIndex(), false);
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


	// ------------------------------------------------------------
	// Event Listeners
	// ------------------------------------------------------------


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
		if (!isACrewmate(npc.getId())) {
			return;
		}
		ensureInstanceExists(npc);
		log.trace("Spawning npc {}={} in wv {}", ((NPC) npc).getId(), npc.getName(), npc.getWorldView().getId());
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if ((event.getActor() instanceof Player))
		{
			Player p = (Player) event.getActor();
			if (p == client.getLocalPlayer())
			{
				int anim = p.getAnimation();

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
		activityMap.put(npc.getIndex(), Pair.of(anim == SALVAGING_ANIMATION || anim == SALVAGING_ANIMATION_PORT, anim == SORTING_ANIMATION));
		log.debug("NPC animation: {}", String.valueOf(anim));
		if (anim == SALVAGING_ANIMATION || anim == SALVAGING_ANIMATION_PORT || anim == SORTING_ANIMATION) {
			markActive(npc);
			lastPosition.put(npc.getIndex(), npc.getWorldLocation());
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		for (Pair<NPC, Boolean> p:
				trackedInstances.values()) {
			NPC npc = p.getLeft();
			Integer idx = npc.getIndex();
			if (!isTrackedNpcId(npc.getId())) {
				trackedInstances.put(idx, Pair.of(npc, false));
			} else {
				trackedInstances.put(idx, Pair.of(npc, true));

				Integer npcX = lastPosition.getOrDefault(idx, npc.getWorldLocation()).getX();
				Integer npcY = lastPosition.getOrDefault(idx, npc.getWorldLocation()).getY();
				if (npcX != npc.getWorldLocation().getX() || npcY != npc.getWorldLocation().getY()) {
					activityMap.put(idx, Pair.of(false, false));
				}

				// Notify if idle
				Integer now = client.getTickCount();
				Integer lastActive = lastActiveTick.getOrDefault(idx, now);
				Pair<Boolean, Boolean> activity = activityMap.getOrDefault(idx, Pair.of(false, false));
				if (config.notifyIdle() && !notified.get(idx) && !activity.getLeft() && !activity.getRight() && now - lastActive > config.idleThresholdTicks()) {
					notified.put(idx, true);
					log.info(crewmateIdToName.get(npc.getId()) + " is idle.");
					notifier.notify(crewmateIdToName.get(npc.getId()) + " is idle.");
				}
			}
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (!(event.getSource() instanceof NPC)) return;
		NPC npc = (NPC) event.getSource();
		if (!isMyCrewmate(npc)) return;

		markActive(npc);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGING_IN) {
			activityMap.clear();
			trackedInstances.clear();
			lastActiveTick.clear();
		}
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
