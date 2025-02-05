import asyncio
import aio_pika
from aiosmtplib import SMTP
import jinja2
import json
import logging

# Конфигурация
RABBITMQ_URL = "amqp://root:werpipl15@bore.pub:5672/"
EMAIL_QUEUE = "email_verification"
SMTP_HOST = "smtp.yandex.com"
SMTP_PORT = 465  # Используем порт 465 для SSL
SMTP_USER = "AnotherSc@yandex.ru"
SMTP_PASSWORD = "cufzeqevxzjpwozi"
MAX_CONCURRENT_EMAILS = 10

# Инициализация шаблонизатора
env = jinja2.Environment(loader=jinja2.FileSystemLoader("templates/"), enable_async=True)
template = env.get_template("email_template.html")


async def send_email(smtp_client, to_email, code):
 message = await template.render_async(email=to_email, code=code)

 email_message = f"""Content-Type: text/html; charset="utf-8"
MIME-Version: 1.0
Subject: Your Verification Code
From: {SMTP_USER}
To: {to_email}

{message}"""

 # Исправленный вызов sendmail
 await smtp_client.sendmail(SMTP_USER, [to_email], email_message.encode("utf-8"))


async def process_message(message: aio_pika.IncomingMessage, smtp_client):
    try:
        data = json.loads(message.body.decode())
        await send_email(smtp_client, data['to'], data['body'])
        logging.info(f"Email sent to {data['to']}")
        await message.ack()
    except Exception as e:
        logging.error(f"Error processing message: {e}")
        await message.nack(requeue=False)


async def worker():
    smtp_client = None
    connection = None
    try:
        # Настройки для Yandex SMTP с SSL
        smtp_client = SMTP(
            hostname=SMTP_HOST,
            port=SMTP_PORT,
            use_tls=True,  # Прямое SSL соединение
            start_tls=False,  # Не использовать STARTTLS
            username=SMTP_USER,
            password=SMTP_PASSWORD,
            timeout=30,
            validate_certs=False  # Отключить проверку сертификатов для теста
        )

        await smtp_client.connect()
        logging.info("Successfully connected to SMTP server")

        # Подключение к RabbitMQ
        connection = await aio_pika.connect(RABBITMQ_URL)
        channel = await connection.channel()
        await channel.set_qos(prefetch_count=MAX_CONCURRENT_EMAILS)

        queue = await channel.declare_queue(EMAIL_QUEUE, durable=False)

        async for message in queue:
            asyncio.create_task(process_message(message, smtp_client))

    except Exception as e:
        logging.error(f"Connection error: {e}")
    finally:
        if smtp_client and smtp_client.is_connected:
            await smtp_client.quit()
        if connection:
            await connection.close()


async def main():
    workers = [worker() for _ in range(MAX_CONCURRENT_EMAILS)]
    await asyncio.gather(*workers)


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    asyncio.run(main())