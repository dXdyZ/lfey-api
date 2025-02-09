from app.database import supabase
from pydantic import BaseModel
import logging

# Используем уже настроенный логгер
logger = logging.getLogger(__name__)

class PluginCreate(BaseModel):
    plugin_name: str
    plugin_description: str
    plugin_preview: str
    plugin_archive: str

class PluginService:
    @staticmethod
    def upload_plugin(plugin: dict):
        try:
            logger.debug("Начало вставки плагина")
            logger.debug(f"Данные для вставки: {plugin}")

            # Проверяем наличие обязательных полей
            required_fields = ["plugin_name", "plugin_description", "plugin_preview", "plugin_archive"]
            for field in required_fields:
                if field not in plugin:
                    logger.error(f"Отсутствует обязательное поле: {field}")
                    return {"error": f"Missing required field: {field}"}

            # Пытаемся выполнить запрос
            logger.debug("Выполнение запроса к Supabase...")
            response = supabase.table("plugins").insert(plugin).execute()
            logger.debug(f"Ответ Supabase: {response}")

            if not response.data:
                logger.error("Пустой ответ от Supabase")
                return {"error": "Empty response from database"}

            logger.info(f"Успешно создан плагин: {response.data[0]}")
            return response.data[0]

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
        response = supabase.table("plugins").select("plugin_archive").eq("id", plugin_id).execute()
        if response.data and response.data[0].get("plugin_archive"):
            return response.data[0]["plugin_archive"]
        return {"error": "Плагин не найден или отсутствует архив"}