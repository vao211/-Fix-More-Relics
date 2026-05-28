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

import java.util.Map;

public class MoreRelicsTrinketItem extends TrinketItem {
    private AttributeModifiersComponent customAttributes = AttributeModifiersComponent.builder().build();

    public MoreRelicsTrinketItem(Settings settings, @Nullable AttributeModifiersComponent customAttributes) {
        super(settings);
        if (customAttributes != null) {
            this.customAttributes = customAttributes;
        }
    }

    @Override
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> uniqueModifiers = ArrayListMultimap.create();
        try {
            Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> defaultModifiers = super.getModifiers(stack, slot, entity, slotIdentifier);
            String itemName = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).getPath();
            for (Map.Entry<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entry : defaultModifiers.entries()) {
                EntityAttributeModifier modifier = entry.getValue();
                String attributeName = entry.getKey().value().getTranslationKey().replace("attribute.name.", "");
                String rawPath = slotIdentifier.getPath() + "_" + itemName + "_base_" + attributeName + "_" + modifier.operation().name();
                String safePath = rawPath.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
                uniqueModifiers.put(entry.getKey(), new EntityAttributeModifier(Identifier.of(slotIdentifier.getNamespace(), safePath), modifier.value(), modifier.operation()));
            }

            for (AttributeModifiersComponent.Entry entry : this.customAttributes.modifiers()) {
                EntityAttributeModifier modifier = entry.modifier();
                String attributeName = entry.attribute().value().getTranslationKey().replace("attribute.name.", "");
                String rawPath = slotIdentifier.getPath() + "_" + itemName + "_" + attributeName + "_" + modifier.operation().name();
                String safePath = rawPath.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
                Identifier uniqueModId = Identifier.of(slotIdentifier.getNamespace(), safePath);
                uniqueModifiers.put(entry.attribute(),
                        new EntityAttributeModifier(uniqueModId, modifier.value(), modifier.operation()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uniqueModifiers;
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