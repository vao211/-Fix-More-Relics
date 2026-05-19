package com.more_relics.fabric.compat.trinkets;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class MoreRelicsTrinketItem extends TrinketItem {
    private AttributeModifiersComponent customAttributes = AttributeModifiersComponent.builder().build();

    public MoreRelicsTrinketItem(Settings settings, @Nullable AttributeModifiersComponent customAttributes) {
        super(settings);
        if (customAttributes != null) {
            this.customAttributes = customAttributes;
        }
    }

    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
        var defaultModifiers = super.getModifiers(stack, slot, entity, slotIdentifier);

        // --- FIX TRINKET STACKING BUG Like RELICS ---
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> mutableModifiers = ArrayListMultimap.create(defaultModifiers);
        String itemName = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).getPath();

        for (var entry : this.customAttributes.modifiers()) {
            Identifier uniqueModId = Identifier.of(slotIdentifier.getNamespace(),
                    slotIdentifier.getPath() + "_" + itemName + "_" + entry.modifier().id().getPath());
            mutableModifiers.put(entry.attribute(),
                    new EntityAttributeModifier(uniqueModId, entry.modifier().value(), entry.modifier().operation()));
        }
        return mutableModifiers;
        // ------------------------------------------------
    }

    public void setConfigurableModifiers(AttributeModifiersComponent component) {
        this.customAttributes = component;
    }

    @Override
    public boolean canUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        var isOnCooldown = false;
        if (entity instanceof PlayerEntity player) {
            isOnCooldown = !player.isCreative() && player.getItemCooldownManager().isCoolingDown(stack.getItem());
        }
        return super.canUnequip(stack, slot, entity) && !isOnCooldown;
    }
}