from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field


Role = Literal["owner", "collector"]
Direction = Literal["inbox", "sent", "unknown"]


class DeviceIn(BaseModel):
    id: str = Field(min_length=1, max_length=128)
    name: str | None = Field(default=None, max_length=128)
    role: Role = "collector"


class SmsIn(BaseModel):
    sms_id: str = Field(min_length=1, max_length=160)
    sender: str | None = Field(default=None, max_length=160)
    body: str = Field(min_length=0, max_length=16000)
    received_at: datetime
    sim_slot: int | None = None
    direction: Direction = "inbox"


class SmsSyncRequest(BaseModel):
    device: DeviceIn
    messages: list[SmsIn] = Field(default_factory=list, max_length=500)


class SmsRow(BaseModel):
    id: int
    device_id: str
    sms_id: str
    sender: str | None
    body: str
    received_at: str
    received_at_ms: int
    sim_slot: int | None
    direction: Direction
    created_at: str


class SmsSyncResponse(BaseModel):
    inserted: int
    duplicates: int
    messages: list[SmsRow]


class SmsListResponse(BaseModel):
    count: int
    messages: list[SmsRow]
