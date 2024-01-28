package br.com.aspenmc.bukkit.utils.item;

import br.com.aspenmc.entity.sender.member.Skin;
import br.com.aspenmc.utils.string.StringFormat;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Joiner;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

@Getter
public class ItemBuilder {

    private Material material;
    private int amount;
    private short durability;
    private boolean useMeta;
    private boolean glow;
    private String displayName;
    private Map<Enchantment, Integer> enchantments;
    private List<PotionEffect> potions;
    private List<String> lore;

    private Color color;
    private String skinOwner;
    private Skin skin;
    private String skinUrl;

    private boolean hideAttributes;
    private boolean unbreakable;
    private List<ItemFlag> itemFlags;

    public ItemBuilder() {
        material = Material.STONE;
        amount = 1;
        durability = 0;
        hideAttributes = false;
        unbreakable = false;
        useMeta = false;
        glow = false;
    }

    public ItemBuilder flag(ItemFlag itemFlag) {
        if (itemFlags == null) {
            itemFlags = new ArrayList<>();
        }

        itemFlags.add(itemFlag);

        if (!useMeta) {
            useMeta = true;
        }
        return this;
    }

    public ItemBuilder flag(Set<ItemFlag> itemFlags) {
        if (this.itemFlags == null) {
            this.itemFlags = new ArrayList<>();
        }

        this.itemFlags.addAll(itemFlags);

        if (!useMeta) {
            useMeta = true;
        }
        return this;
    }

