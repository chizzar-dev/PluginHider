<div align="center">

<img src="https://capsule-render.vercel.app/api?type=rect&color=0:0b1220,100:0e7490&height=110&section=header&text=PluginHider&fontSize=42&fontColor=22d3ee&fontAlignY=54&desc=Hide%20your%20plugin%20list%20from%20players&descSize=13&descColor=94a3b8&descAlignY=80" width="100%" alt="PluginHider" />

<p>
<img src="https://img.shields.io/github/v/release/chizzar-dev/PluginHider?style=flat&label=release&color=06b6d4&labelColor=0b1220" alt="release" />
<img src="https://img.shields.io/badge/Minecraft-1.8%20%E2%80%93%201.21.11-0891b2?style=flat&labelColor=0b1220" alt="Minecraft 1.8 - 1.21.11" />
<img src="https://img.shields.io/badge/Java-8%2B-155e75?style=flat&labelColor=0b1220&logo=openjdk&logoColor=22d3ee" alt="Java 8+" />
<a href="LICENSE"><img src="https://img.shields.io/github/license/chizzar-dev/PluginHider?style=flat&label=license&color=0e7490&labelColor=0b1220" alt="license" /></a>
</p>

</div>

PluginHider keeps players from discovering which plugins your server runs. It blocks the usual listing commands, strips them from the client command tree on 1.13+, and filters tab completion.

*PluginHider, oyuncularin sunucunda hangi pluginlerin kurulu oldugunu gormesini engeller. Listeleme komutlarini kapatir, 1.13+ surumlerde komutu istemci agacindan siler ve tab tamamlamayi suzer.*

## Features · Özellikler
- `/plugins`, `/pl`, `/ver`, `/version`, `/about`, `/?` komutlarını engeller
- `/bukkit:pl`, `/minecraft:me` gibi **ad alanlı** komutları da kapatır — bu olmadan engeli aşmak çok kolay
- 1.13+ sürümlerde komut, istemcinin komut ağacından tamamen **silinir**; oyuncu komutun varlığını bile bilmez
- Tab tamamlamada önerilerden süzülür
- `pluginhider.bypass` yetkisi olanlar (varsayılan: OP) her şeyi görmeye devam eder
- Engellenen denemeler isteğe bağlı olarak konsola yazılır
- Hiçbir bağımlılığı yok, ProtocolLib gerektirmez

## How it works · Nasıl çalışır
| Katman | Ne yapar | Sürüm |
| :-- | :-- | :-- |
| Komut çalıştırma | Komut çalıştırılmadan engellenir | 1.8 → 1.21.11 |
| Komut ağacı | Komut istemciye hiç gönderilmez | 1.13+ |
| Tab tamamlama | Öneri listesinden çıkarılır | 1.13+ |

> 1.13 öncesi sürümlerde sunucu yazılımı istemciye komut ağacı göndermez. Orada yalnızca ilk katman
> çalışır: komut engellenir, ama tab tamamlama filtrelenemez.

## Installation · Kurulum
1. [Releases](https://github.com/chizzar-dev/PluginHider/releases/latest) sayfasından `PluginHider.jar` dosyasını indir.
2. Sunucunun `plugins/` klasörüne at.
3. Sunucuyu yeniden başlat.
4. `plugins/PluginHider/config.yml` dosyasından gizlenecek komutları düzenle.

## Commands · Komutlar
| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/pluginhider reload` | Ayarları yeniden yükler | `pluginhider.admin` |

**Alias:** `/phider`

## Permissions · Yetkiler
| Yetki | Açıklama | Varsayılan |
|-------|----------|------------|
| `pluginhider.bypass` | Gizlenen komutları görür ve kullanır | op |
| `pluginhider.admin` | Ayarları yönetir | op |

## Configuration · Ayarlar
| Anahtar | Açıklama |
|---------|----------|
| `blocked-commands` | Gizlenecek komutların listesi |
| `block-namespaced` | `/bukkit:pl` gibi ad alanlı tüm komutları gizle |
| `allowed-namespaces` | Ad alanı engellemesinden muaf tutulacaklar |
| `log-attempts` | Engellenen denemeleri konsola yaz |
| `messages.blocked` | Engellenince gösterilecek mesaj |

> 💡 **İpucu:** `messages.blocked` değerini sunucunun normal "bilinmeyen komut" mesajıyla aynı
> tutarsan, oyuncu bir engelleme olduğunu bile anlamaz.

## Building · Derleme
```bash
mvn clean package
```
Çıktı · Output: `target/PluginHider.jar`

Her push [GitHub Actions](https://github.com/chizzar-dev/PluginHider/actions/workflows/build.yml) ile derlenir; `v*` etiketli sürümler jar'la birlikte [Releases](https://github.com/chizzar-dev/PluginHider/releases) sayfasına eklenir.
<br><sub>Every push is built by GitHub Actions; tagged `v*` releases attach the jar.</sub>

## License · Lisans
[MIT](LICENSE) — istediğin gibi kullan, değiştir, dağıt · use, modify and distribute freely

<div align="center"><sub>chizzar-dev · Minecraft plugins for 1.8 – 1.21.11</sub></div>
