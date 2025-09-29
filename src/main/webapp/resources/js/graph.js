class GraphDrawer {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');
        this.canvas.width = 400;
        this.canvas.height = 400;
        this.center = { x: this.canvas.width / 2, y: this.canvas.height / 2 };

        // Define pixel sizes for the minimum and maximum R values for smoother scaling
        this.pixelR_at_R1 = this.canvas.width * 0.35; // Pixel size for R=1
        this.pixelR_at_R4 = this.canvas.width * 0.15; // Pixel size for R=4

        this.currentR = null;
        this.points = [];
    }

    clearPoints() {
        this.points = [];
    }

    setCurrentR(r) {
        this.currentR = r;
    }

    addPoint(point) {
        this.points.push(point);
    }

    // Вспомогательный метод для получения текущего pixelR
    getPixelR() {
        const dynamicR = this.currentR !== null ? this.currentR : 1;
        const t = (dynamicR - 1) / 3;
        return this.pixelR_at_R1 + t * (this.pixelR_at_R4 - this.pixelR_at_R1);
    }

    // Вспомогательный метод для преобразования координатной точки графика (x, y) в пиксели холста
    graphToCanvas(x, y, r) {
        const pixelR = this.getPixelR();
        const canvasX = (x / r) * pixelR;
        const canvasY = (y / r) * pixelR;

        return {
            x: this.center.x + canvasX,
            y: this.center.y - canvasY // В canvas Y увеличивается вниз
        };
    }

    // НОВЫЙ МЕТОД: Рисование символа Бэтмена
    drawBatmanShape(r) {
        if (r === null) return;

        const ctx = this.ctx;
        const scaleFactor = r / 7; // Исходная формула Бэтмена рассчитана на R=7
        const step = 0.05;

        ctx.beginPath();
        ctx.fillStyle = 'rgba(255, 165, 0, 0.5)'; // Оранжевый цвет для контраста

        // --- Верхняя часть ---
        for (let localX = -7; localX <= 7; localX += step) {
            const absX = Math.abs(localX);
            let localY;

            if (absX >= 3 && absX <= 7) {
                // Крылья (эллипс)
                localY = 3 * Math.sqrt(Math.max(0, 1 - (localX / 7) ** 2));
            } else if (absX >= 1.5 && absX < 3) {
                // Щёки (нижняя часть окружности)
                const dx = absX - 2.25;
                localY = 2.0 - Math.sqrt(Math.max(0, 1 - dx * dx / (0.75 ** 2)));
            } else if (absX >= 0.6 && absX < 1.5) {
                // Уши
                if (absX <= 0.9) {
                    // Верх уха (полуокружность)
                    const dx = absX - 0.75;
                    localY = 3 + 0.4 * Math.sqrt(Math.max(0, 1 - dx * dx / (0.15 ** 2)));
                } else {
                    // Скос уха
                    localY = 2.0 + (1.5 - absX) * 0.3;
                }
            } else {
                // Плоская голова
                localY = 2.8;
            }

            const worldX = localX * scaleFactor;
            const worldY = localY * scaleFactor;

            // Преобразование мировых координат (относительно r) в пиксели холста
            const { x: pixelX, y: pixelY } = this.graphToCanvas(worldX, worldY, r);

            if (localX === -7) {
                ctx.moveTo(pixelX, pixelY);
            } else {
                ctx.lineTo(pixelX, pixelY);
            }
        }

        // --- Нижняя часть: тело ---
        for (let localX = 7; localX >= -7; localX -= step) {
            const absX = Math.abs(localX);
            let localY;

            if (absX >= 4 && absX <= 7) {
                // Нижние крылья
                localY = -3 * Math.sqrt(Math.max(0, 1 - (localX / 7) ** 2));
            } else {
                // Тело (сложная формула)
                const term1 = Math.abs(localX / 2);
                const term2 = ((3 * Math.sqrt(33) - 7) / 112) * localX ** 2;
                const term3 = Math.sqrt(Math.max(0, 1 - (Math.abs(absX - 2) - 1) ** 2));
                localY = term1 - term2 - 3 + term3;
            }

            const worldX = localX * scaleFactor;
            const worldY = localY * scaleFactor;

            const { x: pixelX, y: pixelY } = this.graphToCanvas(worldX, worldY, r);

            ctx.lineTo(pixelX, pixelY);
        }

        ctx.closePath();
        ctx.fill();
    }

    drawGraph(r = null) {
        this.setCurrentR(r);
        const width = this.canvas.width;
        const height = this.canvas.height;
        const center = this.center;
        const ctx = this.ctx;

        ctx.clearRect(0, 0, width, height);

        // Draw coordinate axes
        ctx.beginPath();
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 3;

        ctx.moveTo(0, center.y);
        ctx.lineTo(width, center.y);

        ctx.moveTo(center.x, 0);
        ctx.lineTo(center.x, height);

        // Draw axis arrows
        ctx.moveTo(width, center.y);
        ctx.lineTo(width - 25, center.y - 12);
        ctx.moveTo(width, center.y);
        ctx.lineTo(width - 25, center.y + 12);

        ctx.moveTo(center.x, 0);
        ctx.lineTo(center.x - 12, 25);
        ctx.moveTo(center.x, 0);
        ctx.lineTo(center.x + 12, 25);
        ctx.stroke();

        ctx.font = '12px Arial';
        ctx.fillStyle = '#000';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';

        // Linear interpolation for smoother scaling
        const dynamicR = this.currentR !== null ? this.currentR : 1;
        const pixelR = this.getPixelR();
        const pixelRHalf = pixelR / 2;

        // Determine axis labels dynamically
        const rLabel = this.currentR !== null ? dynamicR.toFixed(2) : "R";
        const rHalfLabel = this.currentR !== null ? (dynamicR / 2).toFixed(2) : "R/2";

        // Draw axis labels
        ctx.fillText("X", width - 20, center.y + 30);
        ctx.fillText("Y", center.x - 30, 20);

        ctx.fillText(rLabel, center.x + pixelR, center.y + 30);
        ctx.fillText(rHalfLabel, center.x + pixelRHalf, center.y + 30);
        ctx.fillText(`-${rHalfLabel}`, center.x - pixelRHalf, center.y + 30);
        ctx.fillText(`-${rLabel}`, center.x - pixelR, center.y + 30);

        ctx.fillText(rLabel, center.x - 30, center.y - pixelR);
        ctx.fillText(rHalfLabel, center.x - 30, center.y - pixelRHalf);
        ctx.fillText(`-${rHalfLabel}`, center.x - 30, center.y + pixelRHalf);
        ctx.fillText(`-${rLabel}`, center.x - 30, center.y + pixelR);

        // ВЫЗОВ: Рисуем Бэтмена
        this.drawBatmanShape(dynamicR);

        // Redraw all points
        this.points.forEach(point => {
            this.drawPoint(point.x, point.y, point.isHit, point.r);
        });
    }

    drawPoint(x, y, isHit, r) {
        if (this.currentR === null) {
            return;
        }

        const center = this.center;
        const ctx = this.ctx;
        const pixelR = this.getPixelR();

        // Ваша логика масштабирования точки
        const scaledX = (x / r) * this.currentR;
        const scaledY = (y / r) * this.currentR;

        // Теперь преобразуем эти новые 'scaled' координаты в пиксели холста
        const canvasX = (scaledX / this.currentR) * pixelR;
        const canvasY = (scaledY / this.currentR) * pixelR;

        ctx.beginPath();
        ctx.arc(
            center.x + canvasX,
            center.y - canvasY,
            3,
            0,
            Math.PI * 2
        );
        ctx.fillStyle = isHit ? 'green' : 'red';
        ctx.fill();
    }

    // --- ВОССТАНОВЛЕННЫЕ МЕТОДЫ ДЛЯ МЫШИ ---

    // Перерисовка графика без очистки points, но с учетом текущего R
    clearDynamicElements() {
        this.drawGraph(this.currentR);
    }

    // Рисование динамических линий и координат
    drawMouseLines(mouseX, mouseY, displayX, displayY) {
        this.clearDynamicElements();

        const ctx = this.ctx;
        const canvas = this.canvas;

        ctx.beginPath();
        ctx.strokeStyle = 'rgba(0, 0, 0, 0.5)';
        ctx.lineWidth = 1;

        // Линия по X
        ctx.moveTo(mouseX, 0);
        ctx.lineTo(mouseX, canvas.height);

        // Линия по Y
        ctx.moveTo(0, mouseY);
        ctx.lineTo(canvas.width, mouseY);
        ctx.stroke();

        ctx.font = '14px Arial';
        ctx.fillStyle = '#000';
        ctx.textAlign = 'left';
        ctx.textBaseline = 'bottom';

        // Отображение координат
        ctx.fillText(`X: ${displayX.toFixed(2)}`, mouseX + 10, mouseY - 10);
        ctx.fillText(`Y: ${displayY.toFixed(2)}`, mouseX + 10, mouseY + 20);
    }

    // Очистка динамических элементов (линий мыши)
    clearMouseLines() {
        this.clearDynamicElements();
    }

    // Преобразование координат холста в координаты графика
    canvasToGraphCoords(canvasX, canvasY) {
        if (this.currentR === null) {
            return { x: 0, y: 0 };
        }

        const t = (this.currentR - 1) / 3;
        const pixelR = this.pixelR_at_R1 + t * (this.pixelR_at_R4 - this.pixelR_at_R1);

        const x = ((canvasX - this.center.x) * this.currentR) / pixelR;
        const y = ((this.center.y - canvasY) * this.currentR) / pixelR;

        return { x, y };
    }
}