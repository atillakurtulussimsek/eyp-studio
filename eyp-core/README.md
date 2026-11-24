# eyp-core

Kart, ağ veya imza bağımlılığı olmayan çekirdek.

- `model` – Üstveri ve Nihai Üstveri alanları (XSD sırası korunur). Sınıf adları İngilizce, Javadoc'ta Rehber terimi.
- `xml` – StAX ile UTF-8 XML üretimi; `SafeXml` (DTD/XXE kapalı); `SchemaValidator` (paketle gelen, yamalı XSD'ler).
- `opc` – `OpcWriter` (ZIP + [Content_Types].xml + .rels), `OpcReader` (ZIP bombası, yol geçişi, XXE ve yinelenen ad korumalı).
- `pkg` – `PackageBuilder`: Paket Özeti → (imza) → Nihai Üstveri + Core + Nihai Özet → (mühür) → yaz. `EypConstants`: yalnız Rehber ve resmî API'den alınan adlar.
- `validation` – `PackageValidator`: Rehber 2.1 Ek-1 kural kodlarıyla (K.xx) bulgu listesi.

Özetler: her bileşen için SHA-512 + seçilebilir ikinci algoritma (varsayılan SHA-256; resmî API SHA-384 kullanır). Değerler bileşenin ham baytları üzerinden, Base64.
