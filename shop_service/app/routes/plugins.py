from fastapi import APIRouter, HTTPException, Request, Depends
from app.services.plugin_service import PluginService
import logging

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)
from pydantic import BaseModel
router = APIRouter()


class PluginCreate(BaseModel):
    name: str
    description: str
    preview_url: str
    archive_url: str

@router.post("/upload_plugin")
def upload_plugin(plugin: PluginCreate):
    logger.debug(f"Received plugin data: {plugin}")
    result = PluginService.upload_plugin(plugin.model_dump())
    if "error" in result:
        logger.error(f"Error uploading plugin: {result['error']}")
        raise HTTPException(status_code=400, detail=result["error"])
    return result


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