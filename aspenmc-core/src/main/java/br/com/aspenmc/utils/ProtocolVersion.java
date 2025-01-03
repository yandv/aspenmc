package br.com.aspenmc.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@AllArgsConstructor
public enum ProtocolVersion {

    MINECRAFT_1_16_3(753), MINECRAFT_1_16_2(751), MINECRAFT_1_16_1(736), MINECRAFT_1_16(735), MINECRAFT_1_15_2(578),
    MINECRAFT_1_15_1(575), MINECRAFT_1_15(573), MINECRAFT_1_14_4(498), MINECRAFT_1_14_3(490), MINECRAFT_1_14_2(485),
    MINECRAFT_1_14_1(480), MINECRAFT_1_14(477), MINECRAFT_1_13_2(404), MINECRAFT_1_13_1(401), MINECRAFT_1_13(393),
    MINECRAFT_1_12_2(340), MINECRAFT_1_12_1(338), MINECRAFT_1_12(335), MINECRAFT_1_11_1(316), MINECRAFT_1_11(315),
    MINECRAFT_1_10(210), MINECRAFT_1_9_3(110), MINECRAFT_1_9_2(109), MINECRAFT_1_9_1(108), MINECRAFT_1_9(107),
    MINECRAFT_1_8(47), MINECRAFT_1_7_10(5), MINECRAFT_1_7_2(4), UNKNOWN(-1);

    @Getter
    private int id;

    @Override
    public String toString() {
        return super.toString().replace("MINECRAFT_", "").replace("_", ".");
    }

    public static ProtocolVersion getById(int id) {
        return Stream.of(values()).filter(version -> version.getId() == id).findFirst().orElse(UNKNOWN);
    }
}
