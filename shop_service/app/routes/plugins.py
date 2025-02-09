from fastapi import APIRouter, HTTPException
from app.services.plugin_service import PluginService, PluginCreate
import logging

# Используем уже настроенный логгер
logger = logging.getLogger(__name__)

router = APIRouter()

@router.post("/upload_plugin")
async def upload_plugin(plugin: PluginCreate):
    try:
        logger.debug("Получен запрос на создание плагина")

        # Преобразуем Pydantic модель в словарь
        plugin_data = plugin.model_dump()
        logger.debug(f"Преобразованные данные: {plugin_data}")

        # Удаляем поле 'id', если оно есть (должно генерироваться автоматически)
        if 'id' in plugin_data:
            logger.warning("Получен ID от клиента, игнорируем")
            del plugin_data['id']

        # Вызываем сервис
        result = PluginService.upload_plugin(plugin_data)

        if "error" in result:
            logger.error(f"Ошибка в сервисе: {result['error']}")
            raise HTTPException(status_code=400, detail=result["error"])

        return result

    except Exception as e:
        logger.error("Непредвиденная ошибка в роутере", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal server error")

@router.get("/get_plugins")
def get_plugins():
    return PluginService.get_plugins()

@router.get("/get_plugin/{plugin_id}")
def get_plugin(plugin_id: int):
    result = PluginService.get_plugin(plugin_id)
    if "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result

@router.get("/download_plugin/{plugin_id}")
def download_plugin(plugin_id: int):
    result = PluginService.download_plugin(plugin_id)
    if "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return {"download_url": result}