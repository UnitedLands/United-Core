package org.unitedlands.war.books.generators;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.unitedlands.war.books.declaration.DeclarationWarBook;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class DeclarationGenerator extends BookGenerator {
    private final DeclarationWarBook declarationBook;
    private ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
    private BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

    public DeclarationGenerator(DeclarationWarBook declarationBook) {
        super(declarationBook);
        this.declarationBook = declarationBook;
    }

    @Override
    public ItemStack generateBook() {
        List<Component> pages = getPagesWithReason();

        BookMeta.BookMetaBuilder builder = bookMeta.toBuilder();
        // add the pages to the book
        pages.forEach(builder::addPage);
        bookMeta = builder
                .author(text(getName("declarer")))
                .title(text("War Declaration Book"))
                .build();
        bookMeta.displayName(getDisplayName());
        bookMeta.setCustomModelData(2);
        attachWarData();
        bookItem.setItemMeta(bookMeta);
        return bookItem;
    }

    private void attachWarData() {
        PersistentDataContainer pdc = bookMeta.getPersistentDataContainer();
        UUID townUUID = declarationBook.getDeclarer().town().getUUID();
        UUID targetUUID = declarationBook.getWarTarget().uuid();
        pdc.set(NamespacedKey.fromString("eventwar.dow.book.town"), PersistentDataType.STRING, townUUID.toString());
        pdc.set(NamespacedKey.fromString("eventwar.dow.book.type"), PersistentDataType.STRING, declarationBook.getType().name());
        pdc.set(NamespacedKey.fromString("unitedwars.book.target"), PersistentDataType.STRING, targetUUID.toString());
    }

    private List<Component> getPagesWithReason() {
        List<Component> pages = getConfiguredPages();
        pages.addAll(declarationBook.getReason());
        return pages;
    }

}
