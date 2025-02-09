from fastapi import FastAPI, Request
from app.routes import plugins
import logging

# Настройка логгера
logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[
        logging.FileHandler("app.log"),  # Логи в файл
        logging.StreamHandler()  # Логи в консоль
    ]
)
logger = logging.getLogger(__name__)

app = FastAPI()

# Middleware для логирования запросов
@app.middleware("http")
async def log_requests(request: Request, call_next):
    logger.debug(f"Request: {request.method} {request.url}")
    response = await call_next(request)
    logger.debug(f"Response status code: {response.status_code}")
    return response

# Подключение роутера
app.include_router(plugins.router, prefix="/api")

# Тестовый эндпоинт
@app.get("/")
def root():
    return {"message": "Shop Service API is running"}