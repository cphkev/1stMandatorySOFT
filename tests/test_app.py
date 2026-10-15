import unittest
from datetime import datetime

from app import create_app, service


class AppTests(unittest.TestCase):
    def setUp(self):
        self.client = create_app().test_client()

    def test_required_endpoints_exist(self):
        endpoints = [
            "/api/cpr",
            "/api/name-gender",
            "/api/name-gender-dob",
            "/api/cpr-name-gender",
            "/api/cpr-name-gender-dob",
            "/api/address",
            "/api/mobile",
            "/api/person",
            "/api/persons?count=2",
        ]
        for endpoint in endpoints:
            with self.subTest(endpoint=endpoint):
                response = self.client.get(endpoint)
                self.assertEqual(response.status_code, 200)

    def test_bulk_bounds(self):
        self.assertEqual(self.client.get("/api/persons?count=1").status_code, 400)
        self.assertEqual(self.client.get("/api/persons?count=101").status_code, 400)

    def test_person_consistency(self):
        for _ in range(20):
            person = self.client.get("/api/person").get_json()
            self.assertEqual(len(person["cpr"]), 10)
            self.assertTrue(person["cpr"].isdigit())
            dob = datetime.strptime(person["date_of_birth"], "%Y-%m-%d")
            self.assertEqual(person["cpr"][:6], dob.strftime("%d%m%y"))
            if person["gender"] == "female":
                self.assertEqual(int(person["cpr"][-1]) % 2, 0)
            else:
                self.assertEqual(int(person["cpr"][-1]) % 2, 1)

    def test_phone_prefix(self):
        allowed = set(service._build_phone_prefixes())
        for _ in range(50):
            mobile = self.client.get("/api/mobile").get_json()["mobile_phone_number"]
            self.assertEqual(len(mobile), 8)
            self.assertTrue(any(mobile.startswith(prefix) for prefix in allowed))


if __name__ == "__main__":
    unittest.main()
