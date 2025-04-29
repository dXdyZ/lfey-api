import os
import yadisk
from dotenv import load_dotenv
import asyncio
import logging

# Загружаем переменные окружения
load_dotenv()

# Логирование
logger = logging.getLogger(__name__)

class StorageService:
    def __init__(self):
        """
        Инициализация Яндекс.Диска.
        """
        self.y = yadisk.YaDisk(token=os.getenv("YADISK_TOKEN"))
        if not self.y.check_token():
            raise RuntimeError("Невалидный токен Яндекс.Диска")

    async def upload_file(self, local_path: str) -> str:
        """
        Асинхронно загружает файл на Яндекс.Диск.
        :param local_path: Локальный путь к файлу.
        :return: "True" в случае успеха, иначе возвращает ошибку.
        """
        try:
            # Извлекаем имя файла из локального пути
            file_name = os.path.basename(local_path)

            # Формируем путь на Яндекс.Диске
            remote_path = f"/{file_name}"

            # Загружаем файл на Яндекс.Диск асинхронно
            await asyncio.to_thread(self.y.upload, local_path, remote_path)
            return "True"
        except yadisk.exceptions.YaDiskError as e:
            logger.error(f"Ошибка при загрузке файла: {e}")
            return str(e)
        except Exception as e:
            logger.error(f"Ошибка: {e}", exc_info=True)
            return str(e)

    async def download_file(self, remote_path: str, local_path: str) -> str:
        """
        Асинхронно скачивает файл с Яндекс.Диска.
        :param remote_path: Путь к файлу на Яндекс.Диске.
        :param local_path: Локальный путь для сохранения файла.
        :return: "True" в случае успеха, иначе возвращает ошибку.
        """
        try:
            # Преобразуем local_path в абсолютный путь
            local_path = os.path.abspath(local_path)

            # Скачиваем файл с Яндекс.Диска асинхронно
            await asyncio.to_thread(self.y.download, remote_path, local_path)
            return "True"
        except yadisk.exceptions.YaDiskError as e:
            logger.error(f"Ошибка при скачивании файла: {e}")
            return str(e)
        except Exception as e:
            logger.error(f"Ошибка: {e}", exc_info=True)
            return str(e)

    def file_exists(self, remote_path: str) -> bool:
        """
        Проверяет, существует ли файл на Яндекс.Диске.
        :param remote_path: Путь к файлу на Яндекс.Диске.
        :return: True, если файл существует, иначе False.
        """
        try:
            return self.y.exists(remote_path)
        except yadisk.exceptions.YaDiskError as e:
            logger.error(f"Ошибка при проверке файла: {e}")
            return False