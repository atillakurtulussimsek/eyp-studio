Copied from the official e-Yazışma API 2.1 archive (`Cbddo.eYazisma/Xsd`, see docs/references/xsd-eyp-2.1).

Local patches (see docs/xsd-differences.md):
1. `^`/`$` anchors removed from `xsd:pattern` values (XSD regexes are implicitly anchored; the
   anchors are literal characters there and make every valid GUID/TCKN/KKK fail).
2. `Reference/` folder renamed to `reference/` to match the `schemaLocation` in EY-Tipler-2.xsd
   (the original only works on case-insensitive file systems).
3. UTF-8 BOM removed.
4. `CT_Guncelleme`: `<xsd:any/>` (##any) next to `GuvenlikKoduDegisiklikBilgisi` violates Unique Particle
   Attribution, so Xerces refuses to compile EY-Tipler-2.xsd at all. Changed to `namespace="##other" processContents="lax"`.
5. `Id` attributes in EY-PaketOzeti-2 / EY-NihaiOzet-2 / EY-ParafOzeti-2 are `xsd:ID` (NCName). K.34/K.44/K.96 put the
   BelgeId GUID there; a GUID starting with a digit is not an NCName. Changed to `xsd:string`.
