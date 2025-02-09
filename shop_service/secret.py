import secrets

# Генерация секретного ключа длиной 32 символа
secret_key = secrets.token_hex(64)

print(secret_key)

SECRET_KEY = "32a8970858ce663ac2436dbdc7379f910733354be586b7a9751d465f411e3037edd76e3041e652aadbd675f473ad730275a7305f90f1dadd2e51848575af8efd"


a = {
    "id":"2",
    "plugin_name":"sperma",
    "plugin_description":"spermoed",
    "plugin_preview":"photo.png",
    "plugin_archive":"archive.zip"
}