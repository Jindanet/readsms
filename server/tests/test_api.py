import os
import tempfile
import unittest

from fastapi.testclient import TestClient


TEMP_DIR = tempfile.TemporaryDirectory()
DB_PATH = os.path.join(TEMP_DIR.name, "test.json")
os.environ["READSMS_API_TOKEN"] = "test-token"
os.environ["READSMS_DB_PATH"] = DB_PATH
os.environ["READSMS_RETENTION_DAYS"] = "1"

from app.main import app  # noqa: E402


class ApiTest(unittest.TestCase):
    def setUp(self):
        if os.path.exists(DB_PATH):
            os.remove(DB_PATH)
        self.client = TestClient(app)
        self.headers = {"Authorization": "Bearer test-token"}

    def test_rejects_missing_token(self):
        response = self.client.get("/api/sms/recent")
        self.assertEqual(response.status_code, 401)

    def test_validate_token(self):
        response = self.client.get("/api/validate", headers=self.headers)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["ok"], True)

    def test_sync_and_query_sms(self):
        payload = {
            "device": {"id": "phone_b", "name": "Phone B", "role": "collector"},
            "messages": [
                {
                    "sms_id": "android-1",
                    "sender": "KBank",
                    "body": "OTP 123456",
                    "received_at": "2026-05-14T10:20:00+07:00",
                    "sim_slot": 1,
                    "direction": "inbox",
                }
            ],
        }

        first = self.client.post("/api/sms/sync", json=payload, headers=self.headers)
        self.assertEqual(first.status_code, 200)
        self.assertEqual(first.json()["inserted"], 1)
        self.assertEqual(first.json()["duplicates"], 0)

        duplicate = self.client.post("/api/sms/sync", json=payload, headers=self.headers)
        self.assertEqual(duplicate.status_code, 200)
        self.assertEqual(duplicate.json()["inserted"], 0)
        self.assertEqual(duplicate.json()["duplicates"], 1)

        recent = self.client.get("/api/sms/recent", headers=self.headers)
        self.assertEqual(recent.status_code, 200)
        data = recent.json()
        self.assertEqual(data["count"], 1)
        self.assertEqual(data["messages"][0]["device_id"], "phone_b")
        self.assertEqual(data["messages"][0]["body"], "OTP 123456")

    def test_retention_purges_messages_older_than_one_day(self):
        payload = {
            "device": {"id": "phone_c", "name": "Phone C", "role": "collector"},
            "messages": [
                {
                    "sms_id": "old-message",
                    "sender": "OLD",
                    "body": "expired",
                    "received_at": "2000-01-01T00:00:00Z",
                    "direction": "inbox",
                }
            ],
        }

        sync = self.client.post("/api/sms/sync", json=payload, headers=self.headers)
        self.assertEqual(sync.status_code, 200)
        self.assertEqual(sync.json()["inserted"], 1)

        recent = self.client.get("/api/sms/recent", headers=self.headers)
        self.assertEqual(recent.status_code, 200)
        self.assertEqual(recent.json()["count"], 0)


if __name__ == "__main__":
    unittest.main()
