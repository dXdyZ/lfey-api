import os
import aiofiles
import yadisk
from dotenv import load_dotenv

load_dotenv()

YADISK_TOKEN = os.getenv("YADISK_TOKEN")

client = yadisk.YaDisk(token=YADISK_TOKEN)


# You can either use the with statement or manually call client.close() later
with client:
    # Permanently remove "/file-to-remove"
    client.remove("/file.zip", permanently=True)



# print(os.getcwd())