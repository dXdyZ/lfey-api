from fastapi import FastAPI
from app.routes import plugins

app = FastAPI()

app.include_router(plugins.router, prefix="/api")

# Тестовый эндпойнт
@app.get("/")
def root():
    return {"message": "Shop Service API is running"}
