# cades-p4 (Faz 0 denemesi)

DSS + BouncyCastle ile PKCS#11 token üzerindeki anahtarla P4 (CAdES ES X-Long, EPES) imza denemesi. Yalnızca test malzemesi kullanır.

```bash
export SOFTHSM2_CONF=/path/to/softhsm2.conf   # token "eyp-test", PIN 1234
gradle -q run --args=setup   # test PKI + token üzerinde imzacı anahtarı
gradle -q run --args=sign    # work/PaketOzeti-XL.imz üretir ve doğrular
```

Sonuçlar: `docs/decisions/002-imza-kutuphanesi.md`.
