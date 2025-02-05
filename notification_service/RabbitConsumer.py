import asyncio

import aio_pika
import json
import logging

class RabbitMQConsumer:
    def __init__(self, rabbitmq_url, queue_name, max_concurrent_emails):
        self.rabbitmq_url = rabbitmq_url
        self.queue_name = queue_name
        self.max_concurrent_emails = max_concurrent_emails

    async def start(self, email_senders):
        """Подключение к RabbitMQ и начало обработки сообщений."""
        logging.info("Connecting to RabbitMQ...")
        connection = await aio_pika.connect(self.rabbitmq_url)
        channel = await connection.channel()
        await channel.set_qos(prefetch_count=self.max_concurrent_emails)

        queue = await channel.declare_queue(self.queue_name, durable=False)
        logging.info("Connected to RabbitMQ and waiting for messages...")

        async for message in queue:
            # Выбираем SMTP-воркер для обработки сообщения
            email_sender = email_senders.pop(0)
            asyncio.create_task(self.process_message(message, email_sender))
            email_senders.append(email_sender)  # Возвращаем воркер в пул

    async def process_message(self, message: aio_pika.IncomingMessage, email_sender):
        """Обработка сообщения из очереди."""
        try:
            data = json.loads(message.body.decode())
            await email_sender.send_email(data['to'], data['body'])
            logging.info(f"Email sent to {data['to']}")
            await message.ack()
        except Exception as e:
            logging.error(f"Error processing message: {e}")
            await message.nack(requeue=False)