import aiofiles
from fastapi import APIRouter, HTTPException, UploadFile, Form, Depends
from app.services.plugin_service import PluginService
import logging
import yadisk
from fastapi.responses import FileResponse
from fastapi import File, Depends
import os
import uuid
from app.services.storage_service import StorageService

logger = logging.getLogger(__name__)
router = APIRouter()

YADISK_TOKEN = os.getenv("YADISK_TOKEN")
def get_yadisk():
    """
    Возвращает объект Яндекс.Диска.
    """
    y = yadisk.YaDisk(token=YADISK_TOKEN)
    if not y.check_token():
        raise HTTPException(status_code=500, detail="Невалидный токен Яндекс.Диска")
    return y

def get_storage_service():
    """
    Возвращает объект StorageService.
    """
    return StorageService()

@router.post("/upload_plugin")
async def upload_plugin(
    file: UploadFile = File(...),  # Файл плагина
    plugin_name: str = Form(...),  # Имя плагина
    plugin_description: str = Form(...),  # Описание плагина
    storage: StorageService = Depends(StorageService)  # Зависимость для StorageService
):
    """
    Загружает плагин на Яндекс.Диск и сохраняет информацию о плагине в базе данных.
    """
    try:
        # Генерируем уникальное имя файла
        file_path = file.filename  # Расширение файла
        print(file_path+"\n\n\n\n")
        print(type(file_path))
        # Загружаем файл на Яндекс.Диск
        remote_path = f"/{file_path}"
        upload_result = await storage.upload_file(file_path)
        if upload_result != "True":
            raise HTTPException(status_code=500, detail=f"Ошибка при загрузке файла: {upload_result}")


        # Сохраняем информацию о плагине в базе данных
        plugin_data = {
            "plugin_name": plugin_name,
            "plugin_description": plugin_description,
            "plugin_archive": remote_path  # Сохраняем путь к файлу на Яндекс.Диске
        }
        plugin = await PluginService.upload_plugin(plugin_data)
        if "error" in plugin:
            raise HTTPException(status_code=500, detail=plugin["error"])

        return {"status": "success", "message": "Плагин успешно загружен", "plugin": plugin}
    except Exception as e:
        logger.error(f"Ошибка: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Внутренняя ошибка сервера")


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
async def download_plugin(
    plugin_id: int,
    storage: StorageService = Depends(StorageService)  # Зависимость для StorageService
):
    """
    Скачивает плагин с Яндекс.Диска по его ID.
    """
    try:
        # Получаем информацию о плагине
        plugin_info = PluginService.get_plugin(plugin_id)
        if "error" in plugin_info:
            raise HTTPException(status_code=404, detail=plugin_info["error"])

        # Получаем путь к файлу на Яндекс.Диске
        remote_path = plugin_info.get("plugin_archive")

        print(f"""
        #####################
        {remote_path}
        #####################
        """)

        if not remote_path:
            raise HTTPException(status_code=404, detail="Путь к файлу не найден")

        # Создаем папку downloads, если она не существует
        downloads_dir = os.path.join(os.getcwd())
        #os.makedirs(downloads_dir, exist_ok=True)

        # Формируем локальный путь для сохранения файла
        local_path = os.path.join(downloads_dir, os.path.basename(remote_path))

        print(f"""
        ##########################
        Локальный путь: {local_path}
        ##########################
        """)

        # Скачиваем файл с Яндекс.Диска
        download_result = await storage.download_file(remote_path, local_path)
        if download_result != "True":
            raise HTTPException(status_code=500, detail=f"Ошибка при скачивании файла: {download_result}")

        # Возвращаем файл пользователю
        return FileResponse(
            path=local_path,
            filename=os.path.basename(remote_path),  # Имя файла для пользователя
            media_type="application/octet-stream"
        )
    except Exception as e:
        logger.error(f"Ошибка: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Внутренняя ошибка сервера")
