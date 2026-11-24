# EYP Oluşturucu

Varkhe Digital. Özel şirketlerin kamu kurumlarına KEP üzerinden gönderdiği resmî yazılar için
e-Yazışma Teknik Rehberi'ne uygun `.eyp` paketleri üreten masaüstü uygulaması (geliştirme aşamasında).

## Modüller

| Modül | Durum | Sorumluluk |
|---|---|---|
| `eyp-core` | Faz 1 | Model, XML (Üstveri, Paket/Nihai Özet, Nihai Üstveri, Core), OPC/ZIP yazma-okuma, özetler, yapısal doğrulama |
| `eyp-cli` | Faz 1 | `eyp olustur`, `eyp incele`, `eyp dogrula` |
| `eyp-crosscheck` | Faz 1 (yalnız test) | Resmî e-Yazışma Java API 2.1 ile çapraz doğrulama; dağıtılmaz |
| `eyp-sign`, `eyp-pdf`, `eyp-gui` | Planlı | Faz 2–4 |

Kararlar `docs/decisions/`, XSD/resmî API farkları `docs/xsd-differences.md`.

## Derleme

JDK 21 gerekir (≥ 21.0.7). Konumu `JDK21` ortam değişkeniyle verilir:

```bash
export JDK21=/path/to/jdk-21
./gradlew check          # testler + aynı testler tr_TR yerel ayarıyla (testTr)
./gradlew :eyp-cli:installDist
eyp-cli/build/install/eyp/bin/eyp --help
```
