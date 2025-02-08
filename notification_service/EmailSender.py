import asyncio

from aiosmtplib import SMTP
import jinja2
import logging

class EmailSender:
    def __init__(self, smtp_host, smtp_port, smtp_user, smtp_password):
        self.smtp_host = smtp_host
        self.smtp_port = smtp_port
        self.smtp_user = smtp_user
        self.smtp_password = smtp_password
        self.template_env = jinja2.Environment(loader=jinja2.FileSystemLoader("templates/"), enable_async=True)
        self.template = self.template_env.get_template("email_template.html")
        self.smtp_client = None

    async def connect(self):
        """Подключение к SMTP серверу с повторными попытками."""
        max_retries = 5
        for attempt in range(max_retries):
            try:
                self.smtp_client = SMTP(
                    hostname=self.smtp_host,
                    port=self.smtp_port,
                    use_tls=True,
                    start_tls=False,
                    username=self.smtp_user,
                    password=self.smtp_password,
                    timeout=30,
                    validate_certs=False
                )
                await self.smtp_client.connect()
                logging.info("Successfully connected to SMTP server")
                return
            except Exception as e:
                logging.error(f"SMTP connection failed: {e}, attempt {attempt + 1}/{max_retries}")
                await asyncio.sleep(2 ** attempt)  # Экспоненциальная задержка

        logging.error("Failed to connect to SMTP server after multiple attempts.")

    async def send_email(self, to_email, code):
        """Отправка электронного письма с проверкой соединения."""
        if not self.smtp_client or not self.smtp_client.is_connected:
            logging.warning("SMTP client disconnected, reconnecting...")
            await self.connect()

        message = await self.template.render_async(email=to_email, code=code)

        email_message = f"""Content-Type: text/html; charset="utf-8"
    MIME-Version: 1.0
    Subject: Your Verification Code
    From: {self.smtp_user}
    To: {to_email}

    {message}"""

        try:
            await self.smtp_client.sendmail(self.smtp_user, [to_email], email_message.encode("utf-8"))
            logging.info(f"Email sent to {to_email}")
        except Exception as e:
            logging.error(f"Error sending email: {e}")
            await self.connect()  # Переподключаемся и пробуем снова

    async def keep_alive(self):
        """Периодически отправляет NOOP для поддержания соединения."""
        while True:
            await asyncio.sleep(300)  # Раз в 5 минут
            if self.smtp_client and self.smtp_client.is_connected:
                try:
                    await self.smtp_client.noop()
                    logging.info("SMTP keep-alive NOOP sent")
                except Exception as e:
                    logging.warning(f"SMTP keep-alive failed: {e}")
                    await self.connect()  # Переподключаемся