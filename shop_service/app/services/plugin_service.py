from app.database import supabase
from pydantic import BaseModel
import logging
import aiofiles
from app.services.storage_service import StorageService

logger = logging.getLogger(__name__)
storage = StorageService()

class PluginCreate(BaseModel):
    plugin_name: str
    plugin_description: str
    plugin_archive: str


class PluginService:
    @staticmethod
    async def upload_file_to_storage(file_path):
        response = storage.upload_file(file_path)
        if response == "True":
            return "True"
        else:
            return response

    @staticmethod
    async def upload_plugin(plugin: dict):
        """Сохраняет информацию о плагине в базе данных"""
        try:
            logger.debug("Начало вставки плагина")
            logger.debug(f"Данные для вставки: {plugin}")

            # Выполняем вставку данных
            response = supabase.table("plugins").insert(plugin).execute()

            # Проверяем, есть ли ошибки в ответе
            if hasattr(response, "data") and response.data:
                return response.data[0]  # Возвращаем данные
            else:
                logger.error(f"Ошибка Supabase: {response}")
                return {"error": "Не удалось вставить данные в базу"}

        except Exception as e:
            logger.error("Ошибка при вставке плагина", exc_info=True)
            return {"error": str(e)}

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
        response = storage.download_file(plugin_id)