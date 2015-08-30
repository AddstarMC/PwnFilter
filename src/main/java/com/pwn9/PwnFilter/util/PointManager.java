/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2013 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.PwnFilter.util;

import com.pwn9.PwnFilter.FilterState;
import com.pwn9.PwnFilter.PwnFilter;
import com.pwn9.PwnFilter.api.FilterClient;
import com.pwn9.PwnFilter.rules.RuleChain;
import com.pwn9.PwnFilter.rules.action.Action;
import com.pwn9.PwnFilter.rules.action.ActionFactory;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mange the Points system of PwnFilter
 * User: ptoal
 * Date: 13-10-31
 * Time: 3:49 PM
 *
 * @author ptoal
 * @version $Id: $Id
 */
@SuppressWarnings("UnusedDeclaration")
public class PointManager implements FilterClient {

    private static PointManager _instance;
    private final PwnFilter plugin;

    private Map<String,Double> playerPoints = new ConcurrentHashMap<String, Double>(8, 0.75f, 2);
    private TreeMap<Double, Threshold> thresholds = new TreeMap<Double,Threshold>();

    private int leakInterval;
    private Double leakPoints;
    private BukkitTask leakTask;

    private PointManager(PwnFilter p) {
        this.plugin = p;
    }

    /**
     * <p>setup.</p>
     *
     * @param pwnFilter a {@link com.pwn9.PwnFilter.PwnFilter} object.
     * @return a {@link com.pwn9.PwnFilter.util.PointManager} object.
     */
    public static PointManager setup(PwnFilter pwnFilter) {
        ConfigurationSection pointsSection = pwnFilter.getConfig().getConfigurationSection("points");
        if (!pointsSection.getBoolean("enabled")) {
            if (_instance != null) _instance.stopLeaking();
            _instance = null;
            return null;
        }
        if (_instance == null ) {
            _instance = new PointManager(pwnFilter);
        }

        _instance.leakPoints = pointsSection.getDouble("leak.points",1);
        _instance.leakInterval = pointsSection.getInt("leak.interval",30);

        _instance.parseThresholds(pointsSection.getConfigurationSection("thresholds"));

        _instance.startLeaking();

        return _instance;
    }

    /**
     * <p>reset.</p>
     */
    public void reset() {
        stopLeaking();

        // Reset all player points.
        for (String playerName : playerPoints.keySet()) {
            setPlayerPoints(playerName,0.0);
        }

        setup(plugin);
    }

    private void startLeaking() {
        final PointManager pointManager = this;

        if (leakTask == null) {
            BukkitRunnable leakTask = new BukkitRunnable() {
                @Override
                public void run() {
                    //Every interval, check point balances, and if they are > 0, subtract leakPoints
                    // from the players balance.  If they reach 0, remove them from the list.
                    for (String playerName : pointManager.getPlayersWithPoints()) {
                        pointManager.subPlayerPoints(playerName,leakPoints);
                    }
                }
            };
            leakTask.runTaskTimerAsynchronously(plugin, 20, 20 * leakInterval);
        }
    }


    private void stopLeaking() {
        if (leakTask != null) {
            leakTask.cancel();
            leakTask = null;
        }
    }
    private void parseThresholds(ConfigurationSection cs) {

        // Setup the 0 threshold
        Threshold defaultThreshold = new Threshold();
        defaultThreshold.name = "Default";
        defaultThreshold.points = (double)0;
        thresholds.put((double)0,defaultThreshold);

        for (String threshold : cs.getKeys(false)) {
            Threshold newThreshold = new Threshold();
            newThreshold.name = cs.getString(threshold + ".name");
            newThreshold.points = cs.getDouble(threshold+".points");

            for (String action : cs.getStringList(threshold + ".actions.ascending")) {
                Action actionObject = ActionFactory.getActionFromString(action);
                if (actionObject != null) {
                    newThreshold.actionsAscending.add(actionObject);
                } else {
                    LogManager.logger.warning("Unable to parse action in threshold: " + threshold);
                }
            }
            for (String action : cs.getStringList(threshold + ".actions.descending")) {
                Action actionObject = ActionFactory.getActionFromString(action);
                if (actionObject != null) {
                    newThreshold.actionsDescending.add(actionObject);
                } else {
                    LogManager.logger.warning("Unable to parse action in threshold: " + threshold);
                }
            }
            thresholds.put(newThreshold.points, newThreshold);
        }

    }


