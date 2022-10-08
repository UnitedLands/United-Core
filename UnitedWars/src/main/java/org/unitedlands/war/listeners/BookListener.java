package org.unitedlands.war.listeners;

import com.palmergames.bukkit.towny.object.Resident;
import io.github.townyadvanced.eventwar.objects.WarType;
import io.github.townyadvanced.eventwar.objects.WarTypeEnum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.books.Declarer;
import org.unitedlands.war.books.WarTarget;
import org.unitedlands.war.books.WritableDeclaration;
import org.unitedlands.war.books.declaration.DeclarationBook;
import org.unitedlands.war.books.declaration.NationDeclarationBook;
import org.unitedlands.war.books.declaration.TownDeclarationBook;

import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.war.Utils.getMessage;
import static org.unitedlands.war.Utils.getTownyResident;

public class BookListener implements Listener {
    private final UnitedWars unitedWars;

    public BookListener(UnitedWars unitedWars) {
        this.unitedWars = unitedWars;
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        boolean isWritableDeclaration = isWritableDeclaration(event.getPreviousBookMeta().getPersistentDataContainer());
        if (!isWritableDeclaration) return;
        if (!event.isSigning()) return;

        BookMeta bookMeta = event.getNewBookMeta();
        WritableDeclaration writableDeclaration = generateFromMeta(bookMeta);

        // Extract the pages from the new book the player edited
        List<Component> extractedReason = bookMeta.pages();
        // Set the reason of the declaration to the new pages.
        writableDeclaration.setReason(extractedReason);

        // Generate the sealed book.
        WarType type = writableDeclaration.getWarType();
        DeclarationBook declarationBook = null;
        if (type.isTownWar()) {
            declarationBook = new TownDeclarationBook(writableDeclaration);
        } else if (type.isNationWar()) {
            declarationBook = new NationDeclarationBook(writableDeclaration);
        }


        // Replace the held item, the old book, with the new sealed book to be used for declaration.
        DeclarationBook finalDeclarationBook = declarationBook;
        unitedWars.getServer().getScheduler().runTask(unitedWars, () -> event.getPlayer().getInventory().setItemInMainHand(finalDeclarationBook.getBook()));
    }

    private static boolean isWritableDeclaration(PersistentDataContainer pdc) {
        return WritableDeclaration.isWritableDeclaration(pdc);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBookInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        if (!isWritableDeclaration(item.getItemMeta().getPersistentDataContainer())) return;

        Resident resident = getTownyResident(event.getPlayer());
        WritableDeclaration writableDeclaration = generateFromMeta((BookMeta) item.getItemMeta());

        if (!canWriteBook(resident, writableDeclaration)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(getMessage("only-mayors-can-sign", Placeholder.component("declarer-name", text(writableDeclaration.getDeclarerName()))));
            event.getPlayer().closeInventory();
        }
    }

    private boolean canWriteBook(Resident resident, WritableDeclaration writableDeclaration) {
        return resident.getTownOrNull().equals(writableDeclaration.getDeclarer().getTown()) && resident.isMayor();
    }

    private WritableDeclaration generateFromMeta(BookMeta bookMeta) {
        PersistentDataContainer pdc = bookMeta.getPersistentDataContainer();

        UUID declaringUUID = getUUID(pdc, "eventwar.dow.book.town");
        Declarer declarer = new Declarer(UnitedWars.TOWNY_API.getTown(declaringUUID));


        WarType warType = WarTypeEnum.valueOf(pdc.get(NamespacedKey.fromString("eventwar.dow.book.type"), PersistentDataType.STRING).toUpperCase()).getType();
        UUID targetUUID = getUUID(pdc, "unitedwars.book.target");
        WarTarget warTarget = getWarTarget(warType, targetUUID);

        return new WritableDeclaration(declarer, warTarget, warType);
    }

    @NotNull
    private UUID getUUID(PersistentDataContainer pdc, String key) {
        return UUID.fromString(pdc.get(NamespacedKey.fromString(key), PersistentDataType.STRING));
    }


    @NotNull
    private WarTarget getWarTarget(WarType warType, UUID targetUUID) {
        WarTarget warTarget;
        if (warType.isTownWar())
            warTarget = new WarTarget(UnitedWars.TOWNY_API.getTown(targetUUID));
        else
            warTarget = new WarTarget(UnitedWars.TOWNY_API.getNation(targetUUID));
        return warTarget;
    }
}
