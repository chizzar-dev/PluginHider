package com.metox.pluginhider;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * 1.13 ile gelen PlayerCommandSendEvent ve TabCompleteEvent'e baglanir.
 *
 * Bu olaylar 1.8'de yok. Sinifi dogrudan yazmak yerine Class.forName ile
 * bulup EventExecutor ile kaydediyoruz; boylece ayni jar 1.8'de de yuklenir.
 */
final class ModernHooks implements Listener {

    private final PluginHider plugin;

    private ModernHooks(PluginHider plugin) {
        this.plugin = plugin;
    }

    /** Kaydedilen olay sayisini dondurur (0 = surum desteklemiyor). */
    @SuppressWarnings("unchecked")
    static int register(PluginHider plugin) {
        ModernHooks hooks = new ModernHooks(plugin);
        PluginManager pm = plugin.getServer().getPluginManager();
        int count = 0;

        // Komut agacindan silme - istemci komutu hic gormez
        try {
            Class<?> cls = Class.forName("org.bukkit.event.player.PlayerCommandSendEvent");
            pm.registerEvent((Class<? extends Event>) cls, hooks, EventPriority.NORMAL,
                    new EventExecutor() {
                        @Override
                        public void execute(Listener l, Event e) {
                            ((ModernHooks) l).onCommandSend(e);
                        }
                    }, plugin);
            count++;
        } catch (Throwable ignored) {
        }

        // Tab tamamlama onerilerini suzme
        try {
            Class<?> cls = Class.forName("org.bukkit.event.server.TabCompleteEvent");
            pm.registerEvent((Class<? extends Event>) cls, hooks, EventPriority.NORMAL,
                    new EventExecutor() {
                        @Override
                        public void execute(Listener l, Event e) {
                            ((ModernHooks) l).onTabComplete(e);
                        }
                    }, plugin);
            count++;
        } catch (Throwable ignored) {
        }

        return count;
    }

    /** Oyuncuya gonderilen komut listesinden gizli komutlari cikarir. */
    @SuppressWarnings("unchecked")
    void onCommandSend(Event e) {
        try {
            Object sender = e.getClass().getMethod("getPlayer").invoke(e);
            if (!(sender instanceof Player)) return;
            if (plugin.canBypass((Player) sender)) return;

            Object cmds = e.getClass().getMethod("getCommands").invoke(e);
            if (!(cmds instanceof Collection)) return;

            // Bu koleksiyon degistirilebilir; Bukkit silmeye izin verir.
            Iterator<String> it = ((Collection<String>) cmds).iterator();
            while (it.hasNext()) {
                String c = it.next();
                if (c != null && plugin.isHidden(c)) it.remove();
            }
        } catch (UnsupportedOperationException ignored) {
            // Bazi sunucu yazilimlarinda liste salt okunur olabilir
        } catch (Throwable ignored) {
        }
    }

    /** Yazilirken cikan onerilerden gizli komutlari suzer. */
    @SuppressWarnings("unchecked")
    void onTabComplete(Event e) {
        try {
            Object sender = e.getClass().getMethod("getSender").invoke(e);
            if (sender instanceof Player && plugin.canBypass((Player) sender)) return;

            Object bufferObj = e.getClass().getMethod("getBuffer").invoke(e);
            String buffer = bufferObj == null ? "" : bufferObj.toString();

            // Yalnizca komut adinin kendisi yazilirken suz ("/pl" gibi).
            // Komutun argumanlari tamamlaniyorsa karisma.
            if (!buffer.startsWith("/") || buffer.indexOf(' ') >= 0) return;

            Object completionsObj = e.getClass().getMethod("getCompletions").invoke(e);
            if (!(completionsObj instanceof List)) return;
            List<String> completions = (List<String>) completionsObj;

            List<String> kept = new java.util.ArrayList<String>();
            for (String c : completions) {
                if (c == null) continue;
                String name = c.startsWith("/") ? c.substring(1) : c;
                if (!plugin.isHidden(name.toLowerCase(Locale.ENGLISH))) kept.add(c);
            }
            if (kept.size() != completions.size()) {
                Method setter = e.getClass().getMethod("setCompletions", List.class);
                setter.invoke(e, kept);
            }
        } catch (Throwable ignored) {
        }
    }
}
