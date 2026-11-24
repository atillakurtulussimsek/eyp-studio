# Örnek paket analizi

Durum: **Gerçek örnek bekleniyor**

## Kamuya açık örnekler (yalnız yapısal referans)
- `references/opensource/eyazisma.online.api/testFiles/` (Apache-2.0): `paketv1x.eyp`, `paketv2x.eyp`, `.eyps`. Üçüncü parti kütüphane üretimi. İmzalar 4 baytlık yer tutucu, PaketOzeti ilişki tipi yanlış büyük harfli.
- İnternette imzalı gerçek .eyp bulunamadı.

## Gerçek örnekler
Kullanıcının KEP gelen kutusundan alınacak (en az bir 1.3 ve bir 2.x, mümkünse bir özel şirket gönderimi). Her biri için: part listesi, ilişkiler, Core, Üstveri alanları, özet algoritmaları, imza/mühür ASN.1 dökümü (profil, politika OID ve hash'i, SPURI, zaman damgası sağlayıcısı).
