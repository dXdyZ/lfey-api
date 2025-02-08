from pydantic import BaseModel
from supabase import create_client, Client
from dotenv import load_dotenv
import os
import json

load_dotenv()

class DbRequests:
    def __init__(self):
        self.url: str = os.environ.get("SUPABASE_URL")
        self.key: str = os.environ.get("SUPABASE_KEY")
        self.supabase: Client = create_client(self.url, self.key)

    def new_plugin(self, plugin):
        try:
            response = self.supabase.table("plugins").insert(plugin).execute()
            return response.data
        except Exception as exception:
            return {"error": str(exception)}

    def get_plugins(self):
        try:
            response = self.supabase.table("plugins").select("id, plugin_name, plugin_description, plugin_preview, plugin_archive").execute()
            return response.data
        except Exception as exception:
            return {"error": str(exception)}

    def get_plugin_by(self, field, value):
        try:
            response = self.supabase.table("plugins").select("*").eq(field, value).execute()
            return response.data
        except Exception as exception:
            return {"error": str(exception)}

    def delete_plugin_by(self, field, value):
        try:
            response = self.supabase.table('plugins').delete().eq(field, value).execute()
            return response.data
        except Exception as exception:
            return {"error": str(exception)}

