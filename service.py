import json
import random
import re
import string
from dataclasses import dataclass
from datetime import date, timedelta
from pathlib import Path


@dataclass(frozen=True)
class NameRecord:
    first_name: str
    gender: str


class FakeDanishPersonService:
    def __init__(self, names_path: str, addresses_path: str, rng: random.Random | None = None) -> None:
        self._rng = rng or random.Random()
        self._names = self._load_names(names_path)
        self._last_names = self._load_last_names(names_path)
        self._postals = self._load_postal_data(addresses_path)
        self._phone_prefixes = self._build_phone_prefixes()

    @staticmethod
    def _load_names(names_path: str) -> list[NameRecord]:
        data = json.loads(Path(names_path).read_text(encoding="utf-8"))
        return [NameRecord(first_name=item["name"], gender=item["gender"]) for item in data["first_names"]]

    @staticmethod
    def _load_last_names(names_path: str) -> list[str]:
        data = json.loads(Path(names_path).read_text(encoding="utf-8"))
        return data["last_names"]

    @staticmethod
    def _load_postal_data(addresses_path: str) -> list[tuple[str, str]]:
        sql_text = Path(addresses_path).read_text(encoding="utf-8")
        matches = re.findall(r"\((\d+),'([^']+)'\)", sql_text)
        if not matches:
            raise ValueError("No postal codes found in addresses.sql")
        return [(postal, town) for postal, town in matches]

    @staticmethod
    def _build_phone_prefixes() -> list[str]:
        prefixes = [
            "2", "30", "31", "40", "41", "42", "50", "51", "52", "53", "60", "61", "71", "81", "91", "92", "93",
            "342", "359", "362", "389", "398", "431", "441", "462", "466", "468", "472", "474", "476", "478",
            "545", "556", "577", "579", "584", "627", "629", "641", "649", "658", "667", "697", "829",
        ]
        ranged = [
            (344, 349), (356, 357), (365, 366), (485, 486), (488, 489), (493, 496), (498, 499),
            (542, 543), (551, 552), (571, 574), (586, 587), (597, 598), (662, 665), (692, 694),
            (771, 772), (782, 783), (785, 786), (788, 789), (826, 827),
        ]
        for start, end in ranged:
            prefixes.extend(str(prefix) for prefix in range(start, end + 1))
        return prefixes

    def fake_name_gender(self) -> dict:
        first = self._rng.choice(self._names)
        return {
            "first_name": first.first_name,
            "last_name": self._rng.choice(self._last_names),
            "gender": first.gender,
        }

    def _random_birthdate(self) -> date:
        start = date(1940, 1, 1)
        end = date(2025, 12, 31)
        day_offset = self._rng.randint(0, (end - start).days)
        return start + timedelta(days=day_offset)

    def _cpr_for(self, gender: str, dob: date) -> str:
        ddmmyy = dob.strftime("%d%m%y")
        middle = f"{self._rng.randint(0, 999):03d}"
        parity = self._rng.choice([0, 2, 4, 6, 8]) if gender == "female" else self._rng.choice([1, 3, 5, 7, 9])
        return f"{ddmmyy}{middle}{parity}"

    def fake_cpr(self) -> dict:
        gender = self._rng.choice(["female", "male"])
        dob = self._random_birthdate()
        return {"cpr": self._cpr_for(gender, dob)}

    def fake_name_gender_dob(self) -> dict:
        person = self.fake_name_gender()
        dob = self._random_birthdate()
        person["date_of_birth"] = dob.isoformat()
        return person

    def fake_cpr_name_gender(self) -> dict:
        person = self.fake_name_gender()
        dob = self._random_birthdate()
        person["cpr"] = self._cpr_for(person["gender"], dob)
        return person

    def fake_cpr_name_gender_dob(self) -> dict:
        person = self.fake_name_gender()
        dob = self._random_birthdate()
        person["date_of_birth"] = dob.isoformat()
        person["cpr"] = self._cpr_for(person["gender"], dob)
        return person

    def fake_address(self) -> dict:
        postal_code, town = self._rng.choice(self._postals)
        street_len = self._rng.randint(5, 12)
        street = ''.join(self._rng.choice(string.ascii_lowercase) for _ in range(street_len)).capitalize()

        number = str(self._rng.randint(1, 999))
        if self._rng.random() < 0.3:
            number += self._rng.choice(string.ascii_uppercase)

        floor = "st" if self._rng.random() < 0.2 else str(self._rng.randint(1, 99))

        door_modes = ["named", "numeric", "letter"]
        mode = self._rng.choice(door_modes)
        if mode == "named":
            door = self._rng.choice(["th", "mf", "tv"])
        elif mode == "numeric":
            door = str(self._rng.randint(1, 50))
        else:
            letter = self._rng.choice(string.ascii_lowercase)
            sep = "-" if self._rng.random() < 0.5 else ""
            digits = str(self._rng.randint(1, 999))
            door = f"{letter}{sep}{digits}"

        return {
            "street": street,
            "number": number,
            "floor": floor,
            "door": door,
            "postal_code": postal_code,
            "town": town,
        }

    def fake_mobile_phone_number(self) -> dict:
        prefix = self._rng.choice(self._phone_prefixes)
        remaining = 8 - len(prefix)
        return {"mobile_phone_number": f"{prefix}{''.join(str(self._rng.randint(0, 9)) for _ in range(remaining))}"}

    def fake_person(self) -> dict:
        person = self.fake_cpr_name_gender_dob()
        person["address"] = self.fake_address()
        person.update(self.fake_mobile_phone_number())
        return person

    def fake_person_bulk(self, count: int) -> list[dict]:
        if count < 2 or count > 100:
            raise ValueError("count must be between 2 and 100")
        return [self.fake_person() for _ in range(count)]
