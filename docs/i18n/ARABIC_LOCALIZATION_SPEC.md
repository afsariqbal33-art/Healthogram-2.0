# Arabic Localization & Right-to-Left (RTL) Layout Specification

## 1. Native Bilingual Architecture
Healthogram is built from the ground up for full bilingual operation in **Arabic (العربية)** and **English**. The platform treats Arabic as a first-class language rather than a post-development translation layer.

---

## 2. Right-to-Left (RTL) Layout Principles in Compose

1. **Directional Insets & Modifiers**:
   - Use `Modifier.padding(start = ..., end = ...)` instead of `left` / `right`.
   - Directional navigation icons utilize `Icons.AutoMirrored.Filled.ArrowBack` and `Icons.AutoMirrored.Filled.ArrowForward`.
2. **Typography & Font Scaling**:
   - Arabic text rendering utilizes Noto Sans Arabic typography with proportional line-height leading (+20%) to avoid clipping Arabic diacritics and ligatures.
3. **Number & Currency Formatting**:
   - Numerals are formatted using standard Arabic currency glyphs (e.g. `ر.ع.` for OMR, `ر.س.` for SAR).
   - Clinical values (e.g. `5.4 mmol/L`, `120/80 mmHg`) preserve international standard unit notation with RTL text alignment.
4. **Dates & Timestamps**:
   - Bilingual Gregorian calendars with standard localized month representations (`سبتمبر 2026`).
