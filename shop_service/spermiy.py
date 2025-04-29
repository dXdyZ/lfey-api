import math

class Shape:
    def __init__(self, shape_type, size=1):
        """
        Инициализация фигуры.
        :param shape_type: Тип фигуры ("круг" или "квадрат").
        :param size: Размер фигуры (радиус для круга, сторона для квадрата). По умолчанию 1.
        """
        self.shape_type = shape_type
        self.size = float(size)  # Преобразуем размер в число

    def area(self):
        """Вычисление площади фигуры."""
        if self.shape_type == "круг":
            return round(math.pi * self.size**2, 2)  # Площадь круга: π * r^2
        elif self.shape_type == "квадрат":
            return round(self.size**2, 2)  # Площадь квадрата: сторона^2
        else:
            raise ValueError("Неизвестный тип фигуры")

    def perimeter(self):
        """Вычисление периметра фигуры."""
        if self.shape_type == "круг":
            return round(2 * math.pi * self.size, 2)  # Периметр круга: 2 * π * r
        elif self.shape_type == "квадрат":
            return round(4 * self.size, 2)  # Периметр квадрата: 4 * сторона
        else:
            raise ValueError("Неизвестный тип фигуры")


# Ввод данных
input_data = input().split()
shape_type = input_data[0]  # Тип фигуры
size = input_data[1] if len(input_data) > 1 else 1  # Размер фигуры (по умолчанию 1)

# Создание объекта фигуры
shape = Shape(shape_type, size)

# Вычисление и вывод площади и периметра
print(f"{shape.area():.2f} {shape.perimeter():.2f}")