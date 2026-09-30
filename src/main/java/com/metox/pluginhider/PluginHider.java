package com.metox.pluginhider;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Sunucudaki plugin listesini ve komut sizintisini gizler.
 *
 * Uc katman:
 *   1. Komut calistirma  - /pl, /ver gibi komutlar engellenir (tum surumler)
 *   2. Komut agaci       - komutlar istemcinin listesinden silinir (1.13+)
 *   3. Tab tamamlama     - onerilerden suzulur (1.13+)
 */
public class PluginHider extends JavaPlugin implements Listener, TabExecutor {

    private Set<String> blocked = new HashSet<String>();
    private Set<String> allowedNamespaces = new HashSet<String>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadLists();

        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("pluginhider") != null) {
            getCommand("pluginhider").setExecutor(this);
            getCommand("pluginhider").setTabCompleter(this);
        }

        // 1.13+ olaylari: dogrudan sinif referansi vermeden kaydet ki
        // ayni jar 1.8'de de sorunsuz yuklensin.
        int modern = ModernHooks.register(this);
        getLogger().info("PluginHider aktif - algilanan surum 1." + Compat.MINOR
                + (Compat.PATCH > 0 ? "." + Compat.PATCH : "")
                + ", gizlenen komut: " + blocked.size()
                + (modern > 0 ? ", komut agaci filtresi acik" : ", komut agaci filtresi yok (1.13 oncesi)"));
    }

    void reloadLists() {
        Set<String> b = new HashSet<String>();
        for (String s : getConfig().getStringList("blocked-commands")) {
            if (s == null) continue;
            String v = s.trim().toLowerCase(Locale.ENGLISH);
            if (v.startsWith("/")) v = v.substring(1);
            if (!v.isEmpty()) b.add(v);
        }
        blocked = b;

        Set<String> ns = new HashSet<String>();
        for (String s : getConfig().getStringList("allowed-namespaces")) {
            if (s != null && !s.trim().isEmpty()) ns.add(s.trim().toLowerCase(Locale.ENGLISH));
        }
        allowedNamespaces = ns;
    }

    // ------------------------------------------------------------------
    // 1) Komut calistirma
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (canBypass(p)) return;

        String raw = e.getMessage();
        if (raw == null || raw.length() < 2) return;

        String label = label(raw);
        if (label == null || !isHidden(label)) return;

        e.setCancelled(true);
        String reply = getConfig().getString("messages.blocked", "");
        if (reply != null && !reply.isEmpty()) {
            p.sendMessage(Compat.color(reply.replace("%command%", "/" + label)));
        }
        if (getConfig().getBoolean("log-attempts", false)) {
            getLogger().info(p.getName() + " engellenen komutu denedi: /" + label);
        }
    }

    /** "/bukkit:pl arg" -> "bukkit:pl" */
    private String label(String message) {
        String s = message.startsWith("/") ? message.substring(1) : message;
        int sp = s.indexOf(' ');
        if (sp >= 0) s = s.substring(0, sp);
        s = s.trim().toLowerCase(Locale.ENGLISH);
        return s.isEmpty() ? null : s;
    }

    /** Bu komut gizlensin mi? Hem duz hem ad-alanli (bukkit:pl) hali kontrol edilir. */
    boolean isHidden(String label) {
        if (label == null || label.isEmpty()) return false;
        String l = label.toLowerCase(Locale.ENGLISH);

        int colon = l.indexOf(':');
        if (colon > 0) {
            String namespace = l.substring(0, colon);
            String name = l.substring(colon + 1);
            if (getConfig().getBoolean("block-namespaced", true)
                    && !allowedNamespaces.contains(namespace)) {
                return true;
            }
            return blocked.contains(name);
        }
        return blocked.contains(l);
    }

    boolean canBypass(CommandSender s) {
        return s != null && s.hasPermission("pluginhider.bypass");
    }

    // ------------------------------------------------------------------
    // Komut
    // ------------------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("pluginhider.admin")) {
                sender.sendMessage(Compat.color(getConfig().getString(
                        "messages.no-permission", "&cBunun icin yetkin yok.")));
                return true;
            }
            reloadConfig();
            reloadLists();
            sender.sendMessage(Compat.color(getConfig().getString(
                    "messages.reloaded", "&aPluginHider yeniden yuklendi.")
                    .replace("%count%", String.valueOf(blocked.size()))));
            return true;
        }
        sender.sendMessage(Compat.color("&7/" + label + " reload"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("pluginhider.admin")
                && "reload".startsWith(args[0].toLowerCase(Locale.ENGLISH))) {
            return new ArrayList<String>(Arrays.asList("reload"));
        }
        return Collections.emptyList();
    }
}
