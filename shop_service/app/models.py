from pydantic import BaseModel

class PluginBase(BaseModel):
    name: str
    description: str

class PluginCreate(PluginBase):
    archive_url: str