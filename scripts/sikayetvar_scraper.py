"""
Şikayetvar Bahis Numara Scraper
================================
Türkiye'deki tanınmış bahis sitelerine ait Şikayetvar sayfalarından
şikayet metinlerinde geçen telefon numaralarını çeker.

Kullanım:
    pip install requests beautifulsoup4
    python sikayetvar_scraper.py
    python sikayetvar_scraper.py --output fraud_numbers.json --pages 5

Çıktı:
    Mevcut fraud_numbers.json dosyasıyla birleştirilebilecek JSON formatı.
"""

import re
import json
import time
import argparse
import logging
from datetime import datetime, timezone
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

# ---------------------------------------------------------------------------
# Sabitler
# ---------------------------------------------------------------------------

BASE_URL = "https://www.sikayetvar.com"

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/124.0.0.0 Safari/537.36"
    ),
    "Accept-Language": "tr-TR,tr;q=0.9,en;q=0.8",
}

# Şikayetvar'daki bahis/kumar şirketi sayfa slug'ları
BETTING_COMPANIES = [
    "bets10",
    "betturkey",
    "betpark",
    "bahsegel",
    "tipobet",
    "betcio",
    "matbet",
    "sultanbet",
    "artemisbet",
    "betgit",
    "ngsbahis",
    "mariobet",
    "mobilbahis",
    "casinomaxi",
    "casinometropol",
    "superbetin",
    "piabet",
    "redwin",
    "jojobet",
    "betboo",
    "betist",
    "betzula",
    "betandyou",
    "rexbet",
    "1xbet",
    "mostbet",
    "betway",
    "22bet",
    "melbet",
]

# Türk telefon numarası desenleri (raw string, normalize edilecek)
PHONE_PATTERNS = [
    r"\+90[\s\-\.]?5\d{2}[\s\-\.]?\d{3}[\s\-\.]?\d{2}[\s\-\.]?\d{2}",  # +90 5XX ...
    r"\+90[\s\-\.]?5\d{9}",                                               # +905XXXXXXXXX
    r"0[\s\-\.]?5\d{2}[\s\-\.]?\d{3}[\s\-\.]?\d{2}[\s\-\.]?\d{2}",      # 05XX ...
    r"05\d{9}",                                                            # 05XXXXXXXXX
    r"\(0[\s]*5\d{2}\)[\s\-\.]?\d{3}[\s\-\.]?\d{2}[\s\-\.]?\d{2}",      # (05XX) ...
]

COMBINED_PATTERN = re.compile("|".join(PHONE_PATTERNS))

# ---------------------------------------------------------------------------
# Yardımcı fonksiyonlar
# ---------------------------------------------------------------------------


def normalize_phone(raw: str) -> str:
    """Ham telefon dizesini +905XXXXXXXXX formatına dönüştür."""
    digits = re.sub(r"[^\d]", "", raw)
    if digits.startswith("90") and len(digits) == 12:
        return "+" + digits
    if digits.startswith("0") and len(digits) == 11:
        return "+9" + digits
    if len(digits) == 10 and digits.startswith("5"):
        return "+90" + digits
    return None


def extract_phones(text: str) -> list[str]:
    matches = COMBINED_PATTERN.findall(text)
    normalized = []
    for m in matches:
        n = normalize_phone(m)
        if n:
            normalized.append(n)
    return normalized


def fetch_page(url: str, session: requests.Session) -> BeautifulSoup | None:
    try:
        resp = session.get(url, headers=HEADERS, timeout=15)
        if resp.status_code == 200:
            return BeautifulSoup(resp.text, "html.parser")
        if resp.status_code == 404:
            return None
        log.warning("HTTP %s — %s", resp.status_code, url)
        return None
    except requests.RequestException as exc:
        log.warning("İstek hatası: %s — %s", exc, url)
        return None


# ---------------------------------------------------------------------------
# Şikayetvar scraping
# ---------------------------------------------------------------------------


