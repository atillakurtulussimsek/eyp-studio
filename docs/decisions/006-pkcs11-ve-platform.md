# 006 – PKCS#11 kütüphaneleri ve Java sürümü

Durum: **Kısmen karar verildi – gerçek kurulumla teyit gerekli**
Tarih: 2026-11-23

## Bilinen yollar (açık kaynak projelerden; vendor belgesi bulunamadı)
| Kart | Windows | macOS | Linux |
|---|---|---|---|
| AKİS | `C:\Windows\System32\akisp11.dll` | `/usr/local/lib/libakisp11.dylib` | `/usr/lib/libakisp11.so`, `/usr/local/lib/libakisp11.so` |
| SafeSign | `C:\Windows\System32\aetpkss1.dll` | `/usr/local/lib/libaetpkss.dylib` | `/usr/lib/libaetpkss.so(.3)` |
| Gemalto Classic Client | `…\Gemalto\Classic Client\BIN\gclib.dll` | ? | ? |
| IDPrime | `…\IDGo 800 PKCS#11\IDPrimePKCS1164.dll` | `/usr/local/lib/libidprimepkcs11.0.dylib` | `/usr/lib/libIDPrimePKCS11.so` |
| SafeNet eToken | `C:\Windows\System32\eTPKCS11.dll` | `/usr/local/lib/libeTPkcs11.dylib` | `/usr/lib/libeTPkcs11.so` |

## Prompt ile çelişki
- **AKiA, E-Tuğra'nın değil TÜBİTAK'ın AKİS ara katman yazılımının adı** (akiskart.bilgem.tubitak.gov.tr destek sayfası: `Akia_macos_arm_6_8_13.pkg` vb.). `akia_pkcs11.dll` iddiası doğrulanamadı. Modül dosya adları gerçek kurulumla teyit edilecek.

## Java
- JDK-8328785 (21.0.5'te düzeltildi): PKCS#11 v2.x modülleri yüklenemiyor.
- JDK-8335288 (21.0.7'de düzeltildi): bazı modüllerde SunPKCS11 başlatılamıyor.
- Karar: **JDK ≥ 21.0.7** zorunlu; jlink ile gömülür.
- Türkçe yerel ayar: JDK-6220064 vb. tarihsel hatalar var. Kural: `Locale.ROOT`; CI tr_TR ile de koşar.

## Test token
- SoftHSM2: macOS `brew install softhsm` (`/opt/homebrew/lib/softhsm/libsofthsm2.so`), Ubuntu `apt install softhsm2`. Windows'ta yalnız bakımsız üçüncü parti derleme var → Windows CI'da PKCS#11 testleri atlanabilir veya kendi derlememiz kullanılır.
- Token'ı slot ID ile değil etiketle bul.

## Açık sorular
1. Kullanıcının kartı hangisi (AKİS mi, başka mı), hangi ESHS'den alındı?
