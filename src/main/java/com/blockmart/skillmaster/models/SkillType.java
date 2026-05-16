package com.blockmart.skillmaster.models;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SkillType {
    MINING("Mining"),
    WOODCUTTING("Woodcutting"),
    FARMING("Farming"),
    COMBAT("Combat"),
    FISHING("Fishing");

    private final String name;
    public static final int MAX_LEVEL = 100;

    SkillType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String getNames() {
        return Arrays.stream(SkillType.values())
                .map(SkillType::name)
                .collect(Collectors.joining(", "));
    }
}
