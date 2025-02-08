from fastapi import FastAPI, HTTPException, Depends
from pydantic import BaseModel, Field
from sqlalchemy import create_engine, Column, Integer, String, Float
from sqlalchemy.orm import sessionmaker, declarative_base, Session
from typing import Optional

# Настройка подключения к базе данных SQLite
DATABASE_URL = "sqlite:///./test.db"

engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Определение модели Pydantic для данных, которые будут отправляться и возвращаться
class ItemBase(BaseModel):
    name: str
    description: Optional[str] = Field(None, title="Description of the item", max_length=255)
    price: float
    tax: Optional[float] = None

# Определение модели Pydantic для данных, которые будут возвращаться
class Item(ItemBase):
    id: int

    class Config:
        orm_mode = True

# Определение модели SQLAlchemy
class ItemModel(Base):
    __tablename__ = "items"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    description = Column(String, index=True)
    price = Column(Float)
    tax = Column(Float)

# Создание таблицы в базе данных
Base.metadata.create_all(bind=engine)

# Функция для получения сессии базы данных
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# Инициализация FastAPI
app = FastAPI()

# Маршрут для создания нового элемента
@app.post("/items/", response_model=Item)
def create_item(item: ItemBase, db: Session = Depends(get_db)):
    db_item = ItemModel(**item.dict())
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item

# Маршрут для получения списка всех элементов
@app.get("/items/", response_model=list[Item])
def read_items(skip: int = 0, limit: int = 10, db: Session = Depends(get_db)):
    items = db.query(ItemModel).offset(skip).limit(limit).all()
    return items

# Маршрут для получения одного элемента по его ID
@app.get("/items/{item_id}", response_model=Item)
def read_item(item_id: int, db: Session = Depends(get_db)):
    item = db.query(ItemModel).filter(ItemModel.id == item_id).first()
    if item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return item

# Маршрут для обновления элемента
@app.put("/items/{item_id}", response_model=Item)
def update_item(item_id: int, item: ItemBase, db: Session = Depends(get_db)):
    db_item = db.query(ItemModel).filter(ItemModel.id == item_id).first()
    if db_item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    db_item.name = item.name
    db_item.description = item.description
    db_item.price = item.price
    db_item.tax = item.tax
    db.commit()
    db.refresh(db_item)
    return db_item

# Маршрут для частичного обновления элемента
@app.patch("/items/{item_id}", response_model=Item)
def partial_update_item(item_id: int, item: ItemBase, db: Session = Depends(get_db)):
    db_item = db.query(ItemModel).filter(ItemModel.id == item_id).first()
    if db_item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    update_data = item.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_item, key, value)
    db.commit()
    db.refresh(db_item)
    return db_item

# Маршрут для удаления элемента
@app.delete("/items/{item_id}", response_model=dict)
def delete_item(item_id: int, db: Session = Depends(get_db)):
    db_item = db.query(ItemModel).filter(ItemModel.id == item_id).first()
    if db_item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    db.delete(db_item)
    db.commit()
    return {"detail": "Item deleted"}