def scrape_company(slug: str, max_pages: int, session: requests.Session) -> set[str]:
    """Bir şirketin şikayet listesindeki tüm telefon numaralarını çek."""
    found: set[str] = set()

    for page in range(1, max_pages + 1):
        url = f"{BASE_URL}/{slug}/sikayet/" + (f"?page={page}" if page > 1 else "")
        log.info("  Sayfa %d/%d — %s", page, max_pages, url)

        soup = fetch_page(url, session)
        if soup is None:
            break

        # Şikayet başlık ve özet metinleri
        complaint_nodes = soup.select(
            "a.complaint-title, p.complaint-description, div.complaint-text, "
            "span.description, .complaint-content"
        )

        if not complaint_nodes:
            # Sayfa yapısı farklıysa tüm paragrafları tara
            complaint_nodes = soup.find_all("p")

        for node in complaint_nodes:
            phones = extract_phones(node.get_text())
            found.update(phones)

        # Her sayfadan önce kısa bekleme (sunucuyu yormamak için)
        time.sleep(1.5)

    return found


def scrape_search(query: str, max_pages: int, session: requests.Session) -> set[str]:
    """Arama sonuçlarından telefon numarası çek."""
    found: set[str] = set()

    for page in range(1, max_pages + 1):
        url = f"{BASE_URL}/arama?q={requests.utils.quote(query)}&page={page}"
        log.info("  Arama sayfa %d — %s", page, url)

        soup = fetch_page(url, session)
        if soup is None:
            break

        nodes = soup.select("p, span.description, .complaint-content")
        for node in nodes:
            phones = extract_phones(node.get_text())
            found.update(phones)

        time.sleep(1.5)

    return found


# ---------------------------------------------------------------------------
# Ana akış
# ---------------------------------------------------------------------------


def load_existing(path: Path) -> dict:
    if path.exists():
        try:
            return json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            log.warning("Mevcut dosya okunamadı, sıfırdan başlanıyor.")
    return {"version": 1, "updated_at": "", "numbers": []}


def build_output(
    existing: dict,
    new_numbers: set[str],
    source: str = "sikayetvar",
) -> dict:
    existing_set = {e["number"] for e in existing.get("numbers", [])}

    added = 0
    for num in sorted(new_numbers):
        if num not in existing_set:
            existing["numbers"].append(
                {
                    "number": num,
                    "risk_level": "HIGH",
                    "category": "betting",
                    "report_count": 1,
                    "note": f"sikayetvar-scraped",
                }
            )
            existing_set.add(num)
            added += 1

    existing["version"] = existing.get("version", 1) + 1
    existing["updated_at"] = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")

    log.info("Mevcut: %d numara, yeni eklenen: %d", len(existing_set) - added, added)
    return existing


def main():
    parser = argparse.ArgumentParser(description="Şikayetvar bahis numara scraper")
    parser.add_argument(
        "--output",
        default="../fraud_numbers.json",
        help="Çıktı JSON dosyası (varsayılan: ../fraud_numbers.json)",
    )
    parser.add_argument(
        "--pages",
        type=int,
        default=3,
        help="Her şirket için taranacak sayfa sayısı (varsayılan: 3)",
    )
    parser.add_argument(
        "--companies",
        nargs="*",
        help="Sadece belirtilen şirketleri tara (varsayılan: hepsi)",
    )
    parser.add_argument(
        "--search-only",
        action="store_true",
        help="Şirket sayfaları yerine arama sorgusu kullan",
    )
    args = parser.parse_args()

    output_path = Path(args.output)
    companies = args.companies or BETTING_COMPANIES

    session = requests.Session()
    all_phones: set[str] = set()

    if args.search_only:
        search_terms = ["bahis numarası", "bahis spam", "casino spam sms"]
        for term in search_terms:
            log.info("Arama: '%s'", term)
            phones = scrape_search(term, args.pages, session)
            log.info("  → %d numara bulundu", len(phones))
            all_phones.update(phones)
    else:
        for slug in companies:
            log.info("Şirket: %s", slug)
            phones = scrape_company(slug, args.pages, session)
            log.info("  → %d numara bulundu", len(phones))
            all_phones.update(phones)
            time.sleep(2)  # şirketler arası bekleme

    log.info("Toplam benzersiz numara: %d", len(all_phones))

    existing = load_existing(output_path)
    updated = build_output(existing, all_phones)

    output_path.write_text(
        json.dumps(updated, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    log.info("Kaydedildi: %s", output_path.resolve())


if __name__ == "__main__":
    main()
