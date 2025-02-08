from app.database import supabase
from pydantic import BaseModel

class PluginBase(BaseModel):
    name: str
    description: str

class PluginCreate(PluginBase):
    preview_url: str
    archive_url: str

class PluginService:
    @staticmethod
    def upload_plugin(plugin: dict):
        try:
            response = supabase.table("plugins").insert(plugin).execute()
            if response.data:
                return response.data[0]  # Возвращаем добавленный плагин
        except Exception as e:
            return {"error": str(e)}
        return {"error": "Ошибка при добавлении плагина"}

    @staticmethod
    def get_plugins():
        response = supabase.table("plugins").select("*").execute()
        return response.data or []

    @staticmethod
    def get_plugin(plugin_id: int):
        response = supabase.table("plugins").select("*").eq("id", plugin_id).execute()
        if response.data:
            return response.data[0]
        return {"error": "Плагин не найден"}

    @staticmethod
    def download_plugin(plugin_id: int):
        response = supabase.table("plugins").select("plugin_archive").eq("id", plugin_id).execute()
        if response.data and response.data[0].get("plugin_archive"):
            return response.data[0]["plugin_archive"]
        return {"error": "Плагин не найден или отсутствует архив"}