    /**
     * <p>getInstance.</p>
     *
     * @return a {@link com.pwn9.PwnFilter.util.PointManager} object.
     * @throws java.lang.IllegalStateException if any.
     */
    public static PointManager getInstance() throws IllegalStateException {
        if (_instance == null ) {
            throw new IllegalStateException("Point Manager Not initialized.");
        }
        return _instance;
    }

    /**
     * <p>getPlayersWithPoints.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getPlayersWithPoints() {
        return playerPoints.keySet();
    }

    /**
     * <p>Getter for the field <code>playerPoints</code>.</p>
     *
     * @param p a {@link java.lang.String} object.
     * @return a {@link java.lang.Double} object.
     */
    public Double getPlayerPoints(String p) {
        return (playerPoints.containsKey(p))?playerPoints.get(p):0.0;
    }

    /**
     * <p>Getter for the field <code>playerPoints</code>.</p>
     *
     * @param p a {@link org.bukkit.entity.Player} object.
     * @return a {@link java.lang.Double} object.
     */
    public Double getPlayerPoints(Player p) {
        return (playerPoints.containsKey(p.getName()))?playerPoints.get(p.getName()):0.0;
    }

    /**
     * <p>Setter for the field <code>playerPoints</code>.</p>
     *
     * @param playerName a {@link java.lang.String} object.
     * @param points a {@link java.lang.Double} object.
     */
    public void setPlayerPoints(String playerName, Double points) {
        Double old = playerPoints.get(playerName);
        playerPoints.put(playerName,points);
        executeActions(old, points,playerName);
    }

    /**
     * <p>addPlayerPoints.</p>
     *
     * @param playerName a {@link java.lang.String} object.
     * @param points a {@link java.lang.Double} object.
     */
    public void addPlayerPoints(String playerName, Double points) {
        Double current = playerPoints.get(playerName);
        if (current == null) current = 0.0;
        Double updated = current + points;

        playerPoints.put(playerName,updated);

        executeActions(current, updated, playerName);

    }

    /**
     * <p>isEnabled.</p>
     *
     * @return a boolean.
     */
    public static boolean isEnabled() {
        return _instance != null;
    }

    private void executeActions(final Double fromValue, final Double toValue, final String playerName) {
        final Double oldKey = thresholds.floorKey(fromValue);
        final Double newKey = thresholds.floorKey(toValue);

        if (oldKey.equals(newKey)) return;

        if (fromValue < toValue) {

            // Check to see if we've crossed any thresholds on our way up/down, and if so
            // execute the actions for that crossing.
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Map.Entry<Double, Threshold> entry : thresholds.subMap(oldKey, false, newKey, true).entrySet())
                        entry.getValue().executeAscending(playerName);
                }
            };
            task.runTask(plugin);
        } else {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Map.Entry<Double, Threshold> entry : thresholds.subMap(newKey, false, oldKey, true).descendingMap().entrySet())
                        entry.getValue().executeDescending(playerName);
                }
            };
            task.runTask(plugin);
        }

    }

    /**
     * <p>subPlayerPoints.</p>
     *
     * @param playerName a {@link java.lang.String} object.
     * @param points a {@link java.lang.Double} object.
     */
    public void subPlayerPoints(String playerName, Double points) {
        Double updated;
        Double current = playerPoints.get(playerName);
        if (current == null) current = 0.0;
        updated = current - points;
        if ( updated <=0 ) {
            playerPoints.remove(playerName);
            updated = 0.0;
        }
        playerPoints.put(playerName,updated);

        executeActions(current, updated, playerName);

    }


    class Threshold implements Comparable<Threshold> {
        String name;
        Double points;
        List<Action> actionsAscending = new ArrayList<Action>();
        List<Action> actionsDescending = new ArrayList<Action>();

        @Override
        public int compareTo(@NotNull Threshold o) {
            return Double.compare(this.points, o.points);
        }

        public void executeAscending(String playerName) {
            FilterState state = new FilterState(plugin, "", playerName, null, _instance );
            for (Action a : actionsAscending ) {
                a.execute(state);
            }
        }

        public void executeDescending(String playerName) {
            FilterState state = new FilterState(plugin, "", playerName, null, _instance );
            for (Action a : actionsDescending ) {
                a.execute(state);
            }
        }

    }

    /**
     * {@inheritDoc}
     *
     * Setup as a Client, so we can create a FilterState object, and execute actions.
     * Really, the only thing we implement is the getShortName() call.  This is hackish.
     * Should really re-think this implementation.
     */
    @Override
    public String getShortName() {
        return "POINTS";
    }

    /** {@inheritDoc} */
    @Override
    public RuleChain getRuleChain() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void activate(Configuration config) {
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
    }
}
