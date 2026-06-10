from __future__ import annotations

import csv
import random
from datetime import datetime, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "data"

SITES = {
    "news.example.com": ["/index", "/sports", "/finance", "/tech", "/world", "/video"],
    "shop.example.com": ["/index", "/search", "/detail", "/cart", "/order", "/pay"],
    "video.example.com": ["/index", "/movie", "/live", "/series", "/shorts", "/vip"],
    "study.example.com": ["/index", "/course", "/exam", "/resource", "/profile", "/forum"],
    "travel.example.com": ["/index", "/hotel", "/flight", "/guide", "/ticket", "/order"],
    "game.example.com": ["/index", "/download", "/news", "/rank", "/store", "/community"],
}

REGIONS = [
    "Beijing",
    "Shanghai",
    "Guangdong",
    "Zhejiang",
    "Jiangsu",
    "Sichuan",
    "Hubei",
    "Fujian",
    "Shandong",
    "Henan",
    "Chongqing",
    "Tianjin",
]

SITE_WEIGHTS = {
    "news.example.com": 22,
    "shop.example.com": 24,
    "video.example.com": 20,
    "study.example.com": 14,
    "travel.example.com": 10,
    "game.example.com": 10,
}

IP_POOLS = {
    region_index: [
        f"10.{10 + region_index}.{host // 255}.{host % 255 + 1}"
        for host in range(1, 81)
    ]
    for region_index in range(len(REGIONS))
}

HOUR_WEIGHTS = {
    0: 2,
    1: 1,
    2: 1,
    3: 1,
    4: 1,
    5: 1,
    6: 3,
    7: 6,
    8: 12,
    9: 16,
    10: 18,
    11: 15,
    12: 20,
    13: 16,
    14: 14,
    15: 15,
    16: 18,
    17: 20,
    18: 24,
    19: 28,
    20: 32,
    21: 30,
    22: 18,
    23: 8,
}


def weighted_choice(weights: dict):
    keys = list(weights)
    values = [weights[key] for key in keys]
    return random.choices(keys, weights=values, k=1)[0]


def random_ip(region_index: int) -> str:
    return random.choice(IP_POOLS[region_index])


def make_rows(count: int, start_date: datetime, days: int) -> list[list[str]]:
    rows = []
    for _ in range(count):
        site = weighted_choice(SITE_WEIGHTS)
        url = random.choice(SITES[site])
        region_index = random.randrange(len(REGIONS))
        region = REGIONS[region_index]
        day = start_date + timedelta(days=random.randrange(days))
        hour = weighted_choice(HOUR_WEIGHTS)
        minute = random.randrange(60)
        second = random.randrange(60)
        status = random.choices([200, 200, 200, 200, 304, 404, 500], weights=[50, 20, 12, 8, 5, 3, 2], k=1)[0]
        bytes_count = random.randint(800, 24000)
        rows.append(
            [
                day.strftime("%Y-%m-%d"),
                f"{hour:02d}:{minute:02d}:{second:02d}",
                site,
                url,
                random_ip(region_index),
                region,
                str(status),
                str(bytes_count),
            ]
        )
    rows.sort(key=lambda item: (item[0], item[1], item[2], item[3]))
    return rows


def write_csv(name: str, rows: int, days: int, seed: int) -> None:
    random.seed(seed)
    DATA_DIR.mkdir(exist_ok=True)
    path = DATA_DIR / name
    with path.open("w", newline="", encoding="utf-8") as file:
        writer = csv.writer(file)
        writer.writerow(["date", "time", "site", "url", "ip", "region", "status", "bytes"])
        writer.writerows(make_rows(rows, datetime(2026, 5, 1), days))
    print(f"{path} rows={rows}")


def main() -> None:
    write_csv("sample_access_log_medium.csv", rows=1000, days=10, seed=20260609)
    write_csv("sample_access_log_large.csv", rows=10000, days=30, seed=20260610)
    write_csv("sample_access_log_holiday.csv", rows=3000, days=7, seed=20260611)


if __name__ == "__main__":
    main()
