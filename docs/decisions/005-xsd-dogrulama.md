# 005 – XSD şemaları ve doğrulama

Durum: **Karar verildi**
Tarih: 2026-11-23

## Bulgular
- Ayrı bir XSD indirme sayfası yok; şemalar resmî API arşivinde (`Cbddo.eYazisma/Xsd/`). Kopyası: `docs/references/xsd-eyp-2.1/`.
- Kök elemanlar: `UstVeri` (büyük V), `NihaiUstVeri`, `PaketOzeti`, `NihaiOzet`, `BelgeHedef`.
- Bilinen farklar:
  1. Core version: Rehber "rehber sürümü" der, API normal pakete "2.0" yazar.
  2. Güncelleme paketi uzantısı: Rehber `.eypg`, API BENIOKU `.eyp`.
  3. Rehber özet bileşenlerini XMLDSig'e bağlar; XSD kendi namespace'inde iki DigestItem tanımlar.
  4. bahricanli/eyazisma README: resmî API çıktısı şemalardan birebir geçmiyor (ayrıntı yok).
  5. Rehber P3/P4 ↔ CAdES formu etiketlemesi BTK profil rehberiyle tutarsız.

## Karar
- Üretilen XML'ler hem XSD'ye hem resmî API okumasına karşı doğrulanır.
- Farklar `docs/xsd-differences.md`'de belgelenir. Çelişkide resmî API çıktısı esas alınır, çünkü EBYS'ler bu API'yi kullanıyor. Rehber'den sapma kullanıcıya bildirilir.