    public ItemBuilder type(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder amount(int amount) {
        if (amount <= 0) {
            amount = 1;
        }
        this.amount = amount;
        return this;
    }

    public ItemBuilder durability(short durability) {
        this.durability = durability;
        return this;
    }

    public ItemBuilder durability(int durability) {
        this.durability = (short) durability;
        return this;
    }

    public ItemBuilder name(String text) {
        if (!useMeta) {
            useMeta = true;
        }
        this.displayName = text.replace("&", "§");
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment) {
        return enchantment(enchantment, 1);
    }

    public ItemBuilder enchantment(Enchantment enchantment, Integer level) {
        if (enchantments == null) {
            enchantments = new HashMap<>();
        }

        if (level == 0) {
            return this;
        }

        enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder enchantment(Map<Enchantment, Integer> enchantments) {
        if (this.enchantments == null) {
            this.enchantments = new HashMap<>();
        }

        this.enchantments.putAll(enchantments);

        return this;
    }

    public ItemBuilder clearLore() {
        if (!this.useMeta) {
            this.useMeta = true;
        }

        if (this.lore != null) {
            this.lore.clear();
        }

        return this;
    }

    public ItemBuilder formatLore(String... text) {
        if (!this.useMeta) {
            this.useMeta = true;
        }

        if (this.lore == null) {
            this.lore = new ArrayList<>(StringFormat.getLore(30, Joiner.on('\n').join(text)));
        } else {
            this.lore.addAll(StringFormat.getLore(30, Joiner.on('\n').join(text)));
        }

        return this;
    }

    public ItemBuilder lore(String text) {
        if (!this.useMeta) {
            this.useMeta = true;
        }

        if (this.lore == null) {
            this.lore = new ArrayList<>(Collections.singleton(text));
        } else {
            this.lore.add(text);
        }

        return this;
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> text) {
        if (!this.useMeta) {
            this.useMeta = true;
        }
        if (this.lore == null) {
            this.lore = new ArrayList<>();
        }
        for (String str : text) {
            this.lore.add(str.replace("&", "§"));
        }

        return this;
    }

    public ItemBuilder lore(List<String> text, String... replaces) {
        if (!this.useMeta) {
            this.useMeta = true;
        }
        if (this.lore == null) {
            this.lore = new ArrayList<>();
        }
        for (String str : text) {
            this.lore.add(str.replace("&", "§"));

            if (replaces.length % 2 == 0) {
                for (int i = 0; i < replaces.length; i += 2) {
                    this.lore.set(this.lore.size() - 1,
                                  this.lore.get(this.lore.size() - 1).replace(replaces[i], replaces[i + 1]));
                }
            }
        }

        return this;
    }

    public ItemBuilder replaceLore(String... replaces) {
        if (!this.useMeta) {
            return this;
        }

        if (this.lore != null) {
            if (replaces.length % 2 == 0) {
                for (int i = 0; i < replaces.length; i += 2) {
                    for (int j = 0; j < this.lore.size(); j++) {
                        this.lore.set(j, this.lore.get(j).replace(replaces[i], replaces[i + 1]));
                    }
                }
            }
        }

        return this;
    }

    public ItemBuilder potion(PotionEffect potionEffect) {
        if (!this.useMeta) {
            this.useMeta = true;
        }

        if (potions == null) {
            potions = new ArrayList<>();
        }

        potions.add(potionEffect);
        return this;
    }

    public ItemBuilder potion(List<PotionEffect> potions) {
        if (potions == null) {
            return this;
        }

        if (!this.useMeta) {
            this.useMeta = true;
        }

        if (this.potions == null) {
            this.potions = new ArrayList<>();
        }

        this.potions.addAll(potions);
        return this;
    }

    public ItemBuilder glow() {
        this.glow = true;
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        this.glow = glow;
        return this;
    }
    public ItemBuilder color(Color color) {
        this.useMeta = true;
        this.color = color;
        return this;
    }

    public ItemBuilder skin(String skin) {
        this.useMeta = true;
        this.skinOwner = skin;
        return this;
    }

    public ItemBuilder skin(Skin skin) {
        this.useMeta = true;
        this.skin = skin;
        return this;
    }

    public ItemBuilder skin(Player player) {
        this.useMeta = true;

        GameProfile gameProfile = ((CraftPlayer) player).getHandle().getProfile();
        Property property = gameProfile.getProperties().get("textures").stream().findFirst().orElse(null);

        this.skin = new Skin(player.getName(), property.getValue(), property.getSignature());
        return this;
    }

    public ItemBuilder skin(String value, String signature) {
        this.useMeta = true;
        this.skin = new Skin("none", value, signature);
        return this;
    }

    public ItemBuilder skinURL(String skinURL) {
        this.useMeta = true;
        this.skinUrl = skinURL.toLowerCase().startsWith("http://textures.minecraft.net/texture/") ? skinURL :
                       "http://textures.minecraft.net/texture/" + skinURL;
        return this;
    }

    public ItemBuilder hideAttributes() {
        this.useMeta = true;
        this.hideAttributes = true;
        return this;
    }

    public ItemBuilder showAttributes() {
        this.useMeta = true;
        this.hideAttributes = false;
        return this;
    }

    public ItemBuilder unbreakable() {
        this.unbreakable = true;
        return this;
    }

    public ItemStack build() {
        ItemStack stack = new ItemStack(material, amount, durability);

        if (enchantments != null && !enchantments.isEmpty()) {
            for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                stack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }

        if (useMeta) {
            ItemMeta meta = stack.getItemMeta();

            if (displayName != null) {
                meta.setDisplayName(displayName.replace("&", "§"));
            }

            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }

            /** Colored Leather Armor */
            if (color != null) {
                if (meta instanceof LeatherArmorMeta) {
                    ((LeatherArmorMeta) meta).setColor(color);
                }
            }

            if (potions != null) {
                if (meta instanceof PotionMeta) {
                    PotionMeta potionMeta = (PotionMeta) meta;

                    for (PotionEffect potionEffect : potions)
                        potionMeta.addCustomEffect(potionEffect, true);
                }
            }

            /** Skull Heads */
            if (meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) meta;

                if (skin != null) {
                    GameProfile profile = new GameProfile(
                            skin.getUniqueId() == null ? UUID.randomUUID() : skin.getUniqueId(), skin.getPlayerName());
                    profile.getProperties()
                           .put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
                    try {
                        Field field = skullMeta.getClass().getDeclaredField("profile");
                        field.setAccessible(true);
                        field.set(skullMeta, profile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (skinUrl != null) {
                    GameProfile profile = new GameProfile(
                            UUID.nameUUIDFromBytes(skinUrl.getBytes(StandardCharsets.UTF_8)), "random");
                    profile.getProperties().put("textures", new Property("textures", Base64.getEncoder().encodeToString(
                            String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl)
                                  .getBytes(StandardCharsets.UTF_8))));

                    try {
                        Field field = skullMeta.getClass().getDeclaredField("profile");
                        field.setAccessible(true);
                        field.set(skullMeta, profile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (skinOwner != null) {
                    Player player = Bukkit.getPlayer(skinOwner);

                    if (player == null) {
                        skullMeta.setOwner(skinOwner);
                    } else {
                        try {
                            Field field = skullMeta.getClass().getDeclaredField("profile");
                            field.setAccessible(true);
                            field.set(skullMeta, ((CraftPlayer) player).getHandle().getProfile());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            meta.spigot().setUnbreakable(unbreakable);

            /** Item Flags */
            if (hideAttributes) {
                meta.addItemFlags(ItemFlag.values());
            } else {
                meta.removeItemFlags(ItemFlag.values());
            }

            if (itemFlags != null) {
                meta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
            }

            stack.setItemMeta(meta);
        }

        if (glow && (enchantments == null || enchantments.isEmpty())) {
            try {
                Constructor<?> caller = MinecraftReflection.getCraftItemStackClass()
                                                           .getDeclaredConstructor(ItemStack.class);
                caller.setAccessible(true);
                ItemStack item = (ItemStack) caller.newInstance(stack);
                NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
                compound.put(NbtFactory.ofList("ench"));
                return item;
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                     IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        material = Material.STONE;
        amount = 1;
        durability = 0;

        if (useMeta) {
            useMeta = false;
        }

        if (glow) {
            glow = false;
        }

        if (hideAttributes) {
            hideAttributes = false;
        }

        if (unbreakable) {
            unbreakable = false;
        }

        if (displayName != null) {
            displayName = null;
        }

        if (enchantments != null) {
            enchantments.clear();
            enchantments = null;
        }

        if (lore != null) {
            lore.clear();
            lore = null;
        }

        skinOwner = null;
        skinUrl = null;
        color = null;
        return stack;
    }

    public static ItemStack glow(ItemStack stack) {
        try {
            Constructor<?> caller = MinecraftReflection.getCraftItemStackClass()
                                                       .getDeclaredConstructor(ItemStack.class);
            caller.setAccessible(true);
            ItemStack item = (ItemStack) caller.newInstance(stack);
            NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
            compound.put(NbtFactory.ofList("ench"));
            return item;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return stack;
    }

    public ItemBuilder useMeta() {
        this.useMeta = true;
        return this;
    }

    /*
     * Factory
     */

    public static ItemBuilder newItemBuilder(Material material, int amount, short durability) {
        return new ItemBuilder().type(material).amount(amount).durability(durability);
    }

    public static ItemBuilder newItemBuilder(Material material, int amount) {
        return new ItemBuilder().type(material).amount(amount);
    }

    public static ItemBuilder newItemBuilder(Material material, short durability) {
        return new ItemBuilder().type(material).durability(durability);
    }

    public static ItemBuilder newItemBuilder(Material material) {
        return new ItemBuilder().type(material);
    }

    public static ItemBuilder newItemBuilder(ItemStack stack) {
        return new ItemBuilder().type(stack.getType()).amount(stack.getAmount()).durability(stack.getDurability());
    }

    public static ItemBuilder fromStack(ItemStack stack) {
        ItemBuilder builder = new ItemBuilder().type(stack.getType()).amount(stack.getAmount())
                                               .durability(stack.getDurability());

        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();

            builder.flag(meta.getItemFlags());

            if (meta.hasDisplayName()) {
                builder.name(meta.getDisplayName());
            }

            if (meta.hasLore()) {
                builder.lore(meta.getLore());
            }

            if (meta instanceof LeatherArmorMeta) {
                Color color = ((LeatherArmorMeta) meta).getColor();
                if (color != null) {
                    builder.color(color);
                }
            }

            if (meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) meta;

                try {
                    Field field = skullMeta.getClass().getDeclaredField("profile");
                    field.setAccessible(true);

                    GameProfile profile = (GameProfile) field.get(skullMeta);

                    if (profile != null && profile.getProperties().containsKey("textures")) {
                        Property property = profile.getProperties().get("textures").iterator().next();
                        String texture = new String(Base64.getDecoder().decode(property.getValue()));

                        String skinUrl = texture.substring(texture.indexOf("http://"), texture.lastIndexOf("\""));

                        builder.skinURL(skinUrl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (skullMeta.hasOwner()) {
                    builder.skin(skullMeta.getOwner());
                }
            } else if (meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta) meta;

                builder.potion(potionMeta.getCustomEffects());
            }

            for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet())
                builder.enchantment(entry.getKey(), entry.getValue());
        }

        return builder;
    }
}
