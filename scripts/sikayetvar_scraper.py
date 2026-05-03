"""
Türk Spam Numara Toplayıcı
===========================
Birden fazla açık kaynaktan Türkçe spam/bahis numaralarını çeker ve
fraud_numbers.json dosyasını günceller.

Kaynaklar:
  1. symbuzzer/Turkish-Spam-Numbers (GitHub) — doğrulanmış Türk spam listesi
  2. Şikayetvar şikayet sayfaları (erişilebilirse)

Kullanım:
    pip install requests beautifulsoup4
    python sikayetvar_scraper.py
    python sikayetvar_scraper.py --output ../fraud_numbers.json
"""

import re
import csv
import json
import time
import argparse
import logging
from datetime import datetime, timezone
from io import StringIO
from pathlib import Path

try:
    import requests
    from bs4 import BeautifulSoup
except ImportError:
    raise SystemExit(
        "Gerekli paketler eksik. Lütfen çalıştırın:\n  pip install requests beautifulsoup4"
    )

logging.basicConfig(level=logging.INFO, format="%(levelname)s  %(message)s")
log = logging.getLogger(__name__)

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/124.0.0.0 Safari/537.36"
    ),
    "Accept-Language": "tr-TR,tr;q=0.9,en;q=0.8",
}

# ---------------------------------------------------------------------------
# Kaynak 1: symbuzzer/Turkish-Spam-Numbers (GitHub)
# ---------------------------------------------------------------------------

TURKISH_SPAM_NUMBERS_URL = (
    "https://raw.githubusercontent.com/symbuzzer/Turkish-Spam-Numbers/main/SpamBlocker.csv"
)

def fetch_turkish_spam_numbers(session: requests.Session) -> set[str]:
    """symbuzzer/Turkish-Spam-Numbers reposundan spam numaralarını çek."""
    log.info("Kaynak: symbuzzer/Turkish-Spam-Numbers")
    try:
        resp = session.get(TURKISH_SPAM_NUMBERS_URL, headers=HEADERS, timeout=15)
        resp.raise_for_status()
    except requests.RequestException as e:
        log.warning("Turkish-Spam-Numbers çekilemedi: %s", e)
        return set()

    numbers: set[str] = set()
    reader = csv.DictReader(StringIO(resp.text))
    for row in reader:
        num = row.get("number", "").strip()
        if num.startswith("+90") and len(num) == 13:
            numbers.add(num)

    log.info("  → %d numara bulundu", len(numbers))
    return numbers


# ---------------------------------------------------------------------------
# Kaynak 2: Şikayetvar (erişilebilirse)
# ---------------------------------------------------------------------------

PHONE_PATTERN = re.compile(
    r"(?:\+90|0)[\s\-\.]?5\d{2}[\s\-\.]?\d{3}[\s\-\.]?\d{2}[\s\-\.]?\d{2}"
)

SIKAYETVAR_SEARCH_URLS = [
    "https://www.sikayetvar.com/bahis/sms",
    "https://www.sikayetvar.com/bahis/telefon-numarasi",
]

def normalize_phone(raw: str) -> str | None:
    digits = re.sub(r"[^\d]", "", raw)
    if digits.startswith("90") and len(digits) == 12:
        return "+" + digits
    if digits.startswith("0") and len(digits) == 11:
        return "+9" + digits
    if len(digits) == 10 and digits.startswith("5"):
        return "+90" + digits
    return None

def fetch_sikayetvar(session: requests.Session) -> set[str]:
    """Şikayetvar statik sayfalarından telefon numarası çek."""
    log.info("Kaynak: Şikayetvar")
    found: set[str] = set()

    for url in SIKAYETVAR_SEARCH_URLS:
        try:
            resp = session.get(url, headers=HEADERS, timeout=15)
            if resp.status_code == 403:
                log.warning("  HTTP 403 — %s (atlandı)", url)
                continue
            resp.raise_for_status()
        except requests.RequestException as e:
            log.warning("  Hata: %s — %s", e, url)
            continue

        soup = BeautifulSoup(resp.text, "html.parser")
        for node in soup.find_all(["p", "span", "div"]):
            for m in PHONE_PATTERN.findall(node.get_text()):
                n = normalize_phone(m)
                if n:
                    found.add(n)

        time.sleep(2)

    log.info("  → %d numara bulundu", len(found))
    return found


# ---------------------------------------------------------------------------
# fraud_numbers.json güncelle
# ---------------------------------------------------------------------------

def load_existing(path: Path) -> dict:
    if path.exists():
        try:
            return json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            log.warning("Mevcut dosya okunamadı, sıfırdan başlanıyor.")
    return {"version": 1, "updated_at": "", "numbers": []}


def merge(existing: dict, new_numbers: set[str]) -> dict:
    existing_set = {e["number"] for e in existing.get("numbers", [])}
    added = 0
    for num in sorted(new_numbers):
        if num not in existing_set:
            existing["numbers"].append({
                "number": num,
                "risk_level": "HIGH",
                "category": "SPAM",
                "report_count": 1,
                "note": "community-reported",
            })
            existing_set.add(num)
            added += 1

    existing["version"] = existing.get("version", 1) + 1
    existing["updated_at"] = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    log.info("Mevcut: %d numara, yeni eklenen: %d", len(existing_set) - added, added)
    return existing


# ---------------------------------------------------------------------------
# Ana akış
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description="Türk spam numara toplayıcı")
    parser.add_argument(
        "--output", default="../fraud_numbers.json",
        help="Çıktı JSON dosyası (varsayılan: ../fraud_numbers.json)",
    )
    parser.add_argument(
        "--skip-sikayetvar", action="store_true",
        help="Şikayetvar kaynağını atla",
    )
    args = parser.parse_args()

    output_path = Path(args.output)
    session = requests.Session()
    all_numbers: set[str] = set()

    # Kaynak 1: Turkish-Spam-Numbers (her zaman çalışır)
    all_numbers.update(fetch_turkish_spam_numbers(session))

    # Kaynak 2: Şikayetvar (opsiyonel)
    if not args.skip_sikayetvar:
        all_numbers.update(fetch_sikayetvar(session))

    log.info("Toplam benzersiz numara: %d", len(all_numbers))

    existing = load_existing(output_path)
    updated = merge(existing, all_numbers)

    output_path.write_text(
        json.dumps(updated, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    log.info("Kaydedildi: %s", output_path.resolve())


if __name__ == "__main__":
    main()
