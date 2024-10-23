package org.unitedlands.upkeep.listeners;

import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.TextComponent;
import com.palmergames.adventure.text.event.HoverEvent;
import com.palmergames.adventure.text.format.NamedTextColor;
import com.palmergames.adventure.text.format.TextDecoration;
import com.palmergames.adventure.text.minimessage.MiniMessage;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.statusscreens.StatusScreen;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.upkeep.UnitedUpkeep;
import org.unitedlands.upkeep.calculators.TownUpkeepCalculator;
import org.unitedlands.upkeep.util.NationMetaController;
import org.unitedlands.upkeep.util.TerritorialMetaController;

import java.util.Objects;

public class StatusScreenListener implements Listener {
    private final UnitedUpkeep unitedUpkeep;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();
    private Town town;
    private StatusScreen screen;

    public StatusScreenListener(UnitedUpkeep unitedUpkeep) {
        this.unitedUpkeep = unitedUpkeep;
    }

    @EventHandler
    public void onStatusScreen(NationStatusScreenEvent event) {
        this.screen = event.getStatusScreen();
        if (NationMetaController.isOfficialNation(event.getNation(), "major")) {
            this.addMajorNationComponent();
        } else if (NationMetaController.isOfficialNation(event.getNation(), "minor")) {
            this.addMinorNationComponent();
        }

    }

    @EventHandler
    public void onStatusScreen(TownStatusScreenEvent event) {
        this.screen = event.getStatusScreen();
        this.town = event.getTown();
        this.replaceTownSizeComponent();
        this.replaceUpkeepComponent();
        if(TerritorialMetaController.toggledTerritorialWars(this.town)) {
            this.addTerritorialWarComponent();
        }
        if (this.town.hasNation() && NationMetaController.isOfficialNation(this.town.getNationOrNull(), "major")) {
            this.addMajorTownComponent();
        } else if (this.town.hasNation() && NationMetaController.isOfficialNation(this.town.getNationOrNull(), "minor")) {
            this.addMinorTownComponent();
        }
        this.screen.removeStatusComponent("neutralityCost");
    }

    private void addTerritorialWarComponent() {
        Component territorialWarComponent = this.miniMessage.deserialize(Objects.requireNonNull(this.unitedUpkeep.getConfig().getString("messages.territorialWars")));
        Component subtitle = this.screen.getComponentOrNull("subtitle");
        if (subtitle == null) {
            subtitle = this.screen.getComponentOrNull("title");
            this.screen.replaceComponent("title", Objects.requireNonNull(subtitle).append(Component.newline().append(territorialWarComponent)));
        } else {
            this.screen.replaceComponent("subtitle", subtitle.append(Component.newline().append(territorialWarComponent)));
        }
    }

    private void addMajorTownComponent() {
        Component subtitle = this.screen.getComponentOrNull("subtitle");
        if (subtitle == null) {
            subtitle = this.screen.getComponentOrNull("title");
            this.screen.replaceComponent("title", Objects.requireNonNull(subtitle).append(Component.newline().append(this.getMajorTownComponent())));
        } else {
            this.screen.replaceComponent("subtitle", subtitle.append(Component.newline().append(this.getMajorTownComponent())));
        }
    }

    private void addMajorNationComponent() {
        Component subtitle = this.screen.getComponentOrNull("subtitle");
        if (subtitle == null) {
            subtitle = this.screen.getComponentOrNull("nation_title");
            this.screen.replaceComponent("nation_title", Objects.requireNonNull(subtitle).append(Component.newline().append(this.getMajorNationComponent())));
        } else {
            this.screen.replaceComponent("subtitle", subtitle.append(Component.newline().append(this.getMajorNationComponent())));
        }
    }

    private void addMinorTownComponent() {
        Component subtitle = this.screen.getComponentOrNull("subtitle");
        if (subtitle == null) {
            subtitle = this.screen.getComponentOrNull("title");
            this.screen.replaceComponent("title", Objects.requireNonNull(subtitle).append(Component.newline().append(this.getMinorTownComponent())));
        } else {
            this.screen.replaceComponent("subtitle", subtitle.append(Component.newline().append(this.getMinorTownComponent())));
        }
    }

    private void addMinorNationComponent() {
        Component subtitle = this.screen.getComponentOrNull("subtitle");
        if (subtitle == null) {
            subtitle = this.screen.getComponentOrNull("nation_title");
            this.screen.replaceComponent("nation_title", Objects.requireNonNull(subtitle).append(Component.newline().append(this.getMinorNationComponent())));
        } else {
            this.screen.replaceComponent("subtitle", subtitle.append(Component.newline().append(this.getMinorNationComponent())));
        }
    }

    private TownUpkeepCalculator getTownUpkeepCalculator() {
        return new TownUpkeepCalculator(this.unitedUpkeep, this.town);
    }

