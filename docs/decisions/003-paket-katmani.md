# 003 – Paket oluşturma katmanı

Durum: **Karar verildi**
Tarih: 2026-11-23

## Bulgular
- Resmî e-Yazışma API 2.1 (Java + .NET, 30.05.2024) resmiyazisma.gov.tr/eyazisma-ciktilar adresinde; kaynak kod dahil.
- Rehber 2.1 §5.4: "API'ler açık kaynak kodlu olup üzerinde istenildiği şekilde güncelleme ve değişiklik yapılabilir." Arşivde lisans dosyası yok; yeniden dağıtım şartı açıkça yazılmamış.
- API paketi kurar ama imzalamaz. Akış: UstYazi/Ek ekle → Ustveri → PaketOzeti → ImzaEkle(byte[]) → NihaiUstveri → Core → NihaiOzet → MuhurEkle(byte[]) → Kapat.
- Java API bağımlılıkları eski: dom4j 1.6.1 (bilinen XXE açığı CVE-2018-1000632), poi 4.1.1, commons-compress 1.19.
- API normal pakette Core version'a **"2.0"** yazıyor; "2.1" yalnız güncelleme paketinde (`Araclar.PAKET_VERSIYON`). Prompt'taki "Core: 2.0 veya 2.1" ifadesiyle uyumlu, ancak normal paket için 2.0 kullanılmalı.

## Karar
- `eyp-core` Rehber 2.1 + 1.3'e göre sıfırdan yazılır (güncel bağımlılıklar, XXE/ZIP güvenliği bizim kontrolümüzde).
- Resmî Java API 2.1 (ve 1.3) **yalnızca test bağımlılığı** olarak kullanılır: ürettiğimiz her paket resmî API ile açılır, `PaketOzetiDogrula`/`NihaiOzetDogrula` çalıştırılır. Kurulum paketine dahil edilmez.
- Sabitler (ilişki tipleri, yollar, namespace'ler) resmî API `Araclar.java` ve Rehber'den alınır.

## Kaynaklı sabitler (özet)
- İlişki tipleri `http://eyazisma.dpt/iliskiler/{ustyazi,ek,imzasizEk,ustveri,nihaiustveri,parafozeti,parafimzacades,paketozeti,imzacades,nihaiozet,muhurcades,belgehedef,belgeimza}` (küçük harf; bazı açık kaynak projelerin `PaketOzeti` yazımı yanlış).
- İmza ilişkisinin kaynağı PaketOzeti part'ı; mühürünki NihaiOzet part'ı.
- Yollar: `/Imzalar/ImzaCades.imz`, `/Muhur/MuhurCades.imz` (2.x), `/Muhurler/MuhurCades.imz` (1.3).
- Özetler: ham part baytları üzerinden, Base64; 2.x'te her referansta iki DigestItem (SHA-256 + SHA-512).
- Core: category `RESMIYAZISMA`, contentType `application/eyazisma`, identifier = BelgeId, subject = Konu.
- Tüm Id'ler büyük harf (K.80).
