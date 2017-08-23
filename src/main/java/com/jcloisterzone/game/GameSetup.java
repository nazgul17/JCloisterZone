package com.jcloisterzone.game;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.capability.PigHerdCapability;

//TODO rename to GameSetup
//TODO change to immutable object and use it togeter
public class GameSetup {

    private String name;
    private final EnumMap<CustomRule, Object> customRules = new EnumMap<>(CustomRule.class);
    private final Set<Expansion> expansions = EnumSet.noneOf(Expansion.class);


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasExpansion(Expansion expansion) {
        return expansions.contains(expansion);
    }

    @Deprecated //in favor of rules on GameState
    public boolean getBooleanValue(CustomRule rule) {
        assert rule.getType().equals(Boolean.class);
        if (!customRules.containsKey(rule)) return false;
        return (Boolean) customRules.get(rule);
    }

    @Deprecated //in favor of capabilities on GameState
    public boolean hasCapability(Class<? extends Capability<?>> c) {
        return false;
    }

    public Set<Expansion> getExpansions() {
        return expansions;
    }

    public EnumMap<CustomRule, Object> getCustomRules() {
        return customRules;
    }

    public Set<Class<? extends Capability<?>>> getCapabilityClasses() {
        Set<Class<? extends Capability<?>>> classes = new HashSet<>();
        for (Expansion exp : expansions) {
            classes.addAll(Arrays.asList(exp.getCapabilities()));
        }

        if (getBooleanValue(CustomRule.USE_PIG_HERDS_INDEPENDENTLY)) {
            classes.add(PigHerdCapability.class);
        }

//        DebugConfig debugConfig = getDebugConfig();
//        if (debugConfig != null && debugConfig.getOff_capabilities() != null) {
//            List<String> offNames =  debugConfig.getOff_capabilities();
//            for (String tok : offNames) {
//                tok = tok.trim();
//                try {
//                    String className = "com.jcloisterzone.game.capability."+tok+"Capability";
//                    @SuppressWarnings("unchecked")
//                    Class<? extends Capability<?>> clazz = (Class<? extends Capability<?>>) Class.forName(className);
//                    classes = classes.remove(clazz);
//                } catch (Exception e) {
//                    logger.warn("Invalid capability name: " + tok, e);
//                }
//            }
//        }
        return classes;
    }
}
