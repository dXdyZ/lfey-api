from pydantic import BaseModel

class PluginBase(BaseModel):
    name: str
    description: str

class PluginCreate(PluginBase):
    preview_url: str
    archive_url: str