import asyncio
import logging
from RabbitConsumer import RabbitMQConsumer
from EmailSender import EmailSender


async def main():



    # Создаем несколько SMTP-воркеров
    email_senders = [
        EmailSender(
            smtp_host="smtp.yandex.com",
            smtp_port=465,
            smtp_user="AnotherSc@yandex.ru",
            smtp_password="cufzeqevxzjpwozi"
        )

        for _ in range(10)
    ]

    await asyncio.gather(*(sender.connect() for sender in email_senders))

    # Запускаем keep_alive для каждого SMTP-воркера
    for sender in email_senders:
        asyncio.create_task(sender.keep_alive())  # <-- Запускаем keep_alive в фоне
    # Инициализация RabbitMQConsumer
    rabbitmq_consumer = RabbitMQConsumer(
        rabbitmq_url="amqp://root:werpipl15@bore.pub:5672/",
        queue_name="email_verification",
        max_concurrent_emails=10
    )

    # Запуск RabbitMQConsumer с пулом SMTP-воркеров
    await rabbitmq_consumer.start(email_senders)

    # Закрытие соединений при завершении
    for sender in email_senders:
        await sender.close()

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    asyncio.run(main())