    private int getTownsize() {
        return this.town.getTownBlocks().size();
    }

    private void replaceUpkeepComponent() {
        TextComponent upkeepComponent = this.getUpkeepComponent();
        if (this.getBonusBlockDiscount() > 0.0 && this.getNationDiscount() > 0.0) {
            upkeepComponent = upkeepComponent.hoverEvent(HoverEvent.showText(this.getComponentWithAllDiscounts()));
        } else if (this.getNationDiscount() > 0.0) {
            upkeepComponent = upkeepComponent.hoverEvent(HoverEvent.showText(this.getNationDiscountComponent().append(this.getNeutralityComponent())));
        } else if (this.getBonusBlockDiscount() > 0.0) {
            upkeepComponent = upkeepComponent.hoverEvent(HoverEvent.showText(this.getBonusDiscountComponent().append(this.getNeutralityComponent())));
        }

        this.screen.replaceComponent("upkeep", upkeepComponent);
    }

    private void replaceTownSizeComponent() {
        TextComponent townSizeComponent = Component.text("\nTown Size: ", NamedTextColor.DARK_GREEN).append(Component.text(this.getTownsize(), NamedTextColor.GREEN));
        this.screen.replaceComponent("townblocks", townSizeComponent);
    }

    private TextComponent getMajorNationComponent() {
        return (TextComponent)this.miniMessage.deserialize(Objects.requireNonNull(this.unitedUpkeep.getConfig().getString("titles-nation.major")));
    }


    private TextComponent getMajorTownComponent() {
        return (TextComponent)this.miniMessage.deserialize(Objects.requireNonNull(this.unitedUpkeep.getConfig().getString("titles-town.major")));
    }

    private TextComponent getMinorNationComponent() {
        return (TextComponent)this.miniMessage.deserialize(Objects.requireNonNull(this.unitedUpkeep.getConfig().getString("titles-nation.minor")));
    }


    private TextComponent getMinorTownComponent() {
        return (TextComponent)this.miniMessage.deserialize(Objects.requireNonNull(this.unitedUpkeep.getConfig().getString("titles-town.minor")));
    }

    private TextComponent getComponentWithAllDiscounts() {
        return Component.text("").append(this.getNationDiscountComponent()).append(Component.text("\n")).append(this.getBonusDiscountComponent()).append(this.getNeutralityComponent());
    }

    private TextComponent getNationDiscountComponent() {
        return Component.text("[Nation Discount: ", NamedTextColor.DARK_GREEN).append(Component.text(this.getNationDiscount() + " Gold", NamedTextColor.GREEN)).append(Component.text("]", NamedTextColor.DARK_GREEN));
    }

    private TextComponent getBonusDiscountComponent() {
        return Component.text("[Bonus Discount: ", NamedTextColor.DARK_GREEN).append(Component.text(this.getBonusBlockDiscount() + " Gold", NamedTextColor.GREEN)).append(Component.text("(" + this.town.getBonusBlocks() + ")", NamedTextColor.AQUA)).append(Component.text("]", NamedTextColor.DARK_GREEN));
    }

    private TextComponent getNeutralityComponent() {
        if (!this.town.isNeutral()) {
            return Component.empty();
        } else {
            double fee = this.getNeutralityFee();
            return Component.text("\n[Neutrality Fees: ", NamedTextColor.DARK_GREEN).append(Component.text(fee + " Gold", NamedTextColor.RED)).append(Component.text("]", NamedTextColor.DARK_GREEN));
        }
    }

    private double getNeutralityFee() {
        int defaultFee = 25;
        return Math.floor(this.getTownUpkeepCalculator().getDiscountedUpkeep() * 0.1 + (double)defaultFee);
    }

    private TextComponent getUpkeepComponent() {
        TownUpkeepCalculator calculator = this.getTownUpkeepCalculator();
        double neutralityFee = 0.0;
        if (this.town.isNeutral()) {
            neutralityFee = this.getNeutralityFee();
        }

        double upkeep = calculator.townUpkeepWithoutBonusDiscount() + neutralityFee;
        double discountedUpkeep = calculator.calculateTownUpkeep() + neutralityFee;
        return Component.text("").append(Component.text("\nUpkeep: ", NamedTextColor.DARK_GREEN)).append(Component.text(upkeep, NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH)).append(Component.text(" " + discountedUpkeep + " Gold", NamedTextColor.RED));
    }

    private double getNationDiscount() {
        TownUpkeepCalculator calculator = this.getTownUpkeepCalculator();
        return calculator.getNationDiscount();
    }

    private double getBonusBlockDiscount() {
        TownUpkeepCalculator calculator = this.getTownUpkeepCalculator();
        return calculator.townUpkeepWithoutBonusDiscount() - calculator.calculateBonusBlockDiscount();
    }
}
